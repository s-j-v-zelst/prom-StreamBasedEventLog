package org.processmining.streambasedeventlog.models.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.processmining.streambasedeventlog.models.IncrementalRootedPayloadGraph;

public class IncrementalRootedPayloadGraphImpl<T, E extends IncrementalRootedPayloadGraph.Edge<T>, F extends IncrementalRootedPayloadGraph.Edge.Factory<T, E>>
		implements IncrementalRootedPayloadGraph<T, E, F> {

	private final F edgeFactory;
	private Collection<E> edges = new HashSet<>();
	private final E root;

	public IncrementalRootedPayloadGraphImpl(E root, F edgeFactory) {
		this.root = root;
		edges.add(root);
		this.edgeFactory = edgeFactory;
	}

	public E add(T object, E parent) {
		E e = getEdgeFactory().construct(object);
		edges.add(e);
		return e;
	}

	public boolean contains(E edge) {
		return edges.contains(edge);
	}

	public boolean contains(List<T> sequence) {
		return false;
	}

	public F getEdgeFactory() {
		return edgeFactory;
	}

	public Collection<E> getEdges() {
		return edges;
	}

	public E getRootEdge() {
		return root;
	}

	public void remove(E edge) {
		if (!(edge.equals(root))) {
			edges.remove(edge);
		}
	}

}
