import java.util.Iterator;
import java.util.NoSuchElementException;

public class Lookahead<T> implements Iterator<T>, Iterable<T> {

    private Iterator<T> iterator;
    private boolean atStart;
    private boolean atEnd;
    private T nextElement;
    
    public Lookahead(Iterable<T> iterable) {
        this.iterator = iterable.iterator();
        this.atStart = true;
        this.atEnd = false;
        advance();
    }
    
    private void advance() {
        if (iterator.hasNext()) {
            nextElement = iterator.next();
        } else {
            nextElement = null;
            atEnd = true;
        }
    }
    
    public boolean isAtStart() {
        return atStart && !atEnd;
    }
    
    public boolean isAtEnd() {
        return atEnd;
    }
    
    public T peek() {
        return nextElement;
    }
    
    @Override
    public boolean hasNext() {
        return !atEnd;
    }

    @Override
    public T next() {
        if (atEnd) {
            throw new NoSuchElementException();
        }
        
        T currentElement = nextElement;
        atStart = false;
        advance();
        return currentElement;
    }

    @Override
    public Iterator<T> iterator() {
        return this;
    }
}
