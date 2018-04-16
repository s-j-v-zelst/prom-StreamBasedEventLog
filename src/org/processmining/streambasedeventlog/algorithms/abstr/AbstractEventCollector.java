package org.processmining.streambasedeventlog.algorithms.abstr;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.deckfour.xes.model.XAttributeLiteral;
import org.processmining.eventstream.core.interfaces.XSEvent;
import org.processmining.stream.core.abstracts.AbstractXSReader;
import org.processmining.streambasedeventlog.models.XSEventStreamBasedEventStore;
import org.processmining.streambasedeventlog.parameters.StreamBasedEventStorageParametersImpl;
import org.processmining.streambasedeventstorage.models.XSEventStore;

/**
 * abstract entity that handles mapping events to tuple-based encoding (i.e.
 * long[]), storing parameters etc.
 * 
 * @author svzelst <T> resulting class <V> class to visualize
 */
public abstract class AbstractEventCollector<T, V, P extends StreamBasedEventStorageParametersImpl>
		extends AbstractXSReader<XSEvent, T, V> implements XSEventStreamBasedEventStore<T, P> {

	private final P parameters;
	//	private final Queue<String> slidingWindow = new LinkedList<>();
	private final XSEventStore backingStore;
	private final Queue<String> blackList = new LinkedList<>();

	public AbstractEventCollector(final String name, final P parameters, final XSEventStore eventStore) {
		super(name, null);
		this.parameters = parameters;
		//		XSEventStoreSlidingWindowParametersImpl swpar = new XSEventStoreSlidingWindowParametersImpl();
		//		swpar.setSize(parameters.getSlidingWindowSize());
		//		backingStore = new XSEventStoreSlidingWindowImpl("", swpar);
		this.backingStore = eventStore;
	}

	public P getStorageParameters() {
		return parameters;
	}

	protected List<XSEvent> addEventToCaseStore(XSEvent e) {
		backingStore.triggerPacketHandle(e);
		return backingStore.getOutFlux();
		//		String caseId = getCaseId(e);
		//		getSlidingWindow().add(caseId);
		//		if (getSlidingWindow().size() > getParameters().getSlidingWindowSize()) {
		//			return Collections.singleton(getSlidingWindow().poll());
		//		}
		//		return Collections.emptySet();
	}

	protected String getCaseId(XSEvent event) {
		String caseId = ((XAttributeLiteral) event.get(getParameters().getCaseIdentifier())).getValue();
		return caseId;
	}

	protected P getParameters() {
		return parameters;
	}

	//	public Queue<String> getSlidingWindow() {
	//		return slidingWindow;
	//	}

	public XSEventStore getBackingEventStore() {
		return backingStore;
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
