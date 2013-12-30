package jp.gr.java_conf.sqlutils.generator.dto;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import jp.gr.java_conf.sqlutils.date.SqlDate;
import jp.gr.java_conf.sqlutils.date.SqlTime;
import jp.gr.java_conf.sqlutils.generator.dto.config.IColValueConverter;


/**
 * DBMSによって、メタデータ関連は大文字小文字に関わる問題がいろいろあるので、Pluginとして持ち出す
 *
 */
public class DtoGeneratorPlugin4CPP extends DtoGeneratorPlugin{

	/*
	 * C++用に独自の型定義を行う
	 * */
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
			ret.dtoFieldClassType = "std::string";
			ret.getWrapperType = "wrap_string";
			break;

		case Types.BINARY:
		case Types.VARBINARY:
			ret.dtoFieldClassType = "std::vector< char >";//byte[].class.getName();
			break;

		case Types.TINYINT:
		case Types.SMALLINT:
			ret.dtoFieldClassType = "std::int8_t";
			ret.getFromDtoConversion = "wrap_int8_t";
			ret.setToDtoConversion = "val == null ? null : ((java.lang.Number)val).shortValue()"; // シーケンス自動補完機能時、シーケンス取得SQLの戻り値の型が合わないDBMSがあるため
			break;

		case Types.INTEGER:
			ret.dtoFieldClassType = "int32_t";
			ret.getWrapperType = "wrap_int32_t";
			ret.setToDtoConversion = "val == null ? null : ((java.lang.Number)val).intValue()"; // 同上
			break;

		case Types.BIGINT: // PostgresのOID型もここ
			ret.dtoFieldClassType = "std::int64_t";
			ret.getWrapperType = "wrap_int64_t";
			ret.setToDtoConversion = "val == null ? null : ((java.lang.Number)val).longValue()"; // 同上
			 break;

		case Types.DECIMAL:
		case Types.NUMERIC:
		{
			throw new RuntimeException("DECIMALとNUMERICは未サポートの型です。");
		}
		case Types.FLOAT:
			ret.dtoFieldClassType ="double";
			ret.getWrapperType = "wrap_double";
			break;

		case Types.DOUBLE:
		case Types.REAL: // postgresはREAL＝FLOAT,MySQLは設定によって変わる。のでDoubleにまとめる
			ret.dtoFieldClassType = "double";
			ret.getWrapperType = "wrap_double";
			break;

		case Types.BIT: 	// PostgresのBooleanはこれ。但し他にもBIT型などがここに入るが、それらはサポートしない
		case Types.BOOLEAN: // MySQLのBooleanは、実体はTINYINT（＝MySQLではBooleanはサポートされてない）なのでここに入らない
			ret.dtoFieldClassType = "bool";
			ret.getWrapperType = "wrap_bool";
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


}
