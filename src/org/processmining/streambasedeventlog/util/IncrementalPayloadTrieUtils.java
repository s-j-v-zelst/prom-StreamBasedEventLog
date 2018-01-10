package org.processmining.streambasedeventlog.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeLiteralImpl;
import org.processmining.streambasedeventlog.models.EventPayload;
import org.processmining.streambasedeventlog.models.IncrementalPayloadTrie;
import org.processmining.streambasedeventlog.models.IncrementalRootedPayloadGraph;

public class IncrementalPayloadTrieUtils {

	private static final XFactory factory = XFactoryRegistry.instance().currentDefault();

	// currently ignores additional payload
	public static <E extends EventPayload> XLog convertToXLog(
			final IncrementalPayloadTrie<E, IncrementalPayloadTrie.Edge<E>, IncrementalRootedPayloadGraph.Edge.Factory<E, IncrementalPayloadTrie.Edge<E>>> trie,
			final boolean includePassiveCases) {
		return convertToXLog(
				convertRecursive(trie.getRootEdge(), includePassiveCases, new HashMap<String, List<XEvent>>()));
	}

	private static XLog convertToXLog(final Map<String, List<XEvent>> map) {
		XLog log = factory.createLog();
		log.getClassifiers().add(new XEventNameClassifier());
		for (String c : map.keySet()) {
			XAttributeMap xattr = factory.createAttributeMap();
			xattr.put(XConceptExtension.KEY_NAME, new XAttributeLiteralImpl(XConceptExtension.KEY_NAME, c));
			XTrace t = factory.createTrace(xattr);
			t.addAll(map.get(c));
			log.add(t);
		}
		return log;
	}

	// currently ignores additional payload
	private static <E extends EventPayload> Map<String, List<XEvent>> convertRecursive(
			IncrementalPayloadTrie.Edge<E> edge, final boolean includePassiveCases,
			final Map<String, List<XEvent>> recVar) {
		Collection<String> cases = includePassiveCases ? edge.getPayload().getPayLoadCoverage()
				: edge.getPayload().getActiveCaseIdentifiers();
		for (String c : cases) {
			if (!recVar.containsKey(c)) {
				recVar.put(c, new ArrayList<XEvent>());
			}
			XAttributeMap xattr = factory.createAttributeMap();
			xattr.put(XConceptExtension.KEY_NAME,
					new XAttributeLiteralImpl(XConceptExtension.KEY_NAME, edge.getPayload().getActivity()));
			recVar.get(c).add(factory.createEvent(xattr));
		}
		for (IncrementalPayloadTrie.Edge<E> e : edge.getChildren()) {
			convertRecursive(e, includePassiveCases, recVar);
		}
		return recVar;
	}

}
