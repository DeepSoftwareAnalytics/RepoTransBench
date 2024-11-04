package Solid;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public abstract class EvolutionaryAlgorithm<T> {

    private double crossoverRate;
    private double mutationRate;
    private int maxSteps;
    private Double maxFitness;
    protected List<T> population;

    public EvolutionaryAlgorithm(double crossoverRate, double mutationRate, int maxSteps, Double maxFitness) {
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

    @Override
    public String toString() {
        return String.format("EVOLUTIONARY ALGORITHM: \nPOPULATION SIZE: %d \nBEST FITNESS: %f\n", population.size(), fitness(getBestMember()));
    }

    private void clear() {
        population = initialPopulation();
    }

    private T getBestMember() {
        return population.stream().max((member1, member2) -> Double.compare(fitness(member1), fitness(member2))).orElseThrow();
    }

    private T selectParent(List<Double> cumulativeFitness) {
        Random rand = new Random();
        double selection = rand.nextDouble() * cumulativeFitness.get(cumulativeFitness.size() - 1);
        for (int i = 0; i < cumulativeFitness.size(); i++) {
            if (selection < cumulativeFitness.get(i)) {
                return population.get(i);
            }
        }
        return null;  // This line should theoretically never be reached.
    }

    public T run(boolean verbose) {
        clear();

        for (int step = 0; step < maxSteps; step++) {
            if (verbose && (step + 1) % 100 == 0) {
                System.out.println(this);
            }

            // Evaluate fitness of current population
            List<Double> fitnessScores = population.stream().map(this::fitness).collect(Collectors.toList());
            double maxFitnessVal = fitnessScores.stream().max(Double::compare).orElseThrow();

            // Check for termination condition
            if (maxFitness != null && maxFitnessVal >= maxFitness) {
                if (verbose) {
                    System.out.println("TERMINATING - REACHED MAXIMUM FITNESS");
                }
                return getBestMember();
            }

            // Selection process
            List<Double> cumulativeFitness = IntStream.range(0, fitnessScores.size())
                    .mapToDouble(i -> fitnessScores.subList(0, i + 1).stream().mapToDouble(Double::doubleValue).sum())
                    .boxed()
                    .collect(Collectors.toList());

            // Generate new population
            List<T> newPopulation = IntStream.range(0, population.size()).mapToObj(i -> {
                T parent1 = selectParent(cumulativeFitness);
                T parent2 = selectParent(cumulativeFitness);

                T child;
                if (new Random().nextDouble() < crossoverRate) {
                    child = crossover(parent1, parent2);
                } else {
                    child = parent1;
                }

                return mutate(child);
            }).collect(Collectors.toList());

            population = newPopulation;
        }
        if (verbose) {
            System.out.println("TERMINATING - REACHED MAXIMUM STEPS");
        }
        return getBestMember();
    }

    private T deepcopy(T target) {
        // Implement deepcopy based on actual class.
        return target;
    }
}
