import java.util.Iterator;
import java.util.function.Function;

public class MappedIterable<T> implements Iterable<ValueAndKey<T>> {
    private final Iterable<T> iterable;
    private final Function<T, ?> extractor;

    public MappedIterable(Iterable<T> iterable, Function<T, ?> extractor) {
        this.iterable = iterable;
        this.extractor = extractor;
    }

    public Iterator<ValueAndKey<T>> iterator() {
        return new Iterator<ValueAndKey<T>>() {
            private final Iterator<T> iterator = iterable.iterator();

            public boolean hasNext() {
                return iterator.hasNext();
            }

            public ValueAndKey<T> next() {
                T nextValue = iterator.next();
                return new ValueAndKey<>(nextValue, extractor.apply(nextValue));
            }
        };
    }
}
