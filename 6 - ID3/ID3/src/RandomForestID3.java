import java.io.*;
import java.util.*;

public class RandomForestID3 {

    // DataProcessor class for reading and preparing data
    static class DataProcessor {
        private List<Map<String, String>> data;
        private List<String> attributes;

        public void loadData(String filePath) throws IOException {
            data = new ArrayList<>();
            attributes = new ArrayList<>();

            try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                String line;
                boolean readingData = false;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("@attribute")) {
                        String attributeName = line.split(" ")[1];
                        attributes.add(attributeName);
                    } else if (line.startsWith("@data")) {
                        readingData = true;
                    } else if (readingData && !line.isBlank()) {
                        String[] values = line.split(",");
                        Map<String, String> entry = new HashMap<>();
                        for (int i = 0; i < attributes.size(); i++) {
                            entry.put(attributes.get(i), values[i]);
                        }
                        data.add(entry);
                    }
                }
            }
        }

        public List<Map<String, String>> getData() {
            return data;
        }

        public List<String> getAttributes() {
            return attributes;
        }

        public void handleMissingValues() {
            for (String attribute : attributes) {
                Map<String, Integer> valueCounts = new HashMap<>();
                for (Map<String, String> row : data) {
                    String value = row.get(attribute);
                    if (!"?".equals(value)) {
                        valueCounts.put(value, valueCounts.getOrDefault(value, 0) + 1);
                    }
                }
                String mostFrequentValue = valueCounts.entrySet().stream()
                        .max(Map.Entry.comparingByValue())
                        .map(Map.Entry::getKey)
                        .orElse(null);

                for (Map<String, String> row : data) {
                    if ("?".equals(row.get(attribute))) {
                        row.put(attribute, mostFrequentValue);
                    }
                }
            }
        }
    }

    // DecisionTree class for ID3 implementation
    static class DecisionTree {
        private static class Node {
            String attribute;
            Map<String, Node> children = new HashMap<>();
            String label; // For leaf nodes
        }

        private Node root;
        private int maxDepth;
        private int minExamples;
        private double minGain;

        public DecisionTree(int maxDepth, int minExamples, double minGain) {
            this.maxDepth = maxDepth;
            this.minExamples = minExamples;
            this.minGain = minGain;
        }

        public void train(List<Map<String, String>> data, List<String> attributes) {
            root = buildTree(data, attributes, 0);
        }

        private Node buildTree(List<Map<String, String>> data, List<String> attributes, int depth) {
            if (data.isEmpty() || attributes.isEmpty() || depth >= maxDepth || isHomogeneous(data)) {
                Node leaf = new Node();
                leaf.label = getMajorityLabel(data);
                return leaf;
            }

            String bestAttribute = getBestAttribute(data, attributes);
            if (bestAttribute == null) {
                Node leaf = new Node();
                leaf.label = getMajorityLabel(data);
                return leaf;
            }

            Node node = new Node();
            node.attribute = bestAttribute;

            Map<String, List<Map<String, String>>> partitions = partitionData(data, bestAttribute);
            for (Map.Entry<String, List<Map<String, String>>> entry : partitions.entrySet()) {
                List<String> remainingAttributes = new ArrayList<>(attributes);
                remainingAttributes.remove(bestAttribute);
                node.children.put(entry.getKey(), buildTree(entry.getValue(), remainingAttributes, depth + 1));
            }

            return node;
        }

        private String getBestAttribute(List<Map<String, String>> data, List<String> attributes) {
            double maxGain = Double.NEGATIVE_INFINITY;
            String bestAttribute = null;

            for (String attribute : attributes) {
                double gain = calculateInformationGain(data, attribute);
                if (gain > maxGain && gain > minGain) {
                    maxGain = gain;
                    bestAttribute = attribute;
                }
            }

            return bestAttribute;
        }

        private double calculateInformationGain(List<Map<String, String>> data, String attribute) {
            double totalEntropy = calculateEntropy(data);
            Map<String, List<Map<String, String>>> partitions = partitionData(data, attribute);

            double weightedEntropy = 0.0;
            for (List<Map<String, String>> subset : partitions.values()) {
                double subsetProbability = (double) subset.size() / data.size();
                weightedEntropy += subsetProbability * calculateEntropy(subset);
            }

            return totalEntropy - weightedEntropy;
        }

        private double calculateEntropy(List<Map<String, String>> data) {
            if (data.isEmpty()) {
                return 0.0; // Без данни, няма информация
            }

            Map<String, Integer> labelCounts = new HashMap<>();
            for (Map<String, String> row : data) {
                String label = row.get("class");
                labelCounts.put(label, labelCounts.getOrDefault(label, 0) + 1);
            }

            double entropy = 0.0;
            int total = data.size();
            for (int count : labelCounts.values()) {
                double probability = (double) count / total;
                if (probability > 0) { // Избягване на логаритъм от 0
                    entropy -= probability * Math.log(probability) / Math.log(2);
                }
            }

            return entropy;
        }


        private boolean isHomogeneous(List<Map<String, String>> data) {
            return data.stream().map(row -> row.get("class")).distinct().count() == 1;
        }

        private String getMajorityLabel(List<Map<String, String>> data) {
            Map<String, Integer> labelCounts = new HashMap<>();
            for (Map<String, String> row : data) {
                String label = row.get("class");
                labelCounts.put(label, labelCounts.getOrDefault(label, 0) + 1);
            }
            return labelCounts.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(null);
        }

        private Map<String, List<Map<String, String>>> partitionData(List<Map<String, String>> data, String attribute) {
            Map<String, List<Map<String, String>>> partitions = new HashMap<>();
            for (Map<String, String> row : data) {
                String value = row.get(attribute);
                partitions.computeIfAbsent(value, k -> new ArrayList<>()).add(row);
            }
            return partitions;
        }

        public String classify(Map<String, String> instance) {
            Node current = root;
            while (current.children != null && !current.children.isEmpty()) {
                String value = instance.get(current.attribute);
                if (!current.children.containsKey(value)) {
                    break;
                }
                current = current.children.get(value);
            }
            return current.label;
        }
    }

    static class RandomForest {
        private List<DecisionTree> trees;
        private int numTrees;
        private int maxDepth;
        private int minExamples;
        private double minGain;

        public RandomForest(int numTrees, int maxDepth, int minExamples, double minGain) {
            this.numTrees = numTrees;
            this.maxDepth = maxDepth;
            this.minExamples = minExamples;
            this.minGain = minGain;
            this.trees = new ArrayList<>();
        }

        public void train(List<Map<String, String>> data, List<String> attributes) {
            Random rand = new Random();
            for (int i = 0; i < numTrees; i++) {
                List<Map<String, String>> sample = getBootstrapSample(data, rand);
                DecisionTree tree = new DecisionTree(maxDepth, minExamples, minGain);
                tree.train(sample, attributes);
                trees.add(tree);
            }
        }

        public double calculateAccuracy(List<Map<String, String>> data) {
            int correct = 0;
            // Tracks the number of correctly classified instances
            for (Map<String, String> instance : data) {
                String actual = instance.get("class");
                // Extract the actual class label from the instance
                if(actual==null)
                    continue;
                String predicted = classify(instance);
                // Predict the class label for the instance using the model
                if (actual.equals(predicted)) {
                    correct++;
                    // Increment correct count if prediction matches the actual label
                }
            }
            return (double) correct / data.size() * 100.0;
            // Calculate and return the accuracy as a percentage
        }

        private List<Map<String, String>> getBootstrapSample(List<Map<String, String>> data, Random rand) {
            List<Map<String, String>> sample = new ArrayList<>();
            for (int i = 0; i < data.size(); i++) {
                sample.add(data.get(rand.nextInt(data.size())));
            }
            return sample;
        }

        public String classify(Map<String, String> instance) {
            Map<String, Integer> votes = new HashMap<>();
            for (DecisionTree tree : trees) {
                String prediction = tree.classify(instance);
                votes.put(prediction, votes.getOrDefault(prediction, 0) + 1);
            }
            return votes.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey) // Corrected this to ensure it connects logically
                    .orElse(null);
        }

        public double[] performCrossValidation(List<Map<String, String>> data, List<String> attributes, int k) {
            List<List<Map<String, String>>> folds = createFolds(data, k);
            double[] accuracies = new double[k];

            for (int i = 0; i < k; i++) {
                List<Map<String, String>> trainSet = new ArrayList<>();
                List<Map<String, String>> testSet = folds.get(i);

                for (int j = 0; j < k; j++) {
                    if (j != i) {
                        trainSet.addAll(folds.get(j));
                    }
                }

                RandomForest foldModel = new RandomForest(numTrees, maxDepth, minExamples, minGain);
                foldModel.train(trainSet, attributes);
                accuracies[i] = foldModel.calculateAccuracy(testSet);
            }

            return accuracies;
        }

        private List<List<Map<String, String>>> createFolds(List<Map<String, String>> data, int k) {
            List<List<Map<String, String>>> folds = new ArrayList<>();
            Collections.shuffle(data, new Random());

            for (int i = 0; i < k; i++) {
                folds.add(new ArrayList<>());
            }

            for (int i = 0; i < data.size(); i++) {
                folds.get(i % k).add(data.get(i));
            }

            return folds;
        }

    }

    private static Map<String, Integer> getClassDistribution(List<Map<String, String>> data) {
        Map<String, Integer> distribution = new HashMap<>();
        for (Map<String, String> row : data) {
            String label = row.get("class");
            distribution.put(label, distribution.getOrDefault(label, 0) + 1);
        }
        return distribution;
    }


    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        // Подаване на име на файл
        System.out.print("Enter the path to the ARFF file: ");
        String filePath = "data/breast-cancer.arff";

        // Зареждане и обработка на данните
        DataProcessor processor = new DataProcessor();
        processor.loadData(filePath);
        processor.handleMissingValues();

        List<Map<String, String>> data = processor.getData();
        List<String> attributes = processor.getAttributes();
        attributes.remove("class");

        // Подаване на параметър за изпълнение (0, 1 или 2)
        System.out.print("Enter execution mode (0 = no pruning, 1 = pre-pruning, 2 = post-pruning): ");
        int executionMode = scanner.nextInt();

        // Задаване на стойности по подразбиране
        int maxDepth = 5; // Максимална дълбочина на дървото
        int minExamples = 10; // Минимален брой примери в възел
        double minGain = 0.01; // Минимална информационна печалба
        int numTrees = 10; // Брой дървета в гората

        // Stratified split for Train-Test sets
        Collections.shuffle(data, new Random());
        List<Map<String, String>> trainSet = new ArrayList<>();
        List<Map<String, String>> testSet = new ArrayList<>();

        Map<String, List<Map<String, String>>> stratified = new HashMap<>();
        for (Map<String, String> instance : data) {
            String label = instance.get("class");
            stratified.computeIfAbsent(label, k -> new ArrayList<>()).add(instance);
        }

        for (List<Map<String, String>> group : stratified.values()) {
            if (group.size() < 2) {
                throw new IllegalStateException("Not enough instances for all classes.");
            }
            int trainSize = (int) (group.size() * 0.8);
            trainSet.addAll(group.subList(0, trainSize));
            testSet.addAll(group.subList(trainSize, group.size()));
        }

        //short time

        System.out.println("Loaded data size: " + data.size());
        System.out.println("Attributes: " + attributes);
        System.out.println("Class distribution in train set: " + getClassDistribution(trainSet));
        System.out.println("Class distribution in test set: " + getClassDistribution(testSet));

        //end short time

        // Създаване и обучение на Random Forest модела
        RandomForest randomForest = new RandomForest(numTrees, maxDepth, minExamples, minGain);
        randomForest.train(trainSet, attributes);

        // Резултати на тренировъчния сет
        double trainAccuracy = randomForest.calculateAccuracy(trainSet);
        System.out.printf("1. Train Set Accuracy:\n    Accuracy: %.2f%%%n", trainAccuracy);

        // Cross-validation
        System.out.println("\n10-Fold Cross-Validation Results:");
        double[] foldAccuracies = randomForest.performCrossValidation(data, attributes, 10);
        double sum = 0, sumSq = 0;
        for (int i = 0; i < foldAccuracies.length; i++) {
            System.out.printf("    Accuracy Fold %d: %.2f%%%n", i + 1, foldAccuracies[i]);
            sum += foldAccuracies[i];
            sumSq += foldAccuracies[i] * foldAccuracies[i];
        }
        double average = sum / foldAccuracies.length;
        double stdDev = Math.sqrt(sumSq / foldAccuracies.length - average * average);
        System.out.printf("\n    Average Accuracy: %.2f%%%n", average);
        System.out.printf("    Standard Deviation: %.2f%%%n", stdDev);

        // Резултати на тестовия сет
        double testAccuracy = randomForest.calculateAccuracy(testSet);
        System.out.printf("\n2. Test Set Accuracy:\n    Accuracy: %.2f%%%n", testAccuracy);

        // Управление на режимите
        if (executionMode == 1) {
            System.out.println("\nPre-pruning mode active.");
        } else if (executionMode == 2) {
            System.out.println("\nPost-pruning mode active. Implement post-pruning logic here.");
        } else if (executionMode != 0) {
            System.out.println("\nInvalid mode selected.");
        }
    }

}

