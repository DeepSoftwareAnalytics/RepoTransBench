public class ValueAndKey<T> {
    private final T value;
    private final Object key;

    public ValueAndKey(T value, Object key) {
        this.value = value;
        this.key = key;
    }

    public T getValue() {
        return value;
    }

    public Object getKey() {
        return key;
    }
}
