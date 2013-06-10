package jp.gr.java_conf.sqlutils.generator.dto.config;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

public class ColValueConverter {


	public static final String FIELDNAME_PLACEHOLDER = "<FIELDNAME>";


	@XmlType(factoryMethod="getInstance")
	public static class StrTrimConverter extends ColValueConverter {

		static StrTrimConverter STR_TRIM_CONVERTOR;
		static {
			STR_TRIM_CONVERTOR = new StrTrimConverter();
			STR_TRIM_CONVERTOR.dtoFieldClassType = String.class.getName();
			STR_TRIM_CONVERTOR.setToDtoConversion = "val == null ? null : ((String)val).trim()";
			STR_TRIM_CONVERTOR.getFromDtoConversion = "this." + ColValueConverter.FIELDNAME_PLACEHOLDER;
		}

		public static StrTrimConverter getInstance() {
			return STR_TRIM_CONVERTOR;
		}

		private StrTrimConverter() {}
	}



	@XmlType(factoryMethod="getInstance")
	public static class IntBoolConverter extends ColValueConverter {

		static IntBoolConverter INT_BOOL_CONVERTOR;
		static {
			INT_BOOL_CONVERTOR = new IntBoolConverter();
			INT_BOOL_CONVERTOR.dtoFieldClassType = Boolean.class.getName();
			INT_BOOL_CONVERTOR.setToDtoConversion = "val == null ? null : (((java.lang.Number)val).intValue() == 1 ? true : false)";
			INT_BOOL_CONVERTOR.getFromDtoConversion = "this." + FIELDNAME_PLACEHOLDER + " == null ? null : this.<FIELDNAME> ? 1 : 0";
		}

		public static IntBoolConverter getInstance() {
			return INT_BOOL_CONVERTOR;
		}

		private IntBoolConverter() {}
	}




//	@XmlAttribute(name="type")
//	public String type;

	@XmlAttribute(name="dtoFieldClassType")
	public String dtoFieldClassType;

	@XmlAttribute(name="setToDtoConversion")
	public String setToDtoConversion;

	@XmlAttribute(name="getFromDtoConversion")
	public String getFromDtoConversion;

	public void validate(String pos) {
		Config.CheckRequired(dtoFieldClassType, pos + "@dtoFieldClassType");
		Config.CheckRequired(setToDtoConversion, pos + "@setToDtoConversion");
		Config.CheckRequired(getFromDtoConversion, pos + "@getFromDtoConversion");
	}
}
