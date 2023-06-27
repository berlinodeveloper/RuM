package org.processmining.plugins.declareminer.data;

import java.util.Collection;
import java.util.HashMap;

import org.processmining.framework.util.Pair;

/**
 * This data structure is used to manage the different replayer.
 * 
 * This data structure is composed of:
 * <ul>
 * 	<li>case-id;</li>
 * 	<li>replayers data structure + frequency of the case id + current
 * 		bucket.</li>
 * </ul>
 * 
 * @author Andrea Burattin
 */
public class Counting<T> extends HashMap<String, Pair<T, Integer>> {
	
	private static final long serialVersionUID = -973856497040719491L;
	private int size = 0;

	/**
	 * 
	 * @param caseId
	 * @param currentBucket
	 * @param inCaseOfNull
	 */
	public void addObservation(String caseId, T inCaseOfNull) {
		if (containsKey(caseId)) {
			Pair<T, Integer> v = get(caseId);
			put(caseId, new Pair<T, Integer>(v.getFirst(), v.getSecond() + 1));
		} else {
			put(caseId, new Pair<T, Integer>(inCaseOfNull, 1));
			size++;
		}
	}

	/**
	 * 
	 * @param caseId
	 * @param currentBucket
	 * @param inCaseOfNull
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	@SuppressWarnings("unchecked")
	public void addObservation(String caseId, Class<?> inCaseOfNull) throws InstantiationException, IllegalAccessException {
		addObservation(caseId, (T) inCaseOfNull.newInstance());
	}
	
	/**
	 * 
	 * @param caseId
	 * @return
	 */
	public T getItem(String caseId) {
		return get(caseId).getFirst();
	}
	
	/**
	 * 
	 * @param caseId
	 * @param item
	 * @param currentBucket
	 */
	public void putItem(String caseId, T item) {
		Pair<T, Integer> v = get(caseId);
		if(v==null){
			put(caseId, new Pair<T, Integer>(item, 1));
		}else{
		put(caseId, new Pair<T, Integer>(item, v.getSecond()));
		}
//		put(caseId, new Triple<HashMap<String, HashMap<String, Integer>>, Integer, Integer>(trace, 1, currentBucket - 1));
	}
	
	/**
	 * 
	 * @return
	 */
//	public Integer getSize() {
//		return size;
//	}
	public Integer getSize() {
		int tmp = super.size();
		if (tmp > 0) {
			for (Pair<T, Integer> i : values()) {
				T item = i.getFirst();
				if (item instanceof Collection<?>) {
					tmp += ((Collection<?>) item).size();
				}
			}
		}
		return tmp;
	}
}
