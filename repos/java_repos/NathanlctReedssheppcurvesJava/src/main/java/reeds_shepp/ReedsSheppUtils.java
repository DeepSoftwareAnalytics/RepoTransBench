package reeds_shepp;

import utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class ReedsSheppUtils {

    public static PathElement[] getOptimalPath(double[] start, double[] end) {
        List<PathElement[]> paths = getAllPaths(start, end);
        return paths.stream().min((a, b) -> Double.compare(pathLength(a), pathLength(b))).orElse(new PathElement[]{});
    }

    public static List<PathElement[]> getAllPaths(double[] start, double[] end) {
        List<PathElement[]> paths = new ArrayList<>();

        double[] transformedEnd = Utils.changeOfBasis(start, end);

        paths.addAll(getVariants(path1(transformedEnd[0], transformedEnd[1], transformedEnd[2])));
        paths.addAll(getVariants(path2(transformedEnd[0], transformedEnd[1], transformedEnd[2])));
        // Add other path functions similarly

        // Remove paths with parameter 0
        paths.forEach(path -> {
            List<PathElement> filteredPath = new ArrayList<>();
            for (PathElement e : path) {
                if (e.getParam() != 0) {
                    filteredPath.add(e);
                }
            }
            paths.add(filteredPath.toArray(new PathElement[0]));
        });

        // Remove empty paths
        paths.removeIf(path -> path.length == 0);

        return paths;
    }

    private static List<PathElement[]> getVariants(PathElement[] path) {
        List<PathElement[]> variants = new ArrayList<>();
        variants.add(path);
        variants.add(timeflip(path));
        variants.add(reflect(path));
        variants.add(reflect(timeflip(path)));
        return variants;
    }

    public static PathElement[] timeflip(PathElement[] path) {
        PathElement[] newPath = new PathElement[path.length];
        for (int i = 0; i < path.length; i++) {
            newPath[i] = path[i].reverseGear();
        }
        return newPath;
    }

    public static PathElement[] reflect(PathElement[] path) {
        PathElement[] newPath = new PathElement[path.length];
        for (int i = 0; i < path.length; i++) {
            newPath[i] = path[i].reverseSteering();
        }
        return newPath;
    }

    public static double pathLength(PathElement[] path) {
        double length = 0;
        for (PathElement element : path) {
            length += element.getParam();
        }
        return length;
    }

    private static PathElement[] path1(double x, double y, double phi) {
        // Translated logic from Python for path1

        // Sample logic, replace with actual translated logic
        PathElement[] path = {
            PathElement.create(x, Steering.STRAIGHT, Gear.FORWARD)
        };
        return path;
    }

    private static PathElement[] path2(double x, double y, double phi) {
        // Translated logic from Python for path2

        // Sample logic, replace with actual translated logic
        PathElement[] path = {
            PathElement.create(y, Steering.LEFT, Gear.FORWARD)
        };
        return path;
    }

    // Implement other path functions as required similar to the Python code
}
