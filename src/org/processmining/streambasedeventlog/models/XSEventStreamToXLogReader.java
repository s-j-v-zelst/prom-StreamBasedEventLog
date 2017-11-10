package org.processmining.streambasedeventlog.models;

import org.deckfour.xes.model.XLog;
import org.processmining.streambasedeventlog.parameters.StreamBasedEventStorageParametersImpl;

public interface XSEventStreamToXLogReader<P extends StreamBasedEventStorageParametersImpl>
		extends XSEventStreamBasedEventStore<XLog, P> {

	/**
	 * captures the actual memory consumption of the structure
	 * 
	 * @return
	 */
	long getNumberOfMemoryEntriesRepresentingEvents();

	/**
	 * captures the number of events actually described
	 * 
	 * @return
	 */
	long getTotalNumberOfEventsDescribedByMemory();

	/**
	 * fetches all stored metadata in the structure.
	 * 
	 * @return
	 */
	long getTotalPayloadMemoryOccupation();

}
