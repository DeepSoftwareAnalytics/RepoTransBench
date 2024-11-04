import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Dad {
    String name;
    int age;
}

class Son {
    Dad father;
    int age;
    String name;
}

class Ram {
    String brand;
    int capacity;
    String unit;
}

class CpuCore {
    int threads;
    float clock;
    String unit;
}

class Cpu {
    String brand;
    String model;
    int cache;
    List<CpuCore> cores;
}

class Computer {
    String brand;
    Cpu cpu;
    List<Ram> rams;
    Object dictKey;
    String uninitialized;
    List<Ram> rams2;

    int totalRam() {
        return rams.stream().mapToInt(r -> r.capacity).sum();
    }

    int totalRam2() {
        return rams2 != null ? rams2.stream().mapToInt(r -> r.capacity).sum() : 0;
    }
}

class AnyType {
    Object a;
    Object b;
    Object c;
}

class SimpleKeyValue {
    int intKey;
    String strKey;
    float floatKey;
}

class SimpleKeyDefaultValue {
    int intKey = 1;
    String strKey = "default str";
    float floatKey = 1.234f;
}

class AdvancedKeyValue {
    Object tupleKey;
    List<Object> listKey;
    Object dictKey;
}

class ListProdict {
    List<Object> li;
    List<String> liStr;
    List<Integer> liInt;
}

class Recursive {
    Object prodictKey;
    SimpleKeyValue simpleKey;
}

public class ProdictTest {
    @Test
    void testDeepRecursionFromDict() {
        Ram ram1 = new Ram();
        ram1.brand = "Kingston";
        ram1.capacity = 4;
        ram1.unit = "GB";

        Ram ram2 = new Ram();
        ram2.brand = "Samsung";
        ram2.capacity = 8;
        ram2.unit = "GB";

        CpuCore core1 = new CpuCore();
        core1.threads = 2;
        core1.clock = 3.4f;
        core1.unit = "GHz";

        CpuCore core2 = new CpuCore();
        core2.threads = 4;
        core2.clock = 3.1f;
        core2.unit = "GHz";

        Cpu cpu = new Cpu();
        cpu.brand = "Intel";
        cpu.model = "i5-4670";
        cpu.cache = 3;
        cpu.cores = List.of(core1, core2);

        Computer computer = new Computer();
        computer.brand = "acme";
        computer.rams = List.of(ram1, ram2);
        computer.cpu = cpu;
        computer.dictKey = new Object();

        assertEquals("acme", computer.brand);
        assertEquals(12, computer.totalRam());
        assertEquals(0, computer.totalRam2());
    }

    @Test
    void testBracketAccess() {
        SimpleKeyValue pd = new SimpleKeyValue();
        pd.strKey = "str_value_123";
        assertEquals("str_value_123", pd.strKey);
    }

    @Test
    void testNullAssignment() {
        SimpleKeyValue pd = new SimpleKeyValue();

        pd.strKey = "str1";
        assertEquals("str1", pd.strKey);

        pd.strKey = null;
        assertNull(pd.strKey);

        pd.intKey = 1;
        assertEquals(1, pd.intKey);

        pd.intKey = 0; // Java primitives cannot be null, set to default value
        assertEquals(0, pd.intKey);
    }

    @Test
    void testMultipleInstances() {
        class Multi {
            int a;
        }

        Multi m1 = new Multi();
        m1.a = 1;

        Multi m2 = new Multi();
        m2.a = 2;

        assertEquals(1, m2.a - m1.a);
    }

    @Test
    void testProperty() {
        class PropertyClass {
            int first;
            int second;

            int getDiff() {
                return Math.abs(second - first);
            }
        }

        PropertyClass pc = new PropertyClass();
        pc.first = 1;
        pc.second = 2;

        assertEquals(1, pc.getDiff());
    }

    @Test
    void testUseDefaultsMethod() {
        class WithDefault {
            int a = 1;
            String b = "string";
        }

        WithDefault wd = new WithDefault();
        assertEquals(1, wd.a);
        assertEquals("string", wd.b);
    }

    @Test
    void testTypeConversion() {
        class TypeConversionClass {
            int anInt;
            String aStr;
            float aFloat;
        }

        TypeConversionClass tcc = new TypeConversionClass();
        tcc.anInt = Integer.parseInt("1");
        tcc.aStr = "str";
        tcc.aFloat = Float.parseFloat("123.45");

        assertEquals(1, tcc.anInt);
        assertEquals("str", tcc.aStr);
        assertEquals(123.45f, tcc.aFloat);
    }

