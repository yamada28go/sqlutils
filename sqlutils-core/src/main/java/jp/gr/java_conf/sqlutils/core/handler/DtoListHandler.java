package jp.gr.java_conf.sqlutils.core.handler;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import jp.gr.java_conf.sqlutils.core.dto.DtoSet;
import jp.gr.java_conf.sqlutils.core.dto.IDto;
import jp.gr.java_conf.sqlutils.core.exception.CastFailureOnDtoSetException;
import jp.gr.java_conf.sqlutils.core.exception.MissingDtoException;
import jp.gr.java_conf.sqlutils.core.exception.NoSuchColumnException;
import jp.gr.java_conf.sqlutils.core.exception.NoSuchFieldOnDtoException;
import jp.gr.java_conf.sqlutils.core.exception.NoSuchFieldOrMissingDtoException;
import jp.gr.java_conf.sqlutils.core.exception.TableNameRetrieveFailureException;
import jp.gr.java_conf.sqlutils.core.handler.ResultSetParser.ParseResult;

import org.apache.commons.dbutils.handlers.AbstractListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class DtoListHandler<T extends IDto> extends AbstractListHandler<T>{

	@SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.getLogger(DtoListHandler.class);



//	IColNameProvider parser;
//	@SuppressWarnings("unused")
//	private String sql;
//	private String[] selectCols;
//	private String[][] tblAlias;
	private Class<? extends IDto>[] classes;
	private Map<String, Class<IDto>> tblDtoMap;
//	private String rowNumberColName;
	private ResultSetParser parser;

//	public DtoListHandler(Class<? extends IDto> c1) {
//		this(null, new Class<?>[]{c1});
//	}
//
//	public DtoListHandler(Class<? extends IDto> c1, Class<? extends IDto> c2) {
//		this(null, new Class<?>[]{c1,c2});
//	}
//
//	public DtoListHandler(Class<? extends IDto> c1, Class<? extends IDto> c2, Class<? extends IDto> c3) {
//		this(null, new Class<?>[]{c1,c2,c3});
//	}
//
//	public DtoListHandler(Class<? extends IDto> c1, Class<? extends IDto> c2, Class<? extends IDto> c3, Class<? extends IDto> c4) {
//		this(null, new Class<?>[]{c1,c2,c3,c4});
//	}
//
//	public DtoListHandler(Class<? extends IDto> c1, Class<? extends IDto> c2, Class<? extends IDto> c3, Class<? extends IDto> c4, Class<? extends IDto> c5) {
//		this(null, new Class<?>[]{c1,c2,c3,c4,c5});
//	}
//
//	public DtoListHandler(Class<? extends IDto> c1, Class<? extends IDto> c2, Class<? extends IDto> c3, Class<? extends IDto> c4, Class<? extends IDto> c5, Class<? extends IDto> c6) {
//		this(null, new Class<?>[]{c1,c2,c3,c4,c5,c6});
//	}
//
//	public DtoListHandler(Class<? extends IDto> c1, Class<? extends IDto> c2, Class<? extends IDto> c3, Class<? extends IDto> c4, Class<? extends IDto> c5, Class<? extends IDto> c6, Class<? extends IDto> c7) {
//		this(null, new Class<?>[]{c1,c2,c3,c4,c5,c6,c7});
//	}
//
//	public DtoListHandler(Class<? extends IDto> c1, Class<? extends IDto> c2, Class<? extends IDto> c3, Class<? extends IDto> c4, Class<? extends IDto> c5, Class<? extends IDto> c6, Class<? extends IDto> c7, Class<? extends IDto> c8) {
//		this(null, new Class<?>[]{c1,c2,c3,c4,c5,c6,c7,c8});
//	}

	public DtoListHandler(ResultSetParser parser, Class<? extends IDto> c1) {
		this(parser, new Class<?>[]{c1});
	}

	public DtoListHandler(ResultSetParser parser, Class<? extends IDto> c1, Class<? extends IDto> c2) {
		this(parser, new Class<?>[]{c1,c2});
	}

	public DtoListHandler(ResultSetParser parser, Class<? extends IDto> c1, Class<? extends IDto> c2, Class<? extends IDto> c3) {
		this(parser, new Class<?>[]{c1,c2,c3});
	}

	public DtoListHandler(ResultSetParser parser, Class<? extends IDto> c1, Class<? extends IDto> c2, Class<? extends IDto> c3, Class<? extends IDto> c4) {
		this(parser, new Class<?>[]{c1,c2,c3,c4});
	}

	public DtoListHandler(ResultSetParser parser, Class<? extends IDto> c1, Class<? extends IDto> c2, Class<? extends IDto> c3, Class<? extends IDto> c4, Class<? extends IDto> c5) {
		this(parser, new Class<?>[]{c1,c2,c3,c4,c5});
	}

	public DtoListHandler(ResultSetParser parser, Class<? extends IDto> c1, Class<? extends IDto> c2, Class<? extends IDto> c3, Class<? extends IDto> c4, Class<? extends IDto> c5, Class<? extends IDto> c6) {
		this(parser, new Class<?>[]{c1,c2,c3,c4,c5,c6});
	}

	public DtoListHandler(ResultSetParser parser, Class<? extends IDto> c1, Class<? extends IDto> c2, Class<? extends IDto> c3, Class<? extends IDto> c4, Class<? extends IDto> c5, Class<? extends IDto> c6, Class<? extends IDto> c7) {
		this(parser, new Class<?>[]{c1,c2,c3,c4,c5,c6,c7});
	}

	public DtoListHandler(ResultSetParser parser, Class<? extends IDto> c1, Class<? extends IDto> c2, Class<? extends IDto> c3, Class<? extends IDto> c4, Class<? extends IDto> c5, Class<? extends IDto> c6, Class<? extends IDto> c7, Class<? extends IDto> c8) {
		this(parser, new Class<?>[]{c1,c2,c3,c4,c5,c6,c7,c8});
	}

	@SuppressWarnings("unchecked")
	public DtoListHandler(ResultSetParser parser, Class<?>[] classes) {
		this.classes = (Class<? extends IDto>[]) classes;
		this.tblDtoMap = new HashMap<String, Class<IDto>>();
		this.parser = parser;
	}


//	public void setColNameParser(IColNameProvider parser) {
//		this.parser = parser;
//	}
//
//	public void setRowNumberColName(String rowNumberColName) {
//		this.rowNumberColName = rowNumberColName;
//	}


	@SuppressWarnings("unchecked")
	@Override
	protected T handleRow(ResultSet rs) throws SQLException {

		if (parser == null)
			throw new RuntimeException("parser must not null. please set with 'setColNameParser'");

//		if (selectCols == null)
//			throw new RuntimeException("Please set 'sql' to DtoListHandler");

		// 一件も見つからなかった場合、そもそもここに来ない

		T instance = (T) DtoSet.create(classes);
//		String tblName = null;
//		String colName = null;
//		Map<String, String> rInfo;
		Object val = null;

        ResultSetMetaData rsmd = rs.getMetaData();
        int cols = rsmd.getColumnCount();
        for (int i = 1; i <= cols; i++) { // jdbc系APIのindexは1から

        	val = rs.getObject(i);
//        	if (trimFixLenChar && val instanceof String)
//        		val = ((String)val).trim();

        	ParseResult ret = parser.parse(rs, i);
        	if (!ret.available)
        		continue;
//        	if (rowNumberColName != null) {
//    			String label = rsmd.getColumnLabel(i);
//            	if (rowNumberColName.equals(label))
//            		continue;
//        	}
//        	tblName = parser.getTableName(rs, i);
//        	colName = parser.getColumnName(rs, i);
//        	logger.debug("tblname : {" + tblName + "}");
//        	logger.debug("colname : {" + colName + "}");



        	if (classes.length == 1) {
        		try {
        			instance.set(ret.colName, val);
            	} catch (ClassCastException e) {
        			throw new CastFailureOnDtoSetException((Class<IDto>) instance.getClass(), ret.colName, val);
				} catch (NoSuchColumnException e) {
					throw new NoSuchFieldOrMissingDtoException(ret.tblName, ret.colName);
				}

        	} else {
        		if (ret.tblName == null)
        			throw new TableNameRetrieveFailureException(i, ret.tblName, ret.colName);


        		DtoSet set = (DtoSet)instance;
        		Class<IDto> dtoClass = getAndCacheDtoClassWithTblName(set, ret.tblName);
        		if (dtoClass == null)
        			throw new MissingDtoException(ret.tblName);

            	try {
        			set.get(dtoClass).set(ret.colName, val);
            	} catch (ClassCastException e) {
        			throw new CastFailureOnDtoSetException(dtoClass, ret.colName, val);
				} catch (NoSuchColumnException e) {
					throw new NoSuchFieldOnDtoException(dtoClass, ret.colName);
				}
        	}
        }
		return instance;
	}

	@SuppressWarnings("unchecked")
	private Class<IDto> getAndCacheDtoClassWithTblName(DtoSet dtoSet, String tblName) {
		if (!tblDtoMap.containsKey(tblName)) {
			for (IDto dto : dtoSet.values()) {
				if (dto.getTableName().toUpperCase().equals(tblName.toUpperCase())) {
					tblDtoMap.put(tblName, (Class<IDto>) dto.getClass());
					break;
				}
			}
		}
		return tblDtoMap.get(tblName);
	}
}
