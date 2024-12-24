import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Brute {

    public static Iterable<String> brute() {
        return brute(1, 3, true, true, true, true, false);
    }

    public static Iterable<String> brute(boolean letters, boolean numbers, boolean symbols, boolean spaces) {
        return brute(1, 3, true, letters, numbers, symbols, spaces);
    }

    public static Iterable<String> brute(int length, boolean ramp) {
        return brute(1, length, ramp, true, true, true, false);
    }

    public static Iterable<String> brute(int startLength, int length, boolean ramp) {
        return brute(startLength, length, ramp, true, true, true, false);
    }

    public static Iterable<String> brute(int startLength, int length, boolean ramp, boolean letters, boolean numbers, boolean symbols, boolean spaces) {
        String choices = "";
        if (letters) choices += "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        if (numbers) choices += "0123456789";
        if (symbols) choices += "!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~";
        if (spaces) choices += " \t\n\r";
        
        // Randomly shuffle the characters to match sample(choices, len(choices)) in Python
        List<Character> characters = choices.chars().mapToObj(c -> (char) c).collect(Collectors.toList());
        Shuffle(characters);

        List<String> allCombinations = new ArrayList<>();
        for (int i = (ramp ? startLength : length); i <= length; i++) {
            allCombinations.addAll(generateCombinations(characters.stream(), i));
        }

        return allCombinations;
    }
    
    private static List<String> generateCombinations(Stream<Character> characters, int length) {
        if (length <= 0) {
            return List.of("");
        }

        List<String> combinations = new ArrayList<>();
        characters.forEach(character -> {
            generateCombinations(characters, length - 1).forEach(combination -> combinations.add(character + combination));
        });
        return combinations;
    }

    private static void Shuffle(List<Character> list) {
        Random random = new Random();
        for (int i = 0; i < list.size(); i++) {
            int randomIndexToSwap = random.nextInt(list.size());
            char temp = list.get(randomIndexToSwap);
            list.set(randomIndexToSwap, list.get(i));
            list.set(i, temp);
        }
    }
}
