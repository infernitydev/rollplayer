package dev.infernity.rollplayer;

import dev.infernity.rollplayer.eventmanager.RollplayerEventManager;
import dev.infernity.rollplayer.ipc.InstanceIpc;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.IntegrationType;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class Rollplayer extends ListenerAdapter {
    private final AtomicBoolean takeoverSignalled = new AtomicBoolean();
    private InstanceIpc instanceIpc;

    public static void main(String[] ignoredArgs) {
        var resources = Resources.getInstance();
        int ipcPort = resources.getConfig().getInt("ipc.port", 18011);
        var rollplayer = new Rollplayer();

        try {
            rollplayer.instanceIpc = InstanceIpc.acquire(ipcPort, rollplayer::closeForTakeover);
        } catch (IOException | IllegalArgumentException e) {
            resources.getLogger().error("Could not initialize instance IPC on port {}.", ipcPort, e);
            return;
        }
        if (rollplayer.instanceIpc.isWaitingForTakeover()) {
            resources.getLogger().info("Another instance is running; waiting until this instance is ready to take control.");
        }

        String token = resources.getConfig().getString("discord.token");
        JDABuilder.createDefault(token)
                .addEventListeners(rollplayer)
                .setEventManager(new RollplayerEventManager())
                .build();
    }

    private void closeForTakeover() {
        Resources.getInstance().getLogger().info("A replacement instance is ready; handing over control.");
        shutdownAndExit(0);
    }

    private void shutdownAfterFailedTakeover() {
        Resources.getInstance().getLogger().error("The IPC handoff failed!! Exiting.");
        shutdownAndExit(1);
    }

    private void shutdownAndExit(int exitCode) {
        if (instanceIpc != null) {
            instanceIpc.close();
        }
        var api = Resources.getInstance().getJda();
        if (api != null) {
            api.shutdown();
            awaitShutdown(api);
        }
        System.exit(exitCode);
    }

    private void awaitShutdown(JDA api) {
        boolean interrupted = false;
        while (true) {
            try {
                api.awaitShutdown();
                break;
            } catch (InterruptedException e) {
                // Finish the graceful shutdown before exiting, then preserve
                // the interrupt status for the shutdown hook/process caller.
                interrupted = true;
            }
        }
        if (interrupted) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        var api = event.getJDA();
        Resources.getInstance().setJda(api);
        Resources.getInstance().getLogger().info("{} {} is initializing!", Resources.getInstance().getName(), Resources.getInstance().getVersion());
        var listeners = new Listeners();
        Resources.getInstance().getLogger().info("Loading {} listeners.", listeners.listeners.size());
        api.addEventListener(listeners.listeners.toArray());
        var debugServer = Resources.getInstance().getConfig().getLong("debug.testingServer", 0L);

        String updateStrategy = Resources.getInstance().getConfig().getString("commands.update", "on").toLowerCase();

        switch (updateStrategy) {
            case "off" -> Resources.getInstance().getLogger().info("Not updating commands.");
            case "wipe" -> {
                if (debugServer == 0L) {
                    api.updateCommands()
                            .onSuccess(_ -> Resources.getInstance().getLogger().info("Commands wiped."))
                            .queue();
                } else {
                    var guild = api.getGuildById(debugServer);
                    if (guild == null) {
                        Resources.getInstance().getLogger().error("Debug server with ID {} not found.", debugServer);
                    } else {
                        guild.updateCommands()
                                .onSuccess(_ -> Resources.getInstance().getLogger().info("Commands wiped on server {}.", debugServer))
                                .queue();
                    }
                }
            }
            case "force" -> {
                Resources.getInstance().getLogger().info("Forcing update of all commands.");
                if (debugServer == 0L) {
                    api.updateCommands().addCommands(listeners.commands)
                            .onSuccess(_ -> Resources.getInstance().getLogger().info("Commands initialized globally."))
                            .queue();
                } else {
                    var guild = api.getGuildById(debugServer);
                    if (guild == null) {
                        Resources.getInstance().getLogger().error("Debug server with ID {} not found.", debugServer);
                    } else {
                        guild.updateCommands().addCommands(listeners.commands)
                                .onSuccess(_ -> Resources.getInstance().getLogger().info("Commands initialized to server {}.", debugServer))
                                .queue();
                    }
                }
            }
            default -> {
                if (!updateStrategy.equals("on")) {
                    Resources.getInstance().getLogger().warn("Unknown command update strategy '{}' (options are off, on, wipe, and force). Defaulting to 'on'.", updateStrategy);
                }
                if (debugServer == 0L) {
                    api.retrieveCommands().queue(remoteCommands ->
                            updateGlobalCommandsIncrementally(api, listeners.commands, remoteCommands)
                    );
                } else {
                    var guild = api.getGuildById(debugServer);
                    if (guild == null) {
                        Resources.getInstance().getLogger().error("Debug server with ID {} not found.", debugServer);
                    } else {
                        guild.retrieveCommands().queue(remoteCommands ->
                                updateGuildCommandsIncrementally(guild, listeners.commands, remoteCommands)
                        );
                    }
                }
            }
        }

        super.onReady(event);
        Resources.getInstance().getLogger().info("### {}, {}, online. (version {}, built at {})",
                Resources.getInstance().getName(),
                Resources.getInstance().getLabel(),
                Resources.getInstance().getVersion(),
                Resources.getInstance().getTimestamp());

        if (instanceIpc != null && instanceIpc.isWaitingForTakeover()
                && takeoverSignalled.compareAndSet(false, true)) {
            try {
                instanceIpc.signalReadyAndTakeControl();
                Resources.getInstance().getLogger().info("Took control from the previous instance.");
            } catch (IOException e) {
                Resources.getInstance().getLogger().error("Could not complete the IPC handoff.", e);
                shutdownAfterFailedTakeover();
            }
        }
    }

    private void updateGlobalCommandsIncrementally(JDA api, List<CommandData> localCommands, List<Command> remoteCommands) {
        var remoteCommandMap = remoteCommands.stream()
                .collect(Collectors.toMap(Command::getName, Function.identity()));

        var localCommandNames = localCommands.stream()
                .map(CommandData::getName)
                .collect(Collectors.toSet());

        int upserts = 0;
        for (CommandData localCommand : localCommands) {
            var remoteCommand = remoteCommandMap.get(localCommand.getName());
            boolean changed = false;
            if (remoteCommand == null) {
                changed = true;
            } else {
                // Contexts and integration types don't play well in compares so we override them for this
                var localCommandForComparison = localCommand.setContexts(InteractionContextType.ALL).setIntegrationTypes(IntegrationType.ALL);
                CommandData remoteCommandData = CommandData.fromCommand(remoteCommand).setContexts(InteractionContextType.ALL).setIntegrationTypes(IntegrationType.ALL);
                if (!localCommandForComparison.toData().equals(remoteCommandData.toData())) {
                    changed = true;
                }
            }
            if (changed) {
                upserts++;
                api.upsertCommand(localCommand).queue();
            }
        }

        int deletions = 0;
        for (Command remoteCommand : remoteCommands) {
            if (!localCommandNames.contains(remoteCommand.getName())) {
                deletions++;
                api.deleteCommandById(remoteCommand.getId()).queue();
            }
        }
        Resources.getInstance().getLogger().info("{} upserts and {} deletions.", upserts, deletions);
    }

    private void updateGuildCommandsIncrementally(Guild guild, List<CommandData> localCommands, List<Command> remoteCommands) {
        var remoteCommandMap = remoteCommands.stream()
                .collect(Collectors.toMap(Command::getName, Function.identity()));

        var localCommandNames = localCommands.stream()
                .map(CommandData::getName)
                .collect(Collectors.toSet());

        int upserts = 0;
        for (CommandData localCommand : localCommands) {
            var remoteCommand = remoteCommandMap.get(localCommand.getName());
            boolean changed = false;
            if (remoteCommand == null) {
                changed = true;
            } else {
                // Contexts and integration types don't play well in compares so we override them for this
                var localCommandForComparison = localCommand.setContexts(InteractionContextType.ALL).setIntegrationTypes(IntegrationType.ALL);
                CommandData remoteCommandData = CommandData.fromCommand(remoteCommand).setContexts(InteractionContextType.ALL).setIntegrationTypes(IntegrationType.ALL);
                if (!localCommandForComparison.toData().equals(remoteCommandData.toData())) {
                    changed = true;
                }
            }
            if (changed) {
                upserts++;
                guild.upsertCommand(localCommand).queue();
            }
        }

        int deletions = 0;
        for (Command remoteCommand : remoteCommands) {
            if (!localCommandNames.contains(remoteCommand.getName())) {
                deletions++;
                guild.deleteCommandById(remoteCommand.getId()).queue();
            }
        }
        Resources.getInstance().getLogger().info("{} upserts and {} deletions for guild {}.", upserts, deletions, guild.getName());
    }
}
