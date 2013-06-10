package jp.gr.java_conf.sqlutils.common;

import java.util.Iterator;
import java.util.List;

public abstract class ListWrapIterator<B,T> implements Iterator<T> {

	protected abstract T value(B object);


	private Iterator<B> it;

	public ListWrapIterator(List<B> list) {
		this.it = list.iterator();
	}

	public boolean hasNext() {
		return it.hasNext();
	}

	public T next() {
		return value(it.next());
	}

	public void remove() {
		it.remove();
	}
}
