package jp.gr.java_conf.sqlutils.generator.dto.config;

import javax.xml.bind.annotation.XmlElement;

import jp.gr.java_conf.sqlutils.generator.common.NameResolver;
import jp.gr.java_conf.sqlutils.generator.common.NameResolver.Camelize;
import jp.gr.java_conf.sqlutils.generator.common.NameResolver.ToUpper;

public class ColumnNameResolvers {

	private static final NameResolver DEFAULT_DTO_FIELD_NAME_RESOLVER = new Camelize();
	private static final NameResolver DEFAULT_DEFINITION_NAME_RESOLVER = new ToUpper();

	public static ColumnNameResolvers getDefaultResolver() {
		ColumnNameResolvers ret = new ColumnNameResolvers();
		ret.dtoFieldNameResolver = DEFAULT_DTO_FIELD_NAME_RESOLVER;
		ret.definitionNameResolver = DEFAULT_DEFINITION_NAME_RESOLVER;
		return ret;
	}



	@XmlElement(name="dtoFieldName")
	public NameResolver dtoFieldNameResolver;

	@XmlElement(name="definitionName")
	public NameResolver definitionNameResolver;


	public void validate(String pos) {
		if (dtoFieldNameResolver == null)
			Config.throwValidateError(pos + "/dtoFieldNameResolver is missing.");
		if (definitionNameResolver == null)
			Config.throwValidateError(pos + "/definitionNameResolver is missing.");

		dtoFieldNameResolver.validate(pos);
		definitionNameResolver.validate(pos);
	}
}
