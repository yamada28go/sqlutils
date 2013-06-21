package jp.gr.java_conf.sqlutils.core.builder;

import java.util.ArrayList;
import java.util.List;

import jp.gr.java_conf.sqlutils.core.builder.BuilderElement.ColElement;
import jp.gr.java_conf.sqlutils.core.builder.BuilderElement.IAliasSelectable;
import jp.gr.java_conf.sqlutils.core.builder.BuilderElement.IGroupByColumn;
import jp.gr.java_conf.sqlutils.core.builder.BuilderElement.IOrderColumn;
import jp.gr.java_conf.sqlutils.core.builder.BuilderElement.IOrderElement;
import jp.gr.java_conf.sqlutils.core.builder.BuilderElement.ISelectColumn;
import jp.gr.java_conf.sqlutils.core.builder.BuilderElement.ISelectable;
import jp.gr.java_conf.sqlutils.core.builder.BuilderElement.ITblElement;
import jp.gr.java_conf.sqlutils.core.builder.BuilderElement.JoinedTbl;
import jp.gr.java_conf.sqlutils.core.builder.BuilderElement.OrderElement;
import jp.gr.java_conf.sqlutils.core.builder.BuilderElement.RawOrderElement;
import jp.gr.java_conf.sqlutils.core.builder.BuilderElement.SelectFunc;
import jp.gr.java_conf.sqlutils.core.builder.BuilderElement.TblElement;
import jp.gr.java_conf.sqlutils.core.builder.BuilderElement.JoinedTbl.JoinType;
import jp.gr.java_conf.sqlutils.core.builder.ConditionBuilder.ConditionArray.ConditionType;
import jp.gr.java_conf.sqlutils.core.dto.IColumn;
import jp.gr.java_conf.sqlutils.core.dto.ITable;
import jp.gr.java_conf.sqlutils.core.handler.ResultSetParser;


/**
 *
 * static methods
 * use with static-import like
 * <pre>
 * import static jp.gr.java_conf.sqlutils.core.builder.QueryBuilder.*;
 * </pre>
 */
public class QueryBuilder extends ConditionBuilder {


	protected static final IAliasSelectable COLUMN_ALL = new IAliasSelectable() {
		@Override
		public String fullname(boolean appendSchema) {
			return null;
		}
		@Override
		public String alias() {
			return null;
		}
	};



	/**
	 * 'count(*)' function
	 */
	public static SelectFunc count() {
		return new SelectFunc("count(*)", null);
	}

	/**
	 * 'count([col])' function
	 */
	public static SelectFunc count(ISelectColumn<?> col) {
//		return new SelectFunc("count(" + col.fullname() + ")");
		return new SelectFunc("count(%s)", col);
	}

	/**
	 * 'sum([col])' function
	 */
	public static SelectFunc sum(ISelectColumn<?> col) {
//		return new SelectFunc("sum(" + col.fullname() + ")");
		return new SelectFunc("sum(%s)", col);
	}

	/**
	 * 'avg([col])' function
	 */
	public static SelectFunc avg(ISelectColumn<?> col) {
//		return new SelectFunc("avg(" + col.fullname() + ")");
		return new SelectFunc("avg(%s)", col);
	}

	/**
	 * 'max([col])' function
	 */
	public static SelectFunc max(ISelectColumn<?> col) {
//		return new SelectFunc("max(" + col.fullname() + ")");
		return new SelectFunc("max(%s)", col);
	}

	/**
	 * 'min([col])' function
	 */
	public static SelectFunc min(ISelectColumn<?> col) {
//		return new SelectFunc("min(" + col.fullname() + ")");
		return new SelectFunc("min(%s)", col);
	}

	/**
	 * select [expression],.... from ....
	 * 引数の文字列がそのままSelect句に埋め込まれる。
	 * SQL関数等を使用したい場合などの用途
	 * プレーンに取得してから、Javaレベルで加工等することを推奨します。
	 */
	public static SelectFunc rawStatement(String expression) {
		return new SelectFunc(expression, null);
	}


	/**
	 * select TBL.COL1, TBL.COL2,........ from ....
	 * 指定されたテーブルの全カラムをselect句に展開する
	 */
	public static ISelectColumn<?>[] columns(ITblElement tbl) {
		return TblElement.create(tbl).getCols();
	}



