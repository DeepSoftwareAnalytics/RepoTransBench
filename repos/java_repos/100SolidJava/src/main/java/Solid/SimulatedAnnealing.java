package Solid;

import java.util.Random;

public abstract class SimulatedAnnealing<T> {

    private T initialState;
    private T currentState;
    private T bestState;
    private int curSteps = 0;
    private int maxSteps;
    private double currentEnergy;
    private double bestEnergy;
    private Double minEnergy;
    private double startTemp;
    private double currentTemp;
    private Runnable adjustTemp;

    public SimulatedAnnealing(T initialState, double tempBegin, double scheduleConstant, int maxSteps,
                              Double minEnergy, String schedule) {
        this.initialState = initialState;
        this.maxSteps = maxSteps;
        this.minEnergy = minEnergy;
        this.startTemp = tempBegin;

        if ("exponential".equalsIgnoreCase(schedule)) {
            this.adjustTemp = () -> currentTemp *= scheduleConstant;
        } else if ("linear".equalsIgnoreCase(schedule)) {
            this.adjustTemp = () -> currentTemp -= scheduleConstant;
        } else {
            throw new IllegalArgumentException("Annealing schedule must be either \"exponential\" or \"linear\"");
        }
    }

    @Override
    public String toString() {
        return String.format("SIMULATED ANNEALING: \nCURRENT STEPS: %d \nCURRENT TEMPERATURE: %f \nBEST ENERGY: %f \nBEST STATE: %s \n\n",
                curSteps, currentTemp, bestEnergy, bestState.toString());
    }

    private void clear() {
        curSteps = 0;
        currentState = null;
        bestState = null;
        currentEnergy = 0;
        bestEnergy = 0;
    }

    protected abstract T neighbor();

    protected abstract double energy(T state);

    private boolean acceptNeighbor(T neighbor) {
        try {
            double p = Math.exp(-(energy(neighbor) - energy(currentState)) / currentTemp);
            return p >= 1 || p >= new Random().nextDouble();
        } catch (ArithmeticException e) {
            return true;
        }
    }

    public T run(boolean verbose) {
        clear();
        currentState = initialState;
        currentTemp = startTemp;
        bestEnergy = energy(currentState);

        for (int i = 0; i < maxSteps; i++) {
            curSteps++;

            if (verbose && (i + 1) % 100 == 0) {
                System.out.println(this);
            }

            T neighbor = neighbor();

            if (acceptNeighbor(neighbor)) {
                currentState = neighbor;
            }
            currentEnergy = energy(currentState);

            if (currentEnergy < bestEnergy) {
                bestEnergy = currentEnergy;
                bestState = deepcopy(currentState);
            }

            if (minEnergy != null && currentEnergy < minEnergy) {
                System.out.println("TERMINATING - REACHED MINIMUM ENERGY");
                return bestState;
            }

            adjustTemp.run();
            if (currentTemp < 0.000001) {
                System.out.println("TERMINATING - REACHED TEMPERATURE OF 0");
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
