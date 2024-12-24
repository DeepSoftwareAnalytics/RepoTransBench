public class ValueAndIndex<T> {
    private final T value;
    private final int index;

    public ValueAndIndex(T value, int index) {
        this.value = value;
        this.index = index;
    }

    public T getValue() {
        return value;
    }

    public int getIndex() {
        return index;
    }
}