	/**
	 * select [col] as [alias],........ from ....
	 * ColumnにAlias（as句）を明示的に付加する
	 */
	public static <T> ColElement<T> as(ISelectColumn<T> col, String alias) {
		if (col instanceof ColElement) {
			ColElement<T> ret = (ColElement<T>) col;
			if (ret.alias != null) throw new RuntimeException();
			ret.alias = alias;
			return ret;
		} else {
			return new ColElement<T>((IColumn<T>) col, alias);
		}
	}

	/**
	 * select [function] as [alias],........ from ....
	 * 関数式にAlias（as句）を明示的に付加する
	 */
	public static SelectFunc as(SelectFunc function, String alias) {
		if (function.alias != null) throw new RuntimeException();
		function.alias = alias;
		return function;
	}


	/**
	 * select ..... from [tbl] as [alias]
	 * テーブルにAlias（as句）を付加する
	 */
	public static TblElement as(ITable tbl, String alias) {
		return new TblElement(tbl, alias);
	}


	/**
	 * 渡されたColumnをSQLに展開する（[テーブル名].[カラム名]）際に、[テーブル名]を指定されたAliasに置き換える
	 * Aliasを付けたテーブルのカラムを指定する場合に使用する
	 */
	public static <T> ColElement<T> tblalias(String tblAlias, ISelectColumn<T> col) {
		if (col instanceof ColElement) {
			ColElement<T> ret = (ColElement<T>) col;
			if (ret.tblAlias != null) throw new RuntimeException();
			ret.tblAlias = tblAlias;
			return ret;
		} else {
			return new ColElement<T>(tblAlias, (IColumn<T>) col);
		}
	}



	public static IOrderElement desc(IOrderColumn col) {
		return new OrderElement(col, false);
	}

	public static IOrderElement asc(IOrderColumn col) {
		return new OrderElement(col, true);
	}

	public static IOrderElement sortWith(IOrderColumn col, boolean isAsc) {
		return new OrderElement(col, isAsc);
	}


	/**
	 * 集計関数の結果カラムでOrderする場合、引数は文字列で指定する
	 */
	public static IOrderElement desc(String name) {
		return new RawOrderElement(name, false);
	}

	/**
	 * 集計関数の結果カラムでOrderする場合、引数は文字列で指定する
	 */
	public static IOrderElement asc(String name) {
		return new RawOrderElement(name, true);
	}


	/*********************************************************************************/

	public static QueryBuilder get(String dbms) {
		if ("POSTGRES".equals(dbms)) return new PostgresQueryBuilder();
		if ("MYSQL".equals(dbms)) return new MySqlQueryBuilder();
		if ("ORACLE".equals(dbms)) return new OracleQueryBuilder();
		if ("SQLSERVER".equals(dbms)) return new SqlServerQueryBuilder();
		return new QueryBuilder();
	}


	/*********************************************************************************/


	protected boolean distinct;
	protected ArrayList<IAliasSelectable> selectables;
	protected TblElement from;
	protected List<JoinedTbl> joins;
	protected IConditionElement rootWhere;
	protected IGroupByColumn[] groupables;
//	private IConditionElement rootHaving;
	protected String having;
	protected IOrderElement[] orderables;
	protected Integer limit;
	protected Integer offset;
	protected boolean forUpdate;
//	private ISelectColumn<?>[] forUpdateOfs;
	protected boolean containLogicalDeletedRecords;

//	protected String dbms;


	public QueryBuilder(/*String dbms*/) {
		selectables = new ArrayList<IAliasSelectable>();
		joins = new ArrayList<JoinedTbl>();
//		this.dbms = dbms;
//		rootWhere = new ConditionElements(ConditionType.AND, null);
	}

	public QueryBuilder selectAll() {
		throwIf(!this.selectables.isEmpty(), "Select statement is already setted.");
		this.selectables.add(COLUMN_ALL);
		return this;
	}

	public QueryBuilder selectDistinct(ISelectColumn<?>...selectables) {
		throwIf(!this.selectables.isEmpty(), "Select statement is already setted.");
		this.distinct = true;
		return select(selectables);
	}

