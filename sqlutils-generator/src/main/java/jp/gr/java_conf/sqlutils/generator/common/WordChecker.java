package jp.gr.java_conf.sqlutils.generator.common;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.CharUtils;

public class WordChecker {


	private static String[] JAVA_KEYWORDS = new String[]{
		"public",
		"private",
		"protected",
		"static",
		"package",
		"import",
		"class",
		"interface",
		"extends",
		"implements",
		"enum",
		"switch",
		"case",
		"default",
		"if",
		"else",
		"while",
		"else",
		"throw",
		"throws",
		"try",
		"catch",
		"finally",
		"return",
		"break",
		"continue",
		"true",
		"false",
		"null",
		"new",

		"byte",
		"short",
		"int",
		"long",
		"float",
		"double",
		"char",
		"boolean",
	};

	public static boolean isJavaKeyword(String lowerName) {
		if (ArrayUtils.contains(JAVA_KEYWORDS, lowerName))
			return true;
		else
			return false;
	}

	public static boolean startWithUnavailableChar(String name) {

		// 最初の文字による制限のチェック
		char c = name.charAt(0);

		// 漢字・ひらがな等（＝非アスキー）は可（※但し使えない文字もあるが、そこまで対処はしない）
		if (!CharUtils.isAscii(c))
			return false; // OK

		// 英字、'$'、'_'はOK
		if (CharUtils.isAsciiAlpha(c)
		|| c == '_'
		|| c == '$')
			return false; // OK
		else
			return true; // NG：数字とかその他の記号等
	}
}
