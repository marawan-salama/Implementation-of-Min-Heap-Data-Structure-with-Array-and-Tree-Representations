
package edu.uwm.cs351;

import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

/**
 * An implementation of a min-heap that is agnostic on the tree implementation
 * technique, and use any comparator of the values.
 * 
 * @param E type of elements on the heap.
 */
public class MinHeap<E> {
	private CompleteTree<E> tree;
	private Comparator<E> comparator;

	private static Consumer<String> reporter = (s) -> {
		System.err.println("Invariant error: " + s);
	};

	private boolean report(String error) {
		reporter.accept(error);
		return false;
	}

	/**
	 * Check the subtree that all elements are within the given bound, and that the
	 * elements form a min-heap
	 * 
	 * @param l     subtree to check, may be null
	 * @param bound inclusive lower bound
	 * @return if a problem is found, in which case it has been reported
	 */
	private boolean checkSubtree(CompleteTree.Location<E> l, E bound) {
		if (l == null)
			return true;
		if (bound != null && comparator.compare(l.get(), bound) < 0) {
			return report("element is smaller than bound");
		}
		if (l.left() != null && !checkSubtree(l.left(), l.get())) {
			return false;
		}
		if (l.right() != null && !checkSubtree(l.right(), l.get())) {
			return false;
		}
		return true;
	}

	private boolean wellFormed() {
		if (tree == null || comparator == null) {
			return report("null fields");
		}
		if (tree.size() < 0) {
			return report("negative size");
		}
		return checkSubtree(tree.root(), null);
	}

	/**
	 * Create a min-heap with the given tree (which must not be null) and comparator
	 * (if null, using natural ordering)
	 * 
	 * @param tree       complete tree implementation, must be empty
	 * @param comparator ordering to use, many be null (natural ordering)
	 */
	@SuppressWarnings("unchecked")
	public MinHeap(CompleteTree<E> tree, Comparator<E> comparator) {
		if (!tree.isEmpty())
			throw new IllegalArgumentException("tree must be empty");
		if (comparator == null) {
			comparator = (o1, o2) -> ((Comparable<E>) o1).compareTo(o2);
			;
		}
		this.tree = tree;
		this.comparator = comparator;
		assert wellFormed() : "invariant broken in constructor";
	}

	private MinHeap(boolean ignored) {
	} // do not change this constructor

	/**
	 * Return the number of elements in the heap.
	 */
	public int size() {
		assert wellFormed() : "invariant broken in size";
		return tree.size();
	}

	/**
	 * Add a new value to the min heap.
	 * 
	 * @param value to add, must be acceptable to the comparator (usually not null)
	 */
	public void add(E value) {
		assert wellFormed() : "invariant broken in add";
		CompleteTree.Location<E> loc = tree.add(value);
		CompleteTree.Location<E> parent = loc.parent();
		while (parent != null && comparator.compare(loc.get(), parent.get()) < 0) {
			E temp = parent.get();
			parent.set(loc.get());
			loc.set(temp);
			loc = parent;
			parent = loc.parent();
		}
		assert wellFormed() : "invariant broken by add";
	}

	/**
	 * Return the minimum value in the min-heap. This is a constant-time operation.
	 * 
	 * @return minimum element
	 * @exception NoSuchElementException if the heap is empty.
	 */
	public E min() {
		assert wellFormed() : "invariant broken in min";
		if (tree.isEmpty())
			throw new NoSuchElementException();
		return tree.root().get();
	}

	/**
	 * Remove and return the minimum value from this heap.
	 * 
	 * @return the former minimum value.
	 * @exception NoSuchElementException if the heap is empty.
	 */
	public E removeMin() {
		assert wellFormed() : "invariant broken in removeMin";
		if (tree.isEmpty()) {
			throw new NoSuchElementException("min-heap is empty");
		}
		E temp = tree.root().get();
		if (tree.size() == 1) {
			tree.remove();
			return temp;
		}
		CompleteTree.Location<E> loc1 = tree.last();
		tree.root().set(loc1.get());
		tree.remove();
		CompleteTree.Location<E> loc2 = tree.root();
		while (true) {
			CompleteTree.Location<E> left = loc2.left();
			CompleteTree.Location<E> right = loc2.right();
			E value = loc2.get();
			if (left == null && right == null) {
				break;
			}
			if (right == null || comparator.compare(left.get(), right.get()) <= 0) {
				if (comparator.compare(left.get(), value) < 0) {
					loc2.set(left.get());
					left.set(value);
					loc2 = left;
				} else {
					break;
				}
			} else {
				if (comparator.compare(right.get(), value) < 0) {
					loc2.set(right.get());
					right.set(value);
					loc2 = right;
				} else {
					break;
				}
			}
		}
		assert wellFormed() : "invariant broken in removeMin";
		return temp;
	}

	public static class Spy {
		/**
		 * Return the sink for invariant error messages
		 * 
		 * @return
		 */
		public Consumer<String> getReporter() {
			return reporter;
		}

		/**
		 * Change the sink for invariant error messages.
		 * 
		 * @param r where to send invariant error messages.
		 */
		public void setReporter(Consumer<String> r) {
			reporter = r;
		}

		/**
		 * Create a min heap with the given data structure. This method does not check
		 * the invariant. nor does it use the regular constructor that checks the
		 * parameters.
		 * 
		 * @param tree complete tree implementation
		 * @param comp comparator to use
		 * @return new main heap object.
		 */
		public <T> MinHeap<T> createMinHeap(CompleteTree<T> tree, Comparator<T> comp) {
			MinHeap<T> result = new MinHeap<>(false);
			result.comparator = comp;
			result.tree = tree;
			return result;
		}

		/**
		 * Return the results of running the invariant checker on this object. Errors
		 * will likely be reported to the error reporter.
		 * 
		 * @param h min heap to check
		 * @return whether the invariant evaluates to true
		 */
		public boolean wellFormed(MinHeap<?> h) {
			return h.wellFormed();
		}
	}

}
