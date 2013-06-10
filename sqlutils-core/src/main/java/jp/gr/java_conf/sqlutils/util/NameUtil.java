package jp.gr.java_conf.sqlutils.util;

import org.apache.commons.lang.StringUtils;

public class NameUtil {

	public static String Column2DtoField(String colName) {
		return StringUtils.uncapitalize(camelize(colName));
	}

	public static String Table2DtoClass(String tblName) {
		return camelize(tblName);
	}

	public static String DtoField2Column(String fieldName) {
		return reverseCamelize(StringUtils.capitalize(fieldName));
	}

	public static String DtoClass2Table(String className) {
		return reverseCamelize(className);
	}

	// AAA_BBB -> AaaBbb
	public static String camelize(String underbarSplitting) {
		StringBuffer sb = new StringBuffer();
		String[] str = underbarSplitting.split("_");
		for(String temp : str) {
			sb.append(Character.toUpperCase(temp.charAt(0)));
			sb.append(temp.substring(1).toLowerCase());
		}
        return sb.toString();
	}

	// AaaBbb -> AAA_BBB
	public static String reverseCamelize(String camelized) {
		StringBuffer sb = new StringBuffer();
		sb.append(camelized.charAt(0));
		for (int i = 1; i < camelized.length(); i++) {
			char c = camelized.charAt(i);
			if (c >= 'A' && c <= 'Z') {
				sb.append('_').append(c);
			} else {
				sb.append(c);
			}
		}
		return sb.toString().toUpperCase();
	}
}
