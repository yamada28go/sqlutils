package jp.gr.java_conf.sqlutils.generator.dto.config;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlTransient;

public class DtoGeneratorConfig {


	public static class TableSetting {
		/*
		 * XML上のテーブル名の大文字小文字は区別するようにしているが、
		 * 他方JDBCのメタデータから取得したテーブル名が大文字だったり小文字だったり（DBMS次第？ドライバも？）するので、
		 * そちらがどっちで返すかをわかっていないと、xml上で設定したつもりが実際にはテーブル名アンマッチで無視される可能性が
		 * 考えられる。
		 * TODO 使われなかった設定があった場合にチェックする機構をつけるか？
		 */

		@XmlAttribute(name="name")
		public String name;

		public TableNameResolvers tblNameResolver;

		public ColumnNameResolvers defaultColNameResolver;

		@XmlElement(name="column")
		public List<ColumnSetting> cols = new ArrayList<ColumnSetting>();


		@XmlTransient
		public ColumnSetting logicalDeleteCol;

		@XmlTransient
		public ColumnSetting optimisticLockCol;

		@XmlTransient
		public ColumnSetting sequenceRelatedCol;


		// ex: pos = "table[n]"
		public void validate(String pos) {
			Config.CheckRequired(name, pos + "@name");
			if (tblNameResolver != null) tblNameResolver.validate(pos);
			if (defaultColNameResolver != null) defaultColNameResolver.validate(pos);

			List<String> colNames = new ArrayList<String>();
			for (int i = 0; i < cols.size(); i++) {
				ColumnSetting c = cols.get(i);
				String cPos = pos + "/col[" + i + "]";

				// 設定値チェック
				c.validate(cPos);

				// テーブル内での一意性チェック

				// 名前重複チェック
				if (colNames.contains(c.name))
					throw new RuntimeException(cPos + "@name is duplicated : " + c.name);
				colNames.add(c.name);

				// 論理削除フラグ、楽観排他ロック、シーケンス紐付けが一つのカラムにしか設定されていないこと
				if (c.isLogicalDeleteFlag) {
					if (logicalDeleteCol != null)
						Config.throwValidateError(cPos + "@isRogicalDeleteFlag is already setted to another column : " + c.name);
					logicalDeleteCol = c;
				}
				if (c.isOptimisticLockKey) {
					if (optimisticLockCol != null)
						Config.throwValidateError(cPos + "@isOptimisticLockKey is already setted to another column : " + c.name);
					optimisticLockCol = c;
				}
				if (c.sequenceRelation != null) {
					if (sequenceRelatedCol != null)
						Config.throwValidateError(cPos + "/sequenceRelation is already setted to another column : " + c.name);
					sequenceRelatedCol = c;
				}
			}
		}

		public EnumRelation getColEnumRelation(String colName) {
			ColumnSetting c = getColumnSetting(colName);
			if (c != null)
				return c.enumRelation;
			return null;
		}

		public ColValueConverter getColValueConverter(String colName) {
			ColumnSetting c = getColumnSetting(colName);
			if (c != null)
				return c.converter;
			return null;
		}

		public ColumnNameResolvers getColNameResolver(String colName) {
			ColumnNameResolvers r = null;
			ColumnSetting c = getColumnSetting(colName);
			if (c != null)
				r = c.colNameResolver;
			if (r == null)
				r = defaultColNameResolver;
			return r;
		}

		private ColumnSetting getColumnSetting(String colName) {
			for (ColumnSetting c : cols) {
				if (c.name.equals(colName)) {
					return c;
				}
			}
			return null;
		}
	}


	public static class ColumnSetting {

		@XmlAttribute(name="name")
		public String name;

		@XmlAttribute(name="isLogicalDeleteFlag")
		public boolean isLogicalDeleteFlag;

		@XmlAttribute(name="isOptimisticLockKey")
		public boolean isOptimisticLockKey;

//		public ColNameResolver colNameResolver;
		public ColumnNameResolvers colNameResolver;

		@XmlElements({
			@XmlElement(name="converter", type=ColValueConverter.class ),
			@XmlElement(name="strTrimConverter", type=ColValueConverter.StrTrimConverter.class),
			@XmlElement(name="intBoolConverter", type=ColValueConverter.IntBoolConverter.class),
		})
		public ColValueConverter converter;

