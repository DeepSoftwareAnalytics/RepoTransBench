import java.lang.reflect.Field;
import java.util.EnumSet;

public class VarDumpExport {
    private static final int TAB_SIZE = 4;

    private static String display(Object o, int space, int num, Object key, Class<?> typ, boolean proret) {
        StringBuilder st = new StringBuilder();
        if (key != null) {
            if (typ == java.util.Map.class) {
                st.append(" ".repeat(space)).append("['").append(key).append("'] => ");
            } else {
                st.append(" ".repeat(space)).append(key).append(" => ");
            }
        } else if (space > 0) {
            st.append(" ".repeat(space)).append("[").append(num).append("] => ");
        } else {
            st.append("#").append(num).append(" ");
        }

        if (o instanceof String) {
            st.append("str(").append(((String) o).length()).append(") \"").append(o).append("\"");
        } else if (o instanceof Integer) {
            st.append("int(").append(o).append(") ");
        } else if (o instanceof Long) {
            st.append("long(").append(o).append(") ");
        } else if (o instanceof Float || o instanceof Double) {
            st.append("float(").append(o).append(") ");
        } else if (o instanceof Boolean) {
            st.append("bool(").append(((Boolean) o) ? "True" : "False").append(") ");
        } else if (o instanceof Enum) {
            st.append("Enum(").append(o.toString()).append(")");
        } else if (o == null) {
            st.append("NoneType(None) ");
        } else if (o.getClass().isArray() || o instanceof java.util.List || o instanceof java.util.Map) {
            st.append(o.getClass().getSimpleName()).append("(").append(getLength(o)).append(") ");
        } else {
            st.append("object(").append(o.getClass().getSimpleName()).append(") (");
            try {
                Field[] fields = o.getClass().getDeclaredFields();
                st.append(fields.length).append(")");
            } catch (SecurityException e) {
                st.append("?)");
            }
        }

        if (proret) {
            System.out.print(st.toString());
        }

        return st.toString();
    }

    private static String dump(Object o, int space, int num, Object key, Class<?> typ, boolean proret, Object[] parents) {
        for (Object parent : parents) {
            if (o == parent) {
                return display(o, space, num, key, typ, proret) + " …circular reference…";
            }
        }

        String result = display(o, space, num, key, typ, proret);

        if (o == null || o instanceof Enum || o instanceof String || o instanceof Integer || o instanceof Long ||
                o instanceof Float || o instanceof Double || o instanceof Boolean) {
            return result;
        }

        if (o instanceof Object[]) {
            parents = append(parents, o);
            space += TAB_SIZE;
            for (int i = 0; i < ((Object[]) o).length; i++) {
                result += dump(((Object[]) o)[i], space, i, null, o.getClass(), proret, parents);
            }
            return result;
        }

        parents = append(parents, o);
        space += TAB_SIZE;
        if (o instanceof java.util.List) {
            for (int i = 0; i < ((java.util.List<?>) o).size(); i++) {
                result += dump(((java.util.List<?>) o).get(i), space, i, null, o.getClass(), proret, parents);
            }
        } else if (o instanceof java.util.Map) {
            int i = 0;
            for (Object keyObj : ((java.util.Map<?, ?>) o).keySet()) {
                result += dump(((java.util.Map<?, ?>) o).get(keyObj), space, i, keyObj, o.getClass(), proret, parents);
                i++;
            }
        } else {
            for (Field field : o.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                try {
                    result += dump(field.get(o), space, num, field.getName(), o.getClass(), proret, parents);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    private static int getLength(Object o) {
        if (o instanceof Object[]) {
            return ((Object[]) o).length;
        } else if (o instanceof java.util.Collection) {
            return ((java.util.Collection<?>) o).size();
        } else if (o instanceof java.util.Map) {
            return ((java.util.Map<?, ?>) o).size();
        }
        return -1;
    }

    public static void var_dump(Object... obs) {
        int i = 0;
        for (Object x : obs) {
            dump(x, 0, i, null, Object.class, true, new Object[0]);
            i++;
        }
    }

    public static String var_export(Object... obs) {
        StringBuilder result = new StringBuilder();
        int i = 0;
        for (Object x : obs) {
            result.append(dump(x, 0, i, null, Object.class, false, new Object[0]));
            i++;
        }
        return result.toString();
    }

    private static Object[] append(Object[] array, Object element) {
        Object[] newArray = new Object[array.length + 1];
        System.arraycopy(array, 0, newArray, 0, array.length);
        newArray[array.length] = element;
        return newArray;
    }
}
