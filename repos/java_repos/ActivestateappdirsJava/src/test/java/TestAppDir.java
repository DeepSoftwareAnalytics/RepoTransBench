import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.lang.reflect.Field;

public class TestAppDir {

    public void assertHasAttr(Class<?> cls, String attr) {
        try {
            Field field = cls.getDeclaredField(attr);
            assertNotNull(field);
        } catch (NoSuchFieldException e) {
            fail("Attribute " + attr + " not found in class " + cls.getName());
        }
    }

    @Test
    public void testMetadata() {
        // We check here the presence of these attributes
        assertHasAttr(AppDirs.class, "version");
        assertHasAttr(AppDirs.class, "versionInfo");
    }

    @Test
    public void testHelpers() {
        AppDirs appDirs = AppDirsFactory.getInstance();
        
        assertTrue(appDirs.userDataDir("MyApp", "MyCompany") instanceof String);
        assertTrue(appDirs.siteDataDir("MyApp", "MyCompany") instanceof String);
        assertTrue(appDirs.userCacheDir("MyApp", "MyCompany") instanceof String);
        assertTrue(appDirs.userStateDir("MyApp", "MyCompany") instanceof String);
        assertTrue(appDirs.userLogDir("MyApp", "MyCompany") instanceof String);
    }

    @Test
    public void testDirs() {
        AppDirs appDirs = AppDirsFactory.getInstance();
        String userDataDir = appDirs.userDataDir("MyApp", "MyCompany");
        String siteDataDir = appDirs.siteDataDir("MyApp", "MyCompany");
        String userCacheDir = appDirs.userCacheDir("MyApp", "MyCompany");
        String userStateDir = appDirs.userStateDir("MyApp", "MyCompany");
        String userLogDir = appDirs.userLogDir("MyApp", "MyCompany");

        assertTrue(userDataDir instanceof String);
        assertTrue(siteDataDir instanceof String);
        assertTrue(userCacheDir instanceof String);
        assertTrue(userStateDir instanceof String);
        assertTrue(userLogDir instanceof String);
    }
}
