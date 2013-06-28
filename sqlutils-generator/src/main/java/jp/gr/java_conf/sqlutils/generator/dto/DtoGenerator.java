package jp.gr.java_conf.sqlutils.generator.dto;

import java.io.File;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import jp.gr.java_conf.sqlutils.DBManager;
import jp.gr.java_conf.sqlutils.common.ValueEnum.IValueEnum;
import jp.gr.java_conf.sqlutils.core.connection.SimpleConnectionProvider;
import jp.gr.java_conf.sqlutils.core.connection.Tx;
import jp.gr.java_conf.sqlutils.core.dto.IColumn;
import jp.gr.java_conf.sqlutils.core.dto.IColumn.Column;
import jp.gr.java_conf.sqlutils.core.dto.IDto;
import jp.gr.java_conf.sqlutils.core.dto.IDto.IGeneratedDto;
import jp.gr.java_conf.sqlutils.core.dto.IDto.ILogicalDeleting;
import jp.gr.java_conf.sqlutils.core.dto.IDto.IOptimisticLocking;
import jp.gr.java_conf.sqlutils.core.dto.IDto.IPersistable;
import jp.gr.java_conf.sqlutils.core.dto.ITable;
import jp.gr.java_conf.sqlutils.core.dto.ITable.Table;
import jp.gr.java_conf.sqlutils.core.exception.NoSuchColumnException;
import jp.gr.java_conf.sqlutils.core.exception.RuntimeSQLException;
import jp.gr.java_conf.sqlutils.generator.common.VelocityUtil;
import jp.gr.java_conf.sqlutils.generator.dto.config.Config;
import jp.gr.java_conf.sqlutils.generator.dto.config.DtoGeneratorConfig;
import jp.gr.java_conf.sqlutils.generator.jdbc.ColumnInfo;
import jp.gr.java_conf.sqlutils.generator.jdbc.TableInfo;
import jp.gr.java_conf.sqlutils.generator.valueenum.config.EnumGeneratorConfig;

