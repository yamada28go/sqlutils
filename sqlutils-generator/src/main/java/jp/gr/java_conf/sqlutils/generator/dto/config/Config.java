package jp.gr.java_conf.sqlutils.generator.dto.config;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import jp.gr.java_conf.sqlutils.generator.valueenum.config.EnumGeneratorConfig;

import org.apache.commons.lang.StringUtils;

@XmlRootElement(name="root")
public class Config {

	//@XmlAccessorType(XmlAccessType.FIELD) FIELDはdefaultなので省略可
	public static class DBSetting {

		@XmlAttribute(name="dbms")
		public String dbms;

		@XmlAttribute(name="schema")
		public String schema;

		@XmlAttribute(name="driver")
		public String driver;

		@XmlAttribute(name="url")
		public String url;

		@XmlAttribute(name="user")
		public String user;

		@XmlAttribute(name="pass")
		public String pass;

		public void validate() {
			CheckRequired(dbms, "db@dbms");
			CheckRequired(schema, "db@schema");
			CheckRequired(driver, "db@driver");
			CheckRequired(url, "db@url");
			CheckRequired(user, "db@user");
//			CheckRequired(pass, "db@pass");
		}
	}

	public static class OutputSetting {

		@XmlAttribute(name="basePath")
		public String basePath;

		public void validate() {
			CheckRequired(basePath, "output@basePath");
		}
	}


	public static void CheckRequired(String value, String label) {
		if (StringUtils.isEmpty(value)) throwValidateError(label + " is missing");
	}

	public static void throwValidateError(String string) {
		throw new RuntimeException(string);
	}


	//////////////////////////////////////////////////////////////////////////////

	public DBSetting db;

	public OutputSetting output;

	public DtoGeneratorConfig dtoGenerator;

	public EnumGeneratorConfig enumGenerator;

//	@XmlAttribute(name="caseSensitiveNameMatching")
//	public boolean caseSensitiveNameMatching; // caseSensitiveNameMatchingの実装←ちょっと調べればわかるのでやめ



	public void preCheck() {
		db.validate();
		output.validate();
		if (enumGenerator != null)
			enumGenerator.preCheck();
		if (dtoGenerator != null)
			dtoGenerator.preCheck();
	}

	public void postCheck() {
		if (enumGenerator != null)
			enumGenerator.postCheck();
		if (dtoGenerator != null)
			dtoGenerator.postCheck();
	}

}
