package io.schark.pony.utils;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multiset;
import com.google.common.collect.SetMultimap;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * @author Player_Schark
 */
public class PonyMulti<L, R> {
	private final SetMultimap<L, R> left = HashMultimap.create();
	private final SetMultimap<R, L> right = HashMultimap.create();

	public Set<R> getRight(L left) {
		return this.left.get(left);
	}

	public Set<L> getLeft(R right) {
		return this.right.get(right);
	}

	public boolean put(L left, R right) {
		return this.left.put(left, right) && this.right.put(right, left);
	}

	public boolean putAll(L key, Iterable<? extends R> values) {
		boolean changed = false;
		for (R value : values) {
			changed |= this.put(key, value);
		}
		return changed;
	}

	public Multiset<L> getAllLeft() {
		return this.left.keys();
	}

	public Multiset<R> getAllRight() {
		return this.right.keys();
	}

	public <V extends L, K extends R> V getLeftValue(K leftKey, Class<V> out) {
		Set<L> left = this.getLeft(leftKey);
		return this.get(left, out);
	}

	public <V extends R, K extends L> V getRightValue(K rightKey, Class<V> out) {
		Set<R> right = this.getRight(rightKey);
		return this.get(right, out);
	}

	@Nullable private <K, V> V get(Set<K> keys, Class<V> out) {
		for (K key : keys) {
			if (out.isAssignableFrom(key.getClass())) {
				return out.cast(key); //cast to V
			}
		}
		return null;
	}
}
