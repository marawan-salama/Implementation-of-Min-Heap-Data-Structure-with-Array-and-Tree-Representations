
package edu.uwm.cs351;

import java.util.NoSuchElementException;
import java.util.function.Consumer;

/**
 * A dynamic-array implementation of the CompleteTree interface.
 */
public class ArrayCompleteTree<E> implements CompleteTree<E> {
	private static final int INITIAL_CAPACITY = 10;

	private E[] data;
	private int manyItems;

	private static Consumer<String> reporter = (s) -> {
		System.err.println("Invariant error: " + s);
	};

	private boolean report(String error) {
		reporter.accept(error);
		return false;
	}

	private boolean wellFormed() {
		if (data == null)
			return report("array is null");
		if (manyItems < 0)
			return report("manyItems is negative");
		if (manyItems > data.length)
			return report("manyItems is too many: " + manyItems + " > capacity = " + data.length);
		return true;
	}

	/**
	 * Create a new array of the element type.
	 * 
	 * @param cap number of elements to create array
	 * @return array of the required size (that pretends to be of the required type
	 *         -- do not let this array escape the scope of this class).
	 */
	@SuppressWarnings("unchecked")
	private E[] makeArray(int cap) {
		return (E[]) new Object[cap];
	}

	/**
	 * Ensure that the underlying array has at least the given capacity. If it's
	 * necessary to allocate an array, we allocate one that is at least twice as
	 * long.
	 * 
	 * @param minimumCapacity minimum number of elements needed
	 */
	private void ensureCapacity(int minimumCapacity) {
		if (data.length >= minimumCapacity)
			return;
		int newCap = data.length * 2;
		if (newCap < minimumCapacity)
			newCap = minimumCapacity;
		E[] newData = makeArray(newCap);
		for (int i = 0; i < manyItems; ++i) {
			newData[i] = data[i];
		}
		data = newData;
	}

	/**
	 * Create an empty complete tree.
	 */
	public ArrayCompleteTree() {
		data = makeArray(INITIAL_CAPACITY);
		assert wellFormed() : "invariant broken by constructor";
	}

	private /* non-static */ class Location implements CompleteTree.Location<E> {
		private final int index; // 1-based index into tree

		Location(int index) {
			if (index < 1)
				throw new IllegalArgumentException("cannot use a negative index");
			this.index = index;
		}

		@Override // implementation
		public String toString() {
			return "Location(" + index + ")";
		}

		@Override // implementation
		public int hashCode() {
			return index;
		}

		@Override // implementation
		public boolean equals(Object obj) {
			if (!(obj instanceof ArrayCompleteTree<?>.Location))
				return false;
			ArrayCompleteTree<?>.Location loc = (ArrayCompleteTree<?>.Location) obj;
			return loc.index == index;
		}

		@Override // required
		public E get() {
			if (index > manyItems)
				throw new NoSuchElementException("location is outside the tree");
			return data[index - 1];
		}

		@Override // required
		public void set(E val) {
			if (index > manyItems)
				throw new NoSuchElementException("location is outside the tree");
			data[index - 1] = val;
		}

		@Override // required
		public ArrayCompleteTree<E>.Location parent() {
			if (index == 1)
				return null;// throw new NoSuchElementException("root has no parent");
			return new Location(index / 2);
		}

		@Override // required
		public ArrayCompleteTree<E>.Location child(boolean right) {
			int x = index * 2;
			if (x == 0)
				return null;
			if (right == true)
				x++;
			if (x > manyItems)
				return null;
			return new Location(x);
		}

	}

	@Override // required
	public int size() {
		return manyItems;
	}

	@Override // required
	public ArrayCompleteTree<E>.Location root() {
		if (manyItems == 0)
			return null;
		return new Location(1);
	}

	@Override // required
	public ArrayCompleteTree<E>.Location last() {
		if (manyItems == 0)
			return null;
		return new Location(manyItems);
	}

	@Override // required
	public ArrayCompleteTree<E>.Location add(E value) {
		ensureCapacity(manyItems + 1);
		data[manyItems] = value;
		++manyItems;
		assert wellFormed() : "invariant broken by add";
		return new Location(manyItems);
	}

	@Override // required
	public E remove() {
		assert wellFormed() : "invariant broken by remove";
		if (manyItems == 0)
			throw new NoSuchElementException("tree is empty");
		--manyItems;
		E x = data[manyItems];
		assert wellFormed() : "invariant broken by remove";
		return x;
	}

}
