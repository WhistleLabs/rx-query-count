package io.github.petedoyle.querycount;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicLong;

import rx.Observable;
import rx.Observable.Transformer;
import rx.functions.Func2;

/**
 * An RxJava {@link Transformer} that can be composed with a self-updating query to count the
 * number of times that query has emitted results via {@link Observable#compose(Transformer)}.
 * <p>
 * Example usage:
 * <pre>
 *     observable
 *         .compose(QueryCountTransformer.&lt;YourQueryType&gt;create())
 *         .subscribe(new Subscriber&lt;QueryCountResult&lt;YourQueryType&gt;&gt;() {
 *             public void onCompleted() {
 *             }
 *
 *             public void onError(Throwable e) {
 *             }
 *
 *             public void onNext(QueryCountResult&lt;YourQueryType&gt; wrapper) {
 *                 if (!wrapper.isInitialUpdate()) {
 *                     doSomething(wrapper.getResult());
 *                 }
 *             }
 *         });
 * </pre>
 */
public final class QueryCountTransformer<T> implements Transformer<T, QueryCountResult<T>> {

    private final QueryCounter mQueryIncrementer = new QueryCounter();

    public static <T> Observable.Transformer<T, QueryCountResult<T>> create() {
        return new QueryCountTransformer<T>();
    }

    private QueryCountTransformer() {
    }

    @Override
    public Observable<QueryCountResult<T>> call(Observable<T> source) {
        return source.zipWith(mQueryIncrementer, new Func2<T, Long, QueryCountResult<T>>() {

            @Override
            public QueryCountResult<T> call(T t, Long n) {
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

