package org.processmining.streambasedeventlog.parameters;

import org.processmining.eventstream.readers.abstr.XSEventReaderParameters;

public class StreamBasedEventStorageParametersImpl extends XSEventReaderParameters {

	public enum StorageStrategy {
		NAIVE, TRIE;
	}

	public enum TraceRemovalStrategy {
		DROP, SHIFT;
	}

	private int blackListCapacity = 50;

	private int slidingWindowSize = 10000;

	private StorageStrategy storageStrategy = StorageStrategy.NAIVE;

	private TraceRemovalStrategy traceRemovalStrategy = TraceRemovalStrategy.SHIFT;

	private boolean useBlackList = false;

	public int getBlackListCapacity() {
		return blackListCapacity;
	}

	public int getSlidingWindowSize() {
		return slidingWindowSize;
	}

	public StorageStrategy getStorageStrategy() {
		return storageStrategy;
	}

	public TraceRemovalStrategy getTraceRemovalStrategy() {
		return traceRemovalStrategy;
	}

	public boolean isUseBlackList() {
		return useBlackList;
	}

	public void setBlackListCapacity(int blackListCapacity) {
		this.blackListCapacity = blackListCapacity;
	}

	public void setSlidingWindowSize(int slidingWindowSize) {
		this.slidingWindowSize = slidingWindowSize;
	}

	public void setStorageStrategy(StorageStrategy storageStrategy) {
		this.storageStrategy = storageStrategy;
	}

	public void setTraceRemovalStrategy(TraceRemovalStrategy traceRemovalStrategy) {
		this.traceRemovalStrategy = traceRemovalStrategy;
	}

	public void setUseBlackList(boolean useBlackList) {
		this.useBlackList = useBlackList;
	}
}
