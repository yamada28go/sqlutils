package jp.gr.java_conf.sqlutils.core.handler;

import java.sql.ResultSet;

@Deprecated
public interface IColNameProvider {

	/**
	 * @param num 1-based index
	 */
	public String getTableName(ResultSet rs, int num);

	/**
	 * @param num 1-based index
	 */
	public String getColumnName(ResultSet rs, int num);

}
