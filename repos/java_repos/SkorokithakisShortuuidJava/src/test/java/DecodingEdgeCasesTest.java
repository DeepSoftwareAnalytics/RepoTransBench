import org.junit.jupiter.api.Test;
import shortuuid.ShortUUID;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class DecodingEdgeCasesTest {

    @Test
    void testDecodeDict() {
        ShortUUID su = new ShortUUID();
        Method decodeMethod;
        try {
            decodeMethod = ShortUUID.class.getMethod("decode", String.class);

            // 使用反射调用 decode 方法，并传入非法参数
            assertThrows(IllegalArgumentException.class, () -> decodeMethod.invoke(su, (Object) Collections.emptyList().toString()));
            assertThrows(IllegalArgumentException.class, () -> decodeMethod.invoke(su, (Object) new HashMap<>().toString()));
            assertThrows(IllegalArgumentException.class, () -> decodeMethod.invoke(su, (Object) new Integer[]{2}.toString()));
            assertThrows(IllegalArgumentException.class, () -> decodeMethod.invoke(su, (Object) Integer.toString(42)));
            assertThrows(IllegalArgumentException.class, () -> decodeMethod.invoke(su, (Object) Double.toString(42.0)));
        } catch (Exception e) {
            fail("An error occurred: " + e.getMessage());
        }
    }
}
