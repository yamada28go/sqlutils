package jp.gr.java_conf.sqlutils.core.handler;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

@Deprecated
public class JdbcColNameProvider implements IColNameProvider {

	@Override
	public String getTableName(ResultSet rs, int num) {
		try {
			ResultSetMetaData rsmd = rs.getMetaData();
			return rsmd.getTableName(num);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getColumnName(ResultSet rs, int num) {
		try {
			ResultSetMetaData rsmd = rs.getMetaData();
			String ret = rsmd.getColumnLabel(num);
	    	if (ret == null)
	    		ret = rsmd.getColumnName(num);
	    	if (ret == null)
				throw new RuntimeException("Cannot get colName from metadata." +
						" So, we cannot know where to put this column-result.");
			return ret;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

}
