import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class GenomeGenerator {
    public static void main(String[] args) {
        // Generate a reference genome
        String referenceGenome = generateReferenceGenome(10000);

        // Generate reads from the reference genome
        int readLength = 50;
        int numReads = 200;
        String[] reads = generateReads(referenceGenome, readLength, numReads);

        // Introduce mutations in a few reads
        int numReadsWithMutations = 20;
        introduceMutations(reads, referenceGenome, numReadsWithMutations);

        // Print the reference genome and reads
        System.out.println("Reference Genome: " + referenceGenome);
        System.out.println("Reads:");
        for (String read : reads) {
            System.out.println(read);
        }

        // Write the reference genome to a file
        writeToFile("test_data2/genome.txt", referenceGenome);

        // Write the reads to a file
        writeToFile("test_data2/reads.txt", String.join("\n", reads));
    }

    private static String generateReferenceGenome(int length) {
        Random random = new Random();
        StringBuilder genome = new StringBuilder();

        for (int i = 0; i < length; i++) {
            // Randomly select A, C, G, or T
            char nucleotide = "ACGT".charAt(random.nextInt(4));
            genome.append(nucleotide);
        }

        return genome.toString();
    }

    private static String[] generateReads(String referenceGenome, int readLength, int numReads) {
        String[] reads = new String[numReads];
        int genomeLength = referenceGenome.length();

        // Calculate the step size to ensure coverage
        int stepSize = Math.max(1, genomeLength / numReads);

        for (int i = 0; i < numReads; i++) {
            // Calculate the starting position for the read
            int startPos = i * stepSize;

            // Ensure the read doesn't go beyond the end of the genome
            int endPos = Math.min(startPos + readLength, genomeLength);

            // Generate the read
            reads[i] = referenceGenome.substring(startPos, endPos);
        }

        return reads;
    }


    private static void introduceMutations(String[] reads, String referenceGenome, int numReadsWithMutations) {
        Random random = new Random();

        for (int i = 0; i < numReadsWithMutations; i++) {
            // Randomly select a read to introduce mutations
            int readIndex = random.nextInt(reads.length);
            StringBuilder mutatedRead = new StringBuilder(reads[readIndex]);


            int numMutations = random.nextInt(5);
            for (int j = 0; j < numMutations; j++) {
                // Randomly select a position in the read to mutate
                int mutationPos = random.nextInt(reads[readIndex].length());

                // Randomly select a different nucleotide
                char mutatedNucleotide;
                do {
                    mutatedNucleotide = "ACGT".charAt(random.nextInt(4));
                } while (mutatedNucleotide == mutatedRead.charAt(mutationPos));

                // Perform the mutation
                mutatedRead.setCharAt(mutationPos, mutatedNucleotide);
            }

            // Replace the original read with the mutated one
            reads[readIndex] = mutatedRead.toString();
        }
    }

    private static void writeToFile(String fileName, String content) {
        try (FileWriter fileWriter = new FileWriter(fileName)) {
            fileWriter.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
