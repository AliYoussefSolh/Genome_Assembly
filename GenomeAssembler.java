import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.io.BufferedWriter;
import java.io.FileWriter;

public class GenomeAssembler {

    private Map<String, Integer> readsMap = new HashMap<>();
    private String referenceGenome = "";
    private int[] suffixArray;
    private List<String> assembledContigs = new ArrayList<>();

    // Getters for the data
    public Map<String, Integer> getReadsMap() {
        return readsMap;
    }

    public String getReferenceGenome() {
        return referenceGenome;
    }

    public int[] getSuffixArray() {
        return suffixArray;
    }

    // method called to read both files, this method will call two other methods to
    // read each file
    public void readInputFiles(String readsFilePath, String referenceFilePath) throws IOException {
        readReadsFile(readsFilePath);
        readReferenceFile(referenceFilePath);
    }

    // method to read the reads file
    private void readReadsFile(String filePath) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                readsMap.put(line, readsMap.getOrDefault(line, 0) + 1);
            }
        }
    }

    // method to read the reference file
    private void readReferenceFile(String filePath) throws IOException {
        StringBuilder referenceBuilder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                referenceBuilder.append(line);
            }
        }
        referenceGenome = referenceBuilder.toString();
    }

    // create a suffix array using the refrence genome
    private void createSuffixArray() {
        int n = referenceGenome.length();
        Integer[] suffixArray = new Integer[n];
        for (int i = 0; i < n; i++) {
            suffixArray[i] = i;
        }

        Arrays.sort(suffixArray, (i1, i2) -> {
            while (i1 < n && i2 < n) {
                if (referenceGenome.charAt(i1) != referenceGenome.charAt(i2)) {
                    return Character.compare(referenceGenome.charAt(i1), referenceGenome.charAt(i2));
                }
                i1++;
                i2++;
            }
            return Integer.compare(n - i1, n - i2);
        });

        // Convert back to int[] if necessary
        this.suffixArray = new int[n];
        for (int i = 0; i < n; i++) {
            this.suffixArray[i] = suffixArray[i];
        }
        System.out.println("Suffix array created.");
    }

    // go over all the reads that we previously read from file and align them to the
    // reference genome
    public void alignReadsUsingSuffixArray() {
        int totalReads = readsMap.size();
        int readsProcessed = 0;
        for (String read : readsMap.keySet()) {
            List<Integer> potentialAlignments = findPotentialAlignments(read);
            for (int position : potentialAlignments) {
                String referenceSegment = referenceGenome.substring(position,
                        Math.min(referenceGenome.length(), position + read.length()));
                String alignment = simpleAlignment(read, referenceSegment);
                // Process alignment, e.g., store it, print it, etc.
                readsProcessed++;
                int progressPercentage = (int) ((readsProcessed / (double) totalReads) * 100);
                System.out.println("Alignment Progress: " + progressPercentage + "% completed.");
            }
        }
    }

    // a binary sort method to find an allignment between the reads and the refrence
    // genome that was put in a suffix array
    private List<Integer> findPotentialAlignments(String read) {
        List<Integer> positions = new ArrayList<>();
        int low = 0;
        int high = suffixArray.length - 1;

        while (low <= high) {
            int mid = (low + high) / 2;
            int suffixIndex = suffixArray[mid];
            int end = Math.min(suffixIndex + read.length(), referenceGenome.length());
            String suffix = referenceGenome.substring(suffixIndex, end);

            int cmp = read.compareTo(suffix);
            if (cmp == 0) {
                positions.add(suffixIndex);
                // Search for multiple occurrences
                int tempMid = mid;
                while (--tempMid >= 0 && read.equals(referenceGenome.substring(suffixArray[tempMid],
                        Math.min(suffixArray[tempMid] + read.length(), referenceGenome.length())))) {
                    positions.add(suffixArray[tempMid]);
                }
                tempMid = mid;
                while (++tempMid < suffixArray.length && read.equals(referenceGenome.substring(suffixArray[tempMid],
                        Math.min(suffixArray[tempMid] + read.length(), referenceGenome.length())))) {
                    positions.add(suffixArray[tempMid]);
                }
                break;
            } else if (cmp < 0) {
                high = mid - 1;
            } else {
                low = mid + 1;
            }
        }
        return positions;
    }

    // compare the potential alignemnt to the read and out | for match and * for
    // mismatch and - for gap
    private String simpleAlignment(String read, String referenceSegment) {
        StringBuilder alignment = new StringBuilder();
        int readLen = read.length();
        int refLen = referenceSegment.length();
        int minLength = Math.min(readLen, refLen);

        // Compare each character in the read with the reference segment
        for (int i = 0; i < minLength; i++) {
            if (read.charAt(i) == referenceSegment.charAt(i)) {
                alignment.append("|"); // Match
            } else {
                alignment.append("*"); // Mismatch
            }
        }

        // If the read is longer than the reference segment, fill the rest with gaps
        for (int i = minLength; i < readLen; i++) {
            alignment.append("-");
        }

        return alignment.toString();
    }

    // after getting the reads that we want to from the previous method we will
    // assemble them into one read genome while allowing some mismatches
    public void assembleContigs() {
        Map<Integer, String> positionToContigMap = new HashMap<>();
        for (String read : readsMap.keySet()) {
            List<Integer> positions = findPotentialAlignments(read);
            for (int position : positions) {
                positionToContigMap.put(position, read);
            }
        }

        Integer[] positions = positionToContigMap.keySet().toArray(new Integer[0]);
        Arrays.sort(positions);
        int totalPositions = positions.length;
        int positionsProcessed = 0;

        StringBuilder assembledGenome = new StringBuilder();
        int lastPosition = -1;
        for (int position : positions) {
            String contig = positionToContigMap.get(position);
            if (position > lastPosition) {
                assembledGenome.append(contig);
                lastPosition = position + contig.length() - 1;
            }
            positionsProcessed++;
            int progressPercentage = (int) ((positionsProcessed / (double) totalPositions) * 100);
            System.out.println("Contig Assembly Progress: " + progressPercentage + "% completed.");
        }

        assembledContigs.add(assembledGenome.toString());
    }

    // write the assembled contigs to a file
    public void writeAssembledContigsToFile(String filePath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (String contig : assembledContigs) {
                writer.write(contig);
                writer.newLine();
            }
        }
    }

    public static void main(String[] args) {

        GenomeAssembler assembler = new GenomeAssembler();
        try {
            assembler.readInputFiles("reads.txt", "reference.txt");
            assembler.createSuffixArray(); // Creating the suffix array
            assembler.assembleContigs();
            System.out.println("Contigs assembled.");
            assembler.writeAssembledContigsToFile("assembled_reads.txt");
            System.out.println("Assembly completed. Contigs written to 'assembled_reads.txt'.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