		public EnumRelation enumRelation;

		public SequenceRelation sequenceRelation;


		// ex: pos = "table[n]/col[n]"
		public void validate(String pos) {
			if (isLogicalDeleteFlag == true && isOptimisticLockKey == true)
				Config.throwValidateError("isRogicalDeleteFlag と isOptimisticLockKey は同時に設定できません。");
			if (colNameResolver != null) colNameResolver.validate(pos);
			if (converter != null) converter.validate(pos);
			if (enumRelation != null) enumRelation.validate(pos);
			if (sequenceRelation != null) sequenceRelation.validate(pos);
		}
	}


	public static class EnumRelation {

		@XmlAttribute(name="baseClassName")
		public String baseClassName;

		@XmlAttribute(name="enumName")
		public String enumName;

		public void validate(String pos) {
			Config.CheckRequired(baseClassName, pos + "/enumRelation@baseClassName");
			Config.CheckRequired(enumName, pos + "/enumRelation@enumName");
		}

		public String toString() {
			return baseClassName + "." + enumName;
		}
	}

	public static class SequenceRelation {

		@XmlAttribute(name="name")
		public String name;

		public void validate(String pos) {
			Config.CheckRequired(name, pos + "/sequenceRelation@name");
		}

		public String toString() {
			return name;
		}
	}












	@XmlAttribute(name="definitionClassName")
	public String definitionClassName = "TableDefine";

	@XmlAttribute(name="package")
	public String package_;

	public TableNameResolvers defaultTblNameResolver = TableNameResolvers.getDefaultResolver();

	public ColumnNameResolvers defaultColNameResolver = ColumnNameResolvers.getDefaultResolver();


	//@XmlElementWrapper(name="tables")
	@XmlElement(name="table")
	public List<TableSetting> tables = new ArrayList<TableSetting>();


	public void validate() {
		Config.CheckRequired(definitionClassName, "dtoGenerator@definitionClassName");
		Config.CheckRequired(package_, "dtoGenerator@package");
		defaultTblNameResolver.validate("dtoGenerator/defaultTblNameResolver");
		defaultColNameResolver.validate("dtoGenerator/defaultColNameResolver");

		List<String> tblNames = new ArrayList<String>();
		for(int i = 0; i < tables.size(); i++) {
			TableSetting tbl = tables.get(i);
			String pos = "dtoGenerator/table[" + i + "]";
			tbl.validate(pos);

			if (tblNames.contains(tbl.name))
				throw new RuntimeException(pos + "@name is duplicated : " + tbl.name);
			tblNames.add(tbl.name);
		}
	}

	public TableNameResolvers getTblNameResolver(String tblName) {
		TableNameResolvers ret = null;
		TableSetting t = getTableSEtting(tblName);
		if (t != null)
			ret = t.tblNameResolver;
		if (ret == null)
			ret = defaultTblNameResolver;
		return ret;
	}

	public ColumnNameResolvers getColNameResolver(String tblName, String colName) {
		ColumnNameResolvers ret = null;
		TableSetting t = getTableSEtting(tblName);
		if (t != null)
			ret = t.getColNameResolver(colName);
		if (ret == null)
			ret = defaultColNameResolver;
		return ret;
	}

	public ColValueConverter getColValueConverter(String tblName, String colName) {
		TableSetting t = getTableSEtting(tblName);
		if (t != null)
			return t.getColValueConverter(colName);
		return null;
	}

	public EnumRelation getColEnumRelation(String tblName, String colName) {
		TableSetting t = getTableSEtting(tblName);
		if (t != null)
			return t.getColEnumRelation(colName);
		return null;
	}

	public ColumnSetting getColumnSetting(String tblName, String colName) {
		TableSetting t = getTableSEtting(tblName);
		if (t != null)
			return t.getColumnSetting(colName);
		return null;
	}

	private TableSetting getTableSEtting(String tblName) {
		for (TableSetting t : tables) {
			if (t.name.equals(tblName)) {
				return t;
			}
		}
		return null;
	}
}
