package jp.gr.java_conf.sqlutils.generator.dto;

import java.math.BigDecimal;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import jp.gr.java_conf.sqlutils.date.SqlDate;
import jp.gr.java_conf.sqlutils.date.SqlTime;
import jp.gr.java_conf.sqlutils.generator.dto.config.ColumnNameResolvers;
import jp.gr.java_conf.sqlutils.generator.dto.config.IColValueConverter;
import jp.gr.java_conf.sqlutils.generator.dto.config.TableNameResolvers;
import jp.gr.java_conf.sqlutils.generator.jdbc.ColumnInfo;
import jp.gr.java_conf.sqlutils.generator.jdbc.TableInfo;


/**
 * DBMSによって、メタデータ関連は大文字小文字に関わる問題がいろいろあるので、Pluginとして持ち出す
 *
 */
public class DtoGeneratorPlugin {

	public ResultSet getTables(DatabaseMetaData dbmd, String schema) throws SQLException {
		return dbmd.getTables(null, schema, null, new String[]{"TABLE"});
	}

	public ResultSet getViews(DatabaseMetaData dbmd, String schema) throws SQLException {
		return dbmd.getTables(null, schema, null, new String[]{"VIEW"});
	}

	public List<String> getPrimaryKeys(DatabaseMetaData dbmd, String schema, String tableName) throws SQLException {
		List<String> primaryKeys = new ArrayList<String>();
		ResultSet keys = dbmd.getPrimaryKeys(null, schema, tableName);
		while(keys.next()) {
			primaryKeys.add(keys.getString("COLUMN_NAME"));
		}
		return primaryKeys;
	}

	public ResultSet getCols(DatabaseMetaData dbmd, String schema, String tableName) throws SQLException {
		return dbmd.getColumns(null, schema, tableName, null);
	}

	public TableInfo createTable(ResultSet rs, DatabaseMetaData dbmd, boolean isView) throws SQLException {
		TableInfo tbl = new TableInfo();
		tbl.schema = rs.getString("TABLE_SCHEM");
		tbl.name = rs.getString("TABLE_NAME");
		tbl.remarks = getTableRemarks(rs, dbmd);
		tbl.isView = isView;

		TableNameResolvers resolver = DtoGenerator.CONFIG.getTblNameResolver(tbl.name);
		tbl.dtoClassName = resolver.dtoClassNameResolver.resolve(tbl.name);
		tbl.definitionName = resolver.definitionNameResolver.resolve(tbl.name);

		return tbl;
	}

	public String getTableRemarks(ResultSet rs, DatabaseMetaData dbmd) throws SQLException {
		return rs.getString("REMARKS");
	}

	public String getColumnRemarks(ResultSet rs, DatabaseMetaData dbmd) throws SQLException {
		return rs.getString("REMARKS");
	}

	public ColumnInfo createColumn(ResultSet rs, DatabaseMetaData dbmd, TableInfo tbl) throws SQLException {
		ColumnInfo col = new ColumnInfo();
		col.schema = tbl.schema;
		col.tblName = tbl.name; //rs.getString("TABLE_NAME");
		col.name = rs.getString("COLUMN_NAME");
		col.nullable = rs.getString("IS_NULLABLE");
		col.dataType = rs.getInt("DATA_TYPE");
		col.dataTypeName = rs.getString("TYPE_NAME");
		col.size = rs.getInt("COLUMN_SIZE");
		col.defaultValue = rs.getString("COLUMN_DEF");
		col.remarks = getColumnRemarks(rs, dbmd);

		col.isPrimaryKey = false;
		if (tbl.isView == false) {
			if (tbl.primaryKeys.contains(col.name))
				col.isPrimaryKey = true;
		}

		col.isAutoIncrement = rs.getBoolean("IS_AUTOINCREMENT");

		// DTOフィールド名
		ColumnNameResolvers resolver = DtoGenerator.CONFIG.getColNameResolver(col.tblName, col.name);
		col.dtoFieldName = resolver.dtoFieldNameResolver.resolve(col.name);
		col.definitionName = resolver.definitionNameResolver.resolve(col.name);

		// DTOフィールドの型および変換式
		IColValueConverter c = DtoGenerator.CONFIG.getColValueConverter(col.tblName, col.name);
		if (c == null)
			c = getDataTypeConverter(rs, dbmd);
		col.dtoFieldClassType = c.getDtoFieldClassType();
		col.getWrapperType = c.getWrapperType();
		col.setToDtoConversion = c.getSetToDtoConversion();
		col.getFromDtoConversion = c.getGetFromDtoConversion(col.dtoFieldName);

		// カラム設定を保持
		col.config = DtoGenerator.CONFIG.getColumnSetting(col.tblName, col.name);

		return col;
	}

