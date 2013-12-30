package jp.gr.java_conf.sqlutils.generator.dto.config;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import jp.gr.java_conf.sqlutils.generator.dto.DtoGenerator;

public interface IColValueConverter {

	public static final String FIELDNAME_PLACEHOLDER = "@@@FIELDNAME@@@";

	String getDtoFieldClassType();
	String getSetToDtoConversion();
	String getGetFromDtoConversion(String fieldName);
	void validate(String pos);
	
	//! C++ 用 C++内部でデータラッパーに使用する型情報を保持する
	String getWrapperType();

	public static class ColValueConverter implements IColValueConverter {

		@XmlAttribute(name="dtoFieldClassType")
		public String dtoFieldClassType;

		@XmlAttribute(name="setToDtoConversion")
		public String setToDtoConversion;

		@XmlAttribute(name="getFromDtoConversion")
		public String getFromDtoConversion;
		
		public String getWrapperType;

		@Override
		public void validate(String pos) {
			Config.CheckRequired(dtoFieldClassType, pos + "@dtoFieldClassType");
			Config.CheckRequired(setToDtoConversion, pos + "@setToDtoConversion");
			Config.CheckRequired(getFromDtoConversion, pos + "@getFromDtoConversion");
		}

		@Override
		public String getDtoFieldClassType() {
			return dtoFieldClassType;
		}

		@Override
		public String getSetToDtoConversion() {
			return setToDtoConversion;
		}

		@Override
		public String getGetFromDtoConversion(String fieldName) {
			return getFromDtoConversion.replace(ColValueConverter.FIELDNAME_PLACEHOLDER, fieldName);
		}

		@Override
		public String getWrapperType() {
			return getWrapperType;
		}
	}


	@XmlType(factoryMethod="getInstance")
	public static class IntBoolConverter extends ColValueConverter {

		static IntBoolConverter INT_BOOL_CONVERTOR = new IntBoolConverter();

		public static IntBoolConverter getInstance() {
			return INT_BOOL_CONVERTOR;
		}


		private IntBoolConverter(){}

		@Override
		public void validate(String pos) {}

		@Override
		public String getDtoFieldClassType() {
			return Boolean.class.getName();
		}

		@Override
		public String getSetToDtoConversion() {
			return "val == null ? null : (((java.lang.Number)val).intValue() == 1 ? true : false)";
		}

		@Override
		public String getGetFromDtoConversion(String fieldName) {
			final String expression = "this." + FIELDNAME_PLACEHOLDER + " == null ? null : this." + FIELDNAME_PLACEHOLDER + " ? 1 : 0";
			return expression.replace(FIELDNAME_PLACEHOLDER, fieldName);
		}
	}


	@XmlType(factoryMethod="getInstance")
	public static class StrTrimConverter extends ColValueConverter {

		static StrTrimConverter STR_TRIM_CONVERTOR = new StrTrimConverter();

		public static StrTrimConverter getInstance() {
			return STR_TRIM_CONVERTOR;
		}


		private StrTrimConverter(){}

		@Override
		public void validate(String pos) {}

		@Override
		public String getDtoFieldClassType() {
			return String.class.getName();
		}

		@Override
		public String getSetToDtoConversion() {
			return "val == null ? null : ((String)val).trim()";
		}

		@Override
		public String getGetFromDtoConversion(String fieldName) {
			return "this." + fieldName;
		}
	}


	public static class EnumConverter extends ColValueConverter {

		@XmlAttribute(name="baseClassName")
		public String baseClassName;

		@XmlAttribute(name="enumName")
		public String enumName;

		public void validate(String pos) {
			Config.CheckRequired(baseClassName, pos + "/enumRelation@baseClassName");
			Config.CheckRequired(enumName, pos + "/enumRelation@enumName");
//			if (DtoGenerator.ENUM_CONFIG == null)
//				Config.throwValidateError("enumGenerator setting is required for use EnumConverter");
		}

		@Override
		public String getDtoFieldClassType() {
			String baseClass = DtoGenerator.ENUM_CONFIG.package_ + "." + baseClassName;
			return baseClass + "." + enumName;
		}

		@Override
		public String getSetToDtoConversion() {
			String baseClass = DtoGenerator.ENUM_CONFIG.package_ + "." + baseClassName;
			String enumClass = baseClass + "." + enumName;
			return "val == null ? null : " + baseClass + ".get(" + enumClass + ".class, val)";
		}

		@Override
		public String getGetFromDtoConversion(String fieldName) {
			final String expression = "this." + FIELDNAME_PLACEHOLDER + " == null ? null : this." + FIELDNAME_PLACEHOLDER + ".getValue()";
			return expression.replace(FIELDNAME_PLACEHOLDER, fieldName);
		}
	}

}
