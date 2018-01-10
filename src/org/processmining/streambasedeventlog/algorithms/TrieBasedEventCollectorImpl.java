package org.processmining.streambasedeventlog.algorithms;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.deckfour.xes.model.XLog;
import org.processmining.eventstream.core.interfaces.XSEvent;
import org.processmining.streambasedeventlog.algorithms.abstr.AbstractEventCollector;
import org.processmining.streambasedeventlog.models.EventPayload;
import org.processmining.streambasedeventlog.models.IncrementalPayloadTrie;
import org.processmining.streambasedeventlog.models.IncrementalRootedPayloadGraph;
import org.processmining.streambasedeventlog.models.XSEventStreamToXLogReader;
import org.processmining.streambasedeventlog.models.impl.IncrementalPayloadTrieImpl;
import org.processmining.streambasedeventlog.parameters.StreamBasedEventStorageParametersImpl;
import org.processmining.streambasedeventlog.util.IncrementalPayloadTrieUtils;

public class TrieBasedEventCollectorImpl<E extends EventPayload, P extends StreamBasedEventStorageParametersImpl>
		extends AbstractEventCollector<XLog, XLog, P> implements XSEventStreamToXLogReader<P> {

	private final EventPayload.Factory<E> eventPayloadFactory;
	private final Map<String, IncrementalPayloadTrie.Edge<E>> fromEdges = new HashMap<>();

	private final Map<String, IncrementalPayloadTrie.Edge<E>> toEdges = new HashMap<>();

	private final IncrementalPayloadTrie<E, IncrementalPayloadTrie.Edge<E>, IncrementalRootedPayloadGraph.Edge.Factory<E, IncrementalPayloadTrie.Edge<E>>> trie;

	public TrieBasedEventCollectorImpl(final P parameters, final EventPayload.Factory<E> eventPayloadFactory) {
		super("trie event collector", parameters);
		this.eventPayloadFactory = eventPayloadFactory;
		trie = new IncrementalPayloadTrieImpl<E>(
				new IncrementalPayloadTrie.Edge.EdgeImpl<E>(eventPayloadFactory.construct(null)),
				new IncrementalPayloadTrie.EdgeFactory<E>());
		trie.getRootEdge().setDepth(0);
		//		System.out.println("#event, #memory_elements, #events, #payload");
	}

	private void addEventToTrie(XSEvent event) {
		String caseId = getCaseId(event);
		String activity = event.get(getParameters().getActivityIdentifier()).toString();
		if (super.shouldConsiderCase(caseId)) {
			IncrementalPayloadTrie.Edge<E> edge = toEdges.get(caseId);
			if (edge == null) {
				edge = getTrie().getRootEdge();
				fromEdges.put(caseId, edge);
			}
			E payload = getPayloadForExecutedActivity(edge, activity);
			decorateEventPayload(payload, event);
			IncrementalPayloadTrie.Edge<E> newEdge = getTrie().add(payload, edge);
			toEdges.put(caseId, newEdge);
		}
	}

	protected XLog computeCurrentResult() {
		return IncrementalPayloadTrieUtils.convertToXLog(getTrie(), true);
	}

	private E decorateEventPayload(E payload, XSEvent event) {
		String caseId = getCaseId(event);
		payload.getActiveCaseIdentifiers().add(caseId);
		payload.getPayLoadCoverage().add(caseId);
		for (String key : event.keySet()) {
			String value = event.get(key).toString();
			if (!(key.equals(getParameters().getCaseIdentifier()))
					&& !(key.equals(getParameters().getActivityIdentifier()))) {
				if (!payload.getDataForKey(key).containsKey(value)) {
					payload.getDataForKey(key).put(value, new HashSet<String>());
				}
				payload.getDataForKey(key).get(value).add(caseId);
			}
		}
		return payload;
	}

	private void drop(IncrementalPayloadTrie.Edge<E> edge) {
		IncrementalPayloadTrie.Edge<E> parent = edge.getParent();
		parent.getChildren().remove(edge);
		// first remove all the branches "down"
		Collection<String> droppedCases = dropRecursive(edge, new HashSet<String>());
		for (IncrementalPayloadTrie.Edge<E> e : getTrie().getPathEndingIn(parent)) {
			for (String removedCase : droppedCases) {
				// sanity check, this should not be possible, contradiction on the branch drop
				// if this happens, something is wrong in trie construction.
				assert (!e.getPayload().getActiveCaseIdentifiers().contains(removedCase));
				stripCaseIdFromPayload(removedCase, e);
			}
		}
		sanityCheckAfterDrop(droppedCases);
	}

	private Collection<String> dropRecursive(final IncrementalPayloadTrie.Edge<E> edge,
			final Collection<String> removedCases) {
		for (IncrementalPayloadTrie.Edge<E> child : edge.getChildren()) {
			dropRecursive(child, removedCases);
		}
		for (String caseId : edge.getPayload().getActiveCaseIdentifiers()) {
			removePointersForCaseDueToRemoval(caseId);
			removedCases.add(caseId);
		}
		for (String caseId : edge.getPayload().getPayLoadCoverage()) {
			removePointersForCaseDueToRemoval(caseId);
			removedCases.add(caseId);
		}
		edge.getPayload().clear();
		edge.getChildren().clear();
		edge.setParent(null);
		getTrie().remove(edge);
		return removedCases;
	}

	private void findAndUpdateAllCasesPointingToShiftedEdge(final IncrementalPayloadTrie.Edge<E> edge,
			final Collection<String> cases) {
		for (Iterator<Map.Entry<String, IncrementalPayloadTrie.Edge<E>>> it = fromEdges.entrySet().iterator(); it
				.hasNext();) {
			Map.Entry<String, IncrementalPayloadTrie.Edge<E>> entry = it.next();
			if (entry.getValue().equals(edge)) {
				entry.setValue(getTrie().getRootEdge());
				cases.add(entry.getKey());
			}
		}
		for (String affectedCase : cases) {
			removeCaseIdRecursive(affectedCase, getTrie().getRootEdge());
		}
	}

	public Map<String, IncrementalPayloadTrie.Edge<E>> getFromEdges() {
		return fromEdges;
	}

	public long getNumberOfMemoryEntriesRepresentingEvents() {
		return getTrie().getEdges().size();
	}

	private E getPayloadForExecutedActivity(IncrementalPayloadTrie.Edge<E> fromEdge, String activity) {
		for (IncrementalPayloadTrie.Edge<E> edge : fromEdge.getChildren()) {
			if (edge.getPayload().getActivity().equals(activity)) {
				return edge.getPayload();
			}
		}
		return eventPayloadFactory.construct(activity);
	}

	public Map<String, IncrementalPayloadTrie.Edge<E>> getToEdges() {
		return toEdges;
	}

	public long getTotalNumberOfEventsDescribedByMemory() {
		long res = 0;
		for (IncrementalPayloadTrie.Edge<E> e : getTrie().getEdges()) {
			if (!getTrie().getRootEdge().equals(e)) {
				res += e.getPayload().getPayLoadCoverage().size();
			}
		}
		return res;
	}

	public long getTotalPayloadMemoryOccupation() {
		long res = 0l;
		for (IncrementalPayloadTrie.Edge<E> e : getTrie().getEdges()) {
			for (String key : e.getPayload().getAttributeKeys()) {
				res++;
				res += e.getPayload().getDataForKey(key).keySet().size();
				for (String value : e.getPayload().getDataForKey(key).keySet()) {
					res += e.getPayload().getDataForKey(key).get(value).size();
				}
			}
		}
		return res;
	}

	public IncrementalPayloadTrie<E, IncrementalPayloadTrie.Edge<E>, IncrementalRootedPayloadGraph.Edge.Factory<E, IncrementalPayloadTrie.Edge<E>>> getTrie() {
		return trie;
	}

	protected void handleNextPacket(XSEvent event) {
		Collection<String> droppedCases = addEventToCaseStore(event);
		addEventToTrie(event);
		for (String caseId : droppedCases) {
			handleRemovedCase(caseId);
		}
	}

	private void handleRemovedCase(String caseId) {
		if (fromEdges.containsKey(caseId) && toEdges.containsKey(caseId)) {
			IncrementalPayloadTrie.Edge<E> predecessor = fromEdges.get(caseId);
			IncrementalPayloadTrie.Edge<E> newPredecessor = null;
			for (IncrementalPayloadTrie.Edge<E> child : predecessor.getChildren()) {
				if (child.getPayload().getActiveCaseIdentifiers().contains(caseId)) {
					newPredecessor = child;
					break;
				}
			}
			newPredecessor.getPayload().getActiveCaseIdentifiers().remove(caseId);
			fromEdges.put(caseId, newPredecessor);
			if (newPredecessor.getPayload().getActiveCaseIdentifiers().isEmpty()) {
				if (!newPredecessor.equals(getTrie().getRootEdge())) { // don't alter the root (there are cases in which this can happen)...
					switch (getParameters().getTraceRemovalStrategy()) {
						case DROP : // we need to remove the complete subtree, and, remove all references.
							drop(newPredecessor);
							break;
						case SHIFT : // we only need to shift the subtree to the parent of the current edge...
							shift(newPredecessor, caseId);
							break;
					}
				} else {
					System.out.println("[WARNING] trying to alter root!");
				}
			}
			if (shouldRemoveCaseId(caseId)) {
				removeCaseIdRecursive(caseId, getTrie().getRootEdge());
				removePointersForCaseDueToRemoval(caseId);
			}
		}
	}

	private void merge(final IncrementalPayloadTrie.Edge<E> e1, final IncrementalPayloadTrie.Edge<E> e2) {
		// move  the children
		for (IncrementalPayloadTrie.Edge<E> c2 : e2.getChildren()) {
			e1.getChildren().add(c2);
			c2.setParent(e1);
		}
		// copy the payload
		e1.getPayload().getActiveCaseIdentifiers().addAll(e2.getPayload().getActiveCaseIdentifiers());
		e1.getPayload().getPayLoadCoverage().addAll(e2.getPayload().getPayLoadCoverage());
		for (String key : e2.getPayload().getAttributeKeys()) {
			e1.getPayload().getDataForKey(key).putAll(e2.getPayload().getDataForKey(key));
		}
		// remove all stuff related to e2
		e2.getPayload().clear();
		e2.setParent(null);
		e2.getChildren().clear();
		getTrie().remove(e2);
	}

	private void removeCaseIdRecursive(String caseId, IncrementalPayloadTrie.Edge<E> edge) {
		stripCaseIdFromPayload(caseId, edge);
		for (IncrementalPayloadTrie.Edge<E> child : edge.getChildren()) {
			if (child.getPayload().getActiveCaseIdentifiers().contains(caseId)
					|| child.getPayload().getPayLoadCoverage().contains(caseId)) {
				removeCaseIdRecursive(caseId, child);
				break;
			}
		}
	}

	private void removePointersForCaseDueToRemoval(String caseId) {
		fromEdges.remove(caseId);
		toEdges.remove(caseId);
		if (getParameters().isUseBlackList()) {
			addToBlacklist(caseId);
		}
	}

	private void sanityCheckAfterDrop(Collection<String> removed) {
		for (String caseId : removed) {
			for (IncrementalPayloadTrie.Edge<E> e : getTrie().getEdges()) {
				if (e.getPayload().getActiveCaseIdentifiers().contains(caseId)) {
					System.out.println("ERROR: active set contains case <" + caseId + "> on edge " + e
							+ " which should be removed.");
				}
				if (e.getPayload().getPayLoadCoverage().contains(caseId)) {
					System.out.println("ERROR: coverage set contains case <" + caseId + "> on edge " + e
							+ " which should be removed.");
				}
			}
		}
	}

	private void shift(IncrementalPayloadTrie.Edge<E> edge, String caseId) {
		edge.getParent().getChildren().remove(edge);
		fromEdges.put(caseId, getTrie().getRootEdge());
		Collection<String> affectedCases = new HashSet<>();
		affectedCases.add(caseId);
		findAndUpdateAllCasesPointingToShiftedEdge(edge, affectedCases);
		updateChildrenPointersOfShiftedEdge(edge, caseId);
		Collection<IncrementalPayloadTrie.Edge<E>> edges = new HashSet<>();
		for (Iterator<IncrementalPayloadTrie.Edge<E>> it = getTrie().getRootEdge().getChildren().iterator(); it
				.hasNext();) {
			IncrementalPayloadTrie.Edge<E> newEdge = it.next();
			for (IncrementalPayloadTrie.Edge<E> e : edges) {
				if (e.getPayload().getActivity().equals(newEdge.getPayload().getActivity())) {
					merge(e, newEdge);
				}
			}
		}
		// cleanup		
		edge.setParent(null);
		edge.getChildren().clear();
		edge.getPayload().clear();
		getTrie().remove(edge);
	}

	private boolean shouldRemoveCaseId(String caseId) {
		boolean remove = true;
		remove &= toEdges.containsKey(caseId) && fromEdges.containsKey(caseId);
		if (remove) {
			remove &= (toEdges.get(caseId).equals(fromEdges.get(caseId)) || (toEdges.get(caseId).getParent() == null
					&& !(toEdges.get(caseId).equals(getTrie().getRootEdge()))));
		}
		return remove;
	}

	private void stripCaseIdFromPayload(final String caseId, final IncrementalPayloadTrie.Edge<E> edge) {
		edge.getPayload().getActiveCaseIdentifiers().remove(caseId);
		edge.getPayload().getPayLoadCoverage().remove(caseId);
		for (String key : edge.getPayload().getAttributeKeys()) {
			for (Iterator<Map.Entry<String, Collection<String>>> it = edge.getPayload().getDataForKey(key).entrySet()
					.iterator(); it.hasNext();) {
				Map.Entry<String, Collection<String>> entry = it.next();
				entry.getValue().remove(caseId);
				if (entry.getValue().isEmpty()) {
					it.remove();
				}
			}
		}
	}

	private void updateChildrenPointersOfShiftedEdge(final IncrementalPayloadTrie.Edge<E> edge, final String caseId) {
		if (!edge.getChildren().isEmpty()) {
			for (IncrementalPayloadTrie.Edge<E> e : edge.getChildren()) {
				getTrie().getRootEdge().getChildren().add(e);
				e.setParent(getTrie().getRootEdge());
				e.setDepth(getTrie().getRootEdge().getDepth() + 1);
			}
		} else {
			removePointersForCaseDueToRemoval(caseId);
		}
	}

}
