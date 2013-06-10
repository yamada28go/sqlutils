package jp.gr.java_conf.sqlutils.core.persistorhandler;

import java.sql.Timestamp;
import java.util.Date;

import jp.gr.java_conf.sqlutils.DBManager;
import jp.gr.java_conf.sqlutils.common.util.SqlDateUtil;
import jp.gr.java_conf.sqlutils.core.dto.IColumn;

public class PersistorHandler {

	@Deprecated
	public static class PersistorHandlerFactory {
		public InsertHandler newInsertHandler(DBManager manager) {
			return new InsertHandler(manager);
		}
		public UpdateHandler newUpdateHandler(DBManager manager) {
			return new UpdateHandler(manager);
		}
		public DeleteHandler newDeleteHandler(DBManager manager) {
			return new DeleteHandler(manager);
		}
		public SelectHandler newSelectHandler(DBManager manager) {
			return new SelectHandler(manager);
		}
		public LogicalDeleteHandler newLogicalDeleteHandler(DBManager manager) {
			return new LogicalDeleteHandler(manager);
		}
	}


	protected DBManager manager;
	protected boolean appendSchemaToSql = false;


	public PersistorHandler(DBManager manager) {
		this.manager = manager;
	}

	protected Object getOptLockNewValue(IColumn<?> col, Object currentVal) {
		if (col.getDataType() == Timestamp.class)
			return SqlDateUtil.getTimestamp(new Date());
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

	protected boolean getDeletedFlagValue() {
		return true;
	}

	public void setAppendSchemaToSql(boolean appendSchemaToSql) {
		this.appendSchemaToSql = appendSchemaToSql;
	}
}
