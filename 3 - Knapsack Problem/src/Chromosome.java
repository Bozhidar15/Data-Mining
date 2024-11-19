import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Chromosome {
    ArrayList<Boolean> genes;
    int weight;
    int value;
    Random random = new Random();
    Chromosome() {
        this.genes = new ArrayList<>(Collections.nCopies(GeneticKnapsack.numItems, false));
    }


    void randomize(int x) {
        do {
            weight = 0;
            value = 0;
            for (int i = 0; i < GeneticKnapsack.numItems; i++) {
                genes.set(i, random.nextBoolean());
                if (genes.get(i)) {
                    weight += GeneticKnapsack.items.get(i).weight();
                    value += GeneticKnapsack.items.get(i).value();
                }
            }
        } while (weight < GeneticKnapsack.maxWeight); // Повтаряме, докато конфигурацията не е валидна
    }

//    void randomize(int n) {
//        weight = 0;
//        value = 0;
//
//        List<Integer> chosenItems = new ArrayList<>();
//        int remainingWeight = GeneticKnapsack.maxWeight;
//        int firstItem = n < GeneticKnapsack.numItems ? n : random.nextInt(GeneticKnapsack.numItems);
//        chosenItems.add(firstItem);
//        weight += GeneticKnapsack.items.get(firstItem).weight();
//        value += GeneticKnapsack.items.get(firstItem).value();
//        remainingWeight -= GeneticKnapsack.items.get(firstItem).weight();
//
//        for (int i = 0; i < GeneticKnapsack.numItems; i++) {
//            if (!chosenItems.contains(i) && remainingWeight >= GeneticKnapsack.items.get(i).weight()) {
//                if (random.nextBoolean()) {
//                    chosenItems.add(i);
//                    weight += GeneticKnapsack.items.get(i).weight();
//                    value += GeneticKnapsack.items.get(i).value();
//                    remainingWeight -= GeneticKnapsack.items.get(i).weight();
//                }
//            }
//        }
//
//        for (int i = 0; i < GeneticKnapsack.numItems; i++) {
//            genes.set(i, chosenItems.contains(i));
//        }
//    }

    void calculateFitness() {
        weight = 0;
        value = 0;
        for (int i = 0; i < GeneticKnapsack.numItems; i++) {
            if (genes.get(i)) {
                weight += GeneticKnapsack.items.get(i).weight();
                value += GeneticKnapsack.items.get(i).value();
            }
        }

        if (weight > GeneticKnapsack.maxWeight) {
            value = 0;
        }
    }

    Chromosome crossover(Chromosome other) {
        Chromosome offspring = new Chromosome();
        for (int i = 0; i < GeneticKnapsack.numItems; i++) {
            offspring.genes.set(i, random.nextBoolean() ? this.genes.get(i) : other.genes.get(i));
        }
        offspring.calculateFitness();
        return offspring;
    }

    void mutate() {
        for (int i = 0; i < GeneticKnapsack.numItems; i++) {
            if (random.nextDouble() < 0.1) {
                genes.set(i, !genes.get(i));
            }
        }
        calculateFitness();
    }

    int getValue() {
        return value;
    }
}