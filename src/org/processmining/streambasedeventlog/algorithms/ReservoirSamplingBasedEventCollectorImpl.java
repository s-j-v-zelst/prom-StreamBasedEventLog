package org.processmining.streambasedeventlog.algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.deckfour.xes.model.XLog;
import org.processmining.eventstream.core.interfaces.XSEvent;
import org.processmining.stream.core.abstracts.AbstractXSReader;
import org.processmining.streambasedeventlog.models.XSEventStreamToXLogReader;
import org.processmining.streambasedeventlog.parameters.StreamBasedEventStorageParametersImpl;
import org.processmining.streambasedeventlog.util.XSEventCollectionUtils;

public class ReservoirSamplingBasedEventCollectorImpl<P extends StreamBasedEventStorageParametersImpl>
		extends AbstractXSReader<XSEvent, XLog, XLog> implements XSEventStreamToXLogReader<P> {

	private final P params;

	private final XSEvent[][] reservoir;
	private int occupied = 0;
	private final Map<String, Integer> indices;
	private int pckt = 0;

	public ReservoirSamplingBasedEventCollectorImpl(final P params) {
		super("reservoir_sampling_collector", null);
		this.params = params;
		reservoir = new XSEvent[params.getSlidingWindowSize()][0];
		indices = new HashMap<>(params.getSlidingWindowSize());
	}

	public P getStorageParameters() {
		return params;
	}

	public Class<XSEvent> getTopic() {
		return XSEvent.class;
	}

	public long getNumberOfMemoryEntriesRepresentingEvents() {
		int r = 0;
		for (XSEvent[] a : reservoir) {
			r += a.length;
		}
		return r;
	}

	public long getTotalNumberOfEventsDescribedByMemory() {
		return getNumberOfMemoryEntriesRepresentingEvents();
	}

	public long getTotalPayloadMemoryOccupation() {
		long s = 0;
		for (XSEvent[] a : reservoir) {
			for (XSEvent e : a) {
				s += e.size();
			}
		}
		return s;
	}

	protected XLog computeCurrentResult() {
		List<XSEvent> iterable = new ArrayList<>();
		for (XSEvent[] a : reservoir) {
			for (XSEvent e : a) {
				iterable.add(e);
			}
		}
		return XSEventCollectionUtils.convertToXEventLog(iterable, params.getCaseIdentifier(),
				params.getActivityIdentifier());
	}

	protected void handleNextPacket(XSEvent packet) {
		pckt++;
		String caseId = packet.get(params.getCaseIdentifier()).toString();
		int i = -1;
		if (indices.containsKey(caseId)) {
			i = indices.get(caseId);
		} else {
			if (occupied < params.getSlidingWindowSize()) {
				i = indices.size();
				occupied++;
				indices.put(caseId, i);
			} else {
				Random r = new Random();
				int n = r.nextInt(pckt);
				if (n < params.getSlidingWindowSize()) {
					i = r.nextInt(params.getSlidingWindowSize());
					// find i
					String rem = null;
					for (String c : indices.keySet()) {
						if (indices.get(c) == i) {
							rem = c;
							break;
						}
					}
					indices.remove(rem);
					indices.put(caseId, i);
					reservoir[i] = new XSEvent[0];
				}
			}
		}
		if (i > -1) {
			XSEvent[] trace = reservoir[i];
			trace = Arrays.copyOf(trace, trace.length + 1);
			trace[trace.length - 1] = packet;
			reservoir[i] = trace;
		}
	}

}
