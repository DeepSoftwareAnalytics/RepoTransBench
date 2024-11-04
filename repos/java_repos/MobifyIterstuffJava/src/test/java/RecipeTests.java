import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class RecipeTests {

    @Test
    public void testRepeatableTakewhile() {
        Lookahead<Character> data = new Lookahead<>(Arrays.asList('a', 'b', 'c', 'd', '1', '2', '3', 'g', 'h', 'i'));
        
        Predicate<Character> isNotDigit = c -> !Character.isDigit(c);
        Predicate<Character> isDigit = Character::isDigit;

        assertEquals(Arrays.asList('a', 'b', 'c', 'd'), toList(Recipes.repeatableTakeWhile(isNotDigit, data)));
        assertEquals(Arrays.asList('1', '2', '3'), toList(Recipes.repeatableTakeWhile(isDigit, data)));
        assertEquals(Arrays.asList('g', 'h', 'i'), toList(data));
    }

    @Test
    public void testChunked() {
        Function<String, String> identity = s -> s;
        Function<String, String> lower = String::toLowerCase;

        assertEquals(0, chunkedHelper("", identity).size());
        
        assertEquals(1, chunkedHelper("a", identity).size());
        assertEquals(Arrays.asList("a"), chunkedHelper("a", identity).get(0));
        assertEquals(2, chunkedHelper("aA", lower).size());

        assertEquals(2, chunkedHelper("ab", identity).size());
        assertEquals(Arrays.asList("a"), chunkedHelper("ab", identity).get(0));
        assertEquals(Arrays.asList("b"), chunkedHelper("ab", identity).get(1));

        assertEquals(3, chunkedHelper("aabcc", identity).size());
        
        assertEquals(2, chunkedHelper("abBbb", lower).size());

        assertEquals(3, chunkedHelper("abBbbC", lower).size());
    }

    @Test
    public void testBatching() {
        Iterable<Integer> seq = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18);
        Iterator<Iterable<ValueAndIndex<Integer>>> batched = (Iterator<Iterable<ValueAndIndex<Integer>>>) (Iterator<?>) Recipes.batch(seq, 3).iterator();
        
        batchTest(batched.next(), new int[]{0, 1, 2}, 3, true);
        batchTest(batched.next(), new int[]{3, 4, 5}, 3, true);
        batchTest(batched.next(), new int[]{6, 7, 8}, 3, true);
        batchTest(batched.next(), new int[]{9, 10, 11}, 3, true);
        batchTest(batched.next(), new int[]{12, 13, 14}, 3, true);
        batchTest(batched.next(), new int[]{15, 16, 17}, 3, true);
        batchTest(batched.next(), new int[]{18}, 1, false);
    }

    private <T> List<T> toList(Iterable<T> iterable) {
        List<T> list = new ArrayList<>();
        iterable.forEach(list::add);
        return list;
    }

    private List<List<String>> chunkedHelper(String input, Function<String, String> function) {
        return toList(Recipes.chunked(Arrays.asList(input.split("")), function))
                .stream().map(this::toList)
                .collect(Collectors.toList());
    }

    private void batchTest(Iterable<ValueAndIndex<Integer>> batch, int[] expectedValues, int expectedLength, boolean isFullBatch) {
        List<ValueAndIndex<Integer>> list = toList(batch);
        assertEquals(expectedLength, list.size());
        for (int i = 0; i < expectedLength; i++) {
            assertEquals(expectedValues[i], list.get(i).getValue());
        }
        
        if (isFullBatch) {
            assertTrue(expectedLength == 3);
            assertFalse(list.get(0).getIndex() == 0);
        } else {
            assertTrue(expectedLength == 1);
        }
    }
}
