package io.github.zero88.qwe.scheduler.core;

import java.time.Instant;

import io.vertx.core.Promise;

import lombok.NonNull;

/**
 * Represents for a context per each execution round
 */
public interface TaskExecutionContext {

    /**
     * Setup task execution context
     *
     * @param promise    promise
     * @param executedAt execution at time
     * @return a reference to this for fluent API
     * @apiNote It will be invoked by system. In any attempts invoking, {@link IllegalStateException} will be
     *     thrown
     * @see Promise
     */
    @NonNull TaskExecutionContext setup(@NonNull Promise<Object> promise, @NonNull Instant executedAt);

    /**
     * Current execution round
     *
     * @return round
     */
    long round();

    /**
     * Trigger execution at time
     *
     * @return triggeredAt
     */
    @NonNull Instant triggeredAt();

    /**
     * Executed at time
     *
     * @return executedAt
     */
    @NonNull Instant executedAt();

    /**
     * Check whether force stop execution or not
     *
     * @return {@code true} if force stop
     */
    boolean isForceStop();

    /**
     * Mark a flag stop/cancel to cancel executor
     */
    void forceStopExecution();

    /**
     * Completed execution with result data per each round
     *
     * @param data object data
     * @apiNote if task is {@code async} then it should be invoked in handling async result stage
     */
    void complete(Object data);

    /**
     * Failed execution with error per each round
     *
     * @param throwable execution error
     * @apiNote if task is {@code async} then it should be invoked in handling async result stage
     */
    void fail(Throwable throwable);

}