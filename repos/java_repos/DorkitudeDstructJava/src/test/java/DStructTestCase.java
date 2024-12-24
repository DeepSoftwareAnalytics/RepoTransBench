import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.HashMap;
import java.util.Map;


public class DStructTestCase extends BaseTestCase {

    @Test
    public void test_struct() {
        // Test constructing with a map
        DStruct s = new DStruct(Map.of("k1", "v1", "k2", "v2"));
        assertEquals("v1", s.get("k1"));
        assertEquals("v2", s.get("k2"));

        // Test constructing with keyword arguments
        s = new DStruct(Map.of("k1", "v1", "k2", "v2"));
        assertEquals("v1", s.get("k1"));
        assertEquals("v2", s.get("k2"));

        // Test constructing with both
        s = new DStruct(Map.of("k3", "v3", "k1", "v1", "k2", "v2"));
        assertEquals("v1", s.get("k1"));
        assertEquals("v2", s.get("k2"));
        assertEquals("v3", s.get("k3"));

        // Confirm readability as map
        assertEquals("v1", s.get("k1"));
        assertEquals("v2", s.get("k2"));
        assertEquals("v3", s.get("k3"));
    }

    @Test
    public void test_struct_required_attribute_missing() {
        // Make a custom struct class
        class CartesianCoordinate extends DStruct {
            CartesianCoordinate(Map<String, Object> map) {
                super(map);
            }
        }

        // Create a couple of coordinates
        CartesianCoordinate origin = new CartesianCoordinate(Map.of("x", 0, "y", 0));
        CartesianCoordinate point = new CartesianCoordinate(Map.of("x", 5, "y", 12));

        // Confirm their values were stored appropriately
        assertEquals(5, point.get("x"));
        assertEquals(12, point.get("y"));

        // Test a few varieties of improper use
        assertThrows(DStruct.RequiredAttributeMissing.class, () -> new CartesianCoordinate(Map.of("x", 3)));
        assertThrows(DStruct.RequiredAttributeMissing.class, () -> new CartesianCoordinate(Map.of("y", 3)));
        assertThrows(DStruct.RequiredAttributeMissing.class, () -> new CartesianCoordinate(new HashMap<>()));
    }

    @Test
    public void test_struct_required_attribute_invalid() {
        class BaseLabel {
            String name;
            BaseLabel(String name) {
                this.name = name;
            }
        }

        class Label extends BaseLabel {
            Label(String name) {
                super(name);
            }
        }

        class MapLocation extends DStruct {
            MapLocation(Map<String, Object> map) {
                super(map);
            }
        }

        // Make one with a BaseLabel instance for the "label" attribute
        new MapLocation(Map.of("latitude", 1.1, "longitude", 1.1, "label", new BaseLabel("hi")));
        // Make one with a Label instance
        new MapLocation(Map.of("latitude", 1.1, "longitude", 1.1, "label", new Label("hi")));

        // Make one with a single dictionary argument
        MapLocation coffeeshop = new MapLocation(Map.of("latitude", 37.744861, "longitude", -122.477732, "label", new Label("Brown Owl Coffee")));
        // Confirm the values are held
        assertEquals("Brown Owl Coffee", ((Label) coffeeshop.get("label")).name);
        assertEquals(-122.477732, coffeeshop.get("longitude"));
        assertEquals(37.744861, coffeeshop.get("latitude"));

        // Make one with a number of keyword arguments
        MapLocation office = new MapLocation(Map.of("latitude", 37.781586, "longitude", -122.391343, "label", new BaseLabel("Hatchery SF")));
        // Confirm the values are held
        assertEquals("Hatchery SF", ((BaseLabel) office.get("label")).name);
        assertEquals(-122.391343, office.get("longitude"));
        assertEquals(37.781586, office.get("latitude"));

        // Make one with a dictionary argument AND some keyword arguments
        MapLocation space = new MapLocation(Map.of("latitude", 37.773564, "longitude", -122.415869, "label", new Label("pariSoma")));
        // Confirm the values are held
        assertEquals("pariSoma", ((Label) space.get("label")).name);
        assertEquals(-122.415869, space.get("longitude"));
        assertEquals(37.773564, space.get("latitude"));

        // Supply an invalid attribute type
        assertThrows(DStruct.RequiredAttributeInvalid.class, () -> new MapLocation(Map.of("latitude", 1.5, "longitude", 3, "label", new Label("sup"))));
        assertThrows(DStruct.RequiredAttributeInvalid.class, () -> new MapLocation(Map.of("latitude", 1.5, "longitude", 3.4, "label", 991)));
        // Confirm
        assertThrows(DStruct.RequiredAttributeMissing.class, () -> new MapLocation(Map.of("latitude", 1.5, "longitude", 3.4)));
    }

