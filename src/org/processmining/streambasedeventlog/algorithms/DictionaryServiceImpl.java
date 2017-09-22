package org.processmining.streambasedeventlog.algorithms;

import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.TObjectLongMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.map.hash.TObjectLongHashMap;

public class DictionaryServiceImpl {

	private final TObjectLongMap<Object>[] dictionaries;
	private final TLongObjectMap<Object>[] inverse;

	private final Object lock = new Object();

	private final long reserved = -1l;

	@SuppressWarnings("unchecked")
	public DictionaryServiceImpl(final int numDictionaries) {
		dictionaries = new TObjectLongMap[numDictionaries];
		inverse = new TLongObjectMap[numDictionaries];
		for (int i = 0; i < numDictionaries; i++) {
			dictionaries[i] = new TObjectLongHashMap<Object>();
			inverse[i] = new TLongObjectHashMap<Object>();
		}
	}

	public boolean contains(final int dict, final Object o) {
		synchronized (getLock()) {
			return dictionaries[dict].containsKey(o);
		}

	}

	public long getReservedValue() {
		return reserved;
	}

	public Object getLock() {
		return lock;
	}

	public long translate(final int dict, final Object o) {
		synchronized (getLock()) {
			if (dictionaries[dict].containsKey(o)) {
				return dictionaries[dict].get(o);
			} else {
				long val = dictionaries[dict].size();
				dictionaries[dict].put(o, val);
				inverse[dict].put(val, o);
				return val;
			}
		}
	}

	public Object inverse(final int dict, final long code) {
		synchronized (getLock()) {
			if (!inverse[dict].containsKey(code)) {
				return null;
			} else {
				return inverse[dict].get(code);
			}
		}
	}

}
