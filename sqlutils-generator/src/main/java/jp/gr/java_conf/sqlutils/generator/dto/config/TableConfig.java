package jp.gr.java_conf.sqlutils.generator.dto.config;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

public class TableConfig {

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
	public List<ColumnConfig> cols = new ArrayList<ColumnConfig>();


//	@XmlTransient
//	public ColumnSetting logicalDeleteCol;
//
//	@XmlTransient
//	public ColumnSetting optimisticLockCol;
//
//	@XmlTransient
//	public ColumnSetting sequenceRelatedCol;


	// ex: pos = "table[n]"
	public void validate(String pos) {
		Config.CheckRequired(name, pos + "@name");
		if (tblNameResolver != null) tblNameResolver.validate(pos);
		if (defaultColNameResolver != null) defaultColNameResolver.validate(pos);

		List<String> colNames = new ArrayList<String>();
		ColumnConfig logicalDeleteCol = null;
		ColumnConfig optimisticLockKeyCol = null;
//		ColumnSetting optimisticLockCounterCol = null;
		ColumnConfig sequenceRelatedCol = null;
		for (int i = 0; i < cols.size(); i++) {
			ColumnConfig c = cols.get(i);
			String cPos = pos + "/col[" + i + "]";

			// 設定値チェック
			c.validate(cPos);

			// テーブル内での一意性チェック

			// 名前重複チェック
			if (colNames.contains(c.name))
				throw new RuntimeException(cPos + "@name is duplicated : " + c.name);
			colNames.add(c.name);

			// 論理削除フラグ、楽観排他ロック、シーケンス紐付けが一つのカラムにしか設定されていないこと
			if (c.logicalDeleteFlag != null) {
				if (logicalDeleteCol != null)
					Config.throwValidateError(cPos + "/logicalDeleteFlag is already set to another column : " + c.name);
				logicalDeleteCol = c;
			}
			if (c.optimisticLockKey != null) {
				if (optimisticLockKeyCol != null)
					Config.throwValidateError(cPos + "/optimisticLockKey is already set to another column : " + c.name);
//				if (optimisticLockCounterCol != null)
//					Config.throwValidateError(cPos + "/optimisticLockKey and /optimisticLockCounterCol cannot set to one table : " + c.name);
				optimisticLockKeyCol = c;
				// optimisticLockCounterは数値型のみ。Timestampを使ったロックは自動処理できないので
			}
//			if (c.optimisticLockCounter != null) {
//				if (optimisticLockCounterCol != null)
//					Config.throwValidateError(cPos + "/optimisticLockCounter is already set to another column : " + c.name);
//				if (optimisticLockKeyCol != null)
//					Config.throwValidateError(cPos + "/optimisticLockKey and /optimisticLockCounterCol cannot set to one table : " + c.name);
//				optimisticLockCounterCol = c;
//				// optimisticLockCounterは数値型のみ。Timestampを使ったロックは自動処理できないので
//			}
			if (c.sequenceRelation != null) {
				if (sequenceRelatedCol != null)
					Config.throwValidateError(cPos + "/sequenceRelation is already set to another column : " + c.name);
				sequenceRelatedCol = c;
			}
		}
	}

	public IColValueConverter getColValueConverter(String colName) {
		ColumnConfig c = getColumnSetting(colName);
		if (c != null)
			return c.converter;
		return null;
	}

	public ColumnNameResolvers getColNameResolver(String colName) {
		ColumnNameResolvers r = null;
		ColumnConfig c = getColumnSetting(colName);
		if (c != null)
			r = c.colNameResolver;
		if (r == null)
			r = defaultColNameResolver;
		return r;
	}

	ColumnConfig getColumnSetting(String colName) {
		for (ColumnConfig c : cols) {
			if (c.name.equals(colName)) {
				return c;
			}
		}
		return null;
	}
}
