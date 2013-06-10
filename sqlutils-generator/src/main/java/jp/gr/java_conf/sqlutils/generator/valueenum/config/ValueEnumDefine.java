package jp.gr.java_conf.sqlutils.generator.valueenum.config;

import java.util.ArrayList;
import java.util.List;

import jp.gr.java_conf.sqlutils.common.KeyValuePair;

public class ValueEnumDefine {

	public String enumName;
	public List<KeyValuePair<Object>> elems = new ArrayList<KeyValuePair<Object>>();

	public ValueEnumDefine(String enumName) {
		this.enumName = enumName;
	}

	public void add(String elemName, Object elemVal) {
//		String name = elemName.toUpperCase();
//		if (WordChecker.startWithUnavailableChar(name)) {
//			name = "_" + name;
//		}
		elems.add(new KeyValuePair<Object>(elemName, elemVal));
	}

	public String getName() {
		return enumName;
	}

	public List<KeyValuePair<Object>> getElems() {
		return elems;
	}
}
