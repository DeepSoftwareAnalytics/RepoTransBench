import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

public abstract class Codec<T> {
    protected FFX ffx;
    protected int radix;

    public Codec(FFX ffx, int radix) {
        this.ffx = ffx;
        this.radix = radix;
    }
    
    public abstract List<Integer> pack(T input);

    public abstract T unpack(List<Integer> input);

    public T encrypt(T input) {
        return unpack(ffx.encrypt(radix, pack(input)));
    }

    public T decrypt(T input) {
        return unpack(ffx.decrypt(radix, pack(input)));
    }
}

class Sequence extends Codec<String> {
    protected String alphabet;
    protected Map<Character, Integer> packMap;
    protected int length;

    public Sequence(FFX ffx, String alphabet, int length) {
        super(ffx, alphabet.length());
        this.alphabet = alphabet;
        this.length = length;
        this.packMap = new HashMap<>();
        for (int i = 0; i < alphabet.length(); i++) {
            packMap.put(alphabet.charAt(i), i);
        }
    }

    @Override
    public List<Integer> pack(String input) {
        if (input.length() != length) throw new IllegalArgumentException("sequence length must be " + length);
        return input.chars()
                .mapToObj(c -> {
                    if (!packMap.containsKey((char) c)) throw new IllegalArgumentException("non-alphabet character: " + (char) c);
                    return packMap.get((char) c);
                })
                .collect(Collectors.toList());
    }

    @Override
    public String unpack(List<Integer> input) {
        return input.stream().map(i -> alphabet.charAt(i)).map(Object::toString).collect(Collectors.joining());
    }
}

class StringCodec extends Sequence {
    public StringCodec(FFX ffx, String alphabet, int length) {
        super(ffx, alphabet, length);
    }
}

class IntegerCodec extends Codec<Integer> {
    private final StringCodec stringCodec;

    public IntegerCodec(FFX ffx, int length) {
        super(ffx, 10);
        this.stringCodec = new StringCodec(ffx, "0123456789", length);
    }

    @Override
    public List<Integer> pack(Integer input) {
        return stringCodec.pack(String.format("%0" + stringCodec.length + "d", input));
    }

    @Override
    public Integer unpack(List<Integer> input) {
        return Integer.parseInt(stringCodec.unpack(input));
    }
}

