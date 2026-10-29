package com.example.pipeline.result;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Production-grade monadic Result type implemented with Java 21 sealed interfaces and records.
 */
public sealed interface Result<T, E> permits Result.Success, Result.Failure {

    record Success<T, E>(T value) implements Result<T, E> {
        public Success {
            Objects.requireNonNull(value, "Success value must not be null");
        }
    }

    record Failure<T, E>(E error) implements Result<T, E> {
        public Failure {
            Objects.requireNonNull(error, "Failure error must not be null");
        }
    }

    static <T, E> Result<T, E> success(T value) {
        return new Success<>(value);
    }

    static <T, E> Result<T, E> failure(E error) {
        return new Failure<>(error);
    }

    default boolean isSuccess() {
        return this instanceof Success<T, E>;
    }

    default boolean isFailure() {
        return this instanceof Failure<T, E>;
    }

    default <R> Result<R, E> map(Function<? super T, ? extends R> mapper) {
        Objects.requireNonNull(mapper, "Mapper cannot be null");
        return switch (this) {
            case Success<T, E>(T val) -> Result.success(mapper.apply(val));
            case Failure<T, E>(E err) -> Result.failure(err);
        };
    }

    default <R> Result<R, E> flatMap(Function<? super T, ? extends Result<R, E>> mapper) {
        Objects.requireNonNull(mapper, "Mapper cannot be null");
        return switch (this) {
            case Success<T, E>(T val) -> mapper.apply(val);
            case Failure<T, E>(E err) -> Result.failure(err);
        };
    }

    default <R> R fold(Function<? super T, ? extends R> onSuccess, Function<? super E, ? extends R> onFailure) {
        Objects.requireNonNull(onSuccess, "onSuccess cannot be null");
        Objects.requireNonNull(onFailure, "onFailure cannot be null");
        return switch (this) {
            case Success<T, E>(T val) -> onSuccess.apply(val);
            case Failure<T, E>(E err) -> onFailure.apply(err);
        };
    }

    default T getOrElse(T defaultValue) {
        return switch (this) {
            case Success<T, E>(T val) -> val;
            case Failure<T, E> ignored -> defaultValue;
        };
    }

    default T getOrThrow(Function<E, ? extends RuntimeException> exceptionSupplier) {
        Objects.requireNonNull(exceptionSupplier, "Exception supplier cannot be null");
        return switch (this) {
            case Success<T, E>(T val) -> val;
            case Failure<T, E>(E err) -> throw exceptionSupplier.apply(err);
        };
    }

    default Result<T, E> ifSuccess(Consumer<? super T> action) {
        Objects.requireNonNull(action, "Action cannot be null");
        if (this instanceof Success<T, E>(T val)) action.accept(val);
        return this;
    }

    default Result<T, E> ifFailure(Consumer<? super E> action) {
        Objects.requireNonNull(action, "Action cannot be null");
        if (this instanceof Failure<T, E>(E err)) action.accept(err);
        return this;
    }
}
