package org.processmining.streambasedeventlog.algorithms;

import java.util.ArrayDeque;
import java.util.Deque;

import org.deckfour.xes.model.XLog;
import org.processmining.eventstream.core.interfaces.XSEvent;
import org.processmining.stream.core.abstracts.AbstractXSReader;
import org.processmining.streambasedeventlog.models.XSEventStreamToXLogReader;
import org.processmining.streambasedeventlog.parameters.StreamBasedEventStorageParametersImpl;
import org.processmining.streambasedeventlog.util.XSEventCollectionUtils;

import com.google.common.collect.Lists;

public class SlidingWindowBasedEventCollectorImpl<P extends StreamBasedEventStorageParametersImpl>
		extends AbstractXSReader<XSEvent, XLog, XLog> implements XSEventStreamToXLogReader<P> {

	private final P param;
	private final Deque<XSEvent> window = new ArrayDeque<>();

	public SlidingWindowBasedEventCollectorImpl(final P param) {
		super("sliding_window_event_log_creator", null);
		this.param = param;
	}

	public P getStorageParameters() {
		return param;
	}

	public Class<XSEvent> getTopic() {
		return XSEvent.class;
	}

	public long getNumberOfMemoryEntriesRepresentingEvents() {
		return window.size();
	}

	public long getTotalNumberOfEventsDescribedByMemory() {
		return window.size();
	}

	public long getTotalPayloadMemoryOccupation() {
		long l = 0;
		for (XSEvent e : window) {
			l += e.size();
		}
		return l;
	}

	protected XLog computeCurrentResult() {

		return XSEventCollectionUtils.convertToXEventLog(Lists.newArrayList(window.descendingIterator()),
				param.getCaseIdentifier(), param.getActivityIdentifier());
	}

	protected void handleNextPacket(XSEvent packet) {
		window.push(packet);
		if (window.size() > param.getSlidingWindowSize()) {
			window.removeLast();
		}
	}

}
