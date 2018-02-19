import edu.princeton.cs.algs4.Digraph;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdIn;
import edu.princeton.cs.algs4.StdOut;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;


public class SAP {
    private static final boolean LENGTH = true;
    private static final boolean ANCESTOR = false;
    private Digraph digraph;
    private Set<Integer> markedVSet;
    private Set<Integer> markedWSet;
    private int[] distToV;
    private int[] distToW;

    // constructor takes a digraph (not necessarily a DAG)
    public SAP(Digraph G) {
        if (G == null) throw new NullPointerException();

        digraph = new Digraph(G);
        markedVSet = new HashSet<>();
        markedWSet = new HashSet<>();
        distToV = new int[G.V()];
        distToW = new int[G.V()];
        for (int i = 0; i < distToV.length; i++) {
            distToV[i] = Integer.MAX_VALUE;
            distToW[i] = Integer.MAX_VALUE;
        }
    }

    // length of shortest ancestral path between v and w; -1 if no such path
    public int length(int v, int w) {
        return bfs(v, w, LENGTH);
    }

    // a common ancestor of v and w that participates in a shortest ancestral path; -1 if no such path
    public int ancestor(int v, int w) {
        return bfs(v, w, ANCESTOR);
    }

    // length of shortest ancestral path between any vertex in v and any vertex in w; -1 if no such path
    public int length(Iterable<Integer> v, Iterable<Integer> w) {
        return bfs(v, w, LENGTH);
    }

    // a common ancestor that participates in shortest ancestral path; -1 if no such path
    public int ancestor(Iterable<Integer> v, Iterable<Integer> w) {
        return bfs(v, w, ANCESTOR);
    }

    private int bfs(int v, int w, boolean requiredValue) {
        List<Integer> vList = new ArrayList<>();
        List<Integer> wList = new ArrayList<>();
        vList.add(v);
        wList.add(w);
        return bfs(vList, wList, requiredValue);
    }
    private int bfs(Iterable<Integer> v, Iterable<Integer> w, boolean requiredValue) {
        if (v == null || w == null) throw new NullPointerException();

        Queue<Integer> queueV = new LinkedList<>();
        Queue<Integer> queueW = new LinkedList<>();
        int minlength = -1;
        int ancestor = -1;

        // initialize sources
        for (int s : v) {
            markedVSet.add(s);
            distToV[s] = 0;
            queueV.add(s);
        }
        for (int s:  w) {
            markedWSet.add(s);
            distToW[s] = 0;
            queueW.add(s);
        }

        // start two bfs searches for v and w, keep searching in same layer
        int layer = 0;
        while (!queueV.isEmpty() || !queueW.isEmpty()) {
            while (!queueV.isEmpty() && distToV[queueV.peek()] <= layer) {
                int current = queueV.remove();
                for (int nextLayer : digraph.adj(current)) {
                    if (!markedVSet.contains(nextLayer)) {
                        markedVSet.add(nextLayer);
                        distToV[nextLayer] = distToV[current] + 1;
                        queueV.add(nextLayer);
                    }
                }

                if (markedWSet.contains(current)) {
                    int length = distToV[current] + distToW[current];
                    if (ancestor == -1 || length < minlength) {
                        minlength = length;
                        ancestor = current;
                    }
                }
            }

            while (!queueW.isEmpty() && distToW[queueW.peek()] <= layer) {
                int current = queueW.remove();
                for (int nextLayer : digraph.adj(current)) {
                    if (!markedWSet.contains(nextLayer)) {
                        markedWSet.add(nextLayer);
                        distToW[nextLayer] = distToW[current] + 1;
                        queueW.add(nextLayer);
                    }
                }

                if (markedVSet.contains(current)) {
                    int length = distToV[current] + distToW[current];
                    if (ancestor == -1 || length < minlength) {
                        minlength = length;
                        ancestor = current;
                    }
                }
            }

            // increase layer and remove impossible cases
            layer++;
            if (ancestor != -1 && minlength < layer) {
                queueV.clear();
                queueW.clear();
            }
        }

        // set back changed values, which saves time for re-initialization
        for (int changed : markedVSet)
            distToV[changed] = Integer.MAX_VALUE;
        for (int changed : markedWSet)
            distToW[changed] = Integer.MAX_VALUE;
        markedVSet.clear();
        markedWSet.clear();

        if (requiredValue == LENGTH)
            return minlength;
        else
            return ancestor;
    }

    // do unit testing of this class
    public static void main(String[] args) {
        In in = new In(args[0]);
        Digraph G = new Digraph(in);
        SAP sap = new SAP(G);
        while (!StdIn.isEmpty()) {
            int v = StdIn.readInt();
            int w = StdIn.readInt();
            int length   = sap.length(v, w);
            int ancestor = sap.ancestor(v, w);
            StdOut.printf("length = %d, ancestor = %d\n", length, ancestor);
        }
    }
}
