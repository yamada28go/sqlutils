package jp.gr.java_conf.sqlutils.generator.dto.config;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;

public class DtoGeneratorConfig {


	@XmlAttribute(name="definitionClassName")
	public String definitionClassName = "TableDefine";

	@XmlAttribute(name="namespace")
	public String namespace;

	//名前空間の可視宣言を作成する
	public String get_namespace_Declaration_start()
	{
		//名前空間未指定の場合は何もしない
		if(true == namespace.isEmpty())
			return "";
		
		//階層を::で分割して名前空間の結合を行う
		String[] a = namespace.split("::");
		StringBuffer t = new StringBuffer();
		for (String ns : a) {
			
			t.append("namespace ");
			t.append(ns);
			t.append("{ ");
			
		}
		
		return t.toString();
	}

	//名前空間の階層を数える
	private int count_namespace_separator( String namespace )
	{
		String[] a = namespace.split("::");
		if( 0==a.length)
		{
			return 1;
		}
		else
		{
			return a.length;	
		}
	}
	
	//名前空間の可視宣言を作成する
	public String get_namespace_Declaration_end()
	{
		if(true == namespace.isEmpty())
			return "";
		
		StringBuffer t = new StringBuffer();
		for(int i =0;i<count_namespace_separator(this.namespace);i++)
		{
			t.append('}');
		}
		
		return t.toString();
	}
	
	//名前空間の文字列指定が正しい形式が判定する
	void check_namespace()
	{
		//指定が無い場合はok
		if( false == namespace.isEmpty())
		{
			//C++識別子の判定用正規表現としては、完璧では無いが
			//このプログラムでは問題とならない程度で判定処理を記述する
			String regex = "^(\\w+::*)*\\w+$";
			Pattern p = Pattern.compile(regex);

			Matcher m = p.matcher(namespace);
			if (!m.find()){
			  //正規表現に合致しなければ例外を発行する
				throw new RuntimeException("Namespace definition is wrong. [" + this.namespace + " ] ");
			}
			
		}
	}

	public TableNameResolvers defaultTblNameResolver = TableNameResolvers.getDefaultResolver();

	public ColumnNameResolvers defaultColNameResolver = ColumnNameResolvers.getDefaultResolver();

	@XmlElementWrapper(name="columns")
	@XmlElement(name="column")
	public List<ColumnConfig> columns = new ArrayList<ColumnConfig>();

	@XmlTransient
	private List<ColumnConfig> usedColConfigs = new ArrayList<ColumnConfig>();

	//@XmlElementWrapper(name="tables")
	@XmlElement(name="table")
	public List<TableConfig> tables = new ArrayList<TableConfig>();

	@XmlTransient
	private List<TableConfig> usedTblConfigs = new ArrayList<TableConfig>();

	public void preCheck() {
		Config.CheckRequired(definitionClassName, "dtoGenerator@definitionClassName");
		Config.CheckRequired(namespace, "dtoGenerator@namespace");
		check_namespace();
		
		defaultTblNameResolver.validate("dtoGenerator/defaultTblNameResolver");
		defaultColNameResolver.validate("dtoGenerator/defaultColNameResolver");

		for(int i = 0; i < columns.size(); i++) {
			ColumnConfig c = columns.get(i);
			String pos = "dtoGenerator/columns[" + i + "]";
			c.validate(pos);
		}

		List<String> tblNames = new ArrayList<String>();
		for(int i = 0; i < tables.size(); i++) {
			TableConfig tbl = tables.get(i);
			String pos = "dtoGenerator/table[" + i + "]";
			tbl.preCheck(pos);

			if (tblNames.contains(tbl.name))
				throw new RuntimeException(pos + "@name is duplicated : " + tbl.name);
			tblNames.add(tbl.name);
		}
	}

	public void postCheck() {

		// 使用されなかったColumn名があるか？
		for (int i = 0; i < columns.size(); i++) {
			ColumnConfig c = columns.get(i);
			if (!usedColConfigs.contains(c))
				throw new RuntimeException("dtoGenerator/table[" + i + "] is not used. : " + c.name);
		}

		// 使用されなかったTable名があるか？
		for(int i = 0; i < tables.size(); i++) {
			TableConfig tbl = tables.get(i);
			String pos = "dtoGenerator/table[" + i + "]";
			if (!usedTblConfigs.contains(tbl))
				throw new RuntimeException(pos + " is not used. : " + tbl.name);
			tbl.postCheck(pos);
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

		ColumnConfig gc = getGlobalColumnSetting(colName);
		ColumnConfig c = null;

		TableConfig t = getTableSetting(tblName);
		if (t != null)
			c = t.getColumnSetting(colName);

		if (c == null)
			return gc;
		else if (gc == null)
			return c;
		else {
			c.merge(gc);
			return c;
		}
	}

	private ColumnConfig getGlobalColumnSetting(String colName) {
		for (ColumnConfig c : columns) {
			if (c.name.equals(colName)) {
				usedColConfigs.add(c);
				return c;
			}
		}
		return null;
	}

	private TableConfig getTableSetting(String tblName) {
		for (TableConfig t : tables) {
			if (t.name.equals(tblName)) {
				usedTblConfigs.add(t);
				return t;
			}
		}
		return null;
	}
}
