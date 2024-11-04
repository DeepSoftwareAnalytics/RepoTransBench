package main.java;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

class Entity {
    String text;
    String type;
    int start;

    Entity(String text, String type, int start) {
        this.text = text;
        this.type = type;
        this.start = start;
    }
}

public class Nereval {

    public static boolean hasOverlap(Entity x, Entity y) {
        int endX = x.start + x.text.length();
        int endY = y.start + y.text.length();
        return x.start < endY && y.start < endX;
    }

    public static boolean correctText(Entity x, Entity y) {
        return x.text.equals(y.text) && x.start == y.start;
    }

    public static boolean correctType(Entity x, Entity y) {
        return x.type.equals(y.type) && hasOverlap(x, y);
    }

    public static int[] countCorrect(List<Entity> trueEntities, List<Entity> predEntities) {
        int countText = 0;
        int countType = 0;

        for (Entity x : trueEntities) {
            for (Entity y : predEntities) {
                boolean textMatch = correctText(x, y);
                boolean typeMatch = correctType(x, y);

                if (textMatch) {
                    countText++;
                }

                if (typeMatch) {
                    countType++;
                }

                if (typeMatch || textMatch) {
                    break;
                }
            }
        }

        return new int[]{countText, countType};
    }

    public static double precision(int correct, int actual) {
        if (actual == 0) {
            return 0;
        }
        return (double) correct / actual;
    }

    public static double recall(int correct, int possible) {
        if (possible == 0) {
            return 0;
        }
        return (double) correct / possible;
    }

    public static double f1(double p, double r) {
        if (p + r == 0) {
            return 0;
        }
        return 2 * (p * r) / (p + r);
    }

    public static double evaluate(List<List<Entity>> yTrue, List<List<Entity>> yPred) {
        if (yTrue.size() != yPred.size()) {
            throw new IllegalArgumentException("Bad input shape: y_true and y_pred should have the same length.");
        }

        int correct = 0;
        int actual = 0;
        int possible = 0;

        for (int i = 0; i < yTrue.size(); i++) {
            int[] counts = countCorrect(yTrue.get(i), yPred.get(i));
            correct += counts[0] + counts[1];
            possible += yTrue.get(i).size() * 2;
            actual += yPred.get(i).size() * 2;
        }

        return f1(precision(correct, actual), recall(correct, possible));
    }

    public static int[] signTest(List<List<Entity>> truth, List<List<Entity>> modelA, List<List<Entity>> modelB) {
        int better = 0;
        int worse = 0;

        for (int i = 0; i < truth.size(); i++) {
            double scoreA = evaluate(List.of(truth.get(i)), List.of(modelA.get(i)));
            double scoreB = evaluate(List.of(truth.get(i)), List.of(modelB.get(i)));

            if (scoreA > scoreB) {
                worse++;
            } else if (scoreB > scoreA) {
                better++;
            }
        }

        return new int[]{better, worse};
    }

    public static List<JsonEntity> parseJson(String fileName) throws IOException {
        Gson gson = new Gson();
        return gson.fromJson(new FileReader(fileName), new TypeToken<List<JsonEntity>>() {}.getType());
    }

    public static double evaluateJson(String fileName) throws IOException {
        List<JsonEntity> data = parseJson(fileName);
        List<List<Entity>> yTrue = new ArrayList<>();
        List<List<Entity>> yPred = new ArrayList<>();

        for (JsonEntity instance : data) {
            yTrue.add(instance.trueEntities);
            yPred.add(instance.predictedEntities);
        }

        return evaluate(yTrue, yPred);
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java Nereval <file_name>");
            System.exit(1);
        }

        try {
            System.out.printf("F1-score: %.2f%n", evaluateJson(args[0]));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class JsonEntity {
        List<Entity> trueEntities;
        List<Entity> predictedEntities;

        public JsonEntity(List<Entity> trueEntities, List<Entity> predictedEntities) {
            this.trueEntities = trueEntities;
            this.predictedEntities = predictedEntities;
        }
    }
}
