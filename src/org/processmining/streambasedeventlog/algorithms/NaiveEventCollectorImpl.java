package org.processmining.streambasedeventlog.algorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.eventstream.core.interfaces.XSEvent;
import org.processmining.framework.util.Pair;
import org.processmining.streambasedeventlog.algorithms.abstr.AbstractEventCollector;
import org.processmining.streambasedeventlog.models.XSEventStreamToXLogReader;
import org.processmining.streambasedeventlog.parameters.StreamBasedEventLogParametersImpl;

public class NaiveEventCollectorImpl<P extends StreamBasedEventLogParametersImpl>
		extends AbstractEventCollector<XLog, XLog, P> implements XSEventStreamToXLogReader<P> {

	private final Map<String, List<XSEvent>> cases = new HashMap<>();
	private final Map<String, Pair<List<XSEvent>, List<XSEvent>>> delta = new HashMap<>();

	public Map<String, Pair<List<XSEvent>, List<XSEvent>>> getDelta() {
		return delta;
	}

	public NaiveEventCollectorImpl(final P parameters) {
		super("naive_event_collector", parameters);
		//		System.out.println("#event, #memory_elements, #events, #payload");
	}

	protected void handleNextPacket(XSEvent packet) {
		Collection<String> removed = addEventToCaseStore(packet);
		delta.clear();
		String caseId = getCaseId(packet);
		//		packet.remove(getParameters().getCaseIdentifier()); // remove obsolete case identifier;
		if (super.shouldConsiderCase(caseId)) {
			if (!cases.containsKey(caseId)) {
				cases.put(caseId, new ArrayList<XSEvent>());
			}
			cases.get(caseId).add(packet);
		}
		for (String removedCase : removed) {
			List<XSEvent> old = delta.containsKey(removedCase) ? new ArrayList<>(delta.get(removedCase).getFirst())
					: new ArrayList<>(cases.get(removedCase));
			switch (getParameters().getTraceRemovalStrategy()) {
				case DROP :
					removeCase(removedCase);
					break;
				case SHIFT :
					List<XSEvent> currentTrace = getCases().get(removedCase);
					if (currentTrace != null) { // case already removed, probably due to blacklist
						if (currentTrace.size() > 1) {
							getCases().put(removedCase, currentTrace.subList(1, currentTrace.size()));
						} else {
							getCases().remove(removedCase);
							if (getParameters().isUseBlackList()) {
								addToBlacklist(removedCase);
							}
						}
						break;
					} else {
						break;
					}
			}
			List<XSEvent> newInMem = cases.containsKey(removedCase) ? new ArrayList<XSEvent>(cases.get(removedCase))
					: new ArrayList<XSEvent>();
			delta.put(removedCase, new Pair<List<XSEvent>, List<XSEvent>>(old, newInMem));
		}
		//		getCurrentResult();
		//		System.out.println(getNumberOfPacketsReceived() + ", " + getNumberOfMemoryEntriesRepresentingEvents() + ", "
		//				+ getTotalNumberOfEventsDescribedByMemory() + ", " + getTotalPayloadMemoryOccupation());
	}

	private void removeCase(final String caseId) {
		getCases().remove(caseId);
		if (getParameters().isUseBlackList()) {
			addToBlacklist(caseId);
		}
	}

	public Map<String, List<XSEvent>> getCases() {
		return cases;
	}

	public long getNumberOfMemoryEntriesRepresentingEvents() {
		long res = 0;
		for (String caseId : getCases().keySet()) {
			res += getCases().get(caseId).size();
		}
		return res;
	}

	public long getTotalNumberOfEventsDescribedByMemory() {
		return getNumberOfMemoryEntriesRepresentingEvents();
	}

	public long getTotalPayloadMemoryOccupation() {
		long res = 0;
		for (String caseId : getCases().keySet()) {
			for (XSEvent e : getCases().get(caseId)) {
				res += e.size();
			}
		}
		return res;
	}

	protected XLog computeCurrentResult() {
		synchronized (getDeliveryLock()) {
			XFactory xf = getParameters().getXFactory();
			XLog log = xf.createLog();
			for (Map.Entry<String, List<XSEvent>> entry : cases.entrySet()) {
				XAttributeMap traceAttr = xf.createAttributeMap();
				traceAttr.put(XConceptExtension.KEY_NAME, xf.createAttributeLiteral(XConceptExtension.KEY_NAME,
						entry.getKey(), XConceptExtension.instance()));
				XTrace trace = xf.createTrace(traceAttr);
				for (XSEvent e : entry.getValue()) {
					XAttributeMap eventMap = xf.createAttributeMap();
					for (Map.Entry<String, XAttribute> eventEntry : e.entrySet()) {
						eventMap.put(eventEntry.getKey(), (XAttribute) eventEntry.getValue().clone());
					}
					trace.add(xf.createEvent(eventMap));
				}
				log.add(trace);
			}
			return log;
		}
	}

}
