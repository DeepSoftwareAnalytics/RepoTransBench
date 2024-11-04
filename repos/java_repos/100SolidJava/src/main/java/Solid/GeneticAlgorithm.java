package Solid;

import java.util.List;

public abstract class GeneticAlgorithm<T> {
    
    private double crossoverRate;
    private double mutationRate;
    private int maxSteps;
    private Double maxFitness;

    public GeneticAlgorithm(double crossoverRate, double mutationRate, int maxSteps, Double maxFitness) {
        this.crossoverRate = crossoverRate;
        this.mutationRate = mutationRate;
        this.maxSteps = maxSteps;
        this.maxFitness = maxFitness;
    }

    protected double mutationRate() {
        return this.mutationRate;
    }

    protected abstract List<T> initialPopulation();
    protected abstract double fitness(T member);
    protected abstract T crossover(T parent1, T parent2);
    protected abstract T mutate(T member);

    public T run(boolean verbose) {
        // Implementation of the algorithm execution
        return null; // Placeholder return
    }

    private T deepcopy(T target) {
        // Implement deepcopy based on actual class.
        return target;
    }
}
