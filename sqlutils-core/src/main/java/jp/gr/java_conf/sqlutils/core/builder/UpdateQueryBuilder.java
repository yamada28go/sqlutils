package jp.gr.java_conf.sqlutils.core.builder;

import java.util.ArrayList;
import java.util.List;

import jp.gr.java_conf.sqlutils.core.dto.IColumn;
import jp.gr.java_conf.sqlutils.core.dto.ITable;

public class UpdateQueryBuilder extends ConditionBuilder {

	private enum Mode { UPDATE, DELETE }

	private static class UpdateSet {
		public <T> UpdateSet(IColumn<T> col, T val) {
			this.col = col;
			this.val = val;
		}
		IColumn<?> col;
		Object val;
	}

	private static final IConditionElement VOID_CONDITION = new IConditionElement() {
		@Override
		public String toQuery(boolean appendSchema) {
			return "";
		}
		@Override
		public Object[] getArgs() {
			return null;
		}
	};


	/*********************************************************************************/

	public static UpdateQueryBuilder get(String dbms) {
		return new UpdateQueryBuilder();
	}

	/*********************************************************************************/

	private boolean appendSchemaToSql = false;

	private Mode mode;
	private ITable tbl;
	private List<UpdateSet> sets;
	private IConditionElement rootWhere;


	public UpdateQueryBuilder setAppendSchemaToSql(boolean appendSchemaToSql) {
		this.appendSchemaToSql = appendSchemaToSql;
		return this;
	}

	public UpdateQueryBuilder update(ITable tbl) {
		this.tbl = tbl;
		mode = Mode.UPDATE;
		return this;
	}

	public UpdateQueryBuilder updateAllRecords(ITable tbl) {
		update(tbl);
		where(VOID_CONDITION);
		return this;
	}

	public <T> UpdateQueryBuilder set(IColumn<T> col, T val) {
		if (mode != Mode.UPDATE) throw new RuntimeException();
		if (sets == null)
			sets = new ArrayList<UpdateSet>();
		sets.add(new UpdateSet(col, val));
		return this;
	}


	public UpdateQueryBuilder deleteFrom(ITable tbl) {
		this.tbl = tbl;
		mode = Mode.DELETE;
		return this;
	}

	public UpdateQueryBuilder deleteAllRecords(ITable tbl) {
		deleteFrom(tbl);
		where(VOID_CONDITION);
		return this;
	}


	public UpdateQueryBuilder where(IConditionElement condition) {
		rootWhere = condition;
		return this;
	}


	public String createSql(/*DBMS dbms*/) {
		if (mode == null) throw new RuntimeException();
		if (mode == Mode.UPDATE && (sets == null || sets.isEmpty())) throw new RuntimeException();
		if (rootWhere == null) throw new RuntimeException();

		StringBuilder sb = new StringBuilder();
		switch(mode) {
		case DELETE:
			sb.append("delete from ").append(tbl.name(appendSchemaToSql));
			break;

		case UPDATE:
			sb.append("update ").append(tbl.name(appendSchemaToSql)).append(" set ");
			for (UpdateSet set : sets) {
				sb.append(set.col.name()).append("=?,");
			}
			sb.deleteCharAt(sb.length() - 1);
		}

		if (rootWhere != VOID_CONDITION)
			sb.append(" where ").append(rootWhere.toQuery(appendSchemaToSql));
		return sb.toString();
	}

	public Object[] getUpdatePrms() {
		List<Object> ret = new ArrayList<Object>();
		if (sets != null)
			for (UpdateSet set : sets) {
				ret.add(set.val);
			}
		if (rootWhere != VOID_CONDITION)
			for (Object wherePrm : rootWhere.getArgs()) {
				ret.add(wherePrm);
			}
		return ret.toArray();
	}
}
