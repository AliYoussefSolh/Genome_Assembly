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

    public void readInputFiles(String readsFilePath, String referenceFilePath) throws IOException {
        readReadsFile(readsFilePath);
        readReferenceFile(referenceFilePath);
    }

    private void readReadsFile(String filePath) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                readsMap.put(line, readsMap.getOrDefault(line, 0) + 1);
            }
        }
    }

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

    // Getters for the data
    public Map<String, Integer> getReadsMap() {
        return readsMap;
    }

    public String getReferenceGenome() {
        return referenceGenome;
    }
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
    

    public int[] getSuffixArray() {
        return suffixArray;
    }
    public void alignReadsUsingSuffixArray() {
        int totalReads = readsMap.size();
        int readsProcessed = 0;
        for (String read : readsMap.keySet()) {
            List<Integer> potentialAlignments = findPotentialAlignments(read);
            for (int position : potentialAlignments) {
                String referenceSegment = referenceGenome.substring(position, Math.min(referenceGenome.length(), position + read.length()));
                String alignment = simpleAlignment(read, referenceSegment);
                // Process alignment, e.g., store it, print it, etc.
                readsProcessed++;
        int progressPercentage = (int) ((readsProcessed / (double) totalReads) * 100);
        System.out.println("Alignment Progress: " + progressPercentage + "% completed.");
            }
        }
    }
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

    private int matchScore = 3;
    private int mismatchPenalty = -3;
    private int gapPenalty = -2;

    public void alignReads() {
        for (String read : readsMap.keySet()) {
            String alignment = smithWatermanAlignment(read, referenceGenome);
            // Process the alignment, e.g., store it or print it out
            System.out.println("Alignment for read: " + read + "\n" + alignment);
        }
    }

    private String smithWatermanAlignment(String s1, String s2) {
        int[][] scoreMatrix = new int[s1.length() + 1][s2.length() + 1];
        int maxScore = 0;
        int maxI = 0;
        int maxJ = 0;

        // Build score matrix
        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                int match = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? matchScore : mismatchPenalty;
                int scoreDiag = scoreMatrix[i - 1][j - 1] + match;
                int scoreLeft = scoreMatrix[i][j - 1] + gapPenalty;
                int scoreUp = scoreMatrix[i - 1][j] + gapPenalty;
                scoreMatrix[i][j] = Math.max(0, Math.max(Math.max(scoreDiag, scoreLeft), scoreUp));

                if (scoreMatrix[i][j] > maxScore) {
                    maxScore = scoreMatrix[i][j];
                    maxI = i;
                    maxJ = j;
                }
            }
        }

        // Traceback
        StringBuilder aligned1 = new StringBuilder();
        StringBuilder aligned2 = new StringBuilder();
        int i = maxI;
        int j = maxJ;
        while (i > 0 && j > 0 && scoreMatrix[i][j] != 0) {
            if (scoreMatrix[i][j] == scoreMatrix[i - 1][j - 1] + ((s1.charAt(i - 1) == s2.charAt(j - 1)) ? matchScore : mismatchPenalty)) {
                aligned1.insert(0, s1.charAt(i - 1));
                aligned2.insert(0, s2.charAt(j - 1));
                i--;
                j--;
            } else if (scoreMatrix[i][j] == scoreMatrix[i][j - 1] + gapPenalty) {
                aligned1.insert(0, '-');
                aligned2.insert(0, s2.charAt(j - 1));
                j--;
            } else {
                aligned1.insert(0, s1.charAt(i - 1));
                aligned2.insert(0, '-');
                i--;
            }
        }

        return "Alignment:\n" + aligned1.toString() + "\n" + aligned2.toString();
    }
    public void assembleContigs1() {
        // Assume each read is initially a contig
        assembledContigs.addAll(readsMap.keySet());
    
        int totalMerges = 0;
        boolean merged = true;
        while (merged) {
            merged = false;
            int initialContigCount = assembledContigs.size();
    System.out.println("Initial contig count: " + initialContigCount);
            for (int i = 0; i < assembledContigs.size(); i++) {
                for (int j = i + 1; j < assembledContigs.size(); j++) {
                    String mergedContig = tryMerge(assembledContigs.get(i), assembledContigs.get(j));
                    if (!mergedContig.isEmpty()) {
                        assembledContigs.set(i, mergedContig);
                        assembledContigs.remove(j);
                        merged = true;
                        totalMerges++;
                        break;
                    }
                }
                if (merged) {
                    break;
                }
            }
    
            int currentContigCount = assembledContigs.size();
            int mergesInThisIteration = initialContigCount - currentContigCount;
            if (mergesInThisIteration > 0) {
    System.out.println("Iteration completed: Merged " + mergesInThisIteration + " contigs. Total merges so far: " + totalMerges);
}


        }
    
        System.out.println("Contig assembly completed. Total merges: " + totalMerges);
    }
    

    private String tryMerge(String contig1, String contig2) {
        int minOverlap = 30; // Minimum overlap length to consider a merge

        // Try to merge contig2 to the end of contig1
        for (int i = 1; i <= Math.min(contig1.length(), contig2.length()); i++) {
            if (contig1.endsWith(contig2.substring(0, i)) && i >= minOverlap) {
                return contig1 + contig2.substring(i);
            }
        }

        // Try to merge contig1 to the end of contig2
        for (int i = 1; i <= Math.min(contig1.length(), contig2.length()); i++) {
            if (contig2.endsWith(contig1.substring(0, i)) && i >= minOverlap) {
                return contig2 + contig1.substring(i);
            }
        }

        return ""; // No merge possible
    }

    public void writeAssembledContigsToFile(String filePath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (String contig : assembledContigs) {
                writer.write(contig);
                writer.newLine();
            }
        }
    }
    public void alignReadsUsingSmithWaterman() {
        for (String read : readsMap.keySet()) {
            List<Integer> potentialAlignments = findPotentialAlignments(read);
            for (int position : potentialAlignments) {
                String referenceSegment = referenceGenome.substring(position, Math.min(referenceGenome.length(), position + read.length()));
                String alignment = smithWatermanAlignment(read, referenceSegment);
                // Process alignment, e.g., store it, print it, etc.
            }
        }
    }
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
                while (--tempMid >= 0 && read.equals(referenceGenome.substring(suffixArray[tempMid], Math.min(suffixArray[tempMid] + read.length(), referenceGenome.length())))) {
                    positions.add(suffixArray[tempMid]);
                }
                tempMid = mid;
                while (++tempMid < suffixArray.length && read.equals(referenceGenome.substring(suffixArray[tempMid], Math.min(suffixArray[tempMid] + read.length(), referenceGenome.length())))) {
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
    public static void main(String[] args) {
        
        GenomeAssembler assembler = new GenomeAssembler();
        try {
            assembler.readInputFiles("reads.txt", "reference.txt");
            assembler.createSuffixArray(); // Creating the suffix array
            assembler.alignReadsUsingSmithWaterman();
            System.out.println("Reads aligned using Smith-Waterman.");
            assembler.assembleContigs();
            System.out.println("Contigs assembled.");
            assembler.writeAssembledContigsToFile("assembled_reads.txt");
            System.out.println("Assembly completed. Contigs written to 'assembled_reads.txt'.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}
