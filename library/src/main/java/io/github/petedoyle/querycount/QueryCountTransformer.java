package io.github.petedoyle.querycount;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicLong;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.functions.BiFunction;

/**
 * An RxJava {@link ObservableTransformer} that can be composed with a self-updating query to count the
 * number of times that query has emitted results via {@link Observable#compose(ObservableTransformer)}.
 * <p>
 * Example usage:
 * <pre>
 *     observable
 *         .compose(QueryCountTransformer.&lt;YourQueryType&gt;create())
 *         .subscribe(new Consumer&lt;QueryCountResult&lt;YourQueryType&gt;&gt;() {
 *             public void accept(QueryCountResult&lt;YourQueryType&gt; wrapper) throws Exception {
 *                 if (!wrapper.isInitialUpdate()) {
 *                     doSomething(wrapper.getResult());
 *                 }
 *             }
 *         });
 * </pre>
 */
public final class QueryCountTransformer<T> implements ObservableTransformer<T, QueryCountResult<T>> {

    private final QueryCounter mQueryIncrementer = new QueryCounter();

    public static <T> ObservableTransformer<T, QueryCountResult<T>> create() {
        return new QueryCountTransformer<T>();
    }

    private QueryCountTransformer() {
    }

    @Override
    public ObservableSource<QueryCountResult<T>> apply(Observable<T> upstream) {
        return upstream.zipWith(mQueryIncrementer, new BiFunction<T, Long, QueryCountResult<T>>() {
            @Override
            public QueryCountResult<T> apply(T t, Long n) throws Exception {
                return QueryCountResult.create(t, n);
            }
        });
    }

    private static final class QueryCounter implements Iterable<Long> {
        @Override
        public Iterator<Long> iterator() {
            return new Iterator<Long>() {

                private AtomicLong n = new AtomicLong(0);

                @Override
                public boolean hasNext() {
                    return true;
                }

                @Override
                public Long next() {
                    return n.incrementAndGet();
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException("remove() not supported");
                }
            };
        }
    }
}

