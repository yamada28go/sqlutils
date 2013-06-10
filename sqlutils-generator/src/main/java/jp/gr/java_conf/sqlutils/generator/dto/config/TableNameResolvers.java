package jp.gr.java_conf.sqlutils.generator.dto.config;

import javax.xml.bind.annotation.XmlElement;

import jp.gr.java_conf.sqlutils.generator.common.NameResolver;
import jp.gr.java_conf.sqlutils.generator.common.NameResolver.CamelizeAndCapitalize;
import jp.gr.java_conf.sqlutils.generator.common.NameResolver.ToUpper;

public class TableNameResolvers {

	private static final NameResolver DEFAULT_DTO_CLASS_NAME_RESOLVER = new CamelizeAndCapitalize();
	private static final NameResolver DEFAULT_DEFINITION_NAME_RESOLVER = new ToUpper();

	public static TableNameResolvers getDefaultResolver() {
		TableNameResolvers ret = new TableNameResolvers();
		ret.dtoClassNameResolver = DEFAULT_DTO_CLASS_NAME_RESOLVER;
		ret.definitionNameResolver = DEFAULT_DEFINITION_NAME_RESOLVER;
		return ret;
	}



	@XmlElement(name="dtoClassName")
	public NameResolver dtoClassNameResolver;

	@XmlElement(name="definitionName")
	public NameResolver definitionNameResolver;


	public void validate(String pos) {
		if (dtoClassNameResolver == null)
			Config.throwValidateError(pos + "/dtoClassNameResolver is missing.");
		if (definitionNameResolver == null)
			Config.throwValidateError(pos + "/definitionNameResolver is missing.");

		dtoClassNameResolver.validate(pos);
		definitionNameResolver.validate(pos);
	}
}
