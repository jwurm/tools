package de.jenswurm.tools;
/**
 * Author: Jens Wurm
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.jenswurm.tools.JaQL.JoinIndexer;
import de.jenswurm.tools.JaQL.Tuple;

/**
 * 
 * 
 * @author Jens Wurm
 * 
 *         Description: 
 *         Helper class that provides SQL-like query and ordering functionality on lists or sets of objects, useful for complex algorithms.
 */
public class JaQL {

    /**
     * Groups the objects in a list by a joinValue that is derived from each object. Those with an identical joinValue will end up in the same list, indexed by that joinValue, in the returned map.
     * 
     * @param <T> Type of Objects in the list of objects
     * @param <I> Type of the Index to be used
     * @param objects List of objects
     * @param indexer anonymous implementation of the class that reads the indices from each object
     * @return Map that contains the objects indexed by the joinValue that the indexer returned for each of them.
     */
    public static <T, I> Map<I, List<T>> groupBy(List<T> objects, Indexer<T, I> indexer) {
        Map<I, List<T>> ret = new HashMap<I, List<T>>();
        for (T curr : objects) {
            try {
            I key = indexer.getIndex(curr);
            List<T> list = ret.get(key);
            if (list == null) {
                list = new ArrayList<T>();
                ret.put(key, list);
            }
            list.add(curr);
            } catch (Throwable e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
        return ret;
    }
       
    

    /**
     * 
     * 
     * @author Jens Wurm
     * 
     *         Beschreibung: 
     *         Interface of the indexer that reads a joinValue from an object.
     */
    public interface Indexer<T, I> {
        /**
         * @param object
         * @return The index value of the object
         */
        public I getIndex(T object);
    }

    /**
     * 
     * 
     * @author Jens Wurm
     * 
     *         Beschreibung: 
     *         Interface of the indexer that reads a joinValue from an object.
     */
    public interface JoinIndexer<T, S, I> {
        /**
         * @param object from the left list
         * @return The join column value of the object
         */
        public I joinOnLeft(T object);

        /**
         * @param object from the right list
         * @return The join column value of the object
         */
        public I joinOnRight(S object);
    }

    /**
     * Performs an SQL-like inner join on two lists of objects.
     * @param <T> Type of the objects in left list
     * @param <S> Type of the objects in the right list
     * @param <I> Common type of the attribute based on which the lists shall be joined
     * @param leftObjects List of left objects
     * @param rightObjects List of right objects
     * @param indexer a JoinIndexer implementation that can read the join column on each list of the objects.
     * @return List of Tuples that contain the joined values.
     */
    public static <T, S, I> List<Tuple<T, S, I>> innerJoin(List<T> leftObjects, List<S> rightObjects,
            final JoinIndexer<T, S, I> indexer) {
        ArrayList<Tuple<T, S, I>> ret = new ArrayList<Tuple<T, S, I>>();
        Map<I, List<S>> rightMap = groupBy(rightObjects, new Indexer<S, I>() {
            @Override
            public I getIndex(S object) {
                return indexer.joinOnRight(object);
            }
        });
        Map<I, List<T>> leftMap = groupBy(leftObjects, new Indexer<T, I>() {
            @Override
            public I getIndex(T object) {
                return indexer.joinOnLeft(object);
            }
        });

        for (Entry<I, List<T>> curr : leftMap.entrySet()) {
            List<S> list = rightMap.get(curr.getKey());
            if (list != null) {
                for (T currT : curr.getValue()) {
                    for (S currS : list) {
                        ret.add(new Tuple<T, S, I>(currT, currS, curr.getKey()));
                    }
                }
            }
        }

        return ret;
    }

    public static class Tuple<T, S, I> {
        private T left;
        private S right;
        private I joinValue;

        private Tuple(T left, S right, I key) {
            this.left = left;
            this.right = right;
            this.joinValue = key;
        }

        /**
         * @return the joinValue
         */
        public I getJoinValue() {
            return joinValue;
        }

        /**
         * @return the left
         */
        public T getLeft() {
            return left;
        }

        /**
         * @return the right
         */
        public S getRight() {
            return right;
        }
    }

	/**
	 * Performs an SQL-like left join on two lists of objects.
	 * 
	 * @param <T>
	 *            Type of the objects in left list
	 * @param <S>
	 *            Type of the objects in the right list
	 * @param <I>
	 *            Common type of the attribute based on which the lists shall be
	 *            joined
	 * @param leftObjects
	 *            List of left objects
	 * @param rightObjects
	 *            List of right objects
	 * @param indexer
	 *            a JoinIndexer implementation that can read the join column on
	 *            each list of the objects.
	 * @return List of Tuples that contain the joined values.
	 */
	public static <T, S, I> List<Tuple<T, S, I>> leftJoin(List<T> leftObjects,
			List<S> rightObjects, final JoinIndexer<T, S, I> indexer) {
		ArrayList<Tuple<T, S, I>> ret = new ArrayList<Tuple<T, S, I>>();
		Map<I, List<S>> rightMap = groupBy(rightObjects, new Indexer<S, I>() {
			@Override
			public I getIndex(S object) {
				return indexer.joinOnRight(object);
			}
		});
		Map<I, List<T>> leftMap = groupBy(leftObjects, new Indexer<T, I>() {
			@Override
			public I getIndex(T object) {
				return indexer.joinOnLeft(object);
			}
		});

		for (Entry<I, List<T>> curr : leftMap.entrySet()) {
			List<S> list = rightMap.get(curr.getKey());
			if (list != null) {
				for (T currT : curr.getValue()) {
					for (S currS : list) {
						ret.add(new Tuple<T, S, I>(currT, currS, curr.getKey()));
					}
				}
			} else {
				for (T currT : curr.getValue()) {
					ret.add(new Tuple<T, S, I>(currT, null, curr.getKey()));
				}
			}
		}

		return ret;
	}
}
