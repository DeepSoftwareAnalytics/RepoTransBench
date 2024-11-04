import java.util.*;

public class Optimize {
    private static final Map<String, String> X_ROT_CW = Map.of(
        "U", "F", "B", "U", "D", "B", "F", "D", "E", "Si", "S", "E", "Y", "Z", "Z", "Yi"
    );
    private static final Map<String, String> Y_ROT_CW = Map.of(
        "B", "L", "R", "B", "F", "R", "L", "F", "S", "Mi", "M", "S", "Z", "X", "X", "Zi"
    );
    private static final Map<String, String> Z_ROT_CW = Map.of(
        "U", "L", "R", "U", "D", "R", "L", "D", "E", "Mi", "M", "E", "Y", "Xi", "X", "Y"
    );

    private static Map<String, String> getRotTable(String rot) {
        switch (rot) {
            case "X": return X_ROT_CW;
            case "Xi": return invertMap(X_ROT_CW);
            case "Y": return Y_ROT_CW;
            case "Yi": return invertMap(Y_ROT_CW);
            case "Z": return Z_ROT_CW;
            case "Zi": return invertMap(Z_ROT_CW);
            default: return Collections.emptyMap();
        }
    }

    private static Map<String, String> invertMap(Map<String, String> map) {
        Map<String, String> inverted = new HashMap<>();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            inverted.put(entry.getValue(), entry.getKey());
        }
        return inverted;
    }

    private static String invert(String move) {
        return move.endsWith("i") ? move.substring(0, 1) : move + "i";
    }

    public static void applyRepeatThreeOptimization(List<String> moves) {
        // Implement the applyRepeatThreeOptimization method
    }

    public static void applyDoUndoOptimization(List<String> moves) {
        // Implement the applyDoUndoOptimization method
    }

    public static void applyNoFullCubeRotationOptimization(List<String> moves) {
        // Implement the applyNoFullCubeRotationOptimization method
    }

    public static List<String> optimizeMoves(List<String> moves) {
        List<String> result = new ArrayList<>(moves);
        applyNoFullCubeRotationOptimization(result);
        applyRepeatThreeOptimization(result);
        applyDoUndoOptimization(result);
        return result;
    }
}