	public IColValueConverter getDataTypeConverter(ResultSet colMetaData, DatabaseMetaData dbmd) throws SQLException {

		IColValueConverter.ColValueConverter ret = new IColValueConverter.ColValueConverter();
		ret.getFromDtoConversion = "this." + IColValueConverter.FIELDNAME_PLACEHOLDER; // DTOフィールド名は、このConverterが生成された時点では未定なのでプレースホルダを使用

		int type = colMetaData.getInt("DATA_TYPE");
		@SuppressWarnings("unused")
		String typeName = colMetaData.getString("TYPE_NAME");

		// その他
		switch(type) {
		case Types.CHAR: // 固定長の場合でも、DBMSによってはトリムされた値が返る。MySQLのEnum型もここ
		case Types.VARCHAR:
		case Types.LONGVARCHAR:
			ret.dtoFieldClassType = String.class.getName();
			break;

		case Types.BINARY:
		case Types.VARBINARY:
			ret.dtoFieldClassType = "byte[]";//byte[].class.getName();
			break;

		case Types.TINYINT:
		case Types.SMALLINT:
			ret.dtoFieldClassType = Short.class.getName();
			ret.setToDtoConversion = "val == null ? null : ((java.lang.Number)val).shortValue()"; // シーケンス自動補完機能時、シーケンス取得SQLの戻り値の型が合わないDBMSがあるため
			break;

		case Types.INTEGER:
			ret.dtoFieldClassType = Integer.class.getName();
			ret.setToDtoConversion = "val == null ? null : ((java.lang.Number)val).intValue()"; // 同上
			break;

		case Types.BIGINT: // PostgresのOID型もここ
			ret.dtoFieldClassType = Long.class.getName();
			ret.setToDtoConversion = "val == null ? null : ((java.lang.Number)val).longValue()"; // 同上
			 break;

		case Types.DECIMAL:
		case Types.NUMERIC:
			ret.dtoFieldClassType = BigDecimal.class.getName();
			break;

		case Types.FLOAT:
			ret.dtoFieldClassType = Float.class.getName();
			break;

		case Types.DOUBLE:
		case Types.REAL: // postgresはREAL＝FLOAT,MySQLは設定によって変わる。のでDoubleにまとめる
			ret.dtoFieldClassType = Double.class.getName();
			break;

		case Types.BIT: 	// PostgresのBooleanはこれ。但し他にもBIT型などがここに入るが、それらはサポートしない
		case Types.BOOLEAN: // MySQLのBooleanは、実体はTINYINT（＝MySQLではBooleanはサポートされてない）なのでここに入らない
			ret.dtoFieldClassType = Boolean.class.getName();
			break;

		case Types.DATE:
			// Oracle10g-DATE型
			ret.dtoFieldClassType = SqlDate.class.getName();
			ret.setToDtoConversion = "val == null ? null : " + SqlDate.class.getName() + ".getInstance((java.util.Date)val)";
			break;

		case Types.TIMESTAMP:
			// Oracle10g-TIMESTAMP型
			// Oracle11g-DATE型、TIMESTAMP型
			ret.dtoFieldClassType = getTimestampTypeJdbcClass().getName();
			break;

		case Types.TIME:
			ret.dtoFieldClassType = SqlTime.class.getName();
			ret.setToDtoConversion = "val == null ? null : " + SqlTime.class.getName() + ".getInstance((java.util.Date)val)";
			break;

		case Types.BLOB:
		case Types.CLOB:
		case Types.LONGVARBINARY:
			ret.dtoFieldClassType = Object.class.getName();
			break;

		default: // SQL99で規定されていない独自型は全部ここ
			ret.dtoFieldClassType = Object.class.getName();
			break;
//			throw new RuntimeException(
//					String.format(
//							"unknown column type = [%s(%s)] at table=[%s],col=[%s]",
//							typeName, type, tbl.name, col.name));
		}

		if (ret.setToDtoConversion == null)
			if (ret.dtoFieldClassType.equals(Object.class.getName()))
				ret.setToDtoConversion = "val";
			else
				ret.setToDtoConversion = "(" + ret.dtoFieldClassType + ")val";
		return ret;
	}

	/**
	 * カラム型がTypes.TIMESTAMPの場合に、実際に取得処理時にResultSetから取得されるデータ型は、
	 * DBMSによって異なる場合がある（java.util.Date/java.sql.Timestamp）ため
	 */
	public Class<?> getTimestampTypeJdbcClass() {
		return Timestamp.class;
	}
}
