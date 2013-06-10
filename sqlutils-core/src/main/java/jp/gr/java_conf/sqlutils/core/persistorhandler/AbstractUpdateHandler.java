package jp.gr.java_conf.sqlutils.core.persistorhandler;

import java.util.Collection;

import jp.gr.java_conf.sqlutils.DBManager;
import jp.gr.java_conf.sqlutils.core.dto.IColumn;
import jp.gr.java_conf.sqlutils.core.dto.IDto.IPersistable;
import jp.gr.java_conf.sqlutils.core.exception.NoSuchColumnException;

public class AbstractUpdateHandler extends PersistorHandler {

	public AbstractUpdateHandler(DBManager manager) {
		super(manager);
	}


	protected String createUpdateSql(String tblName,
			Collection<IColumn<?>> keyCols,
			Collection<IColumn<?>> attrCols,
			IPersistable dto,
			boolean ignoreNullColumn) throws NoSuchColumnException {

		StringBuilder sb = new StringBuilder();
		sb.append("update ").append(tblName).append(" set ");
		for (IColumn<?> c : attrCols) {
			if (ignoreNullColumn == false || dto.get(c.name()) != null)
				sb.append(c.name()).append("=?,");
		}
		sb.deleteCharAt(sb.length() - 1);
		sb.append(createWhereBlockSql(keyCols));
		return sb.toString();
	}

	protected String createDeleteSql(String tblName, Collection<IColumn<?>> keyCols) {
		return "delete from " + tblName + createWhereBlockSql(keyCols);
	}

	protected String createWhereBlockSql(Collection<IColumn<?>> keyCols) {
		StringBuilder sb = new StringBuilder();
		sb.append(" where ");
		for (IColumn<?> c : keyCols) {
			sb.append(c.name()).append("=? and ");
		}
		sb.delete(sb.length() - 4, sb.length());
		return sb.toString();
	}
}
