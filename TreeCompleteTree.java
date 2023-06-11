
package edu.uwm.cs351;

import java.util.NoSuchElementException;
import java.util.function.Consumer;

import edu.uwm.cs.util.Pair;
import edu.uwm.cs.util.PowersOfTwo;

public class TreeCompleteTree<E> implements CompleteTree<E> {
	private static class Node<T> implements Location<T> {
		Node<T> parent, left, right;
		T data;

		Node(T data) {
			this.data = data;
		}

		@Override // required
		public T get() {
			return data;
		}

		@Override // required
		public void set(T val) {
			data = val;
		}

		@Override // required
		public Location<T> parent() {
			return parent;
		}

		@Override // required
		public Location<T> child(boolean right) {
			return right ? this.right : left;
		}
	}

	private Node<E> root;
	private int manyNodes;
	// NO MORE FIELDS!

	private static Consumer<String> reporter = (s) -> {
		System.err.println("Invariant error: " + s);
	};

	private boolean report(String error) {
		reporter.accept(error);
		return false;
	}

	private boolean checkSubtree(Node<E> r, Node<E> p, int size) {
		if (r == null) {
			if (size == 0)
				return true;
			return report("found null tree of supposed size " + size);
		}
		if (r.parent != p)
			return report("Found bad parent on node with " + r.data);
		if (size <= 0)
			return report("a non-empty subtree cannot have size = " + size);
		int power = PowersOfTwo.next(size / 2);
		int prev = power / 2;
		int left = (power + prev > size) ? (size - prev) : power - 1;
		return checkSubtree(r.left, r, left) && checkSubtree(r.right, r, size - left - 1);
	}

	private boolean wellFormed() {
		return checkSubtree(root, null, manyNodes);
	}

	/**
	 * Create an empty complete tree.
	 */
	public TreeCompleteTree() {
		root = null;
		manyNodes = 0;
		assert wellFormed() : "invariant broken by constructor";
	}

	/**
	 * Locate a node in the tree using the algorithm explained in Activity 14. It
	 * will start with the root and then go left or right at each step depending on
	 * what the algorithm says. It will return the "lag" (parent) pointer too, which
	 * will make it easier to handle additions and removals.
	 * <p>
	 * If the node is not in the tree yet, the node may be null. If the node is at
	 * the root, the parent may be null.
	 * <p>
	 * This is useful for accessing the last element, including before we add or
	 * remove it.
	 * 
	 * @param n number of node to locate, one-based. It must be positive and must be
	 *          no more than one more than manyNodes.
	 * @return two nodes, either of which could be null, the first is the node
	 *         itself, and the second is its parent
	 */
	private Pair<Node<E>, Node<E>> find(int n) {
		if (n <= 0 || n > manyNodes + 1)
			throw new IllegalArgumentException("bad index " + n);
		Node<E> node = root;
		Node<E> parent = null;
		int power = PowersOfTwo.next(n);
		int i = (int) (Math.log(power) / Math.log(2));
		for (int l = i - 2; l >= 0; l--) {
			parent = node;
			if ((n & (1 << l)) == 0) {
				node = node.left;
			} else {
				node = node.right;
			}
		}
		return new Pair<>(node, parent);
	}

	public static class Spy {
		public static <E> Pair<Location<E>, Location<E>> find(CompleteTree<E> tree, int n) {
			Pair<Node<E>, Node<E>> pair = ((TreeCompleteTree<E>) tree).find(n);
			return new Pair<>(pair.fst(), pair.snd());
		}
	}

	@Override // required
	public int size() {
		assert wellFormed() : "invariant broken before size";
		return manyNodes;
	}

	@Override // required
	public Location<E> root() {
		assert wellFormed() : "invariant broken before root";
		return root;
	}

	@Override // required
	public Location<E> last() {
		assert wellFormed() : "invariant broken before last";
		if (manyNodes == 0) {
			return null;
		}
		return find(manyNodes).fst();
	}

	@Override // required
	public Location<E> add(E value) {
		assert wellFormed() : "invariant broken before add";
		Node<E> newNode = new Node<>(value);
		if (manyNodes == 0) {
			root = newNode;
		} else {
			Pair<Node<E>, Node<E>> pair = find(manyNodes + 1);
			Node<E> parent = pair.snd();
			if (manyNodes % 2 != 0) {
				parent.left = newNode;
			} else {
				parent.right = newNode;
			}
			newNode.parent = parent;
		}
		manyNodes++;
		assert wellFormed() : "invariant broken after add";
		return newNode;
	}

	@Override // required
	public E remove() {
		assert wellFormed() : "invariant broken by remove";
		if (manyNodes == 0)
			throw new NoSuchElementException("tree is empty");
		Pair<Node<E>, Node<E>> pair = find(manyNodes);
		E result = pair.fst().data;
		if (pair.fst().parent == null) {
			root = null;
		} else {
			if (pair.fst().parent.left == pair.fst())
				pair.fst().parent.left = null;
			else
				pair.fst().parent.right = null;
		}
		manyNodes--;
		assert wellFormed() : "invariant broken by remove";
		return result;
	}

}