import org.apache.commons.io.FileUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DtoGenerator {

	private static final Logger logger = LoggerFactory.getLogger(DtoGenerator.class);


//	public static Config config;
	public static EnumGeneratorConfig ENUM_CONFIG;
	public static DtoGeneratorConfig CONFIG;
	public static DtoGeneratorPlugin PLUGIN;
	public static String SCHEMA;
	public static String OUTPUT_BASE;

	@SuppressWarnings("deprecation")
	public DtoGenerator(Config config) {

		// DBManager
		Properties props = new Properties();
		props.put("user", config.db.user);
		props.put("password", config.db.pass);
		props.put("remarksReporting", "true"); // TODO Oracleの場合、これを有効にしないとコメント情報が取れない模様だが未確認
		DBManager.init(
			config.db.dbms,
			new SimpleConnectionProvider(
					config.db.driver, config.db.url, props));

		// Config
		CONFIG = config.dtoGenerator;
		ENUM_CONFIG = config.enumGenerator;
		SCHEMA = config.db.schema;
		OUTPUT_BASE = config.output.basePath;

		// Plugin
		DtoGenerator.PLUGIN = new DtoGeneratorPlugin();

	}

	@SuppressWarnings("deprecation")
	public void generate() throws Exception {

		logger.info("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
		logger.info("@@@@ start generate DTO-classes from db. @@@@");
		logger.info("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");

		File outputDir = new File(OUTPUT_BASE, CONFIG.package_.replace(".", File.separator));
		logger.debug(outputDir.getAbsolutePath());

		// clean output-dir (or mkdir)
		if (outputDir.exists())
			FileUtils.cleanDirectory(outputDir);
		else
			FileUtils.forceMkdir(outputDir);


		// init velocity
		VelocityUtil.initVelocity();


		// parse tables from schema
		final List<TableInfo> tables = new ArrayList<TableInfo>();
		new Tx() {
			@Override
			protected void run(DBManager manager) {
				try {
					DatabaseMetaData dbmd = manager.getConnection().getMetaData();

					// DBMSによる大文字小文字に関わる問題は、Pluginに持ち出す

					// list tables
					ResultSet rsTbls = PLUGIN.getTables(dbmd, SCHEMA);
					while(rsTbls.next()) {

						// table
						String name = rsTbls.getString("TABLE_NAME");
						logger.debug("==========================");
						logger.debug(" " + name);

						TableInfo table = PLUGIN.createTable(rsTbls, dbmd, false);
						tables.add(table);

						// primary keys
						List<String> primaryKeys = PLUGIN.getPrimaryKeys(dbmd, SCHEMA, name);
						logger.debug(" primarykeys : " + primaryKeys);
						table.primaryKeys = primaryKeys;

						// cols
						final List<ColumnInfo> columns = new ArrayList<ColumnInfo>();
						logger.debug(" --------");
						ResultSet rsCols = PLUGIN.getCols(dbmd, SCHEMA, name);
						while(rsCols.next()) {
							String colName = rsCols.getString("COLUMN_NAME");
							int type = rsCols.getInt("DATA_TYPE");
							String typeName = rsCols.getString("TYPE_NAME");
							String isKey = primaryKeys.contains(colName) ? "<PK>" : "";
							logger.debug(" > " + colName + " : " + typeName + "(" + type + ") " + isKey);

							columns.add(PLUGIN.createColumn(rsCols, dbmd, table));
						}
						table.cols = columns;
					}

					// list views
					ResultSet rs = PLUGIN.getViews(dbmd, SCHEMA);
					while(rs.next()) {
						// table
						String name = rs.getString("TABLE_NAME");
						logger.debug("==========================");
						logger.debug(" " + name);
						TableInfo table = PLUGIN.createTable(rsTbls, dbmd, true);
						tables.add(table);
						// cols
						final List<ColumnInfo> columns = new ArrayList<ColumnInfo>();
						logger.debug(" --------");
						ResultSet cols = PLUGIN.getCols(dbmd, SCHEMA, name);
						while(cols.next()) {
							String colName = cols.getString("COLUMN_NAME");
							int type = cols.getInt("DATA_TYPE");
							String typeName = cols.getString("TYPE_NAME");
							logger.debug(" > " + colName + " : " + typeName + "(" + type + ") ");
							columns.add(PLUGIN.createColumn(cols, dbmd, table));
						}
						table.cols = columns;
					}

				} catch (SQLException e) {
					throw new RuntimeSQLException(e);
				}
			}
		}.execute(DBManager.get());

		logger.debug(tables.toString());


		// generate dto-java file
		Template template = VelocityUtil.getTemplate(this, "Dto.vm");
		for (TableInfo table : tables) {
			VelocityContext context = new VelocityContext();
			context.put("schema", SCHEMA);
			context.put("class_ITable", ITable.class.getCanonicalName());
			context.put("class_IColumn", IColumn.class.getCanonicalName());
			context.put("class_IDto", IDto.class.getCanonicalName());
			context.put("class_IPersistableDto", IPersistable.class.getCanonicalName());
			context.put("class_IGeneratedDto", IGeneratedDto.class.getCanonicalName());
			context.put("class_IOptimisticLockingDto", IOptimisticLocking.class.getCanonicalName());
			context.put("class_ILogicalDeletingDto", ILogicalDeleting.class.getCanonicalName());
			context.put("class_IValueEnum", IValueEnum.class.getCanonicalName());
			context.put("class_NoSuchColumnException", NoSuchColumnException.class.getCanonicalName());
			context.put("tbl", table);
			context.put("className", table.getDtoClassName());
			context.put("packageName", CONFIG.package_);
//			context.put("defPackageName", defPackageName);
			context.put("defClassName", CONFIG.definitionClassName);
//			context.put("noColumnFieldNameConversion", settings.generator.no_column_field_name_conversion);
			VelocityUtil.writeToFile(template, context, outputDir, table.getDtoClassName());
		}

		// generate definition-java file
		template = VelocityUtil.getTemplate(this, "Definitions.vm");
		VelocityContext context = new VelocityContext();
		context.put("schema", SCHEMA);
		context.put("packageName", CONFIG.package_);
//		context.put("dtoPackageName", dtoPackageName);
		context.put("class_IDto", IDto.class.getName());
		context.put("class_ITable", ITable.class.getName());
		context.put("class_Table", Table.class.getCanonicalName());
		context.put("class_IColumn", IColumn.class.getName());
		context.put("class_Column", Column.class.getCanonicalName());
		context.put("tbls", tables);
		context.put("defClassName", CONFIG.definitionClassName);
		VelocityUtil.writeToFile(template, context, outputDir, CONFIG.definitionClassName);

		logger.info("@@@@ done. @@@@");
	}

}
