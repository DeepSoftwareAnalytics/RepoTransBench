import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class Recipes {

    public static <T> Iterable<T> repeatableTakeWhile(Predicate<T> predicate, Lookahead<T> iterable) {
        if (iterable == null) throw new IllegalArgumentException("Iterable cannot be null");

        List<T> result = new ArrayList<>();

        while (!iterable.isAtEnd() && predicate.test(iterable.peek())) {
            result.add(iterable.next());
        }

        return result;
    }

    public static <T> Iterable<Iterable<T>> batch(Iterable<T> iterable, int size) {
        Lookahead<ValueAndIndex<T>> lookahead = new Lookahead<>(new IndexedIterable<>(iterable));
        List<Iterable<T>> resultBatches = new ArrayList<>();

        while (!lookahead.isAtEnd()) {
            int endCount = lookahead.peek().getIndex() + size;
            resultBatches.add(repeatableTakeWhile(t -> ((ValueAndIndex<T>) t).getIndex() < endCount, (Lookahead<T>) lookahead));
        }

        return resultBatches;
    }

    public static <T> Iterable<Iterable<T>> chunked(Iterable<T> iterable, Function<T, ?> extractor) {
        Lookahead<ValueAndKey<T>> lookahead = new Lookahead<>(new MappedIterable<>(iterable, extractor));
        List<Iterable<T>> resultChunks = new ArrayList<>();

        while (!lookahead.isAtEnd()) {
            resultChunks.add(new Chunk<>(lookahead));
        }

        return resultChunks;
    }
}
