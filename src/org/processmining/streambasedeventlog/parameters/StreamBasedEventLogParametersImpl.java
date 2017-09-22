package org.processmining.streambasedeventlog.parameters;

import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryRegistry;

public class StreamBasedEventLogParametersImpl extends StreamBasedEventStorageParametersImpl {

	private XFactory xFactory = XFactoryRegistry.instance().currentDefault();

	public XFactory getXFactory() {
		return xFactory;
	}

	public void setxFactory(XFactory xFactory) {
		this.xFactory = xFactory;
	}
	
	

}
