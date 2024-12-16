import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ARFFConverter {
    public static void main(String[] args) {

        // Въвеждане на пътища към файловете
        String namesFilePath = "data/breast-cancer.names";

        String dataFilePath = "data/breast-cancer.data";

        String arffFilePath = "data/breast-cancer.arff";

        try {
            List<String> attributes = parseNamesFile(namesFilePath);
            List<String[]> data = parseDataFile(dataFilePath);

            writeArffFile(arffFilePath, "breast-cancer", attributes, data);
            System.out.println("ARFF file created successfully: " + arffFilePath);
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private static List<String> parseNamesFile(String filePath) throws IOException {
        List<String> attributes = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#") || line.startsWith("@")) {
                    continue; // Пропускаме празни редове или коментари
                }

                String[] parts = line.split(":");
                if (parts.length == 2) {
                    String attributeName = parts[0].trim();
                    String[] attributeValues = parts[1].trim().split(",");

                    if (attributeValues.length == 1 && attributeValues[0].equalsIgnoreCase("continuous")) {
                        attributes.add("@ATTRIBUTE " + attributeName + " NUMERIC");
                    } else {
                        attributes.add("@ATTRIBUTE " + attributeName + " {" + String.join(",", attributeValues).trim() + "}");
                    }
                }
            }
        }

        attributes.add("@ATTRIBUTE Class {no-recurrence-events,recurrence-events}"); // Специфично за breast-cancer.names
        return attributes;
    }

    private static List<String[]> parseDataFile(String filePath) throws IOException {
        List<String[]> data = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    String[] values = line.split(",");
                    data.add(values);
                }
            }
        }

        return data;
    }

    private static void writeArffFile(String filePath, String relationName, List<String> attributes, List<String[]> data) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write("@RELATION " + relationName);
            writer.newLine();
            writer.newLine();

            for (String attribute : attributes) {
                writer.write(attribute);
                writer.newLine();
            }

            writer.newLine();
            writer.write("@DATA");
            writer.newLine();

            for (String[] instance : data) {
                writer.write(String.join(",", instance));
                writer.newLine();
            }
        }
    }
}
