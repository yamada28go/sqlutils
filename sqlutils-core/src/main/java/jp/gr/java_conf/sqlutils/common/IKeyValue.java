package jp.gr.java_conf.sqlutils.common;

public interface IKeyValue<T> {
	String getKey();
	T getValue();
}
