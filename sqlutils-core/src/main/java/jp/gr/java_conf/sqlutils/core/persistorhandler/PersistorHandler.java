package jp.gr.java_conf.sqlutils.core.persistorhandler;

import java.sql.Timestamp;
import java.util.Date;

import jp.gr.java_conf.sqlutils.DBManager;
import jp.gr.java_conf.sqlutils.core.dto.IColumn;

public class PersistorHandler {

	protected DBManager manager;
	protected boolean appendSchemaToSql = false;


	public PersistorHandler(DBManager manager) {
		this.manager = manager;
	}


	/**
	 * Javaではシステム日付はミリ秒までしか取得できないので、レコードのアクセスタイムスタンプとしては不足する恐れがある。
	 * 一応対応実装しているが、お勧めしない。
	 * 楽観ロック用キーとして使用するなら数値型のカウンタを使用するか、
	 * Generator設定でカラムにIgnoreOnUpdate等を指定し、DBMS側に任せる事を勧める（但しUPDATE時に自動更新するような設定は一部DBMSでしか不可能）。
	 */
	protected Object getOptimisticLockKeyNewValue(IColumn<?> col, Object currentVal) {
		if (col.getDataType() == Timestamp.class)
			return new Timestamp(new Date().getTime());
		else if (col.getDataType() == Date.class)
			return new Date();
		else if (col.getDataType() == Integer.class
				|| col.getDataType() == Long.class) {
			if (currentVal == null)
				return 1;
			else
				return ((Number)currentVal).intValue() + 1;
		} else
			throw new RuntimeException("Unavailable data type");
	}

	public void setAppendSchemaToSql(boolean appendSchemaToSql) {
		this.appendSchemaToSql = appendSchemaToSql;
	}
}