    @Test
    public void test_struct_required_attribute_dictionary() {
        class SlowInt extends DStruct {
            SlowInt(Map<String, Object> map) {
                super(map);
            }
        }

        // This should work
        new SlowInt(Map.of("value", 9));

        // This should work
        new SlowInt(Map.of("value", 9));

        // Try not sending a field called "value"
        assertThrows(DStruct.RequiredAttributeMissing.class, () -> new SlowInt(Map.of("x", 9)));

        // Try sending an invalid type for "value"
        assertThrows(DStruct.RequiredAttributeInvalid.class, () -> new SlowInt(Map.of("value", 9.4)));
    }

    @Test
    public void test_flexible_schema() {
        class HippieStruct extends DStruct {
            HippieStruct(Map<String, Object> map) {
                super(map);
            }

            @Override
            public Class<?>[] getExtraAllowedTypes(Class<?> type) {
                Class<?>[] extraTypes = super.getExtraAllowedTypes(type);

                if (type == Integer.class) {
                    extraTypes = new Class<?>[]{Float.class};
                } else if (type == Float.class) {
                    extraTypes = new Class<?>[]{Integer.class};
                }

                return extraTypes;
            }
        }

        // None of these should fail, since float and int are interchangeable now
        new HippieStruct(Map.of("x", 1, "y", 1));
        new HippieStruct(Map.of("x", 1.5, "y", 1.5));
        new HippieStruct(Map.of("x", 1, "y", 1.5));
        new HippieStruct(Map.of("x", 1.5, "y", 1));

        // But this should still fail
        assertThrows(DStruct.RequiredAttributeInvalid.class, () -> new HippieStruct(Map.of("x", "DUDE THAT IS A STRING!", "y", 1)));
    }

    @Test
    public void test_delayed_verification() {
        class Product extends DStruct {
            Product(Map<String, Object> map) {
                super(map);
                this.put("price_displayed", "$" + (float) this.get("price_in_cents") / 100);
                this.checkStructSchema();
            }
        }

        // Make a valid Product
        Product product = new Product(Map.of("name", "The Ten Faces of Innovation", "category", "Books", "price_in_cents", 1977));
        assertEquals("$19.77", product.get("price_displayed"));

        // Make a Product that's missing a required attribute
        assertThrows(DStruct.RequiredAttributeMissing.class, () -> new Product(Map.of("name", "The Ten Faces of Innovation", "price_in_cents", 1977)));

        // Make an invalid Product
        assertThrows(DStruct.RequiredAttributeInvalid.class, () -> new Product(Map.of("name", "The Ten Faces of Innovation", "category", null, "price_in_cents", 1977)));
    }

    @Test
    public void test_extra_allowed_types() {
        class NonUser extends DStruct {
            NonUser(Map<String, Object> map) {
                super(map);
            }

            @Override
            public Class<?>[] getExtraAllowedTypes(Class<?> type) {
                if (type == Integer.class) {
                    return new Class<?>[]{Integer.class, null};
                }
                return new Class<?>[0];
            }
        }

        // Make a valid NonUser with name and age
        new NonUser(Map.of("name", "user_from_linked_in_ad_31351513_A13CB941FF22", "age", 27));

        // Make a valid NonUser with name but an age of `None`
        new NonUser(Map.of("name", "user_from_linked_in_ad_31351513_A13CB941FF22", "age", null));

        // Make a NonUser and don't send the age
        assertThrows(DStruct.RequiredAttributeMissing.class, () -> new NonUser(Map.of("name", "user_from_linked_in_ad_31351513_A13CB941FF22")));

        // Make a NonUser and send an age that is not an `int` OR `None`
        assertThrows(DStruct.RequiredAttributeInvalid.class, () -> new NonUser(Map.of("name", "user_from_linked_in_ad_31351513_A13CB941FF22", "age", "eighteen")));
    }
}
