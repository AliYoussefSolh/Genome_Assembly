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

public class MismatchGenomeAssembler {

    private Map<String, Integer> readsMap = new HashMap<>();
    private String referenceGenome = "";
    private int[] suffixArray;
    private List<String> assembledContigs = new ArrayList<>();

    public Map<String, Integer> getReadsMap() {
        return readsMap;
    }

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

    private void createSuffixArray() {
        int n = referenceGenome.length();
        Integer[] suffixArray = new Integer[n];
        for (int i = 0; i < n; i++) {
            suffixArray[i] = i;
        }

        Arrays.sort(suffixArray, (i1, i2) -> {
            int index1 = i1;
            int index2 = i2;
            while (index1 < n && index2 < n) {
                if (referenceGenome.charAt(index1) != referenceGenome.charAt(index2)) {
                    return Character.compare(referenceGenome.charAt(index1), referenceGenome.charAt(index2));
                }
                index1++;
                index2++;
            }
            return Integer.compare(n - index1, n - index2);
        });

        this.suffixArray = new int[n];
        for (int i = 0; i < n; i++) {
            this.suffixArray[i] = suffixArray[i];
        }

    }

    private List<Integer> findPotentialAlignments(String read, int d) {
        List<Integer> positions = new ArrayList<>();
        int n = read.length();
        int k = (int) Math.floor(n / (d + 1.0));

        for (int i = 0; i <= d; i++) {
            int seedStart = i * k;
            int seedEnd = (i < d) ? seedStart + k : n;
            String seed = read.substring(seedStart, seedEnd);

            List<Integer> seedMatches = findSeedMatches(seed);

            for (int seedMatch : seedMatches) {

                int startPosInGenome = seedMatch - seedStart;
                if (startPosInGenome >= 0) {

                    // Check if the entire string has at most d mutations from the original genome
                    int mutations = 0;
                    for (int j = 0; j < n; j++) {
                        if ((startPosInGenome + j) < referenceGenome.length()
                                && read.charAt(j) != referenceGenome.charAt(startPosInGenome + j)) {
                            mutations++;
                        }
                        if (mutations > d) {
                            break;
                        }
                    }

                    if (mutations <= d && !positions.contains(startPosInGenome)) {
                        positions.add(startPosInGenome);
                    }
                }
            }
        }

        return positions;
    }

    private List<Integer> findSeedMatches(String seed) {
        List<Integer> matches = new ArrayList<>();
        int low = 0;
        int high = suffixArray.length - 1;

        while (low <= high) {
            int mid = (low + high) / 2;
            int suffixIndex = suffixArray[mid];
            int end = Math.min(suffixIndex + seed.length(), referenceGenome.length());
            String suffix = referenceGenome.substring(suffixIndex, end);

            int cmp = seed.compareTo(suffix);
            if (cmp == 0) {
                matches.add(suffixIndex);

                int tempMid = mid;
                while (--tempMid >= 0 && seed.equals(referenceGenome.substring(suffixArray[tempMid],
                        Math.min(suffixArray[tempMid] + seed.length(), referenceGenome.length())))) {
                    matches.add(suffixArray[tempMid]);
                }
                tempMid = mid;
                while (++tempMid < suffixArray.length && seed.equals(referenceGenome.substring(suffixArray[tempMid],
                        Math.min(suffixArray[tempMid] + seed.length(), referenceGenome.length())))) {
                    matches.add(suffixArray[tempMid]);
                }
                break;
            } else if (cmp < 0) {
                high = mid - 1;
            } else {
                low = mid + 1;
            }
        }

        return matches;
    }

    public void assembleContigs() {
        Map<Integer, String> positionToContigMap = new HashMap<>();
        for (String read : readsMap.keySet()) {
            List<Integer> positions = findPotentialAlignments(read, 10);
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
            int overlap = Math.max(0, lastPosition - position + 1);

            // Append only the non-overlapping part to the assembled genome
            assembledGenome.append(contig.substring(overlap));

            // Update the last position
            lastPosition = position + contig.length() - 1;

            positionsProcessed++;
            int progressPercentage = (int) ((positionsProcessed / (double) totalPositions) * 100);

        }

        assembledContigs.add(assembledGenome.toString());
    }

    public void writeAssembledContigsToFile(String filePath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (String contig : assembledContigs) {
                writer.write(contig);
                writer.newLine();
            }
        }
    }

    public static void main(String[] args) {
        long startTime = System.nanoTime();
        MismatchGenomeAssembler assembler = new MismatchGenomeAssembler();
        try {
            assembler.readInputFiles("test_data2/reads.txt", "test_data2/genome.txt");
            assembler.createSuffixArray(); // Creating the suffix array
            assembler.assembleContigs();
            assembler.writeAssembledContigsToFile("assembled_reads.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
        long endTime = System.nanoTime();
        long totalTime = endTime - startTime;
        double seconds = (double) totalTime / 1_000_000_000.0;
        System.out.println("Total time taken: " + seconds + " seconds");
    }

}
