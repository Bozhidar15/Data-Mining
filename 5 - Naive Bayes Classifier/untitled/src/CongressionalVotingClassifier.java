import weka.classifiers.bayes.NaiveBayes;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.ReplaceMissingValues;

import java.io.IOException;
import java.util.Random;

public class CongressionalVotingClassifier {

    public static void main(String[] args) throws Exception {
        //javac -cp "D:\Java IntelliJ\weka\Weka-3-8-6\weka.jar" CongressionalVotingClassifier.java

        //java --add-opens java.base/java.lang=ALL-UNNAMED -cp "D:\Java IntelliJ\weka\Weka-3-8-6\weka.jar;."
        // CongressionalVotingClassifier 0

        if (args.length == 0) {
            System.out.println("Usage: java CongressionalVotingClassifier <0 for abstained or 1 for imputation>");
            return;
        }

        int mode = Integer.parseInt(args[0]);
        if (mode != 0 && mode != 1) {
            System.out.println("Invalid mode. Use 0 for treating '?' as abstained, 1 for imputation.");
            return;
        }

        // Load data
        String filePath = "D:\\СУ\\4ти курс\\IS\\homeworks\\Data Mining\\Tasks\\Data-Mining\\5 - Naive Bayes Classifier\\untitled\\data\\house-votes-84.arff"; // Replace with the correct path if necessary
        Instances data = DataSource.read(filePath);
        if (data == null) {
            throw new IOException("No source has been specified or file not found.");
        }

        data.setClassIndex(data.numAttributes() - 1);

        if (mode == 0) {
            // Treat '?' as a third class (abstained)
            for (int i = 0; i < data.numInstances(); i++) {
                for (int j = 0; j < data.numAttributes(); j++) {
                    if (data.instance(i).isMissing(j)) {
                        data.instance(i).setValue(j, "?");
                    }
                }
            }
        } else {
            // Impute missing values with the most frequent value
            ReplaceMissingValues replaceMissingValues = new ReplaceMissingValues();
            replaceMissingValues.setInputFormat(data);
            data = Filter.useFilter(data, replaceMissingValues);
        }

        // training (80%) and testing (20%) sets
        data.randomize(new Random(42));
        data.stratify(5); // Maintain class distribution

        int trainSize = (int) Math.round(data.numInstances() * 0.8);
        int testSize = data.numInstances() - trainSize;
        Instances trainData = new Instances(data, 0, trainSize);
        Instances testData = new Instances(data, trainSize, testSize);

        // NaiveBayes model
        NaiveBayes naiveBayes = new NaiveBayes();
        naiveBayes.buildClassifier(trainData);

        // Evaluate the model on training data
        evaluateModel(naiveBayes, trainData, "Train Set");

        // Perform cross-validation
        crossValidation(trainData);

        // Evaluate the model on test data
        evaluateModel(naiveBayes, testData, "Test Set");
    }

    private static void evaluateModel(NaiveBayes model, Instances data, String label) throws Exception {
        int correct = 0;
        for (int i = 0; i < data.numInstances(); i++) {
            Instance instance = data.instance(i);
            double predicted = model.classifyInstance(instance);
            if (predicted == instance.classValue()) {
                correct++;
            }
        }
        double accuracy = 100.0 * correct / data.numInstances();
        System.out.printf("Accuracy on %s: %.2f%%\n", label, accuracy);
    }

    private static void crossValidation( Instances data) throws Exception {
        int folds = 10;
        System.out.println("Performing 10-fold cross-validation...");

        double[] accuracies = new double[folds];
        for (int i = 0; i < folds; i++) {
            Instances train = data.trainCV(folds, i);
            Instances test = data.testCV(folds, i);

            NaiveBayes foldModel = new NaiveBayes();
            foldModel.buildClassifier(train);

            int correct = 0;
            for (int j = 0; j < test.numInstances(); j++) {
                Instance instance = test.instance(j);
                double predicted = foldModel.classifyInstance(instance);
                if (predicted == instance.classValue()) {
                    correct++;
                }
            }
            double accuracy = 100.0 * correct / test.numInstances();
            accuracies[i] = accuracy;
            System.out.printf("Fold %d accuracy: %.2f%%\n", i + 1, accuracy);
        }

        // Calculate average accuracy and standard deviation
        double sum = 0.0, sumSquared = 0.0;
        for (double acc : accuracies) {
            sum += acc;
            sumSquared += acc * acc;
        }
        double mean = sum / folds;
        double variance = (sumSquared / folds) - (mean * mean);
        double stdDev = Math.sqrt(variance);

        System.out.printf("Average Accuracy: %.2f%%\n", mean);
        System.out.printf("Standard Deviation: %.2f%%\n", stdDev);
    }
}