	public QueryBuilder select(ISelectable...selectables) {
		throwIf(!this.selectables.isEmpty(), "Select statement is already setted.");
		if (selectables.length == 0) throw new RuntimeException();
		for (ISelectable s : selectables) {
			if (s instanceof ColElement<?>)
				this.selectables.add((ColElement<?>)s);
			else if (s instanceof SelectFunc)
				this.selectables.add((SelectFunc)s);
			else
				this.selectables.add(wrap((IColumn<?>)s));
		}
		return this;
	}

	public QueryBuilder selectCountAll() {
		throwIf(!this.selectables.isEmpty(), "Select statement is already setted.");
		select(count());
		return this;
	}


	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static ColElement<?> wrap(IColumn<?> col) {
		return new ColElement(col);
	}


	public QueryBuilder from(ITblElement fromTbl) {
		throwIf(this.from != null, "From statement is already setted.");
		this.from = TblElement.create(fromTbl);
		return this;
	}

	public QueryBuilder innerJoin(ITblElement tbl, IConditionElement on) {
		joins.add(new JoinedTbl(JoinType.INNER, TblElement.create(tbl), on));
		return this;
	}

	public QueryBuilder leftOuterJoin(ITblElement tbl, IConditionElement on) {
		joins.add(new JoinedTbl(JoinType.LEFT_OUTER, TblElement.create(tbl), on));
		return this;
	}

	public QueryBuilder where(IConditionElement condition) {
//		throwIf(this.rootWhere != null, "Where-condition is already setted.");
//		rootWhere = condition;
		if (rootWhere == null) {
			rootWhere = condition;
			return this;
		} else {
			if (rootWhere instanceof ConditionArray) {
				ConditionArray w = (ConditionArray)rootWhere;
				if (w.type == ConditionType.AND) {
					w.appendCondition(condition);
					return this;
				} else {
					// ROOTがOR条件の場合の条件区追加はNGとする
					throw new RuntimeException("exist rootWhere is Type-OR. So cannot add new condition.");
				}
			} else {
				IConditionElement t = rootWhere;
				rootWhere = new ConditionArray(ConditionType.AND, t, condition);
				return this;
			}
		}
	}

	public QueryBuilder groupBy(IGroupByColumn...groupables) {
		throwIf(this.groupables != null, "Group-by statement is already setted.");
		this.groupables = groupables;
		return this;
	}

// HAVING句の中身は、規定するのが困難なので、自由入力を前提とする
//	public QueryBuilder2 having(IConditionElement condition) {
//		this.rootHaving = condition;
//		return this;
//	}
	public QueryBuilder having(String condition) {
		throwIf(this.offset != null, "Offset statement is already setted.");
		if (this.having != null) throw new RuntimeException("Having statement is already setted.");
		this.having = condition;
		return this;
	}

	public QueryBuilder orderBy(IOrderElement...orderables) {
		throwIf(this.orderables != null, "Order-by statement is already setted.");
		this.orderables = orderables;
		return this;
	}

	public QueryBuilder limit(int limit) {
		throwIf(this.limit != null, "Limit statement is already setted.");
		this.limit = limit;
		return this;
	}

	public QueryBuilder offset(int offset) {
		throwIf(this.offset != null, "Offset statement is already setted.");
		this.offset = offset;
		return this;
	}

	public QueryBuilder forUpdate() {
		this.forUpdate = true;
		return this;
	}

//	/**
//	 * supported by Oracle,Postgres
//	 */
//	public QueryBuilder forUpdateOf(ISelectColumn...cols) {
//		this.forUpdate = true;
//		this.forUpdateOfs = cols;
//		return this;
//	}



	protected void throwIf(boolean condition, String errMsg) {
		if (condition) throw new RuntimeException(errMsg);
	}


	public QueryBuilder containLogicalDeletedRecords() {
		containLogicalDeletedRecords = true;
		return this;
	}





//	/**
//	 * Select文のカラムに別名（「 as <テーブル名>___<カラム名>」）を付けるか否か
//	 * defaultは、複数テーブルをJoinしていた場合に、区別のために付加される。
//	 * よって、常にfalseを返すような上書きをすると、区別できなくなるため正常動作しないので注意
//	 * 通常は、デフォルト値のままか、あるいは常にTrueを返すよううわがく事
//	 */
//	protected boolean isAppendAsStmt2SelectColSql() {
//		return joins.size() > 0;
//	}

