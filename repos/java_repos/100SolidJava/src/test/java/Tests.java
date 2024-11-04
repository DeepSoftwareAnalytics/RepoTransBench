import Solid.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

public class Tests {

    public class EvolutionaryAlgorithmImpl extends EvolutionaryAlgorithm<String> {

        public EvolutionaryAlgorithmImpl(double crossoverRate, double mutationRate, int maxSteps, Double maxFitness) {
            super(crossoverRate, mutationRate, maxSteps, maxFitness);
        }

        @Override
        protected List<String> initialPopulation() {
            return IntStream.range(0, 50).mapToObj(i -> {
                Random rand = new Random();
                StringBuilder sb = new StringBuilder();
                for (int j = 0; j < 5; j++) {
                    sb.append((char) ('a' + rand.nextInt(26)));
                }
                return sb.toString();
            }).collect(Collectors.toList());
        }

        @Override
        protected double fitness(String member) {
            return (int) IntStream.range(0, 5)
                    .filter(i -> "clout".charAt(i) == member.charAt(i))
                    .count();
        }

        @Override
        protected String crossover(String parent1, String parent2) {
            Random rand = new Random();
            int partition = rand.nextInt(parent1.length() - 1);
            return parent1.substring(0, partition) + parent2.substring(partition);
        }

        @Override
        protected String mutate(String member) {
            Random rand = new Random();
            if (mutationRate() >= rand.nextDouble()) {
                char[] chars = member.toCharArray();
                chars[rand.nextInt(5)] = (char) ('a' + rand.nextInt(26));
                return new String(chars);
            }
            return member;
        }
    }

    private EvolutionaryAlgorithmImpl evolutionaryAlgorithm;

    @BeforeEach
    public void setUp() {
        evolutionaryAlgorithm = new EvolutionaryAlgorithmImpl(0.5, 0.7, 500, null);
    }

    @Test
    public void testInitialPopulation() {
        List<String> population = evolutionaryAlgorithm.initialPopulation();
        assertEquals(50, population.size());
        assertTrue(population.stream().allMatch(member -> member.length() == 5));
    }

    @Test
    public void testFitness() {
        double fitness = evolutionaryAlgorithm.fitness("clout");
        assertEquals(5.0, fitness);
    }

    @Test
    public void testCrossover() {
        String parent1 = "abcde";
        String parent2 = "fghij";
        String child = evolutionaryAlgorithm.crossover(parent1, parent2);
        assertEquals(5, child.length());
    }

    @Test
    public void testMutate() {
        String member = "abcde";
        String mutatedMember = evolutionaryAlgorithm.mutate(member);
        assertEquals(5, mutatedMember.length());
    }

    @Test
    public void testRun() {
        String result = evolutionaryAlgorithm.run(false);
        assertNotNull(result);
    }

    public class GeneticAlgorithmImpl extends GeneticAlgorithm<List<Integer>> {

        public GeneticAlgorithmImpl(double crossoverRate, double mutationRate, int maxSteps, Double maxFitness) {
            super(crossoverRate, mutationRate, maxSteps, maxFitness);
        }

        @Override
        protected List<List<Integer>> initialPopulation() {
            Random rand = new Random();
            return IntStream.range(0, 50).mapToObj(i -> 
                IntStream.range(0, 6).mapToObj(j -> rand.nextInt(2)).collect(Collectors.toList())
            ).collect(Collectors.toList());
        }

        @Override
        protected double fitness(List<Integer> member) {
            List<Integer> target = Arrays.asList(0, 0, 0, 1, 1, 1);
            return IntStream.range(0, 6)
                    .filter(i -> target.get(i).equals(member.get(i)))
                    .count();
        }

        @Override
        protected List<Integer> crossover(List<Integer> parent1, List<Integer> parent2) {
            Random rand = new Random();
            int partition = rand.nextInt(parent1.size() - 1);
            List<Integer> child = new ArrayList<>(parent1.subList(0, partition));
            child.addAll(parent2.subList(partition, parent2.size()));
            return child;
        }

        @Override
        protected List<Integer> mutate(List<Integer> member) {
            Random rand = new Random();
            if (mutationRate() >= rand.nextDouble()) {
                int index = rand.nextInt(member.size());
                member.set(index, member.get(index) == 0 ? 1 : 0);
            }
            return member;
        }
    }

    private GeneticAlgorithmImpl geneticAlgorithm;

    @BeforeEach
    public void setUpGenetic() {
        geneticAlgorithm = new GeneticAlgorithmImpl(0.5, 0.7, 500, null);
    }

    @Test
    public void testInitialPopulationGA() {
        List<List<Integer>> population = geneticAlgorithm.initialPopulation();
        assertEquals(50, population.size());
        assertTrue(population.stream().allMatch(member -> member.size() == 6));
    }

    @Test
    public void testFitnessGA() {
        double fitness = geneticAlgorithm.fitness(Arrays.asList(0, 0, 0, 1, 1, 1));
        assertEquals(6.0, fitness);
    }
}

