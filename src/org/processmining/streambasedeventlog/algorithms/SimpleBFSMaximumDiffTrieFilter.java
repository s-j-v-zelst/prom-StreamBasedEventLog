package org.processmining.streambasedeventlog.algorithms;

import java.util.LinkedList;
import java.util.Queue;

import org.processmining.streambasedeventlog.models.EventPayload;
import org.processmining.streambasedeventlog.models.IncrementalPayloadTrie;
import org.processmining.streambasedeventlog.models.IncrementalPayloadTrie.Edge;
import org.processmining.streambasedeventlog.models.IncrementalRootedPayloadGraph;

public class SimpleBFSMaximumDiffTrieFilter {

	public static <T extends IncrementalPayloadTrie<P, E, F>, E extends IncrementalPayloadTrie.Edge<P>, F extends IncrementalRootedPayloadGraph.Edge.Factory<P, E>, P extends EventPayload> T apply(
			T trie) {
		for (E e : trie.getEdges()) {
			e.setRelevant(false);
		}
		Queue<Edge<P>> queue = new LinkedList<>();
		queue.add(trie.getRootEdge());
		while (!queue.isEmpty()) {
			Edge<P> edge = queue.poll();
			edge.setRelevant(true);
			int maxVal = Integer.MIN_VALUE;
			for (Edge<P> c : edge.getChildren()) {
				maxVal = Math.max(maxVal, c.getPayload().getActiveCaseIdentifiers().size());
			}
			double threshold = 0.5 * maxVal;
			for (Edge<P> c : edge.getChildren()) {
				if (c.getPayload().getActiveCaseIdentifiers().size() >= threshold) {
					queue.add(c);
				}
			}
		}
		return trie;

	}

}
