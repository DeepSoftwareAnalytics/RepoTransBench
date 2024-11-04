import java.util.concurrent.CompletableFuture;

// A class containing example methods to be tested
public class TestFunction {
    public int testFunc(int arg1, int arg2) {
        return arg1 + arg2;
    }

    public CompletableFuture<Integer> asyncTestFunc(int arg1, int arg2) {
        return CompletableFuture.supplyAsync(() -> arg1 + arg2);
    }
}

