package jp.gr.java_conf.sqlutils.core.persistorhandler;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import jp.gr.java_conf.sqlutils.DBManager;
import jp.gr.java_conf.sqlutils.core.dto.IColumn;
import jp.gr.java_conf.sqlutils.core.dto.ITable;
import jp.gr.java_conf.sqlutils.core.dto.IDto.IPersistable;
import jp.gr.java_conf.sqlutils.core.exception.NoSuchColumnException;

import org.apache.commons.dbutils.handlers.ArrayHandler;

public class SelectHandler extends PersistorHandler {


	public SelectHandler(DBManager manager) {
		super(manager);
	}


	@SuppressWarnings("unchecked")
	public <T extends IPersistable> T exec(T dto, boolean forUpdate) {

		try {
			ITable tbl = dto.getTableDefinition();

			// カラム
			LinkedHashSet<IColumn<?>> whereCols = new LinkedHashSet<IColumn<?>>();
			if (tbl.getLogicalDeleteFlagCol() != null)
				whereCols.add(tbl.getLogicalDeleteFlagCol());
			for (IColumn<?> c : tbl.getCols()) {
				if (c.isPrimaryKey()/* || c.isDelFlag()*/) {
					whereCols.add(c);
				}
			}

			// exec sql
			String sql = createSelectSql(tbl, whereCols, forUpdate);
			List<Object> params = new ArrayList<Object>();
			for (IColumn<?> c : whereCols) {
				if (c.isPrimaryKey()) {
					Object val = dto.get(c.name());
// jdbcの仕様に従う
//					if (c.isFixedLenStr())
//						params.add(StringUtils.rightPad((String)val, c.getSize(), ' '));
//					else
						params.add(val);
				}
				else
					params.add(!getDeletedFlagValue());
			}
//			@SuppressWarnings("unchecked")
			Object[] rs = manager.execQuery(new ArrayHandler(), sql, params.toArray());
			if (rs == null)
				return null;
			T ret;
			try {
				ret = (T) dto.getClass().newInstance();
				for (int i = 0; i < tbl.getCols().length; i++) {
					IColumn<?> c = tbl.getCols()[i];
					ret.set(c.name(), rs[i]);
				}
				return ret;
			} catch (InstantiationException e) {
				throw new RuntimeException(e);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
//			DtoListHandler<T> handler = new DtoListHandler<T>(dto.getClass());
//			handler.setColNameParser(new SimpleSqlColNameProvider(sql));
//			List<T> list = manager.execQuery(handler, sql, params.toArray());
//			if (list.isEmpty())
//				return null;
//			else
//				return list.get(0);


		} catch (NoSuchColumnException e) {
			throw new RuntimeException("unexpected!");
		}
	}

	protected String createSelectSql(ITable tbl, LinkedHashSet<IColumn<?>> whereCols, boolean forUpdate) {
		StringBuilder sb = new StringBuilder();
		sb.append("select ");
		for (IColumn<?> c : tbl.getCols()) {
			sb.append(tbl.name(appendSchemaToSql)).append(".").append(c.name()).append(",");
		}
		sb.delete(sb.length() - 1, sb.length());
		sb.append(" from ").append(tbl.name(appendSchemaToSql));
		sb.append(" where ");
		for (IColumn<?> c : whereCols) {
			sb.append(c.name()).append("=? and ");
		}
		sb.delete(sb.length() - 4, sb.length());
		if (forUpdate)
			sb.append(" for update");
		return sb.toString();
	}
}
