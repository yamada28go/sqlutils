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
 * インスタンスメソッドとスタティックメソッドを使って、Selectクエリーを構築する.<br/>
 * インスタンスの再利用は想定されていない。<br/>
 * DBMSによる構文の相違などは、このクラスを継承した個別クラスを用意する事で解消する。
 * <p>
 * スタティックメソッドの使用の際は、スタティックインポートを使用する事で、コードの短略化が図れる。
 * <pre>
 * import static jp.gr.java_conf.sqlutils.core.builder.QueryBuilder.*;
 * </pre>
 * @see jp.gr.java_conf.sqlutils.core.builder.ConditionBuilder
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
	 * SELECT句の要素を生成する.<br/>
	 * COUNT関数。
	 * <p>
	 * Usage:<br/>
	 * builder.select(count()).from(....
	 * <p>
	 * then ,generated sql is:<br/>
	 * 'select count(*) from ..'
	 */
	public static SelectFunc count() {
		return new SelectFunc("count(*)", null);
	}

	/**
	 * SELECT句の要素を生成する.<br/>
	 * COUNT関数。
	 * <p>
	 * Usage:<br/>
	 * builder.select(count(TBL1.COL1)).from(....
	 * <p>
	 * then ,generated sql is:<br/>
	 * 'select count(TBL1.COL1) from ..'
	 */
	public static SelectFunc count(ISelectColumn<?> col) {
//		return new SelectFunc("count(" + col.fullname() + ")");
		return new SelectFunc("count(%s)", col);
	}

	/**
	 * SELECT句の要素を生成する.<br/>
	 * SUM関数。
	 * <p>
	 * Usage:<br/>
	 * builder.select(sum(TBL1.COL1)).from(....
	 * <p>
	 * then ,generated sql is:<br/>
	 * 'select sum(TBL1.COL1) from ..'
	 */
	public static SelectFunc sum(ISelectColumn<?> col) {
//		return new SelectFunc("sum(" + col.fullname() + ")");
		return new SelectFunc("sum(%s)", col);
	}

	/**
	 * SELECT句の要素を生成する.<br/>
	 * AVG関数。
	 * <p>
	 * Usage:<br/>
	 * builder.select(avg(TBL1.COL1)).from(....
	 * <p>
	 * then ,generated sql is:<br/>
	 * 'select avg(TBL1.COL1) from ..'
	 */
	public static SelectFunc avg(ISelectColumn<?> col) {
//		return new SelectFunc("avg(" + col.fullname() + ")");
		return new SelectFunc("avg(%s)", col);
	}

	/**
	 * SELECT句の要素を生成する.<br/>
	 * MAX関数。
	 * <p>
	 * Usage:<br/>
	 * builder.select(max(TBL1.COL1)).from(....
	 * <p>
	 * then ,generated sql is:<br/>
	 * 'select max(TBL1.COL1) from ..'
	 */
	public static SelectFunc max(ISelectColumn<?> col) {
//		return new SelectFunc("max(" + col.fullname() + ")");
		return new SelectFunc("max(%s)", col);
	}

	/**
	 * SELECT句の要素を生成する.<br/>
	 * MIN関数。
	 * <p>
	 * Usage:<br/>
	 * builder.select(min(TBL1.COL1)).from(....
	 * <p>
	 * then ,generated sql is:<br/>
	 * 'select min(TBL1.COL1) from ..'
	 */
	public static SelectFunc min(ISelectColumn<?> col) {
//		return new SelectFunc("min(" + col.fullname() + ")");
		return new SelectFunc("min(%s)", col);
	}

	/**
	 * SELECT句の要素を生成する.<br/>
	 * 引数の文字列がそのままSelect句に埋め込まれる。
	 */
	public static SelectFunc rawStatement(String expression) {
		return new SelectFunc(expression, null);
	}


	/**
	 * SELECT句の要素（複数）を生成する.<br/>
	 * テーブルの全カラムを指定する代わりに、テーブルを指定する事で全カラム分を展開する。
	 *
	 * <p>
	 * Usage:<br/>
	 * builder.select(<b>columns</b>(TBL1)).from(....
	 * <p>
	 * then ,generated sql is:<br/>
	 * 'select TBL1.COL1,TBL1.COL2,TBL1.COL3... from ..'
	 */
	public static ISelectColumn<?>[] columns(ITblElement tbl) {
		return TblElement.create(tbl).getCols();
	}



	/**
	 * カラムに対して別名を設定する.<br/>
	 * <p>
	 * Usage:<br/>
	 * builder.select(<b>as</b>(TBL1.COL1, "ALIASNAME")).from(....
	 * <p>
	 * then ,generated sql is:<br/>
	 * 'select TBL1.COL1 as ALIASNAME from ..'
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
	 * 数式に対して別名を設定する.<br/>
	 * <p>
	 * Usage:<br/>
	 * builder.select(<b>as</b>(sum(TBL1.COL1), "COL1_SUM")).from(....
	 * <p>
	 * then ,generated sql is:<br/>
	 * 'select sum(TBL1.COL1) as COL1_SUM from ..'
	 */
	public static SelectFunc as(SelectFunc function, String alias) {
		if (function.alias != null) throw new RuntimeException();
		function.alias = alias;
		return function;
	}


	/**
	 * テーブルに対して別名を設定する.<br/>
	 * 通常、{@link #tblalias(String, ISelectColumn) tblalias}と同時に使用する。
	 * <p>
	 * Usage:<br/>
	 * builder.select(tblalias("T1", TBL1.COL1)).from(<b>as</b>(TBL1, "T1"))
	 * <p>
	 * then ,generated sql is:<br/>
	 * 'select T1.COL1 from TBL1 T1'
	 */
	public static TblElement as(ITable tbl, String alias) {
		return new TblElement(tbl, alias);
	}


	/**
	 * SELECT句の要素を生成する.<br/>
	 * 別名定義されたテーブル名に合致したカラム名となるよう変換される。
	 * 通常、{@link #as(ITable, String) as}と同時に使用する。
	 *
	 * <p>
	 * Usage:<br/>
	 * builder.select(<b>tblalias</b>("T1", TBL1.COL1)).from(as(TBL1, "T1"))
	 * <p>
	 * then ,generated sql is:<br/>
	 * 'select T1.COL1 from TBL1 T1'
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



	/**
	 * ORDER句の要素を生成する.<br/>
	 * 降順
	 */
	public static IOrderElement desc(IOrderColumn col) {
		return new OrderElement(col, false);
	}

	/**
	 * ORDER句の要素を生成する.<br/>
	 * 昇順
	 */
	public static IOrderElement asc(IOrderColumn col) {
		return new OrderElement(col, true);
	}

	/**
	 * ORDER句の要素を生成する.<br/>
	 * 条件を動的に決定する。
	 */
	public static IOrderElement sortWith(IOrderColumn col, boolean isAsc) {
		return new OrderElement(col, isAsc);
	}


	/**
	 * ORDER句の要素を生成する.<br/>
	 * 集計関数の結果カラムでOrderする場合、引数は文字列で指定する
	 */
	public static IOrderElement desc(String name) {
		return new RawOrderElement(name, false);
	}

	/**
	 * ORDER句の要素を生成する.<br/>
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


	/**
	 * コンストラクタ
	 */
	public QueryBuilder() {
		selectables = new ArrayList<IAliasSelectable>();
		joins = new ArrayList<JoinedTbl>();
	}

	/**
	 * SELECT句を構築する.<br/>
	 * 要素指定を省略し、全カラムを展開したSQLを生成する。
	 * SELECT句を構築するメソッドは一度しか呼びだす事はできない。
	 *
	 * <p>
	 * Usage:<br/>
	 * builder.selectAll().from(TBL1)
	 * <p>
	 * then ,generated sql is:<br/>
	 * 'select TBL1.COL1,TBL1.COL2.... from TBL1'
	 */
	public QueryBuilder selectAll() {
		throwIf(!this.selectables.isEmpty(), "Select statement is already setted.");
		this.selectables.add(COLUMN_ALL);
		return this;
	}

	/**
	 * SELECT句を構築する.<br/>
	 * SELECT句を構築するメソッドは一度しか呼びだす事はできない。
	 *
	 * <p>
	 * Usage:<br/>
	 * builder.selectDistinct(TBL1.COL1, TBL1.COL2).from(TBL1)
	 * <p>
	 * then ,generated sql is:<br/>
	 * 'select distinct TBL1.COL1, TBL1.COL2 from TBL1'
	 */
	public QueryBuilder selectDistinct(ISelectColumn<?>...selectables) {
		throwIf(!this.selectables.isEmpty(), "Select statement is already setted.");
		this.distinct = true;
		return select(selectables);
	}

	/**
	 * SELECT句を構築する.<br/>
	 * SELECT句を構築するメソッドは一度しか呼びだす事はできない。
	 *
	 * <p>
	 * Usage:<br/>
	 * builder.select(TBL1.COL1, TBL1.COL2, sum(TBL1.COL3)).from(TBL1)
	 * <p>
	 * then ,generated sql is:<br/>
	 * 'select TBL1.COL1, TBL1.COL2, sum(TBL1.COL3) from TBL1'
	 */
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

	/**
	 * SELECT句を構築する.<br/>
	 * SELECT句を構築するメソッドは一度しか呼びだす事はできない。
	 *
	 * <p>
	 * builder.select(count()).from(TBL1)
	 *
	 * @Deprecated 冗長メソッドにつき削除予定
	 */
	@Deprecated
	public QueryBuilder selectCountAll() {
		throwIf(!this.selectables.isEmpty(), "Select statement is already setted.");
		select(count());
		return this;
	}


	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static ColElement<?> wrap(IColumn<?> col) {
		return new ColElement(col);
	}


	/**
	 * FROM句を構築する.<br/>
	 * 一度しか呼びだす事はできない。
	 *
	 * <p>
	 * builder.selectAll().from(TBL1)
	 *
	 */
	public QueryBuilder from(ITblElement fromTbl) {
		throwIf(this.from != null, "From statement is already setted.");
		this.from = TblElement.create(fromTbl);
		return this;
	}

	/**
	 * JOIN句(inner join)を構築する.<br/>
	 *
	 * <pre>
	 * builder
	 *     .selectAll()
	 *     .from(TBL1)
	 *     .innerJoin(TBL2, equal(TBL1.COL1, TBL2.COL2))
	 * </pre>
	 */
	public QueryBuilder innerJoin(ITblElement tbl, IConditionElement on) {
		joins.add(new JoinedTbl(JoinType.INNER, TblElement.create(tbl), on));
		return this;
	}

	/**
	 * JOIN句(left outer join)を構築する.<br/>
	 *
	 * <pre>
	 * builder
	 *     .selectAll()
	 *     .from(TBL1)
	 *     .leftOuterJoin(TBL2, equal(TBL1.COL1, TBL2.COL2))
	 * </pre>
	 *
	 */
	public QueryBuilder leftOuterJoin(ITblElement tbl, IConditionElement on) {
		joins.add(new JoinedTbl(JoinType.LEFT_OUTER, TblElement.create(tbl), on));
		return this;
	}

	/**
	 * WHERE句を構築する.<br/>
	 * #and() や #or() を使用して、条件を入れ子にする事も可能。
	 *
	 * <pre>
	 * builder
	 *     .selectAll()
	 *     .from(TBL1)
	 *     .where(equal(TBL1.COL1, value))
	 *
	 * builder
	 *     .selectAll()
	 *     .from(TBL1)
	 *     .where(
	 *         and(
	 *             equal(TBL1.COL1, val1),
	 *             equal(TBL1.COL2, val2),
	 *             or(
	 *                 equal(TBL1.COL3, "a"),
	 *                 equal(TBL1.COL3, "A")
	 *             )
	 *         ))
	 * </pre>
	 *
	 * 複数回呼び出した場合、トップレベルにand条件で追加される。
	 * <pre>
	 * builder
	 *     .selectAll()
	 *     .from(TBL1)
	 *     .where(equal(TBL1.COL1, val1))
	 *     .where(equal(TBL1.COL2, val2))
	 * </pre>
	 */
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

	/**
	 * GROUP BY句を構築する.<br/>
	 *
	 * <pre>
	 * builder
	 *     .select(
	 *         TBL1.COL1,
	 *         as(sum(TBL1.COL2), "COL2_SUM")
	 *     )
	 *     .from(TBL1)
	 *     .groupBy(TBL1.COL1)
	 * </pre>
	 *
	 */
	public QueryBuilder groupBy(IGroupByColumn...groupables) {
		throwIf(this.groupables != null, "Group-by statement is already setted.");
		this.groupables = groupables;
		return this;
	}

	/**
	 * HAVING句を構築する.<br/>
	 *
	 * <pre>
	 * builder
	 *     .select(
	 *         TBL1.COL1,
	 *         as(sum(TBL1.COL2), "COL2_SUM")
	 *     )
	 *     .from(TBL1)
	 *     .groupBy(TBL1.COL1)
	 *     .having("COL2_SUM > 0")
	 * </pre>
	 *
	 */
	public QueryBuilder having(String condition) {
		throwIf(this.offset != null, "Offset statement is already setted.");
		if (this.having != null) throw new RuntimeException("Having statement is already setted.");
		this.having = condition;
		return this;
	}

	/**
	 * ORDER BY句を構築する.<br/>
	 *
	 * <pre>
	 * builder
	 *     .selectAll()
	 *     .from(TBL1)
	 *     .orderBy(asc(TBL1.COL1), desc(TBL1.COL2))
	 * </pre>
	 */
	public QueryBuilder orderBy(IOrderElement...orderables) {
		throwIf(this.orderables != null, "Order-by statement is already setted.");
		this.orderables = orderables;
		return this;
	}

	/**
	 * LIMIT句を付加する.<br/>
	 *
	 * <pre>
	 * builder
	 *     .selectAll()
	 *     .from(TBL1)
	 *     .limit(20)
	 *     .offset(0)
	 * </pre>
	 */
	public QueryBuilder limit(int limit) {
		throwIf(this.limit != null, "Limit statement is already setted.");
		this.limit = limit;
		return this;
	}

	/**
	 * OFFSET句を付加する.<br/>
	 *
	 * <pre>
	 * builder
	 *     .selectAll()
	 *     .from(TBL1)
	 *     .limit(20)
	 *     .offset(0)
	 * </pre>
	 */
	public QueryBuilder offset(int offset) {
		throwIf(this.offset != null, "Offset statement is already setted.");
		this.offset = offset;
		return this;
	}

	/**
	 * FORUPDATE句を付加する.<br/>
	 *
	 * <pre>
	 * builder
	 *     .selectAll()
	 *     .from(TBL1)
	 *     .where(....)
	 *     .forUpdate()
	 * </pre>
	 */
	public QueryBuilder forUpdate() {
		this.forUpdate = true;
		return this;
	}


	protected void throwIf(boolean condition, String errMsg) {
		if (condition) throw new RuntimeException(errMsg);
	}


	/**
	 * 論理削除済みカラムを抽出対象とする.<br/>
	 * このメソッドを呼ばない限り、QueryBuilderを介した取得処理においては、論理削除済みのレコードは抽出されない。<br/>
	 * ※SQL構築時に、論理削除済みレコードを除外する構文が自動的に付加される。
	 *
	 * <pre>
	 * builder
	 *     .selectAll()
	 *     .from(TBL1)
	 *     .where(....)
	 *     .containLogicalDeletedRecords()
	 * </pre>
	 */
	public QueryBuilder containLogicalDeletedRecords() {
		containLogicalDeletedRecords = true;
		return this;
	}






	/**
	 * Select文中のテーブル名の前にスキーマ名を付加するか否か
	 */
	protected boolean isAppendSchemaName() {
		return false;
	}

	/**
	 * WHERE句の条件構造のみを取り出す.<br/>
	 * 一般的に一覧画面を作成する際など、同一条件で2度のDBアクセス（件数取得とデータ取得）が発生するため、
	 * 条件生成処理だけを共通化する目的に使用。
	 *
	 * <pre>
	 * private IConditionElement getCommonWhere() {
	 *     return builder
	 *             .where(......)
	 *             .getWhere();
	 * }
	 *
	 * builder
	 *     .selectAll()
	 *     .from(TBL1)
	 *     .where(getCommonWhere())
	 * </pre>
	 */
	public IConditionElement getWhere() {
		return rootWhere;
	}

	/**
	 * クエリー（PreparedStatement）にバインドする値を取り出す.<br/>
	 * 通常このメソッドをユーザが呼び出す事は想定されて無い。
	 */
	public Object[] getQueryPrms() {
		if (rootWhere == null)
			return null;
		else
			return rootWhere.getArgs();
	}

	/**
	 * クエリー文字列を生成する.<br/>
	 * 通常このメソッドをユーザが呼び出す事は想定されて無い。
	 */
	public String buildQuery(/*DBMS dbms*/) {

		if (selectables.size() == 0) throw new RuntimeException();
		if (from == null) throw new RuntimeException();
		if (limit == null && offset != null) throw new RuntimeException();
		if (limit != null && offset == null) throw new RuntimeException();

		prepareLogicalDelete();


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

	}


	protected void prepareLogicalDelete() {
		if (!containLogicalDeletedRecords) {
			if (from.tbl.getLogicalDeleteFlagCol() != null) {
				@SuppressWarnings("unchecked")
				IColumn<Object> col = (IColumn<Object>) from.tbl.getLogicalDeleteFlagCol();
				if (from.alias != null)
//					where(equal(tblalias(from.alias, col), !getDeletedFlagValue()));
					where(or(
							isNull(tblalias(from.alias, col)),
							equal(tblalias(from.alias, col), col.getLogicalUnDeletedValue())));
				else
//					where(equal(col, !getDeletedFlagValue()));
					where(or(
							isNull(col),
							equal(col, col.getLogicalUnDeletedValue())));
			}
			for (JoinedTbl j : joins) {
				if (j.tbl.tbl./*isLogicalDeleting()*/getLogicalDeleteFlagCol() != null) {
					@SuppressWarnings("unchecked")
					IColumn<Object> col = (IColumn<Object>)j.tbl.tbl.getLogicalDeleteFlagCol();
					if (j.tbl.alias != null)
//						where(equal(tblalias(j.tbl.alias, col), !getDeletedFlagValue()));
						where(or(
								isNull(tblalias(j.tbl.alias, col)),
								equal(tblalias(j.tbl.alias, col), col.getLogicalUnDeletedValue())));
					else
//						where(equal(col, !getDeletedFlagValue()));
						where(or(
								isNull(col),
								equal(col, col.getLogicalUnDeletedValue())));
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



	/**
	 * シーケンスから値を採番するためのSQLを返却する。
	 */
	public String getGetSequenceValSql(String seqName) {
		return "select nextval('" + seqName + "')";
	}

	/**
	 * オートインクリメント型で新規採番された値を取得するためのSQLを返却する。
	 */
	public String getGetAutoIncrementedValSql() {
		throw new RuntimeException("Not supported!");
	}

	/**
	 * パーサーを返却する。
	 * @return ResultSetの各要素が、どのテーブル・カラムから取得された値かを判定するパーサー
	 * @see jp.gr.java_conf.sqlutils.core.handler.ResultSetParser
	 */
	public ResultSetParser getResultSetParser() {
		return new ResultSetParser(getQueryGetColNames());
	}
}
