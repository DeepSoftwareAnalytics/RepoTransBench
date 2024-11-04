import java.util.Iterator;

public class Chunk<T> implements Iterable<T> {
    private Lookahead<ValueAndKey<T>> lookahead;

    public Chunk(Lookahead<ValueAndKey<T>> lookahead) {
        this.lookahead = lookahead;
    }

    public Iterator<T> iterator() {
        return new Iterator<T>() {

            public boolean hasNext() {
                return !lookahead.isAtEnd() && lookahead.peek().getKey().equals(lookahead.peek().getKey());
            }

            public T next() {
                return lookahead.next().getValue();
            }
        };
    }
}
