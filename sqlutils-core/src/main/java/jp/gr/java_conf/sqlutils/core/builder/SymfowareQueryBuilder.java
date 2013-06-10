package jp.gr.java_conf.sqlutils.core.builder;

import jp.gr.java_conf.sqlutils.core.builder.BuilderElement.IAliasSelectable;
import jp.gr.java_conf.sqlutils.core.builder.BuilderElement.JoinedTbl;
import jp.gr.java_conf.sqlutils.core.dto.IColumn;

public class SymfowareQueryBuilder extends QueryBuilder {


	public String getGetSequenceValSql(String seqName) {
		return "select " + seqName + ".nextval from RDBII_SYSTEM.RDBII_ASSISTTABLE";
	}

	public String buildQuery() {

		if (selectables.size() == 0) throw new RuntimeException();
		if (from == null) throw new RuntimeException();
		if (limit == null && offset != null) throw new RuntimeException();
		if (limit != null && offset == null) throw new RuntimeException();

		prepareLogicalDelete();

		StringBuilder sb = new StringBuilder();
		if (limit == null && offset == null) {// limit,offsetが指定されていない場合

			appendSelectStmt(sb);
			appendFromStmt(sb);
			appendJoinStmt(sb);
			appendWhereStmt(sb);
			appendGroupByStmt(sb);
			appendHavingStmt(sb);
			appendOrderByStmt(sb);
//			appendLimitOffsetStmt(sb);
			appendForUpdateStmt(sb);
			return sb.toString();

		} else {

			/*
			 * select * from (
			 * select
			 *   <>___<>...
			 *   ,rownum as ___ROW_NUMBER___,
			 *
			 * from (
			 * 		select
			 * 		 SOME_COLUMN as <>___<>*
			 * 		 from TABLE
			 * 		 join TABLES..
			 * 		 ORDER BY ..
			 * ) as SUB___
			 * ) as MAIN___
			 * where MAIN___.___ROW_NUMBER___ between FROM and TO;
			 */
			int from = offset + 1;
			int to = offset + limit;
			sb.append("select * from (");
				appendSelectParentStmt(sb);
				sb.append(",rownum as ").append(getRowNumberColName()).append(" ");
				sb.append("from (");
					appendSelectSubStmt(sb);
					appendFromStmt(sb);
					appendJoinStmt(sb);
					appendWhereStmt(sb);
					appendGroupByStmt(sb);
					appendHavingStmt(sb);
					appendOrderByStmt(sb);
				sb.append(") SUB___ ");
			sb.append(") MAIN___ ");
			sb.append(" where MAIN___.").append(getRowNumberColName()).append(" between ")
				.append(from).append(" and ").append(to);
			appendForUpdateStmt(sb);
			return sb.toString();
		}
	}

	protected void appendSelectParentStmt(StringBuilder sb) {
		sb.append("select ");
		if (selectables.get(0) == COLUMN_ALL) {
			int i = 1;
			for (@SuppressWarnings("unused") IColumn<?> c : from.tbl.getCols()) {
	//			sb.append(from.name(isAppendSchemaName())).append(getTableColSeparator()).append(c.name());
				sb.append("COL").append(i++);
				sb.append(",");
			}
			for (JoinedTbl j : joins) {
				for (@SuppressWarnings("unused") IColumn<?> c : j.tbl.tbl.getCols()) {
	//				sb.append(j.tbl.name(isAppendSchemaName())).append(getTableColSeparator()).append(c.name());
					sb.append("COL").append(i++);
					sb.append(",");
				}
			}
			sb.deleteCharAt(sb.length() - 1);
		} else {
			int i = 1;
			for (IAliasSelectable s : selectables) {
	//			if (s instanceof SelectFunc) 検証してないけど多分OK
	//				throw new RuntimeException("");

	//			sb.append(s.fullname(isAppendSchemaName()).replace(".", getTableColSeparator()));
				if (s.alias() != null)
					sb.append(s.alias());
				else
					sb.append("COL").append(i++);
				sb.append(",");
			}
			sb.deleteCharAt(sb.length() - 1);
		}
	}

	protected void appendSelectSubStmt(StringBuilder sb) {
		sb.append("select ");
		if (selectables.get(0) == COLUMN_ALL) {
			int i = 1;
			for (IColumn<?> c : from.tbl.getCols()) {
				sb.append(from.name(isAppendSchemaName())).append(".").append(c.name());
	//			sb.append(" as ").append(from.name(isAppendSchemaName())).append(getTableColSeparator()).append(c.name());
				sb.append(" as COL").append(i++);
				sb.append(",");
			}
			for (JoinedTbl j : joins) {
				for (IColumn<?> c : j.tbl.tbl.getCols()) {
					sb.append(j.tbl.name(isAppendSchemaName())).append(".").append(c.name());
	//				sb.append(" as ").append(j.tbl.name(isAppendSchemaName())).append(getTableColSeparator()).append(c.name());
					sb.append(" as COL").append(i++);
					sb.append(",");
				}
			}
			sb.deleteCharAt(sb.length() - 1);
		} else {
			int i = 1;
			for (IAliasSelectable s : selectables) {
	//			if (s instanceof SelectFunc)
	//				throw new RuntimeException("");

				sb.append(s.fullname(isAppendSchemaName()));
				if (s.alias() != null)
					sb.append(" as ").append(s.alias());
				else
					sb.append(" as COL").append(i++);
				sb.append(",");
			}
			sb.deleteCharAt(sb.length() - 1);
		}
	}

	/**
	 * Symfowareは半角全角混在がNGなので、
	 * カラム名が日本語の場合、連結文字も日本語にする事
	 */
	protected String getRowNumberColName() {
		return "___";
	}
}
