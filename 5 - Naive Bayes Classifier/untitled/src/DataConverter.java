import java.io.*;

public class DataConverter {
    public static void main(String[] args) throws IOException {
        String inputFile = "data/house-votes-84.data";
        String outputFile = "data/house-votes-84.arff";

        BufferedReader reader = new BufferedReader(new FileReader(inputFile));
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));

        // Write ARFF header
        writer.write("@relation house-votes\n\n");
        writer.write("@attribute class {democrat,republican}\n");
        String[] attributes = {
                "handicapped-infants", "water-project-cost-sharing",
                "adoption-of-the-budget-resolution", "physician-fee-freeze",
                "el-salvador-aid", "religious-groups-in-schools",
                "anti-satellite-test-ban", "aid-to-nicaraguan-contras",
                "mx-missile", "immigration", "synfuels-corporation-cutback",
                "education-spending", "superfund-right-to-sue", "crime",
                "duty-free-exports", "export-administration-act-south-africa"
        };
        for (String attr : attributes) {
            writer.write("@attribute " + attr + " {y,n,?}\n");
        }
        writer.write("\n@data\n");

        // Write ARFF data
        String line;
        while ((line = reader.readLine()) != null) {
            writer.write(line.replace(",", ",") + "\n");
        }

        reader.close();
        writer.close();
        System.out.println("Conversion complete: " + outputFile);
    }
}
