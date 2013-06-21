package jp.gr.java_conf.sqlutils.core.builder;


public class OracleQueryBuilder extends QueryBuilder {


	public String getGetSequenceValSql(String seqName) {
		return "select " + seqName + ".nextval from dual";
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
			int from = offset + 1;
			int to = offset + limit;
			sb.append("select * from (");
				appendSelectStmt(sb);
				sb.append(",row_number() over ("); // ☆RowNumberは必ず最後に追加する事。ResultSetParserは、数が合わない場合には無視する仕様のため
				appendOrderByStmt(sb);
				sb.append(") as ").append(getRowNumberColName()).append(" ");
				appendFromStmt(sb);
				appendJoinStmt(sb);
				appendWhereStmt(sb);
				appendGroupByStmt(sb);
				appendHavingStmt(sb);
			sb.append(") MAIN___ "); // テーブルの別名定義に'as'を使うのは、対応してないVerのOracleが居るのでNG
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
