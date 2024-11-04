package Solid;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public abstract class TabuSearch<T> {

    private int curSteps;
    private int tabuSize;
    private Deque<T> tabuList;
    private T initialState;
    private T current;
    private T best;
    private int maxSteps;
    private Double maxScore;

    public TabuSearch(T initialState, int tabuSize, int maxSteps, Double maxScore) {
        this.initialState = initialState;
        this.tabuSize = tabuSize;
        this.maxSteps = maxSteps;
        this.maxScore = maxScore;
    }

    @Override
    public String toString() {
        return String.format("TABU SEARCH: \nCURRENT STEPS: %d \nBEST SCORE: %f \nBEST MEMBER: %s \n\n",
                curSteps, score(best), best.toString());
    }

    private void clear() {
        curSteps = 0;
        tabuList = new ArrayDeque<>(tabuSize);
        current = initialState;
        best = initialState;
    }

    protected abstract double score(T state);

    protected abstract List<T> neighborhood();

    private T best(List<T> neighborhood) {
        return neighborhood.stream()
                .max((x, y) -> Double.compare(score(x), score(y)))
                .orElseThrow();
    }

    public T run(boolean verbose) {
        clear();
        Random random = new Random();
        for (int i = 0; i < maxSteps; i++) {
            curSteps++;
            if ((i + 1) % 100 == 0 && verbose) {
                System.out.println(this);
            }

            List<T> neighborhood = neighborhood();
            T neighborhoodBest = best(neighborhood);

            while (true) {
                List<T> nonTabuNeighborhood = neighborhood.stream()
                        .filter(x -> !tabuList.contains(x))
                        .collect(Collectors.toList());
                if (nonTabuNeighborhood.isEmpty()) {
                    System.out.println("TERMINATING - NO SUITABLE NEIGHBORS");
                    return best;
                }
                if (tabuList.contains(neighborhoodBest)) {
                    if (score(neighborhoodBest) > score(best)) {
                        tabuList.add(neighborhoodBest);
                        best = deepcopy(neighborhoodBest);
                        break;
                    } else {
                        neighborhood.remove(neighborhoodBest);
                        neighborhoodBest = best(neighborhood);
                    }
                } else {
                    tabuList.add(neighborhoodBest);
                    current = neighborhoodBest;
                    if (score(current) > score(best)) {
                        best = deepcopy(current);
                    }
                    break;
                }
            }
            if (maxScore != null && score(best) > maxScore) {
                System.out.println("TERMINATING - REACHED MAXIMUM SCORE");
                return best;
            }
        }
        System.out.println("TERMINATING - REACHED MAXIMUM STEPS");
        return best;
    }

    private T deepcopy(T target) {
        // Assuming target object is Immutable or has a proper deepcopy method to clone the object.
        // This needs to be implemented based on actual class being used.
        return target;
    }
}
