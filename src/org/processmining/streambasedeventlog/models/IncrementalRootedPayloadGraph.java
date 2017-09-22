package org.processmining.streambasedeventlog.models;

import java.util.Collection;
import java.util.List;

import org.processmining.streambasedeventlog.models.impl.IncrementalRootedPayloadGraphImpl;

public interface IncrementalRootedPayloadGraph<T, E extends IncrementalRootedPayloadGraph.Edge<T>, F extends IncrementalRootedPayloadGraph.Edge.Factory<T, E>> {

	public class Factory {

		public static <T, E extends IncrementalRootedPayloadGraph.Edge<T>, F extends IncrementalRootedPayloadGraph.Edge.Factory<T, E>> IncrementalRootedPayloadGraph<T, E, F> construct(
				final E root, final F edgeFactory) {
			return new IncrementalRootedPayloadGraphImpl<T, E, F>(root, edgeFactory);
		}

	}

	public abstract interface Edge<T> {

		public abstract interface Factory<T, E> {
			E construct(T t);
		}

		public class EdgeImpl<T> implements Edge<T> {

			private final T payload;

			public EdgeImpl(final T payload) {
				this.payload = payload;
			}

			public T getPayload() {
				return payload;
			}

		}

		T getPayload();
	}

	E add(final T object, final E parent);

	boolean contains(E edge);

	boolean contains(final List<T> sequence);

	Collection<E> getEdges();

	E getRootEdge();

	void remove(final E edge);

	F getEdgeFactory();
}
