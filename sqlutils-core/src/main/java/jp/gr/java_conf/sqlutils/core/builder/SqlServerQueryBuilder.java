package jp.gr.java_conf.sqlutils.core.builder;


public class SqlServerQueryBuilder extends QueryBuilder {


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
			 * select *
			 *  from (
			 * 		select
			 * 		 SOME_COLUMNS...
			 * 		,row_number() over (ORDER BY ..) as ___ROW_NUMBER___
			 * 		 from TABLE
			 * 		 join TABLES..
			 * ) as MAIN___
			 * where MAIN___.___ROW_NUMBER___ between FROM and TO;
			 */

			// カウントに注意
			int from = offset * limit + 1;
			int to = from + limit - 1;

			sb.append("select * from (");
				appendSelectStmt(sb);
				sb.append(",row_number() over (");
				appendOrderByStmt(sb);
				sb.append(") as ").append(getRowNumberColName()).append(" ");
				appendFromStmt(sb);
				appendJoinStmt(sb);
				appendWhereStmt(sb);
				appendGroupByStmt(sb);
				appendHavingStmt(sb);
			sb.append(") MAIN___ ");
			sb.append(" where MAIN___.").append(getRowNumberColName()).append(" between ")
				.append(from).append(" and ").append(to);
			appendForUpdateStmt(sb);
			return sb.toString();

		}
	}

	protected String getRowNumberColName() {
		return "___";
	}
}
