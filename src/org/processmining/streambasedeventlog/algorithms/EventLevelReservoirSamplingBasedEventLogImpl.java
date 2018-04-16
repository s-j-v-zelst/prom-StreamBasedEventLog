package org.processmining.streambasedeventlog.algorithms;

import org.deckfour.xes.model.XLog;
import org.processmining.eventstream.core.interfaces.XSEvent;
import org.processmining.stream.core.abstracts.AbstractXSReader;
import org.processmining.streambasedeventlog.models.XSEventStreamToXLogReader;
import org.processmining.streambasedeventlog.parameters.StreamBasedEventStorageParametersImpl;
import org.processmining.streambasedeventlog.util.XSEventCollectionUtils;
import org.processmining.streambasedeventstorage.algorithms.XSEventStoreReservoirEventLevelSamplingImpl;
import org.processmining.streambasedeventstorage.parameters.XSEventStoreReservoirEventLevelSamplingParametersImpl;

public class EventLevelReservoirSamplingBasedEventLogImpl<P extends StreamBasedEventStorageParametersImpl>
		extends AbstractXSReader<XSEvent, XLog, XLog> implements XSEventStreamToXLogReader<P> {

	private final P params;

	private final XSEventStoreReservoirEventLevelSamplingImpl reservoir;

	public EventLevelReservoirSamplingBasedEventLogImpl(final P params,
			final XSEventStoreReservoirEventLevelSamplingParametersImpl resParam) {
		super("reservoir_event_level_sampling_collector", null);
		this.params = params;
		reservoir = new XSEventStoreReservoirEventLevelSamplingImpl(resParam);
	}

	public P getStorageParameters() {
		return params;
	}

	public Class<XSEvent> getTopic() {
		return XSEvent.class;
	}

	public long getNumberOfMemoryEntriesRepresentingEvents() {
		return reservoir.getCurrentResult().size();
	}

	public long getTotalNumberOfEventsDescribedByMemory() {
		return getNumberOfMemoryEntriesRepresentingEvents();
	}

	public long getTotalPayloadMemoryOccupation() {
		long size = 0;
		for (XSEvent e : reservoir.getCurrentResult()) {
			size += e.size();
		}
		return size;
	}

	protected XLog computeCurrentResult() {
		return XSEventCollectionUtils.convertToXEventLog(reservoir.getCurrentResult(), params.getCaseIdentifier(),
				params.getActivityIdentifier());
	}

	protected void handleNextPacket(XSEvent packet) {
		reservoir.triggerPacketHandle(packet);
	}

}
