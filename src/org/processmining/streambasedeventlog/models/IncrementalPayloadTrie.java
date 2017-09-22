package org.processmining.streambasedeventlog.models;

import java.util.ArrayList;
import java.util.List;

public interface IncrementalPayloadTrie<T, E extends IncrementalPayloadTrie.Edge<T>, F extends IncrementalRootedPayloadGraph.Edge.Factory<T, E>>
		extends IncrementalRootedPayloadGraph<T, E, F> {

	public interface Edge<T> extends IncrementalRootedPayloadGraph.Edge<T> {

		public class EdgeImpl<T> implements IncrementalPayloadTrie.Edge<T> {

			private List<Edge<T>> children = new ArrayList<>();
			private final IncrementalRootedPayloadGraph.Edge<T> delegate;
			private Edge<T> parent;
			private boolean relevant;
			private int depth = -1;

			public EdgeImpl(final T payload) {
				this(payload, null);
			}

			public EdgeImpl(final T payload, final IncrementalPayloadTrie.Edge<T> parent) {
				this.delegate = new IncrementalRootedPayloadGraph.Edge.EdgeImpl<T>(payload);
				this.parent = parent;
			}

			@SafeVarargs
			public EdgeImpl(final T payload, final IncrementalPayloadTrie.Edge<T> parent,
					final IncrementalPayloadTrie.Edge<T>... children) {
				this.delegate = new IncrementalPayloadTrie.Edge.EdgeImpl<T>(payload);
				this.parent = parent;
				for (IncrementalPayloadTrie.Edge<T> child : children) {
					this.children.add(child);
				}
			}

			public List<Edge<T>> getChildren() {
				return children;
			}

			public Edge<T> getParent() {
				return parent;
			}

			public T getPayload() {
				return delegate.getPayload();
			}

			public boolean isRelevant() {
				return relevant;
			}

			public void setParent(Edge<T> e) {
				parent = e;
			}

			public void setRelevant(boolean relevant) {
				this.relevant = relevant;
			}

			public int getDepth() {
				return depth;
			}

			public void setDepth(int d) {
				depth = d;
			}

		}

		List<Edge<T>> getChildren();

		Edge<T> getParent();

		void setParent(Edge<T> e);

		int getDepth();

		void setDepth(final int d);
		
		boolean isRelevant();
		
		void setRelevant(boolean relevant);

	}

	public class EdgeFactory<T>
			implements IncrementalRootedPayloadGraph.Edge.Factory<T, IncrementalPayloadTrie.Edge<T>> {

		public Edge<T> construct(T t) {
			return new IncrementalPayloadTrie.Edge.EdgeImpl<T>(t);
		}

	}

	List<E> getPathEndingIn(E edge);

	void filter();

}
