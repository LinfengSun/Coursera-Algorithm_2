import edu.princeton.cs.algs4.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class WordNet {
    private Map<String, Set<Integer>> Noun = new HashMap<>();
    private Map<Integer, String> Id = new HashMap<>();
    private Digraph relationDigraph;
    private SAP sap;

    // constructor takes the name of the two input files
    public WordNet(String synsets, String hypernyms) {
        if (synsets == null || hypernyms == null) throw new NullPointerException();

        // initialize synsets data
        In synsetsInput = new In(synsets);
        for (String line : synsetsInput.readAllLines()) {
            String[] fields = line.split(",");

            int synsetId = Integer.parseInt(fields[0]);

            for (String noun : fields[1].split(" ")) {
                if (Noun.get(noun) == null)
                    Noun.put(noun, new HashSet<>());
                Noun.get(noun).add(synsetId);
            }
            Id.put(synsetId, fields[1]);
        }

        // initialize hypernyms data
        In hypernymsInput = new In(hypernyms);
        String[] lines = hypernymsInput.readAllLines();

        int notRootCount = 0; // used for later check
        relationDigraph = new Digraph(Id.size());
        for (String line : lines) {
            String[] fields = line.split(",");

            int synsetId = Integer.parseInt(fields[0]);
            if (fields.length < 2) continue;
            for (int i = 1; i < fields.length; i++) {
                int hypernymId = Integer.parseInt(fields[i]);
                relationDigraph.addEdge(synsetId, hypernymId);
            }
            notRootCount++;
        }

        // check whether relation digraph is a rooted DAG
        DirectedCycle circleChecker = new DirectedCycle(relationDigraph);
        if (notRootCount != lines.length - 1 && circleChecker.hasCycle()) throw new IllegalArgumentException();

        // initialize sap
        sap = new SAP(relationDigraph);
    }

    // returns all WordNet nouns
    public Iterable<String> nouns() {
        return Noun.keySet();
    }

    // is the word a WordNet noun?
    public boolean isNoun(String word) {
        return Noun.containsKey(word);
    }

    // distance between nounA and nounB (defined below)
    public int distance(String nounA, String nounB) {
        if (nounA == null || nounB == null) throw new NullPointerException();
        if (!isNoun(nounA) || !isNoun(nounB)) throw new IllegalArgumentException();

        Set<Integer> synsetA = Noun.get(nounA);
        Set<Integer> synsetB = Noun.get(nounB);

        return sap.length(synsetA, synsetB);
    }

    // a synset (second field of synsets.txt) that is the common ancestor of nounA and nounB
    // in a shortest ancestral path (defined below)
    public String sap(String nounA, String nounB) {
        if (nounA == null || nounB == null) throw new NullPointerException();
        if (!isNoun(nounA) || !isNoun(nounB)) throw new IllegalArgumentException();

        Set<Integer> synsetA = Noun.get(nounA);
        Set<Integer> synsetB = Noun.get(nounB);

        int ancestorId = sap.ancestor(synsetA, synsetB);
        return Id.get(ancestorId);
    }

    // do unit testing of this class
    public static void main(String[] args) {
	WordNet word = new WordNet("synsets.txt", "hypernyms.txt");       
        StdOut.println(word.sap("two-grain_spelt", "Old_World_chat"));
        StdOut.println(word.distance("two-grain_spelt", "Old_World_chat"));
    }
}