	/**
	 * Select文中のテーブル名の前にスキーマ名を付加するか否か
	 */
	protected boolean isAppendSchemaName() {
		return false;
	}



//	public void appendWhere(IConditionElement c) {
//		if (rootWhere == null) {
//			rootWhere = c;
//			return;
//		}
//		if (rootWhere instanceof ConditionElements) {
//			ConditionElements w = (ConditionElements)rootWhere;
//			if (w.type == ConditionType.AND) {
//				w.appendCondition(c);
//				return;
//			}
//		}
//		IConditionElement t = rootWhere;
//		rootWhere = new ConditionElements(ConditionType.AND, t, c);
//	}

	public IConditionElement getWhere() {
		return rootWhere;
	}

	public Object[] getQueryPrms() {
		if (rootWhere == null)
			return null;
		else
			return rootWhere.getArgs();
	}

//	public String[] getQueryGetColNames() {
//		List<String> ret = new ArrayList<String>();
//		if (selectables.get(0) == COLUMN_ALL) {
//			for (IColumn<?> c : from.tbl.getCols()) {
//				ret.add(from.name(isAppendSchemaName()) + "." + c.name());
//			}
//			for (JoinedTbl j : joins) {
//				for (IColumn<?> c : j.tbl.tbl.getCols()) {
//					ret.add(j.tbl.name(isAppendSchemaName()) + "." + c.name());
//				}
//			}
//		} else {
//			for (IAliasSelectable s : selectables) {
//				if (s.alias() != null)
//					ret.add(s.alias());
//				else
//					ret.add(s.fullname(isAppendSchemaName()));
//			}
//		}
//		return ret.toArray(new String[0]);
//	}


