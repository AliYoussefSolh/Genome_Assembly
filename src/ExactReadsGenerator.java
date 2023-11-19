import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ExactReadsGenerator {

    public static List<String> extractReads(String genome, int readLength) {
        List<String> reads = new ArrayList<>();

        for (int i = 0; i <= genome.length() - readLength; i++) {
            String read = genome.substring(i, i + readLength);
            reads.add(read);
        }

        return reads;
    }

    public static void writeReadsToFile(List<String> reads, String filePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (String read : reads) {
                writer.write(read);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws FileNotFoundException, IOException {
        StringBuilder referenceBuilder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader("resources/test_cases/t5.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                referenceBuilder.append(line);
            }
        }
        String genomeSequence = referenceBuilder.toString();
        // String genomeSequence = "ABCDEFGHIGKLMNO";
        int readLength = 100;

        List<String> extractedReads = extractReads(genomeSequence, readLength);
        writeReadsToFile(extractedReads, "resources/test_cases/reads5.txt");
        // Printing out the extracted reads
        // for (String read : extractedReads) {
        // System.out.println(read);
        // }
    }
}
