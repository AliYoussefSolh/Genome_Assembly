import java.io.*;

public class FileSplitter {
    public static void main(String[] args) {
        String inputFilePath = "assembled_reads.txt"; // Replace with your input file path
        String outputDirectory = "output/"; // Replace with your desired output directory
        int LENGTH = 50;
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFilePath))) {
            StringBuilder content = new StringBuilder();
            String line;

            // Read the entire content from the input file
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }

            String fullContent = content.toString();
            int totalLength = fullContent.length();
            int partLength = totalLength / LENGTH;

            // Create output directory if it doesn't exist
            File dir = new File(outputDirectory);
            if (!dir.exists()) {
                dir.mkdir();
            }

            // Split the content into 4 parts and write each part to a separate file
            for (int i = 0; i < LENGTH; i++) {
                int startIdx = i * partLength;
                int endIdx = (i == LENGTH - 1) ? totalLength : ((i + 1) * partLength);

                String outputFileName = outputDirectory + "partt" + (i + 1) + ".txt";
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFileName))) {
                    writer.write(fullContent.substring(startIdx, endIdx));
                }
            }

            System.out.println("File split successful.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
