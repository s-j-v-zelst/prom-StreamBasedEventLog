package org.processmining.streambasedeventlog.models.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.processmining.streambasedeventlog.models.EventPayload;
import org.processmining.streambasedeventlog.models.PayLoadImpl;
import org.processmining.streambasedeventlog.models.Payload;

public class EventPayloadImpl implements EventPayload {

	private String activity;
	private final Collection<String> caseIds = new HashSet<>();
	private final Collection<String> payloadCoverage = new HashSet<>();
	private final Map<String, Payload> payloads = new HashMap<>();

	public EventPayloadImpl(String activity) {
		this.activity = activity;
	}

	public String getActivity() {
		return activity;
	}

	public Payload getDataForKey(String attribute) {
		if (!payloads.containsKey(attribute)) {
			payloads.put(attribute, new PayLoadImpl());
		}
		return payloads.get(attribute);
	}

	public Collection<String> getAttributeKeys() {
		return payloads.keySet();
	}

	public Collection<String> getActiveCaseIdentifiers() {
		return caseIds;
	}

	public Collection<String> getPayLoadCoverage() {
		return payloadCoverage;
	}

	public void clear() {
		this.activity = null;
		getActiveCaseIdentifiers().clear();
		getPayLoadCoverage().clear();
		for (String key : payloads.keySet()) {
			payloads.get(key).clear();
		}

	}

}
