package io.github.petedoyle.querycount;

import com.google.auto.value.AutoValue;

/**
 * Wraps a query result together with the emit count, or the number of times this query result
 * has been updated.
 *
 * @param <T> The type of your query result, to be wrapped inside a {@link QueryCountResult}.
 */
@AutoValue
public abstract class QueryCountResult<T> {
    /**
     * Returns the query result.
     *
     * @return the query result.
     */
    public abstract T getResult();

    /**
     * Returns the number of times the query {@link io.reactivex.Observable} has been emitted.
     *
     * @return the number of times the query {@link io.reactivex.Observable} has been emitted.
     * The initial query result will return 1.
     */
    abstract long getEmitCount();

    public static <T> QueryCountResult<T> create(T result, long emitCount) {
        return new AutoValue_QueryCountResult<>(result, emitCount);
    }

    /**
     * Returns the number of times this query has been updated.
     *
     * @return the number of times this query has been updated. The initial query result will
     * return 0.
     */
    public long getUpdateCount() {
        return getEmitCount() - 1;
    }

    /**
     * Returns {@code true} if this is the first time a query result has been returned, else
     * {@code false}.
     *
     * @return {@code true} if this is the first time a query result has been returned, else
     * {@code false}.
     */
    public boolean isInitialUpdate() {
        return getEmitCount() == 1;
    }
}