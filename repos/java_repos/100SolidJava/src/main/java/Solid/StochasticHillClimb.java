package Solid;

import java.util.Random;

public abstract class StochasticHillClimb<T> {

    private T initialState;
    private T currentState;
    private T bestState;
    private int curSteps = 0;
    private int maxSteps;
    private Double bestObjective;
    private Double maxObjective;
    private double temp;

    public StochasticHillClimb(T initialState, double temp, int maxSteps, Double maxObjective) {
        this.initialState = initialState;
        this.maxSteps = maxSteps;
        this.maxObjective = maxObjective;
        this.temp = temp;
    }

    @Override
    public String toString() {
        return String.format("STOCHASTIC HILL CLIMB: \nCURRENT STEPS: %d \nBEST OBJECTIVE: %f \nBEST STATE: %s \n\n",
                curSteps, bestObjective, bestState.toString());
    }

    private void clear() {
        curSteps = 0;
        currentState = null;
        bestState = null;
        bestObjective = null;
    }

    protected abstract T neighbor();

    protected abstract double objective(T state);

    private boolean acceptNeighbor(T neighbor) {
        try {
            double p = 1.0 / (1.0 + Math.exp((objective(currentState) - objective(neighbor)) / temp));
            return p >= 1 || p >= new Random().nextDouble();
        } catch (ArithmeticException e) {
            return true;
        }
    }

    public T run(boolean verbose) {
        clear();
        currentState = initialState;
        for (int i = 0; i < maxSteps; i++) {
            curSteps++;

            if ((i + 1) % 100 == 0 && verbose) {
                System.out.println(this);
            }

            T neighbor = neighbor();

            if (acceptNeighbor(neighbor)) {
                currentState = neighbor;
            }

            if (objective(currentState) > (bestObjective == null ? 0 : bestObjective)) {
                bestObjective = objective(currentState);
                bestState = deepcopy(currentState);
            }

            if (maxObjective != null && (bestObjective == null ? 0 : bestObjective) > maxObjective) {
                System.out.println("TERMINATING - REACHED MAXIMUM OBJECTIVE");
                return bestState;
            }
        }
        System.out.println("TERMINATING - REACHED MAXIMUM STEPS");
        return bestState;
    }

    private T deepcopy(T target) {
        // Implement deepcopy based on actual class.
        return target;
    }
}
