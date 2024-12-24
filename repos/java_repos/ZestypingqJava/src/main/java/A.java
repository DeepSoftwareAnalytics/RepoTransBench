public class A {
    private final Q q = new Q();
    private String attrib1 = "Attrib1";
    private String attrib2 = "Attrib2";

    public void run1(String message) {
        q.q(message);
    }

    public static void run2(String message) {
        new Q().q(message);
    }

    public static void run3(String message) {
        new Q().q(message);
    }
}
