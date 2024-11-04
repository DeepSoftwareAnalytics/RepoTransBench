import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import java.util.HashMap;
import java.util.Map;
import java.util.EnumSet;

class Foo {
    int x;
    String y;
    boolean z;

    public Foo() {
        this.x = 5;
        this.y = "abc";
        this.z = true;
    }
}

class Bar {
    boolean value;
    Foo foo;

    public Bar() {
        this.value = false;
        this.foo = new Foo();
    }
}

enum Color {
    RED(1),
    GREEN(2);

    private int value;

    Color(int value) {
        this.value = value;
    }
}

class ObjectWithoutDict {
}

class ObjectWithCircularReference {
    ObjectWithCircularReference r;

    public ObjectWithCircularReference() {
        this.r = this;
    }
}

class DeepCircularReferenceParent {
    DeepCircularReferenceChild child;

    public DeepCircularReferenceParent() {
        this.child = new DeepCircularReferenceChild(this);
    }
}

class DeepCircularReferenceChild {
    DeepCircularReferenceParent parent;

    public DeepCircularReferenceChild(DeepCircularReferenceParent parent) {
        this.parent = parent;
    }
}

class ParentObjectThatIsEqualToOthers {
    Foo foo;

    public ParentObjectThatIsEqualToOthers() {
        this.foo = new Foo();
    }

    @Override
    public boolean equals(Object obj) {
        return true;
    }
}

public class VarExportTests {

    @Test
    public void test_var_export() {
        Object[][] data = new Object[][] {
            {null, "#0 NoneType(None) "},
            {true, "#0 bool(True) "},
            {false, "#0 bool(False) "},
            {"", "#0 str(0) \"\""},
            {"a", "#0 str(1) \"a\""},
            {"abc", "#0 str(3) \"abc\""},
            {0, "#0 int(0) "},
            {12, "#0 int(12) "},
            {-13, "#0 int(-13) "},
            {21.37, "#0 float(21.37) "},
            {Color.RED, "#0 Enum(RED)"},
            {Color.GREEN, "#0 Enum(GREEN)"},
            {
                new HashMap<String, Object>() {{
                    put("foo", 12);
                    put("bar", false);
                }},
                "#0 dict(2) " +
                "    ['foo'] => int(12) " +
                "    ['bar'] => bool(False) "
            },
            {
                new Foo(), "#0 object(Foo) (3)" +
                "    x => int(5) " +
                "    y => str(3) \"abc\"" +
                "    z => bool(True) "
            },
            {
                new Bar(), "#0 object(Bar) (2)" +
                "    value => bool(False) " +
                "    foo => object(Foo) (3)" +
                "        x => int(5) " +
                "        y => str(3) \"abc\"" +
                "        z => bool(True) "
            },
        };

        for (Object[] entry : data) {
            Object given = entry[0];
            String expected = (String) entry[1];
            assertEquals(expected, VarDump.var_export(given));
        }
    }

    @Test
    public void test_var_export_multiple_values_at_once() {
        assertEquals(
            "#0 str(3) \"foo\"" +
            "#1 int(55) " +
            "#2 object(Bar) (2)" +
            "    value => bool(False) " +
            "    foo => object(Foo) (3)" +
            "        x => int(5) " +
            "        y => str(3) \"abc\"" +
            "        z => bool(True) " +
            "#3 bool(False) ",
            VarDump.var_export("foo", 55, new Bar(), false)
        );
    }

    @Test
    public void test_var_export_object_without_dict() {
        assertTrue(VarDump.var_export(new ObjectWithoutDict()).matches(
            "#0 object\\(ObjectWithoutDict\\) .+"
        ));
    }

    @Test
    public void test_var_export_circular_reference() {
        assertEquals(
            "#0 object(ObjectWithCircularReference) (1)" +
            "    r => object(ObjectWithCircularReference) (1) …circular reference…",
            VarDump.var_export(new ObjectWithCircularReference())
        );
    }

    @Test
    public void test_var_export_compares_parents_by_reference_not_value() {
        assertEquals(
            "#0 object(ParentObjectThatIsEqualToOthers) (1)" +
            "    foo => object(Foo) (3)" +
            "        x => int(5) " +
            "        y => str(3) \"abc\"" +
            "        z => bool(True) ",
            VarDump.var_export(new ParentObjectThatIsEqualToOthers())
        );
    }

    @Test
    public void test_var_export_deep_circular_reference() {
        assertEquals(
            "#0 object(DeepCircularReferenceParent) (1)" +
            "    child => object(DeepCircularReferenceChild) (1)" +
            "        parent => object(DeepCircularReferenceParent) (1) …circular reference…",
            VarDump.var_export(new DeepCircularReferenceParent())
        );
    }
}
