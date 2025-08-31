package dev.infernity.rollplayer.rollplayerlib3;

import java.util.Optional;
import java.util.function.Function;

public interface Result<T> {
    static <R> Result<R> success(R object) {
        return new Success<>(object);
    }
    static <R> Result<R> error(String message) {
        return new Error<R>(message);
    }

    Optional<T> result();
    Optional<String> error();
    boolean isSuccess();
    boolean isError();

    <E extends Throwable> T getOrThrow(Function<String, E> exceptionSupplier) throws E;

    default T getOrThrow() {
        return getOrThrow(IllegalStateException::new);
    }


    record Success<S>(S value) implements Result<S> {
        @Override
        public Optional<S> result() {
            return Optional.of(value);
        }

        @Override
        public Optional<String> error() {
            return Optional.empty();
        }

        @Override
        public boolean isSuccess() {
            return true;
        }

        @Override
        public boolean isError() {
            return false;
        }

        @Override
        public <E extends Throwable> S getOrThrow(Function<String, E>  exceptionSupplier) {
            return value;
        }
    }

    record Error<S>(String message) implements Result<S> {
        @Override
        public Optional<S> result() {
            return Optional.empty();
        }

        @Override
        public Optional<String> error() {
            return Optional.of(message);
        }

        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        public boolean isError() {
            return true;
        }

        @Override
        public <E extends Throwable> S getOrThrow(Function<String, E> exceptionSupplier) throws E {
            throw exceptionSupplier.apply(message);
        }
    }
}

