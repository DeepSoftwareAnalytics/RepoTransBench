import java.util.HashMap;
import java.util.Map;

public class DStruct {

    public DStruct(Map<String, Object> inputDict) {
        loadStructInputs(inputDict);
        this._structHasLoaded = true;
        if (structSchemaCheckOnInit()) {
            checkStructSchema();
        }
    }

    private final Map<String, Object> data = new HashMap<>();
    private boolean _structHasLoaded = false;

    public void loadStructInputs(Map<String, Object> inputDict) {
        if (inputDict != null) {
            data.putAll(inputDict);
        }
    }

    public Object get(String key) {
        return data.get(key);
    }

    public void put(String key, Object value) {
        data.put(key, value);
    }

    public static boolean structSchemaCheckOnInit() {
        return true;
    }

    public void checkStructSchema() {
        checkStructSchema(getClass());
    }

    public void checkStructSchema(Class<?> clazz) {
        for (Map.Entry<String, Object> entry : requiredAttributes(clazz).entrySet()) {
            String key = entry.getKey();
            Class<?> requiredType = (Class<?>) entry.getValue();

            if (!data.containsKey(key)) {
                throw new RequiredAttributeMissing(this, key);
            }

            if (requiredType != null) {
                Object value = data.get(key);
                Class<?>[] allowedTypes = getExtraAllowedTypes(requiredType);

                boolean isValid = false;
                for (Class<?> type : allowedTypes) {
                    if (type.isInstance(value)) {
                        isValid = true;
                        break;
                    }
                }

                if (!isValid) {
                    throw new RequiredAttributeInvalid(this, key, value);
                }
            }
        }
    }

    public Class<?>[] getExtraAllowedTypes(Class<?> type) {
        if (type == String.class) {
            return new Class<?>[]{String.class};
        }
        return new Class<?>[]{type};
    }

    public static Map<String, Object> requiredAttributes(Class<?> clazz) {
        Map<String, Object> requiredAttributes = new HashMap<>();

        while (clazz != null && clazz != Object.class) {
            for (Map.Entry<String, Attribute> entry : attributesOf(clazz).entrySet()) {
                if (entry.getValue() instanceof RequiredAttribute) {
                    requiredAttributes.put(entry.getKey(), entry.getValue().getType());
                }
            }
            clazz = clazz.getSuperclass();
        }

        return requiredAttributes;
    }

    private static Map<String, Attribute> attributesOf(Class<?> clazz) {
        Map<String, Attribute> attributes = new HashMap<>();
        for (java.lang.reflect.Field field : clazz.getDeclaredFields()) {
            if (Attribute.class.isAssignableFrom(field.getType())) {
                try {
                    attributes.put(field.getName(), (Attribute) field.get(null));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Unable to access attribute: " + field.getName(), e);
                }
            }
        }
        return attributes;
    }

    public static abstract class Attribute {
        public abstract Class<?> getType();
    }

    public static class RequiredAttribute extends Attribute {
        private final Class<?> type;

        public RequiredAttribute() {
            this(null);
        }

        public RequiredAttribute(Class<?> type) {
            this.type = type;
        }

        @Override
        public Class<?> getType() {
            return this.type;
        }
    }

    public static class RequiredAttributeMissing extends RuntimeException {
        public RequiredAttributeMissing(DStruct structInstance, String key) {
            super("You need an attribute called `" + key + "` when making a " + structInstance.getClass().getSimpleName());
        }
    }

    public static class RequiredAttributeInvalid extends RuntimeException {
        public RequiredAttributeInvalid(DStruct structInstance, String key, Object value) {
            super("The value of the attribute`" + key + "` must be an instance of " +
                    structInstance.getClass().getSimpleName() + ". Instead, I got: " + value + ", which is a " + value.getClass());
        }
    }
}

