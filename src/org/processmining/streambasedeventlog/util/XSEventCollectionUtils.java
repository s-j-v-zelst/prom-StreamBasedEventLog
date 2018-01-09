package org.processmining.streambasedeventlog.util;

import java.util.Map;

import org.apache.commons.collections15.map.HashedMap;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.eventstream.core.interfaces.XSEvent;

public class XSEventCollectionUtils {

	private static final XFactory fact = XFactoryRegistry.instance().currentDefault();

	public static XLog convertToXEventLog(final Iterable<XSEvent> events, final String caseIdentifier,
			final String activityIdentifier) {
		XLog log = fact.createLog();
		log.getClassifiers().add(new XEventNameClassifier());
		Map<String, XTrace> cases = new HashedMap<>();
		for (XSEvent e : events) {
			XAttributeMap map = fact.createAttributeMap();
			for (String k : e.keySet()) {
				if (!(k.equals(caseIdentifier))) {
					XAttribute a = e.get(k);
					if (k.equals(activityIdentifier)) {
						map.put(XConceptExtension.KEY_NAME, a);
					} else {
						map.put(k, a);
					}
				}
			}
			XEvent xesEv = fact.createEvent(map);
			String caseId = e.get(caseIdentifier).toString();
			if (!cases.containsKey(caseId)) {
				XAttributeMap traceAttr = fact.createAttributeMap();
				traceAttr.put(XConceptExtension.KEY_NAME, e.get(caseIdentifier));
				cases.put(caseId, fact.createTrace(traceAttr));
				log.add(cases.get(caseId));
			}
			cases.get(caseId).add(xesEv);
		}
		return log;
	}

}
