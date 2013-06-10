package jp.gr.java_conf.sqlutils.core.handler;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResultSetParser {

	@SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.getLogger(ResultSetParser.class);

	public static class ParseResult {
		boolean available;
//		String schmName;
		String tblName;
		String colName;
	}


	private String[] aliasNames;

	public ResultSetParser(String[] aliasNames) {
		this.aliasNames = aliasNames;
	}

	public ParseResult parse(ResultSet rs, int num) throws SQLException {

		ParseResult ret = new ParseResult();

		int i = num - 1;
		if (i == aliasNames.length) {
			/*
			 * RowNumberが自動追加された場合などは数が合わなくなる。
			 * この場合は、自動追加される場合は必ず最終カラムに追加するルールとする事で回避する
			 * RowNumber as XXXのエイリアス名（ラベル）を指定して撥ねる事も考えられるが、
			 * 同名のカラム名が存在しないとも限らない（あるいはユーザがRowNumberも欲しがるかもしれない）ので。
			 */
			ret.available = false;
			return ret;
		}

		ret.available = true;
		String aliasName = aliasNames[i];
		String[] tmp = aliasName.split("\\s*\\.\\s*");
		if (tmp.length == 3) {
			// <schema>.<tbl>.<col>
			ret.tblName = tmp[1];
			ret.colName = tmp[2];
		} else if (tmp.length == 2) {
			// <tbl>.<col>
			ret.tblName = tmp[0];
			ret.colName = tmp[1];
		} else if (tmp.length == 1) {
			// <col>
			ret.tblName = null;
			ret.colName = tmp[0];
		} else {
			throw new RuntimeException();
		}

//		ResultSetMetaData rsmd = rs.getMetaData();
//
//		logger.debug("getSchemaName : " + rsmd.getSchemaName(num));
//		logger.debug("getTableName : " + rsmd.getTableName(num));
//		logger.debug("getColumnName : " + rsmd.getColumnName(num));
//		logger.debug("getColumnLabel : " + rsmd.getColumnLabel(num));
//
//		ret.tblName = rsmd.getTableName(num);
//		ret.colName = rsmd.getColumnLabel(num);
////		ret.colLabel = rsmd.getColumnLabel(num);
////    	if (ret.columnName == null)
////    		ret.columnName = rsmd.getColumnName(num);
//    	if (ret.colName == null)
//			throw new RuntimeException("Cannot get colName(or colLabel) from metadata." +
//					" So, we cannot know where to put this column-result.");
    	return ret;
	}
}