	public String buildQuery(/*DBMS dbms*/) {

		if (selectables.size() == 0) throw new RuntimeException();
		if (from == null) throw new RuntimeException();
		if (limit == null && offset != null) throw new RuntimeException();
		if (limit != null && offset == null) throw new RuntimeException();

		prepareLogicalDelete();
//		if (!containLogicalDeletedRecords) {
//			if (from.tbl.isLogicalDeleting()) {
//				IColumn<Boolean> col = getLogicalDelFlagCol(from.tbl);
//				if (from.alias != null)
//					where(equal(tblalias(from.alias, col), !getDeletedFlagValue()));
//				else
//					where(equal(col, !getDeletedFlagValue()));
//			}
//			for (JoinedTbl j : joins) {
//				if (j.tbl.tbl.isLogicalDeleting()) {
//					IColumn<Boolean> col = getLogicalDelFlagCol(j.tbl.tbl);
//					if (j.tbl.alias != null)
//						where(equal(tblalias(j.tbl.alias, col), !getDeletedFlagValue()));
//					else
//						where(equal(col, !getDeletedFlagValue()));
//				}
//			}
//		}


		StringBuilder sb = new StringBuilder();
		appendSelectStmt(sb);
		appendFromStmt(sb);
		appendJoinStmt(sb);
		appendWhereStmt(sb);
		appendGroupByStmt(sb);
		appendHavingStmt(sb);
		appendOrderByStmt(sb);
		if (limit != null && offset != null)
			appendLimitOffsetStmt(sb);
		appendForUpdateStmt(sb);
		return sb.toString();



//		if ((limit == null && offset == null) // limit,offsetが指定されていない場合
//		|| (dbms != DBMS.ORACLE && dbms != DBMS.SQLSERVER && dbms != DBMS.SYMFOWARE)) { // あるいは指定されててもOracle、SqlServer以外の場合
//
//			appendSelectStmt(sb);
//			appendFromStmt(sb);
//			appendJoinStmt(sb);
//			appendWhereStmt(sb);
//			appendGroupByStmt(sb);
//			appendHavingStmt(sb);
//			appendOrderByStmt(sb);
//			appendLimitOffsetStmt(sb);
//			appendForUpdateStmt(sb);
//			return sb.toString();
//
//		} else if (dbms == DBMS.ORACLE || dbms == DBMS.SQLSERVER) {
//
//			/*
//			 * select *
//			 *  from (
//			 * 		select
//			 * 		 SOME_COLUMNS...
//			 * 		,row_number() over (ORDER BY ..) as ___ROW_NUMBER___
//			 * 		 from TABLE
//			 * 		 join TABLES..
//			 * ) as MAIN___
//			 * where MAIN___.___ROW_NUMBER___ between FROM and TO;
//			 */
//			int from = 0;
//			int to = 0;
//			if (dbms == DBMS.ORACLE ) {
//				from = offset + 1;
//				to = offset + limit;
//			} else if (dbms == DBMS.SQLSERVER) {
//				from = offset * limit + 1;
//				to = from + limit - 1;
//			}
//			sb.append("select * from (");
//				appendSelectStmt(sb);
//				sb.append(",row_number() over (");
//				appendOrderByStmt(sb);
//				sb.append(") as ").append(getRowNumberColName()).append(" ");
//				appendFromStmt(sb);
//				appendJoinStmt(sb);
//				appendWhereStmt(sb);
//				appendGroupByStmt(sb);
//				appendHavingStmt(sb);
//			sb.append(") MAIN___ "); // テーブルの別名定義に'as'を使うのは、対応してないVerのOracleが居るのでNG
//			sb.append(" where MAIN___.").append(getRowNumberColName()).append(" between ")
//				.append(from).append(" and ").append(to);
//			appendForUpdateStmt(sb);
//			return sb.toString();
//
//		} else if (dbms == DBMS.SYMFOWARE) {
//
//			/*
//			 * select * from (
//			 * select
//			 *   <>___<>...
//			 *   ,rownum as ___ROW_NUMBER___,
//			 *
//			 * from (
//			 * 		select
//			 * 		 SOME_COLUMN as <>___<>*
//			 * 		 from TABLE
//			 * 		 join TABLES..
//			 * 		 ORDER BY ..
//			 * ) as SUB___
//			 * ) as MAIN___
//			 * where MAIN___.___ROW_NUMBER___ between FROM and TO;
//			 */
//			int from = offset + 1;
//			int to = offset + limit;
//			sb.append("select * from (");
//				appendSelectParentStmt(sb);
//				sb.append(",rownum as ").append(getRowNumberColName()).append(" ");
//				sb.append("from (");
//					appendSelectSubStmt(sb);
//					appendFromStmt(sb);
//					appendJoinStmt(sb);
//					appendWhereStmt(sb);
//					appendGroupByStmt(sb);
//					appendHavingStmt(sb);
//					appendOrderByStmt(sb);
//				sb.append(") SUB___ ");
//			sb.append(") MAIN___ ");
//			sb.append(" where MAIN___.").append(getRowNumberColName()).append(" between ")
//				.append(from).append(" and ").append(to);
//			appendForUpdateStmt(sb);
//			return sb.toString();
//		}
//		else
//			throw new RuntimeException("unexpected!");
	}


	protected void prepareLogicalDelete() {
		if (!containLogicalDeletedRecords) {
			if (from.tbl./*isLogicalDeleting()*/getLogicalDeleteFlagCol() != null) {
				@SuppressWarnings("unchecked")
				IColumn<Boolean> col = /*getLogicalDelFlagCol(from.tbl)*/(IColumn<Boolean>)from.tbl.getLogicalDeleteFlagCol();
				if (from.alias != null)
					where(equal(tblalias(from.alias, col), !getDeletedFlagValue()));
				else
					where(equal(col, !getDeletedFlagValue()));
			}
			for (JoinedTbl j : joins) {
				if (j.tbl.tbl./*isLogicalDeleting()*/getLogicalDeleteFlagCol() != null) {
					@SuppressWarnings("unchecked")
					IColumn<Boolean> col = /*getLogicalDelFlagCol(j.tbl.tbl)*/ (IColumn<Boolean>)j.tbl.tbl.getLogicalDeleteFlagCol();
					if (j.tbl.alias != null)
						where(equal(tblalias(j.tbl.alias, col), !getDeletedFlagValue()));
					else
						where(equal(col, !getDeletedFlagValue()));
				}
			}
		}
	}

