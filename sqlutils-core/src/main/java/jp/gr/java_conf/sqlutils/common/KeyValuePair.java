package jp.gr.java_conf.sqlutils.common;

import java.io.Serializable;

public class KeyValuePair<T> implements IKeyValue<T>, Serializable {

	private static final long serialVersionUID = 1L;


	private String key;
	private T value;

	public KeyValuePair(String key, T value) {
		if (key == null)
			throw new RuntimeException("Key is required.");
		this.key = key;
		this.value = value;
	}

	public String getKey() {
		return key;
	}

	public T getValue() {
		return value;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof KeyValuePair))
			return false;

		KeyValuePair<?> kvp = (KeyValuePair<?>)obj;
		if (!this.getKey().equals(kvp.getKey()))
			return false;

		Object v = kvp.getValue();
		if (v == null && this.value == null)
			return true;
		if (v != null && this.value != null)
			if (v.getClass() == this.value.getClass()
			&& v.equals(value))
				return true;

		return false;
	}
}
