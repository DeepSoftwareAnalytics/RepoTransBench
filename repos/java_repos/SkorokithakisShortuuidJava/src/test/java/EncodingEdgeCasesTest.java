import org.junit.jupiter.api.Test;
import shortuuid.ShortUUID;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class EncodingEdgeCasesTest {

    @Test
    void testDecodeDict() {
        ShortUUID su = new ShortUUID();
        Method encodeMethod;
        try {
            encodeMethod = ShortUUID.class.getMethod("encode", String.class);

            // 使用反射调用 encode 方法，并传入非法参数
            assertThrows(IllegalArgumentException.class, () -> encodeMethod.invoke(su, (Object) Collections.emptyList().toString()));
            assertThrows(IllegalArgumentException.class, () -> encodeMethod.invoke(su, (Object) new HashMap<>().toString()));
            assertThrows(IllegalArgumentException.class, () -> encodeMethod.invoke(su, (Object) new Integer[]{2}.toString()));
            assertThrows(IllegalArgumentException.class, () -> encodeMethod.invoke(su, (Object) Integer.toString(42)));
            assertThrows(IllegalArgumentException.class, () -> encodeMethod.invoke(su, (Object) Double.toString(42.0)));
        } catch (Exception e) {
            fail("An error occurred: " + e.getMessage());
        }
    }
}
