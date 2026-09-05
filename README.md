# Rollplayer
*now on Java!*

## Setup
- Compile the project.
- Create a rollplayer.properties file in the same folder as the jar with dependencies.
  - The only required field is `discord.token`, which should be set to the token.
- If another instance is already running, a new instance asks it to hand over control once the new instance is ready. The IPC port defaults to `43127` and can be changed with `ipc.port`.
- Run the jar file.
