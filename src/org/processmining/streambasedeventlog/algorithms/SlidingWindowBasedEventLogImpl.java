package org.processmining.streambasedeventlog.algorithms;

import org.deckfour.xes.model.XLog;
import org.processmining.eventstream.core.interfaces.XSEvent;
import org.processmining.stream.core.abstracts.AbstractXSReader;
import org.processmining.streambasedeventlog.models.XSEventStreamToXLogReader;
import org.processmining.streambasedeventlog.parameters.StreamBasedEventStorageParametersImpl;
import org.processmining.streambasedeventlog.util.XSEventCollectionUtils;
import org.processmining.streambasedeventstorage.algorithms.XSEventStoreSlidingWindowImpl;
import org.processmining.streambasedeventstorage.parameters.XSEventStoreSlidingWindowParametersImpl;

public class SlidingWindowBasedEventLogImpl<P extends StreamBasedEventStorageParametersImpl>
		extends AbstractXSReader<XSEvent, XLog, XLog> implements XSEventStreamToXLogReader<P> {

	private final P param;
	private final XSEventStoreSlidingWindowImpl sw;

	public SlidingWindowBasedEventLogImpl(final P param, final XSEventStoreSlidingWindowParametersImpl swparams) {
		super("sliding_window_event_log_creator", null);
		swparams.setActivityIdentifier(param.getActivityIdentifier());
		swparams.setCaseIdentifier(param.getCaseIdentifier());
		this.param = param;
		sw = new XSEventStoreSlidingWindowImpl(swparams);
	}

	public P getStorageParameters() {
		return param;
	}

	public Class<XSEvent> getTopic() {
		return XSEvent.class;
	}

	public long getNumberOfMemoryEntriesRepresentingEvents() {
		return sw.getCurrentResult().size();
	}

	public long getTotalNumberOfEventsDescribedByMemory() {
		return sw.getCurrentResult().size();
	}

	public long getTotalPayloadMemoryOccupation() {
		long l = 0;
		for (XSEvent e : sw.getCurrentResult()) {
			l += e.size();
		}
		return l;
	}

	protected XLog computeCurrentResult() {
		return XSEventCollectionUtils.convertToXEventLog(sw.getCurrentResult(), param.getCaseIdentifier(),
				param.getActivityIdentifier());
	}

	protected void handleNextPacket(XSEvent packet) {
		sw.triggerPacketHandle(packet);
	}

}
