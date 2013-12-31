package jp.gr.java_conf.sqlutils.generator.valueenum;

import jp.gr.java_conf.sqlutils.DBManager;
import jp.gr.java_conf.sqlutils.core.connection.SimpleConnectionProvider;
import jp.gr.java_conf.sqlutils.generator.dto.config.Config;
import jp.gr.java_conf.sqlutils.generator.valueenum.config.EnumGeneratorConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnumGenerator {

	private static final Logger logger = LoggerFactory.getLogger(EnumGenerator.class);

	public static EnumGeneratorConfig CONFIG;
	public static String SCHEMA;
	//public static String OUTPUT_BASE;

	@SuppressWarnings("deprecation")
	public EnumGenerator(Config config) {

		// DBManager
		DBManager.init(
			config.db.dbms,
			new SimpleConnectionProvider(
					config.db.driver, config.db.url, config.db.user, config.db.pass));

		// Config
		CONFIG = config.enumGenerator;
		SCHEMA = config.db.schema;
		//OUTPUT_BASE = config.output.basePath;

	}

	@SuppressWarnings("deprecation")
	public void generate() throws Exception {
		
		logger.info("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
		logger.info("@@@@ start generate enums-classes from db. @@@@");
		logger.info("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
		
		logger.info("This vesrion is not support enums-classes");
		
		throw new RuntimeException("This vesrion is not support enums-classes");
		
/*
		File outputDir = new File(OUTPUT_BASE, CONFIG.package_.replace(".", File.separator));
		logger.debug(outputDir.getAbsolutePath());

		// clean output-dir (or mkdir)
		if (outputDir.exists())
			FileUtils.cleanDirectory(outputDir);
		else
			FileUtils.forceMkdir(outputDir);


		// init velocity
		VelocityUtil.initVelocity();

		//
		for (SrcTable item : CONFIG.tables) {

			String sql = String.format("select %s,%s,%s from %s order by %s,%s,%s",
					item.enumNameCol,
					item.enumItemNameCol,
					item.enumItemValueCol,
					item.tblName,
					item.enumNameCol,
					item.enumItemOrderCol,
					item.enumItemNameCol);
			List<Object[]> results = DBManager.get().execQuery(new ArrayListHandler(), sql);

			DataType type = item.dataType;
			List<ValueEnumDefine> enums = new ArrayList<ValueEnumDefine>();
			String currentEnumName = "";
			ValueEnumDefine def = null;
			for (Object[] e : results) {
				String enumName = (String)e[0];
				String enumClassName = item.enumNameResolver.resolve(enumName);

				if (!currentEnumName.equals(enumName)) {

					if (def != null)
						enums.add(def);

					currentEnumName = enumName;
					def = new ValueEnumDefine(enumClassName);
					logger.debug("==========================");
					logger.debug(" " + def.enumName);
				}

				String itemName = (String)e[1];
				String itemEnumName = item.itemNameResolver.resolve(itemName);
				def.add(itemEnumName, e[2]);
				logger.debug(" > " + itemEnumName + " (" + e[2] + ")");

			}
			if (def != null)
				enums.add(def);


			// generate enum-base-class java
			VelocityContext context = new VelocityContext();
			Template template;
			if (type == DataType.INT)
				template = VelocityUtil.getTemplate(this, "IntValueEnums.vm");
			else
				template = VelocityUtil.getTemplate(this, "StrValueEnums.vm");
			context.put("packageName", CONFIG.package_);
			context.put("class_IValueEnum", IValueEnum.class.getCanonicalName());
			context.put("baseClassName", item.baseClassName);
			context.put("enums", enums);
			VelocityUtil.writeToFile(template, context, outputDir, item.baseClassName);

			logger.info("@@@@ done. @@@@");
		}
					*/

	}
}
