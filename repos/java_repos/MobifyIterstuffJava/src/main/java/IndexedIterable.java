import java.util.Iterator;

public class IndexedIterable<T> implements Iterable<ValueAndIndex<T>> {
    private final Iterable<T> iterable;

    public IndexedIterable(Iterable<T> iterable) {
        this.iterable = iterable;
    }

    public Iterator<ValueAndIndex<T>> iterator() {
        return new Iterator<ValueAndIndex<T>>() {
            private final Iterator<T> iterator = iterable.iterator();
            private int index = 0;

            public boolean hasNext() {
                return iterator.hasNext();
            }

            public ValueAndIndex<T> next() {
                return new ValueAndIndex<>(iterator.next(), index++);
            }
        };
    }
}
