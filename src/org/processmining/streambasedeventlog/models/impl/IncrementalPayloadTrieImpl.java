package org.processmining.streambasedeventlog.models.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.processmining.streambasedeventlog.models.IncrementalPayloadTrie;
import org.processmining.streambasedeventlog.models.IncrementalRootedPayloadGraph;

public class IncrementalPayloadTrieImpl<T> implements
		IncrementalPayloadTrie<T, IncrementalPayloadTrie.Edge<T>, IncrementalRootedPayloadGraph.Edge.Factory<T, IncrementalPayloadTrie.Edge<T>>> {

	private final IncrementalRootedPayloadGraph<T, IncrementalPayloadTrie.Edge<T>, IncrementalRootedPayloadGraph.Edge.Factory<T, IncrementalPayloadTrie.Edge<T>>> delegate;

	public IncrementalPayloadTrieImpl(final IncrementalPayloadTrie.Edge<T> root,
			IncrementalRootedPayloadGraph.Edge.Factory<T, IncrementalPayloadTrie.Edge<T>> edgeFactory) {
		delegate = IncrementalRootedPayloadGraph.Factory.construct(root, edgeFactory);
	}

	public IncrementalPayloadTrie.Edge<T> add(T object, IncrementalPayloadTrie.Edge<T> parent) {
		IncrementalPayloadTrie.Edge<T> edge = null;
		if (contains(parent)) {
			for (IncrementalPayloadTrie.Edge<T> e : parent.getChildren()) {
				if (e.getPayload().equals(object)) {
					edge = e;
					break;
				}
			}
			if (edge == null) {
				edge = delegate.getEdgeFactory().construct(object);
				delegate.getEdges().add(edge);
				edge.setParent(parent);
				parent.getChildren().add(edge);
				edge.setDepth(parent.getDepth() + 1);
			}
		}
		return edge;
	}

	public boolean contains(IncrementalPayloadTrie.Edge<T> edge) {
		return delegate.contains(edge);
	}

	public boolean contains(List<T> sequence) {
		IncrementalPayloadTrie.Edge<T> edge = getRootEdge();
		boolean match = false;
		for (T t : sequence) {
			match = false;
			for (IncrementalPayloadTrie.Edge<T> c : edge.getChildren()) {
				match |= c.getPayload().equals(t);
				if (match) {
					edge = c;
					break;
				}
			}
			if (!match)
				return false;
		}
		return match;
	}

	public Collection<IncrementalPayloadTrie.Edge<T>> getEdges() {
		return delegate.getEdges();
	}

	public IncrementalPayloadTrie.Edge<T> getRootEdge() {
		return delegate.getRootEdge();
	}

	public void remove(IncrementalPayloadTrie.Edge<T> edge) {
		delegate.remove(edge);
	}

	public IncrementalRootedPayloadGraph.Edge.Factory<T, IncrementalPayloadTrie.Edge<T>> getEdgeFactory() {
		return delegate.getEdgeFactory();
	}

	public List<IncrementalPayloadTrie.Edge<T>> getPathEndingIn(IncrementalPayloadTrie.Edge<T> edge) {
		List<IncrementalPayloadTrie.Edge<T>> path = new ArrayList<>();
		path.add(edge);
		IncrementalPayloadTrie.Edge<T> parent = edge.getParent();
		while (parent != null) {
			path.add(0, parent);
			parent = parent.getParent();
		}
		return path;
	}

	public void filter() {
		// TODO Auto-generated method stub

	}

}
