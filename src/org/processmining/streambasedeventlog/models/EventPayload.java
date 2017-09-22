package org.processmining.streambasedeventlog.models;

import java.util.Collection;

import org.processmining.streambasedeventlog.models.impl.EventPayloadImpl;

/**
 * 
 * @author svzelst
 *
 * @param <T>
 *            activity type
 * @param <S>
 *            attribute identifier type, e.g. String-based:
 *            "resource"=>Payload<>, Integer-based: 0=> Payload<>
 * @param <U>
 *            attribute value type
 * @param <V>
 *            payload meta data
 * @param <C>
 *            case-id type
 * 
 */
public interface EventPayload {

	public interface Factory<E extends EventPayload> {
		public E construct(String activity);
	}

	public class FactoryNaiveImpl implements EventPayload.Factory<EventPayload> {

		public EventPayload construct(final String activity) {
			return new EventPayloadImpl(activity);
		}

	}

	Collection<String> getActiveCaseIdentifiers();

	Collection<String> getPayLoadCoverage();

	String getActivity();

	Payload getDataForKey(String attribute);

	Collection<String> getAttributeKeys();

	void clear();
}
