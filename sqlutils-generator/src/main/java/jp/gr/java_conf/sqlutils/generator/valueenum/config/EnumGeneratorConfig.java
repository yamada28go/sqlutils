package jp.gr.java_conf.sqlutils.generator.valueenum.config;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlEnum;

import jp.gr.java_conf.sqlutils.generator.common.NameResolver;
import jp.gr.java_conf.sqlutils.generator.common.NameResolver.CamelizeAndCapitalize;
import jp.gr.java_conf.sqlutils.generator.common.NameResolver.ToUpper;
import jp.gr.java_conf.sqlutils.generator.dto.config.Config;

public class EnumGeneratorConfig {

	@XmlEnum(String.class)
	public enum DataType { INT, STR }

	public static class SrcTable {

		@XmlAttribute(name="dataType")
		public DataType dataType;

		@XmlAttribute(name="tblName")
		public String tblName;

		@XmlAttribute(name="baseClassName")
		public String baseClassName;

		@XmlAttribute(name="enumNameCol")
		public String enumNameCol = "ENUM_NAME";

		@XmlAttribute(name="enumItemNameCol")
		public String enumItemNameCol = "ENUM_ITEM_NAME";

		@XmlAttribute(name="enumItemValueCol")
		public String enumItemValueCol = "ENUM_ITEM_VALUE";

		@XmlAttribute(name="enumItemOrderCol")
		public String enumItemOrderCol = "ENUM_ITEM_ORDER";


		private static final NameResolver DEFAULT_ENUM_NAME_RESOLVER = new CamelizeAndCapitalize();
		private static final NameResolver DEFAULT_ENUM_ITEM_NAME_RESOLVER = new ToUpper();

//		@XmlAttribute(name="isStringValue")
//		public boolean isStringValue;

//		@XmlElementWrapper(name="enumNameResolver")
//		@XmlElement(name="defaultResolver")
//		@XmlElements({
//			@XmlElement(name="enumNameDefaultResolver", type=EnumNameResolver.DefaultResolver.class ),
//			@XmlElement(name="enumNameVoidResolver", type=EnumNameResolver.VoidResolver.class)
//		})
		public NameResolver enumNameResolver = DEFAULT_ENUM_NAME_RESOLVER;

//		@XmlElementWrapper(name="itemNameResolver")
//		@XmlElement(name="defaultResolver")
		public NameResolver itemNameResolver = DEFAULT_ENUM_ITEM_NAME_RESOLVER;


		public void validate(String pos) {
			if (dataType == null) Config.throwValidateError(pos + "@dataType is missing");
			Config.CheckRequired(tblName, pos + "/@tblName");
			Config.CheckRequired(baseClassName, pos + "@baseClassName");
			Config.CheckRequired(enumNameCol, pos + "@enumNameCol");
			Config.CheckRequired(enumItemNameCol, pos + "@enumItemNameCol");
			Config.CheckRequired(enumItemValueCol, pos + "@enumItemValueCol");
			Config.CheckRequired(enumItemOrderCol, pos + "@enumItemOrderCol");
			if (enumNameResolver == null) Config.throwValidateError(pos + "/enumNameResolver is missing");
			if (itemNameResolver == null) Config.throwValidateError(pos + "/itemNameResolver is missing");
			enumNameResolver.validate(pos);
			itemNameResolver.validate(pos);
		}
	}

//	public static class NameResolverWrapper {
//
//
//		public static NameResolverWrapper getDefaultEnumNameResolver() {
//			NameResolverWrapper ret = new NameResolverWrapper();
//			ret.resolver = DEFAULT_ENUM_NAME_RESOLVER;
//			return ret;
//		}
//
//		public static NameResolverWrapper getDefaultEnumItemNameResolver() {
//			NameResolverWrapper ret = new NameResolverWrapper();
//			ret.resolver = DEFAULT_ENUM_ITEM_NAME_RESOLVER;
//			return ret;
//		}
//
//		public NameResolver resolver;
//
//		public void validate(String pos) {
////			if (resolver == null) Config.throwValidateError(pos + "/resolver is missing");
//			resolver.validate(pos);
//		}
//	}



	@XmlAttribute(name="package")
	public String package_;

	@XmlElement(name="srcTable")
	public List<SrcTable> tables;


	public void validate() {
		Config.CheckRequired(package_, "/enumGenerator@package");

		// TODO チェックとか
		for(int i = 0; i < tables.size(); i++) {
			SrcTable tbl = tables.get(i);
			String pos = "enumGenerator/srcTable[" + i + "]";
			tbl.validate(pos);
		}
	}
}
