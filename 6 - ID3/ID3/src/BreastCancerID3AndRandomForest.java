import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Enumeration;

import weka.core.Instances;
import weka.core.Instance;
import weka.core.converters.ConverterUtils.DataSource;
import weka.core.FastVector;
import weka.classifiers.trees.ID3;
import weka.classifiers.Evaluation;
import weka.classifiers.meta.CVParameterSelection;
import weka.classifiers.meta.ThresholdSelector;
import weka.filters.Filter;
import weka.filters.unsupervised.instance.ReplaceMissingValues;
import weka.classifiers.meta.RandomForest;

public class BreastCancerID3AndRandomForest {

    public static void main(String[] args) throws Exception {
        // Зареждане на данни
        DataSource source = new DataSource("breast-cancer.arff");
        Instances data = source.getDataSet();
        if (data.classIndex() == -1) {
            data.setClassIndex(data.numAttributes() - 1);
        }

        // Запълване на липсващи стойности
        ReplaceMissingValues filter = new ReplaceMissingValues();
        filter.setInputFormat(data);
        Instances filledData = Filter.useFilter(data, filter);

        // Подразделение на данни
        Instances train = filledData.trainCV(5, 0);
        Instances test = filledData.testCV(5, 0);

        // Имплементация на ID3
        ID3 tree = new ID3();
        tree.buildClassifier(train);

        // Оценка на модела върху тренировъчното множество
        double trainAccuracy = evaluateModel(tree, train);

        // 10-кратна кръстосана проверка
        Evaluation eval = new Evaluation(train);
        eval.crossValidateModel(tree, train, 10, new java.util.Random(42));
        double avgAccuracy = eval.pctCorrect();
        double stdDeviation = Math.sqrt(eval.errorRate() * (1 - eval.errorRate()) / 10);

        // Оценка на точността върху тестовото множество
        double testAccuracy = evaluateModel(tree, test);

        // Оценка на Random Forest
        RandomForest rf = new RandomForest();
        rf.setNumTrees(100); // Брой дървета в ансамбъла
        rf.buildClassifier(train);

        // Оценка на Random Forest върху тренировъчното множество
        double rfTrainAccuracy = evaluateModel(rf, train);

        // Оценка на Random Forest върху тестовото множество
        double rfTestAccuracy = evaluateModel(rf, test);

        // Изход
        System.out.printf("1. Train Set Accuracy (ID3): %.2f%%%n", trainAccuracy * 100);
        System.out.printf("10-Fold Cross-Validation Results (ID3):%n");
        System.out.printf("    Accuracy Fold 1: %.2f%%%n", eval.correct(0) / eval.numInstances() * 100);
        System.out.printf("    Accuracy Fold 2: %.2f%%%n", eval.correct(1) / eval.numInstances() * 100);
        System.out.printf("    Accuracy Fold 3: %.2f%%%n", eval.correct(2) / eval.numInstances() * 100);
        System.out.printf("    Accuracy Fold 4: %.2f%%%n", eval.correct(3) / eval.numInstances() * 100);
        System.out.printf("    Accuracy Fold 5: %.2f%%%n", eval.correct(4) / eval.numInstances() * 100);
        System.out.printf("    Average Accuracy (ID3): %.2f%%%n", avgAccuracy);
        System.out.printf("    Standard Deviation (ID3): %.2f%%%n", stdDeviation * 100);
        System.out.printf("2. Test Set Accuracy (ID3): %.2f%%%n", testAccuracy * 100);

        System.out.printf("3. Train Set Accuracy (Random Forest): %.2f%%%n", rfTrainAccuracy * 100);
        System.out.printf("4. Test Set Accuracy (Random Forest): %.2f%%%n", rfTestAccuracy * 100);
    }

    private static double evaluateModel(Object model, Instances data) throws Exception {
        int correct = 0;
        int total = data.numInstances();
        for (int i = 0; i < total; i++) {
            Instance instance = data.instance(i);
            double predicted;
            if (model instanceof ID3) {
                predicted = ((ID3) model).classifyInstance(instance);
            } else if (model instanceof RandomForest) {
                predicted = ((RandomForest) model).classifyInstance(instance);
            } else {
                throw new IllegalArgumentException("Unsupported model type.");
            }
            if (predicted == instance.classValue()) {
                correct++;
            }
        }
        return (double) correct / total;
    }
}