	protected String[] getQueryGetColNames() {
		List<String> ret = new ArrayList<String>();
		if (selectables.get(0) == COLUMN_ALL) {
			for (IColumn<?> c : from.tbl.getCols()) {
				ret.add(from.name(isAppendSchemaName()) + "." + c.name());
			}
			for (JoinedTbl j : joins) {
				for (IColumn<?> c : j.tbl.tbl.getCols()) {
					ret.add(j.tbl.name(isAppendSchemaName()) + "." + c.name());
				}
			}
		} else {
			for (IAliasSelectable s : selectables) {
				if (s.alias() != null)
					ret.add(s.alias());
				else
					ret.add(s.fullname(isAppendSchemaName()));
			}
		}
		return ret.toArray(new String[0]);
	}



	/** 'select .....' */
	protected void appendSelectStmt(StringBuilder sb) {

		sb.append("select ");
		if (distinct)
			sb.append("distinct ");
		if (selectables.get(0) == COLUMN_ALL) {
			for (IColumn<?> c : from.tbl.getCols()) {
				sb.append(from.name(isAppendSchemaName())).append(".").append(c.name());
				sb.append(",");
			}
			for (JoinedTbl j : joins) {
				for (IColumn<?> c : j.tbl.tbl.getCols()) {
					sb.append(j.tbl.name(isAppendSchemaName())).append(".").append(c.name());
					sb.append(",");
				}
			}
			sb.deleteCharAt(sb.length() - 1);

		} else {
			for (IAliasSelectable s : selectables) {
				sb.append(s.fullname(isAppendSchemaName()));
				if (s.alias() != null)
					sb.append(" as ").append(s.alias());
				sb.append(",");
			}
			sb.deleteCharAt(sb.length() - 1);
		}
	}

//	protected void appendSelectParentStmt(StringBuilder sb) {
//		sb.append("select ");
//		if (selectables.get(0) == COLUMN_ALL) {
//			int i = 1;
//			for (@SuppressWarnings("unused") IColumn<?> c : from.tbl.getCols()) {
////				sb.append(from.name(isAppendSchemaName())).append(getTableColSeparator()).append(c.name());
//				sb.append("COL").append(i++);
//				sb.append(",");
//			}
//			for (JoinedTbl j : joins) {
//				for (@SuppressWarnings("unused") IColumn<?> c : j.tbl.tbl.getCols()) {
////					sb.append(j.tbl.name(isAppendSchemaName())).append(getTableColSeparator()).append(c.name());
//					sb.append("COL").append(i++);
//					sb.append(",");
//				}
//			}
//			sb.deleteCharAt(sb.length() - 1);
//		} else {
//			int i = 1;
//			for (IAliasSelectable s : selectables) {
////				if (s instanceof SelectFunc) 検証してないけど多分OK
////					throw new RuntimeException("");
//
////				sb.append(s.fullname(isAppendSchemaName()).replace(".", getTableColSeparator()));
//				if (s.alias() != null)
//					sb.append(s.alias());
//				else
//					sb.append("COL").append(i++);
//				sb.append(",");
//			}
//			sb.deleteCharAt(sb.length() - 1);
//		}
//	}
//
//	protected void appendSelectSubStmt(StringBuilder sb) {
//		sb.append("select ");
//		if (selectables.get(0) == COLUMN_ALL) {
//			int i = 1;
//			for (IColumn<?> c : from.tbl.getCols()) {
//				sb.append(from.name(isAppendSchemaName())).append(".").append(c.name());
////				sb.append(" as ").append(from.name(isAppendSchemaName())).append(getTableColSeparator()).append(c.name());
//				sb.append(" as COL").append(i++);
//				sb.append(",");
//			}
//			for (JoinedTbl j : joins) {
//				for (IColumn<?> c : j.tbl.tbl.getCols()) {
//					sb.append(j.tbl.name(isAppendSchemaName())).append(".").append(c.name());
////					sb.append(" as ").append(j.tbl.name(isAppendSchemaName())).append(getTableColSeparator()).append(c.name());
//					sb.append(" as COL").append(i++);
//					sb.append(",");
//				}
//			}
//			sb.deleteCharAt(sb.length() - 1);
//		} else {
//			int i = 1;
//			for (IAliasSelectable s : selectables) {
////				if (s instanceof SelectFunc)
////					throw new RuntimeException("");
//
//				sb.append(s.fullname(isAppendSchemaName()));
//				if (s.alias() != null)
//					sb.append(" as ").append(s.alias());
//				else
//					sb.append(" as COL").append(i++);
//				sb.append(",");
//			}
//			sb.deleteCharAt(sb.length() - 1);
//		}
//	}

//	protected String getTableColSeparator() {
//		return Const.TBL_COL_SEPARATOR;
//	}

