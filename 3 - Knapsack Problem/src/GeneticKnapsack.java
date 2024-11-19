import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.Scanner;

public class GeneticKnapsack {
    static int maxWeight;
    static int numItems;
    static ArrayList<Iteam> items = new ArrayList<>();
    static Chromosome bestSolution = null;
    static Random random = new Random();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        maxWeight = scanner.nextInt();
        numItems = scanner.nextInt();

        for (int i = 0; i < numItems; i++) {
            int weight = scanner.nextInt();
            int value = scanner.nextInt();
            items.add(new Iteam(weight, value));
        }

        scanner.close();
        int populationSize = 500;
        int generations = 10000;

        ArrayList<Chromosome> population = initializePopulation(populationSize);
        ChromosomeComparator comparator = new ChromosomeComparator();

        bestSolution = Collections.max(population, comparator);

        for (int generation = 1; generation <= generations; generation++) {
            evaluatePopulation(population);

            if (generation == 1 || generation == generations || generation % (generations / 10) == 0) {
                System.out.println("Generation " + generation + ": " + bestSolution.getValue());
            }

            population = generateNewPopulation(population);
        }
    }

    static ArrayList<Chromosome> initializePopulation(int size) {
        ArrayList<Chromosome> population = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            Chromosome chromosome = new Chromosome();
            chromosome.randomize(i);
            population.add(chromosome);
        }
        return population;
    }

    static void evaluatePopulation(ArrayList<Chromosome> population) {
        for (Chromosome chromosome : population) {
            chromosome.calculateFitness();
        }
    }

//    static ArrayList<Chromosome> generateNewPopulation(ArrayList<Chromosome> population) {
//        ArrayList<Chromosome> newPopulation = new ArrayList<>();
//        for (int i = 0; i < population.size(); i++) {
//            Chromosome parent1 = selectParent(population);
//            Chromosome parent2 = selectParent(population);
//            Chromosome offspring = parent1.crossover(parent2);
//            offspring.mutate();
//            newPopulation.add(offspring);
//        }
//        return newPopulation;
//    }

    static ArrayList<Chromosome> generateNewPopulation(ArrayList<Chromosome> population) {
        ArrayList<Chromosome> newPopulation = new ArrayList<>();
        ChromosomeComparator comparator = new ChromosomeComparator();

        for (int i = 0; i < population.size(); i++) {
            Chromosome parent1 = selectParent(population);
            Chromosome parent2 = selectParent(population);
            Chromosome offspring = parent1.crossover(parent2);
            offspring.mutate();
            newPopulation.add(offspring);
        }

        Chromosome newBestSolution = Collections.max(newPopulation, comparator);

        if (bestSolution == null || newBestSolution.getValue() > bestSolution.getValue()) {
            bestSolution = newBestSolution;
        }

        return newPopulation;
    }

    static Chromosome selectParent(ArrayList<Chromosome> population) {
        Chromosome parent1 = population.get(random.nextInt(population.size()));
        Chromosome parent2 = population.get(random.nextInt(population.size()));
        return parent1.getValue() > parent2.getValue() ? parent1 : parent2;
    }


}
