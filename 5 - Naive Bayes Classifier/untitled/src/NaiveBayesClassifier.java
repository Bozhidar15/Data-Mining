import java.io.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class NaiveBayesClassifier {
    private List<Instance> instances;
    private Map<String, Integer> classCounts;
    private Map<String, Map<String, Integer>> attributeCounts;
    private int totalInstances;

    public NaiveBayesClassifier() {
        this.instances = new ArrayList<>();
        this.classCounts = new HashMap<>();
        this.attributeCounts = new HashMap<>();
        this.totalInstances = 0;
    }

    public void addInstance(Instance instance) {
        this.instances.add(instance);
        this.totalInstances++;
        this.classCounts.put(instance.classification, this.classCounts.getOrDefault(instance.classification, 0) + 1);

        for (int i = 0; i < instance.attributes.size(); i++) {
            String key = i + "-" + instance.attributes.get(i);
            Map<String, Integer> count = attributeCounts.getOrDefault(key, new HashMap<>());
            count.put(instance.classification, count.getOrDefault(instance.classification, 0) + 1);
            attributeCounts.put(key, count);
        }
    }

    public void train(List<Instance> trainingData) {
        this.instances.clear();
        this.classCounts.clear();
        this.attributeCounts.clear();
        this.totalInstances = 0;

        for (Instance instance : trainingData) {
            addInstance(instance);
        }
    }

    public double classify(List<Instance> testData) {
        int correctPredictions = 0;
        for (Instance instance : testData) {
            String predictedClass = predict(instance);
            if (predictedClass != null && predictedClass.equals(instance.classification)) {
                correctPredictions++;
            }
        }
        return testData.size() > 0 ? (correctPredictions / (double) testData.size() * 100.0) : 0.0;
    }

    private String predict(Instance instance) {
        double maxProb = Double.NEGATIVE_INFINITY;
        String bestClass = null;
        for (String classVal : classCounts.keySet()) {
            double prob = Math.log(classCounts.get(classVal) / (double) totalInstances);
            for (int i = 0; i < instance.attributes.size(); i++) {
                String attrKey = i + "-" + instance.attributes.get(i);
                Map<String, Integer> counts = attributeCounts.getOrDefault(attrKey, new HashMap<>());
                int count = counts.getOrDefault(classVal, 0) + 1;
                prob += Math.log(count / (double) (classCounts.get(classVal) + instance.attributes.size()));
            }
            if (prob > maxProb) {
                maxProb = prob;
                bestClass = classVal;
            }
        }
        return bestClass;
    }

    public static void main(String[] args) throws IOException {
        String path = "D:\\СУ\\4ти курс\\IS\\homeworks\\Data Mining\\Tasks\\Data-Mining\\5 - Naive Bayes " +
                "Classifier\\untitled\\data\\house-votes-84.csv";
        List<Instance> allData = loadData(path, 1);
        Collections.shuffle(allData);

        // 10-fold cross-validation
        int k = 10;
        List<Double> accuracies = crossValidate(allData, k);
        double averageAccuracy = accuracies.stream().mapToDouble(a -> a).average().orElse(0.0);
        double stdDeviation = calculateStandardDeviation(accuracies, averageAccuracy);

        System.out.println("10-Fold Cross-Validation Results:");
        for (int i = 0; i < accuracies.size(); i++) {
            System.out.printf("Accuracy Fold %d: %.2f%%\n", i + 1, accuracies.get(i));
        }
        System.out.printf("\nAverage Accuracy: %.2f%%\n", averageAccuracy);
        System.out.printf("Standard Deviation: %.2f%%\n", stdDeviation);

        // Train and test split
        int trainSize = (int) (allData.size() * 0.8);
        List<Instance> trainData = new ArrayList<>(allData.subList(0, trainSize));
        List<Instance> testData = new ArrayList<>(allData.subList(trainSize, allData.size()));

        NaiveBayesClassifier classifier = new NaiveBayesClassifier();
        classifier.train(trainData);
        double trainAccuracy = classifier.classify(trainData);
        double testAccuracy = classifier.classify(testData);

        System.out.printf("\nTrain Set Accuracy:\n    Accuracy: %.2f%%\n", trainAccuracy);
        System.out.printf("\nTest Set Accuracy:\n    Accuracy: %.2f%%\n", testAccuracy);
    }

    private static List<Double> crossValidate(List<Instance> data, int k) {
        List<Double> accuracies = new ArrayList<>();
        int foldSize = data.size() / k;
        for (int i = 0; i < k; i++) {
            int start = i * foldSize;
            int end = i < k - 1 ? start + foldSize : data.size();
            List<Instance> testData = new ArrayList<>(data.subList(start, end));
            List<Instance> trainData = new ArrayList<>(data);
            trainData.removeAll(testData);

            NaiveBayesClassifier classifier = new NaiveBayesClassifier();
            classifier.train(trainData);
            double accuracy = classifier.classify(testData);
            accuracies.add(accuracy);
        }
        return accuracies;
    }

    private static double calculateStandardDeviation(List<Double> accuracies, double mean) {
        double variance = accuracies.stream().mapToDouble(a -> Math.pow(a - mean, 2)).average().orElse(0.0);
        return Math.sqrt(variance);
    }

    private static List<Instance> loadData(String filePath, int missingValueTreatment) throws IOException {
        List<Instance> instances = new ArrayList<>();
        List<String[]> rawData = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line;
        while ((line = reader.readLine()) != null) {
            rawData.add(line.split(","));
        }
        reader.close();

        List<String> mostCommonValues = missingValueTreatment == 1 ? calculateMostCommonValues(rawData) : null;

        for (String[] data : rawData) {
            if (missingValueTreatment == 1) {
                handleMissingValues(data, mostCommonValues);
            }
            instances.add(new Instance(data));
        }
        return instances;
    }

    private static List<String> calculateMostCommonValues(List<String[]> rawData) {
        List<String> mostCommonValues = new ArrayList<>();
        int numAttributes = rawData.get(0).length - 2; // Изваждаме 2 заради полетата id и class

        for (int i = 2; i < numAttributes + 2; i++) {
            int finalI = i;
            Map<String, Long> frequencyMap = rawData.stream()
                    .map(row -> row[finalI])
                    .filter(val -> !val.equals("?"))
                    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

            String mostCommon = Collections.max(frequencyMap.entrySet(), Map.Entry.comparingByValue()).getKey();
            mostCommonValues.add(mostCommon);
        }

        return mostCommonValues;
    }

    private static void handleMissingValues(String[] data, List<String> mostCommonValues) {
        for (int i = 2; i < data.length; i++) {
            if (data[i].equals("?")) {
                data[i] = mostCommonValues.get(i - 2);
            }
        }
    }
}
