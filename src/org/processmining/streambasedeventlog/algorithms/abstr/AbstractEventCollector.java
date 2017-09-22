package org.processmining.streambasedeventlog.algorithms.abstr;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;

import org.deckfour.xes.model.XAttributeLiteral;
import org.deckfour.xes.model.XLog;
import org.processmining.eventstream.core.interfaces.XSEvent;
import org.processmining.eventstream.readers.xlog.XSEventStreamToXLogReader;
import org.processmining.stream.core.abstracts.AbstractXSReader;
import org.processmining.streambasedeventlog.parameters.StreamBasedEventStorageParametersImpl;

/**
 * abstract entity that handles mapping events to tuple-based encoding (i.e.
 * long[]), storing parameters etc.
 * 
 * @author svzelst
 *
 */
public abstract class AbstractEventCollector<P extends StreamBasedEventStorageParametersImpl>
		extends AbstractXSReader<XSEvent, XLog, XLog> implements XSEventStreamToXLogReader {

	private final P parameters;
	private final Queue<String> slidingWindow = new LinkedList<>();
	private final Queue<String> blackList = new LinkedList<>();

	public AbstractEventCollector(String name, P parameters) {
		super(name, null);
		this.parameters = parameters;
	}

	protected Collection<String> addEventToCaseStore(XSEvent e) {
		String caseId = getCaseId(e);
		getSlidingWindow().add(caseId);
		if (getSlidingWindow().size() > getParameters().getSlidingWindowSize()) {
			return Collections.singleton(getSlidingWindow().poll());
		}
		return Collections.emptySet();
	}

	protected String getCaseId(XSEvent event) {
		String caseId = ((XAttributeLiteral) event.get(getParameters().getCaseIdentifier())).getValue();
		return caseId;
	}

	protected P getParameters() {
		return parameters;
	}

	public Queue<String> getSlidingWindow() {
		return slidingWindow;
	}

	public Class<XSEvent> getTopic() {
		return XSEvent.class;
	}

	protected boolean shouldConsiderCase(String caseId) {
		return !(getParameters().isUseBlackList()) || (getParameters().isUseBlackList() && !blackList.contains(caseId));
	}

	protected void addToBlacklist(String caseId) {
		if (!blackList.contains(caseId)) {
			blackList.add(caseId);
		}
		while (blackList.size() > getParameters().getBlackListCapacity()) {
			blackList.poll();
		}
	}

}
