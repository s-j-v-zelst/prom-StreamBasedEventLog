package org.processmining.streambasedeventlog.models;

import org.processmining.eventstream.core.interfaces.XSEvent;
import org.processmining.stream.core.interfaces.XSReader;
import org.processmining.streambasedeventlog.parameters.StreamBasedEventStorageParametersImpl;

public interface XSEventStreamBasedEventStore<T, P extends StreamBasedEventStorageParametersImpl>
		extends XSReader<XSEvent, T> {

	P getStorageParameters();

}