    @Test
    void testDeepCopy() {
        class Prodict {
            int number;
            String data;
            Prodict next;

            Prodict(int number, String data, Prodict next) {
                this.number = number;
                this.data = data;
                this.next = next;
            }
        }

        Prodict rootNode = new Prodict(1, "ROOT node", null);

        Prodict copied = new Prodict(rootNode.number, rootNode.data, rootNode.next);

        assertEquals(rootNode.number, copied.number);
        assertEquals(rootNode.data, copied.data);
        assertNotSame(rootNode, copied);
    }

    @Test
    void testUnknownAttr() {
        Ram ram = new Ram();
        ram.brand = "Samsung";
        ram.capacity = 4;
        ram.unit = "YB";

        assertEquals("Samsung", ram.brand);

        try {
            ram.getClass().getField("flavor");
            fail();
        } catch (NoSuchFieldException e) {
            // expected
        }
    }

    @Test
    void testDefaultNone() {
        class Car {
            String brand;
            Integer year;
        }

        Car honda = new Car();
        honda.brand = "Honda";
        assertNull(honda.year);

        try {
            honda.getClass().getField("color");
            fail();
        } catch (NoSuchFieldException e) {
            // expected
        }
    }

    @Test
    void testToDictRecursive() {
        Dad dad = new Dad();
        dad.name = "Bob";

        Son son = new Son();
        son.name = "Jeremy";
        son.father = dad;

        assertNotNull(son.father);
        assertEquals("Bob", son.father.name);
    }

    @Test
    void testToDictExcludeNone() {
        Dad dad = new Dad();
        dad.name = "Bob";

        Son son = new Son();
        son.name = "Jeremy";
        son.father = dad;

        assertNotNull(son.father);
        assertNull(son.age);

        son.age = 0;
        assertNotNull(son.age);
    }

    @Test
    void testIssue12() {
        class Comment {
            int userId;
            String comment;
            String date;
        }

        class Post {
            String title;
            String text;
            String date;
            List<Comment> comments;
        }

        class User {
            int userId;
            String userName;
            List<Post> posts;
        }

        Comment comment1 = new Comment();
        comment1.userId = 2;
        comment1.comment = "Good to see you blogging";
        comment1.date = "2018-01-02 03:04:06";

        Comment comment2 = new Comment();
        comment2.userId = 3;
        comment2.comment = "Good for you";
        comment2.date = "2018-01-02 03:04:07";

        Post post = new Post();
        post.title = "Hello World";
        post.text = "This is my first blog post...";
        post.date = "2018-01-02 03:04:05";
        post.comments = List.of(comment1, comment2);

        User user = new User();
        user.userId = 1;
        user.userName = "rambo";
        user.posts = List.of(post);

        assertEquals(1, user.userId);
        assertEquals("rambo", user.userName);
        assertEquals(1, user.posts.size());
        assertEquals("Hello World", user.posts.get(0).title);
        assertEquals(2, user.posts.get(0).comments.size());
        assertEquals("Good to see you blogging", user.posts.get(0).comments.get(0).comment);
    }

    @Test
    void testIssue15() {
        try {
            class Prodict {
                Object self;
            }

            Prodict p = new Prodict();
            p.self = 1;
            assertTrue(true);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    void testAcceptGenerator() {
        String s = ";O2Sat:92;HR:62;RR:0";
        String[] parts = s.split(";");

        for (String part : parts) {
            if (part.contains(":")) {
                String[] kv = part.split(":");
                String key = kv[0];
                String value = kv[1];
                assertNotNull(key);
                assertNotNull(value);
            }
        }
    }

    @Test
    void testPickle() {
        try {
            // Java serialization
            java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
            java.io.ObjectOutputStream out = new java.io.ObjectOutputStream(bos);
            out.writeObject(new SimpleKeyValue() {{
                intKey = 42;
            }});
            out.flush();
            byte[] data = bos.toByteArray();
            out.close();

            java.io.ByteArrayInputStream bis = new java.io.ByteArrayInputStream(data);
            java.io.ObjectInputStream in = new java.io.ObjectInputStream(bis);
            SimpleKeyValue skv = (SimpleKeyValue) in.readObject();
            in.close();

            assertEquals(42, skv.intKey);
        } catch (Exception e) {
            fail();
        }
    }
}
