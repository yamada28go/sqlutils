package jp.gr.java_conf.sqlutils.generator.dto.config;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

public class DtoGeneratorConfig {


	@XmlAttribute(name="definitionClassName")
	public String definitionClassName = "TableDefine";

	@XmlAttribute(name="package")
	public String package_;

	public TableNameResolvers defaultTblNameResolver = TableNameResolvers.getDefaultResolver();

	public ColumnNameResolvers defaultColNameResolver = ColumnNameResolvers.getDefaultResolver();

	@XmlElementWrapper(name="columns")
	@XmlElement(name="column")
	public List<ColumnConfig> columns = new ArrayList<ColumnConfig>();

	//@XmlElementWrapper(name="tables")
	@XmlElement(name="table")
	public List<TableConfig> tables = new ArrayList<TableConfig>();


	public void validate() {
		Config.CheckRequired(definitionClassName, "dtoGenerator@definitionClassName");
		Config.CheckRequired(package_, "dtoGenerator@package");
		defaultTblNameResolver.validate("dtoGenerator/defaultTblNameResolver");
		defaultColNameResolver.validate("dtoGenerator/defaultColNameResolver");

		List<String> tblNames = new ArrayList<String>();
		for(int i = 0; i < tables.size(); i++) {
			TableConfig tbl = tables.get(i);
			String pos = "dtoGenerator/table[" + i + "]";
			tbl.validate(pos);

			if (tblNames.contains(tbl.name))
				throw new RuntimeException(pos + "@name is duplicated : " + tbl.name);
			tblNames.add(tbl.name);
		}
	}

	public TableNameResolvers getTblNameResolver(String tblName) {
		TableNameResolvers ret = null;
		TableConfig t = getTableSetting(tblName);
		if (t != null)
			ret = t.tblNameResolver;
		if (ret == null)
			ret = defaultTblNameResolver;
		return ret;
	}

	public ColumnNameResolvers getColNameResolver(String tblName, String colName) {
		ColumnNameResolvers ret = null;
		TableConfig t = getTableSetting(tblName);
		if (t != null)
			ret = t.getColNameResolver(colName);
		if (ret == null)
			ret = defaultColNameResolver;
		return ret;
	}

	public IColValueConverter getColValueConverter(String tblName, String colName) {
		TableConfig t = getTableSetting(tblName);
		if (t != null)
			return t.getColValueConverter(colName);
		return null;
	}

	public ColumnConfig getColumnSetting(String tblName, String colName) {
		/*
		 * TODO columnsで設定された全テーブルのカラム対象の一括設定、とのマージ
		 */
		TableConfig t = getTableSetting(tblName);
		if (t != null)
			return t.getColumnSetting(colName);
		return null;
	}

	private TableConfig getTableSetting(String tblName) {
		for (TableConfig t : tables) {
			if (t.name.equals(tblName)) {
				return t;
			}
		}
		return null;
	}
}
