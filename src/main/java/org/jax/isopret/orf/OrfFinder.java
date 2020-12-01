package org.jax.isopret.orf;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * A simple implementation of an ORF finder that takes a cDNA sequence and returns the longest ORF found in it.
 * @author Peter N Robinson
 */
public class OrfFinder {

    Map<Integer, Integer> startToLenMap = new HashMap<>();
    private final String cDNA;
    private final String longestAaSequence;
    private final boolean hasORF;

    private final Set<String> stopCodons = Set.of("TAA", "TAG", "TGA");

    private final static Map<String, String> codon2aaMap;
    static {
        codon2aaMap = new HashMap<>();
        codon2aaMap.put("GCT", "A");
        codon2aaMap.put("GCC","A");
        codon2aaMap.put("GCA", "A");
        codon2aaMap.put("GCG", "A");
        codon2aaMap.put("ATT", "I");
        codon2aaMap.put("ATC", "I");
        codon2aaMap.put("ATA", "I");
        codon2aaMap.put("CGT", "R");
        codon2aaMap.put("CGC", "R");
        codon2aaMap.put("CGA","R");
        codon2aaMap.put("CGG","R");
        codon2aaMap.put("AGA","R");
        codon2aaMap.put("AGG","R");
        codon2aaMap.put("CTT", "L");
        codon2aaMap.put("CTC","L");
        codon2aaMap.put("CTA","L");
        codon2aaMap.put("CTG","L");
        codon2aaMap.put("TTA","L");
        codon2aaMap.put("TTG","L");
        codon2aaMap.put("AAT","N");
        codon2aaMap.put("AAC","N");
        codon2aaMap.put("AAA","K");
        codon2aaMap.put("AAG","K");
        codon2aaMap.put("GAT","D");
        codon2aaMap.put("GAC","D");
        codon2aaMap.put("ATG","M");
        codon2aaMap.put("TTT","F");
        codon2aaMap.put("TTC","F");
        codon2aaMap.put("TGT","C");
        codon2aaMap.put("TGC","C");
        codon2aaMap.put("CCT","P");
        codon2aaMap.put("CCC","P");
        codon2aaMap.put("CCA","P");
        codon2aaMap.put("CCG","P");
        codon2aaMap.put("CAA","Q");
        codon2aaMap.put("CAG","Q");
        codon2aaMap.put("TCT","S");
        codon2aaMap.put("TCC","S");
        codon2aaMap.put("TCA","S");
        codon2aaMap.put("TCG","S");
        codon2aaMap.put("AGT","S");
        codon2aaMap.put("AGC","S");
        codon2aaMap.put("GAA","E");
        codon2aaMap.put("GAG","E");
        codon2aaMap.put("ACT","T");
        codon2aaMap.put("ACC","T");
        codon2aaMap.put("ACA","T");
        codon2aaMap.put("ACG","T");
        codon2aaMap.put("TGG","W");
        codon2aaMap.put("GGT","G");
        codon2aaMap.put("GGC","G");
        codon2aaMap.put("GGA","G");
        codon2aaMap.put("GGG","G");
        codon2aaMap.put("TAT","Y");
        codon2aaMap.put("TAC","Y");
        codon2aaMap.put("CAT","H");
        codon2aaMap.put("CAC","H");
        codon2aaMap.put("GTT","V");
        codon2aaMap.put("GTC","V");
        codon2aaMap.put("GTA","V");
        codon2aaMap.put("GTG","V");
        codon2aaMap.put("TAA","*");
        codon2aaMap.put("TGA","*");
        codon2aaMap.put("TAG","*");
    }

    public OrfFinder(String seq) {
        this.cDNA = seq;
        Optional<String> aa = getLongestOrf();
        if (aa.isPresent()) {
            this.longestAaSequence = aa.get();
            this.hasORF = true;
        } else {
            this.longestAaSequence = "";
            this.hasORF = false;
        }
    }

    public String getLongestAaSequence() {
        return longestAaSequence;
    }

    public boolean hasORF() {
        return hasORF;
    }

    /**
     * calulcate the longest ORF of this cDNA. Return an empty Optional if there is none.
     * @return Amino acid sequence of the longest ORF or an empty Optional if this is not a protein coding transcript
     */
    public Optional<String> getLongestOrf() {
        Map<Integer, Integer> startToLenMap = new HashMap<>();
        int i = this.cDNA.indexOf("ATG");
        if (i < 0) {
            return Optional.empty(); // no start codon
        }
        int N = this.cDNA.length();
        int startPos = i;
        i += 3;
        while (i < N) {
            String codon = this.cDNA.substring(i-3, i);
            if (stopCodons.contains(codon)) {
                int len = i - startPos;
                startToLenMap.put(startPos, len);
                i = this.cDNA.indexOf("ATG", i);
                if (i<0) {
                    break;
                } else {
                    startPos = i;
                }
            }
            i += 3;
        }
        if (startToLenMap.isEmpty()) {
            return Optional.empty();
        } else {
            int maxStartPos = getMax(startToLenMap);
            i = maxStartPos;
            String codon = this.cDNA.substring(i, i+3);
            StringBuilder aaSeq = new StringBuilder();
            while (! stopCodons.contains(codon)) {
                String aa = codon2aaMap.get(codon);
                aaSeq.append(aa);
                i += 3;
                codon = this.cDNA.substring(i, i+3);
            }
            return Optional.of(aaSeq.toString());
        }
    }

    /**
     * @return the Key of the map that is associated with the highest value
     */
    private static <K, V extends Comparable<V>> K getMax(Map<K, V> map) {
        Optional<Map.Entry<K, V>> maxEntry = map.entrySet()
                .stream()
                .max(Map.Entry.comparingByValue());
        return maxEntry.get()
                .getKey();
    }
}
