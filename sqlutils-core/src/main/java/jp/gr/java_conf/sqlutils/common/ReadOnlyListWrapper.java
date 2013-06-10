package jp.gr.java_conf.sqlutils.common;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public abstract class ReadOnlyListWrapper<E,B> implements List<E> {


	private List<B> list;
	private Iterator<E> it;

	public ReadOnlyListWrapper(List<B> list) {
		this.list = list;
		this.it = new ListWrapIterator<B, E>(list) {
			@Override
			protected E value(B object) {
				return ReadOnlyListWrapper.this.value(object);
			}
		};
	}

	protected abstract E value(B object);


	public boolean add(E o) {
		throw new RuntimeException();
	}

	public void add(int index, E element) {
		throw new RuntimeException();
	}

	public boolean addAll(Collection<? extends E> c) {
		throw new RuntimeException();
	}

	public boolean addAll(int index, Collection<? extends E> c) {
		throw new RuntimeException();
	}

	public void clear() {
		list.clear();
	}

	public boolean contains(Object o) {
		throw new RuntimeException();
	}

	public boolean containsAll(Collection<?> c) {
		throw new RuntimeException();
	}

	public E get(int index) {
		return value(list.get(index));
	}

	public int indexOf(Object o) {
		throw new RuntimeException();
	}

	public boolean isEmpty() {
		return list.isEmpty();
	}

	public Iterator<E> iterator() {
		return it;
	}

	public int lastIndexOf(Object o) {
		throw new RuntimeException();
	}

	public ListIterator<E> listIterator() {
		throw new RuntimeException();
	}

	public ListIterator<E> listIterator(int index) {
		throw new RuntimeException();
	}

	public boolean remove(Object o) {
		throw new RuntimeException();
	}

	public E remove(int index) {
		throw new RuntimeException();
	}

	public boolean removeAll(Collection<?> c) {
		throw new RuntimeException();
	}

	public boolean retainAll(Collection<?> c) {
		throw new RuntimeException();
	}

	public E set(int index, E element) {
		throw new RuntimeException();
	}

	public int size() {
		return list.size();
	}

	public List<E> subList(int fromIndex, int toIndex) {
		throw new RuntimeException();
	}

	public Object[] toArray() {
		throw new RuntimeException();
	}

	public <T> T[] toArray(T[] a) {
		throw new RuntimeException();
	}
}
