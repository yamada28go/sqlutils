package jp.gr.java_conf.sqlutils.generator.common;

import javax.xml.bind.annotation.XmlAttribute;

import jp.gr.java_conf.sqlutils.generator.dto.config.Config;

import org.apache.commons.lang.StringUtils;


public abstract class NameResolver {

	public interface INameResolver {}

	static final String FIELDNAME_SUFFIX = "_";
	static final String FIELDNAME_PREFIX = "_";


	/**
	 * no conversion.
	 */
	public static class Void extends NameResolver {
		@Override
		protected String resolveInner(String baseName) {
			return baseName;
		}
		public void validate(String pos) {
		}
	}

	/**
	 * simply returns a specified name
	 */
	public static class Specified extends NameResolver {

		@XmlAttribute(name="name")
		public String name;

		@Override
		protected String resolveInner(String baseName) {
			return name;
		}

		public void validate(String pos) {
			if (name == null) Config.throwValidateError(pos + "@name is missing");
		}
	}

	/**
	 * AAA_BBB -> aaaBbb
	 * aaa_bbb -> aaaBbb
	 * aaa     -> aaa
	 */
	public static class Camelize extends NameResolver {
		@Override
		protected String resolveInner(String baseName) {
			return StringUtils.uncapitalize(camelize(baseName));
		}
		public void validate(String pos) {
		}
	}

	/**
	 * AAA_BBB -> AaaBbb
	 * aaa_bbb -> AaaBbb
	 * aaa     -> Aaa
	 */
	public static class CamelizeAndCapitalize extends NameResolver {
		@Override
		protected String resolveInner(String baseName) {
			return camelize(baseName);
		}
		public void validate(String pos) {
		}
	}

	public static class ToUpper extends NameResolver {
		@Override
		protected String resolveInner(String baseName) {
			return baseName.toUpperCase();
		}
		public void validate(String pos) {
		}
	}

	public static class ToLower extends NameResolver {
		@Override
		protected String resolveInner(String baseName) {
			return baseName.toLowerCase();
		}
		public void validate(String pos) {
		}
	}

	/**
	 * AaaBbb -> AAA_BBB
	 * aaaBbb -> AAA_BBB
	 */
	public static class ReverseCamelizeToUpper extends NameResolver {
		@Override
		protected String resolveInner(String baseName) {
			return reverseCamelize(baseName).toUpperCase();
		}
		public void validate(String pos) {
		}
	}

	/**
	 * AaaBbb -> aaa_bbb
	 * aaaBbb -> aaa_bbb
	 */
	public static class ReverseCamelizeToLower extends NameResolver {
		@Override
		protected String resolveInner(String baseName) {
			return reverseCamelize(baseName);
		}
		public void validate(String pos) {
		}
	}



	/**
	 * aaa_bbb -> AaaBbb
	 */
	protected static String camelize(String underbarSplitted) {
		StringBuffer sb = new StringBuffer();
		String[] str = underbarSplitted.split("_");
		for(String temp : str) {
			sb.append(Character.toUpperCase(temp.charAt(0)));
			sb.append(temp.substring(1).toLowerCase());
		}
        return sb.toString();
	}

	/**
	 * AaaBbb -> aaa_bbb
	 * aaaBbb -> aaa_bbb
	 */
	protected static String reverseCamelize(String camelized) {
		StringBuffer sb = new StringBuffer();
		sb.append(camelized.charAt(0));
		for (int i = 1; i < camelized.length(); i++) {
			char c = camelized.charAt(i);
			if (c >= 'A' && c <= 'Z') {
				sb.append('_').append(c);
			} else {
				sb.append(String.valueOf(c).toLowerCase());
			}
		}
		return sb.toString();
	}




	protected abstract String resolveInner(String baseName);
	public abstract void validate(String pos);


	@XmlAttribute(name="prefix")
	public String prefix;

	@XmlAttribute(name="suffix")
	public String suffix;

	public String resolve(String baseName) {

		String ret = resolveInner(baseName);

		if (prefix != null)
			ret = prefix + ret;

		if (suffix != null)
			ret += suffix;

		// javaの予約語、あるいは変数命名規則に違反していないか
		if (WordChecker.isJavaKeyword(ret))
			ret += FIELDNAME_SUFFIX;

		if (WordChecker.startWithUnavailableChar(ret))
			ret = FIELDNAME_PREFIX + ret;

		return ret;
	}

}
