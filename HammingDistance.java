import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class HammingDistance {

    public static void main(String[] args) {
        try {
            // Read genomes from files
            String assembledGenome = readGenomeFromFile("C:\\Users\\HES\\Desktop\\Genome_Assembly\\output\\part1.txt");
            String referenceGenome = readGenomeFromFile("C:\\Users\\HES\\Desktop\\Genome_Assembly\\output\\partt1.txt");
            System.out.println(assembledGenome.charAt(assembledGenome.length() - 1));
            System.out.println(referenceGenome.charAt(referenceGenome.length() - 1));
            // Calculate Hamming distance
            int hammingDistance = calculateHammingDistance(assembledGenome, referenceGenome);

            // Calculate percentage similarity
            double percentageSimilarity = calculatePercentageSimilarity(
                    Math.max(assembledGenome.length(), referenceGenome.length()), hammingDistance);

            // Display results
            System.out.println("Hamming Distance: " + hammingDistance);
            System.out.println("Percentage Similarity: " + percentageSimilarity + "%");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String readGenomeFromFile(String fileName) throws IOException {
        StringBuilder genome = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                genome.append(line);
            }
        }
        return genome.toString();
    }

    private static int calculateHammingDistance(String genome1, String genome2) {
        int minLength = Math.min(genome1.length(), genome2.length());
        int hammingDistance = Math.abs(genome1.length() - genome2.length()); // Account for length difference

        for (int i = 0; i < minLength; i++) {
            if (genome1.charAt(i) != genome2.charAt(i)) {
                hammingDistance++;
            }
        }
        return hammingDistance;
    }

    private static double calculatePercentageSimilarity(int maxLength, int hammingDistance) {
        return ((double) (maxLength - hammingDistance) / maxLength) * 100.0;
    }
}
