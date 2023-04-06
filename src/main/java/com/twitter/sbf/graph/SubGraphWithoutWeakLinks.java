package com.twitter.sbf.graph;

import java.util.Set;

import com.twitter.sbf.util.Util;

import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * An edge (u, v) is called a "weak link" if u and v share no common neighbors.
 *
 * This class creates a representation of a subgraph of an input graph with 2 properties:
 * 1. The caller can specify which nodes of the original graph are included in the subgraph.
 * 2. Only edges which are not weak links are included in the neighbors returned for a node.
 */
public class SubGraphWithoutWeakLinks implements GraphInterface {
    private Graph fullGraph;
    private Set<Integer> nodesOfSubgraph;
    private long numWeakLinks = 0;

    public SubGraphWithoutWeakLinks(Graph fullGraph, Set<Integer> nodesOfSubgraph) {
        this.fullGraph = fullGraph;
        this.nodesOfSubgraph = nodesOfSubgraph;

        // Check if nodesOfSubgraph contains any nodes that are not in the full graph
        for (int nodeId : nodesOfSubgraph) {
            if (!fullGraph.containsNode(nodeId)) {
                throw new IllegalArgumentException("Node " + nodeId + " is not in the full graph.");
            }
        }
    }

    /**
     * Return the ids of the nodes in this graph
     * @return iterator with the ids
     */
    public IntIterator getAllNodeIds() {
        return Util.intIteratorFromSet(nodesOfSubgraph);
    }

    /**
     * Return the neighbors of a specified node
     * @param nodeId id of the node whose neighbors are desired
     * @return iterator with the neighbors of the specified node
     */
    public IntIterator getNeighbors(int nodeId) {
        if (!nodesOfSubgraph.contains(nodeId)) {
            return IntIterators.EMPTY_ITERATOR;
        } else {
            int[] allNeighbors = fullGraph.getNeighbors(nodeId);
            IntSet neighborsIncludingWeakLinks = new IntOpenHashSet();
            for (int n : allNeighbors) {
                if (nodesOfSubgraph.contains(n)) {
                    neighborsIncludingWeakLinks.add(n);
                }
            }

            IntSet neighborsWithoutWeakLinks = new IntOpenHashSet();
            for (int neighbor : neighborsIncludingWeakLinks) {
                IntSet sharedNeighbors = new IntOpenHashSet(fullGraph.getNeighbors(neighbor));
                sharedNeighbors.retainAll(neighborsIncludingWeakLinks);
                if (!sharedNeighbors.isEmpty()) {
                    neighborsWithoutWeakLinks.add(neighbor);
                } else {
                    numWeakLinks++;
                }
            }

            return neighborsWithoutWeakLinks.iterator();
        }
    }

    public long getNumWeakLinks() {
        return numWeakLinks;
    }
}
