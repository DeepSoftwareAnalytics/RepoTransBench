public class ProfileCode {
    // Define the expected output here
    public static final String expected_output = "event: ns : Nanoseconds\nevents: ns\n";

    public static void top() {
        mid1();
        mid2();
        mid3(5);
        C1.samename();
        C2.samename();
    }

    public static void mid1() {
        bot();
        for (int i = 0; i < 5; i++) {
            mid2();
        }
        bot();
    }

    public static void mid2() {
        bot();
    }

    public static void bot() {
        // Do nothing
    }

    public static void mid3(int x) {
        if (x > 0) {
            mid4(x);
        }
    }

    public static void mid4(int x) {
        mid3(x - 1);
    }
}

class C1 {
    public static void samename() {
        // Do nothing
    }
}

class C2 {
    public static void samename() {
        // Do nothing
    }
}
