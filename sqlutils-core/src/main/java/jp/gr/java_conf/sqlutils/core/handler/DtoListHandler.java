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



/**
 * 検索結果をDTOに格納して返却するHandler.<br/>
 * @see org.apache.commons.dbutils.ResultSetHandler
 */
public class DtoListHandler<T extends IDto> extends AbstractListHandler<T>{

	@SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.getLogger(DtoListHandler.class);


	private Class<? extends IDto>[] classes;
	private Map<String, Class<IDto>> tblDtoMap;
	private ResultSetParser parser;


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


	@SuppressWarnings("unchecked")
	@Override
	protected T handleRow(ResultSet rs) throws SQLException {

		// 一件も見つからなかった場合、このメソッドは呼ばれない。

		if (parser == null)
			throw new RuntimeException("parser must not null. please set with 'setColNameParser'");


		T instance = (T) DtoSet.create(classes);

		Object val = null;

        ResultSetMetaData rsmd = rs.getMetaData();
        int cols = rsmd.getColumnCount();
        for (int i = 1; i <= cols; i++) { // jdbc系APIのindexは1から

        	val = rs.getObject(i);

        	ParseResult ret = parser.parse(rs, i);
        	if (!ret.available)
        		continue;


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