	/** 'select .....' */
//	private void appendSelectStmtSub(StringBuilder sb, String parent) {
//
//		sb.append("select ");
////		if (distinct)
////			sb.append("distinct ");
//		if (selectables.get(0) == COLUMN_ALL) {
//			for (IColumn<?> c : from.tbl.getCols()) {
//				sb.append(parent).append(".").append(c.name());
//				sb.append(" as ").append(from.name(false)).append(SystemVars.sqlTableColSeparator).append(c.name());
//				sb.append(",");
//			}
//			for (JoinedTbl j : joins) {
//				for (IColumn<?> c : j.tbl.tbl.getCols()) {
//					sb.append(parent).append(".").append(c.name());
//					sb.append(" as ").append(j.tbl.name(false)).append(SystemVars.sqlTableColSeparator).append(c.name());
//					sb.append(",");
//				}
//			}
//			sb.deleteCharAt(sb.length() - 1);
//
//		} else {
//			for (IAliasSelectable s : selectables) {
//				sb.append(s.fullname(isAppendSchemaName()));
//				if (s.alias() != null)
//					sb.append(" as ").append(s.alias());
//				sb.append(",");
//			}
//			sb.deleteCharAt(sb.length() - 1);
//		}
//	}

	/** ' from .....' */
	protected void appendFromStmt(StringBuilder sb) {
		sb.append(" from ").append(from.aliasExpression(isAppendSchemaName()));
	}

	/** ' inner(or left outer) join .....' */
	protected void appendJoinStmt(StringBuilder sb) {
		for (JoinedTbl j : joins) {
			sb.append(j.toQuery(isAppendSchemaName()));
		}
	}

	/** ' where .....' */
	protected void appendWhereStmt(StringBuilder sb) {
		if (rootWhere != null)
			sb.append(" where ").append(rootWhere.toQuery(isAppendSchemaName()));

	}

	/** ' group by .....' */
	protected void appendGroupByStmt(StringBuilder sb) {
		if (groupables != null && groupables.length != 0) {
			sb.append(" group by ");
			for (IGroupByColumn g : groupables) {
				sb.append(g.fullname(isAppendSchemaName())).append(",");
			}
			sb.deleteCharAt(sb.length() - 1);
		}
	}

	/** ' having .....' */
	protected void appendHavingStmt(StringBuilder sb) {
//		if (rootHaving != null)
//			sb.append(" having ").append(rootHaving.toQuery());
		if (having != null)
			sb.append(" having ").append(having);
	}

	/** ' order by .....' */
	protected void appendOrderByStmt(StringBuilder sb) {
		if (orderables != null && orderables.length != 0) {
			sb.append(" order by ");
			for (IOrderElement o : orderables) {
				sb.append(o.toQuery(isAppendSchemaName())).append(",");
			}
			sb.deleteCharAt(sb.length() - 1);
		}
	}

	/** ' limit n offset n' */
	protected void appendLimitOffsetStmt(StringBuilder sb) {
		if (limit != null && offset != null) {
			sb.append(" limit ").append(limit).append(" offset ").append(offset);
		}
	}

	/** ' for update' */
	protected void appendForUpdateStmt(StringBuilder sb) {
		if (forUpdate) {
			sb.append(" for update");
		}
	}



	public String getGetSequenceValSql(String seqName) {
		return "select " + seqName + ".nextval from dual";
	}


	public String getGetAutoIncrementedValSql() {
		throw new RuntimeException("Not supported!");
	}


//	/**
//	 * 論理削除機能に対応するカラム型はBooleanのみなので、キャストは保証される（と思う）
//	 */
//	@SuppressWarnings("unchecked")
//	protected IColumn<Boolean> getLogicalDelFlagCol(ITable tbl) {
//		for (IColumn<?> col : tbl.getCols()) {
//			if (col.isDelFlag()) {
//				return (IColumn<Boolean>) col;
//			}
//		}
//		return null;
//	}

	protected boolean getDeletedFlagValue() {
		return true;
	}


	public ResultSetParser getResultSetParser() {
		return new ResultSetParser(getQueryGetColNames());
	}
}
