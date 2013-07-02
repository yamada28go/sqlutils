package jp.gr.java_conf.sqlutils.core.builder;

import java.util.ArrayList;
import java.util.List;

import jp.gr.java_conf.sqlutils.core.dto.IColumn;
import jp.gr.java_conf.sqlutils.core.dto.ITable;

/**
 * インスタンスメソッドとスタティックメソッドを使って、Update文を構築する.<br/>
 * インスタンスの再利用は想定されていない。
 * <p>
 * スタティックメソッドの使用の際は、スタティックインポートを使用する事で、コードの短略化が図れる。
 * <pre>
 * import static jp.gr.java_conf.sqlutils.core.builder.UpdateQueryBuilder.*;
 * </pre>
 * @see jp.gr.java_conf.sqlutils.core.builder.ConditionBuilder
 */
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

//	public static UpdateQueryBuilder get(String dbms) {
//		return new UpdateQueryBuilder();
//	}

	/*********************************************************************************/

	private boolean appendSchemaToSql = false;

	private Mode mode;
	private ITable tbl;
	private List<UpdateSet> sets;
	private IConditionElement rootWhere;


	/**
	 * 生成されるSQLに、スキーマ名もセットするか否か<br/>
	 * デフォルト=false。<br/>
	 * マルチスキーマあるいはデフォルトスキーマ以外のテーブルを対象に処理を行う場合にtrueをセット。
	 */
	public UpdateQueryBuilder setAppendSchemaToSql(boolean appendSchemaToSql) {
		this.appendSchemaToSql = appendSchemaToSql;
		return this;
	}

	/**
	 * Update処理の対象テーブルを指定する.<br/>
	 * 意図しない全件更新を回避するため、必ずWhere句を設定する必要がある。
	 * <pre>
	 * {@code
	 * int ret = manager.execUpdate(
	 *     new UpdateQueryBuilder()
	 *         .update(TBL1)
	 *         .set(TBL1.COL2, true)
	 *         .where(equal(TBL1.COL3, "value")));
	 * }
	 * </pre>
	 */
	public UpdateQueryBuilder update(ITable tbl) {
		this.tbl = tbl;
		mode = Mode.UPDATE;
		return this;
	}

	/**
	 * Update処理の対象テーブルを指定する.<br/>
	 * 全件更新処理に特化したメソッド。Where句は受付けない。
	 * <pre>
	 * {@code
	 * int ret = manager.execUpdate(
	 *     new UpdateQueryBuilder()
	 *         .updateAllRecords(TBL1)
	 *         .set(TBL1.COL2, true);
	 * }
	 * </pre>
	 */
	public UpdateQueryBuilder updateAllRecords(ITable tbl) {
		update(tbl);
		where(VOID_CONDITION);
		return this;
	}

	/**
	 * 更新値を指定する.<br/>
	 * <pre>
	 * {@code
	 * int ret = manager.execUpdate(
	 *     new UpdateQueryBuilder()
	 *         .update(TBL1)
	 *         .set(TBL1.COL1, val1)
	 *         .set(TBL1.COL2, val2)
	 *         .set(TBL1.COL3, val3)
	 *         .where(equal(TBL1.COL3, "value")));
	 * }
	 * </pre>
	 */
	public <T> UpdateQueryBuilder set(IColumn<T> col, T val) {
		if (mode != Mode.UPDATE) throw new RuntimeException();
		if (sets == null)
			sets = new ArrayList<UpdateSet>();
		sets.add(new UpdateSet(col, val));
		return this;
	}

	/**
	 * Delete処理の対象テーブルを指定する.<br/>
	 * 意図しない全件削除を回避するため、必ずWhere句を設定する必要がある。
	 * <pre>
	 * {@code
	 * int ret = manager.execUpdate(
	 *     new UpdateQueryBuilder()
	 *         .deleteFrom(TBL1)
	 *         .where(equal(TBL1.COL3, "value")));
	 * }
	 * </pre>
	 */
	public UpdateQueryBuilder deleteFrom(ITable tbl) {
		this.tbl = tbl;
		mode = Mode.DELETE;
		return this;
	}

	/**
	 * Delete処理の対象テーブルを指定する.<br/>
	 * 全件削除に特化したメソッド。Where句は受付けない。
	 * <pre>
	 * {@code
	 * int ret = manager.execUpdate(
	 *     new UpdateQueryBuilder()
	 *         .deleteAllRecords(TBL1)
	 * }
	 * </pre>
	 */
	public UpdateQueryBuilder deleteAllRecords(ITable tbl) {
		deleteFrom(tbl);
		where(VOID_CONDITION);
		return this;
	}


	/**
	 * 条件句を構築する.<br/>
	 * #and() や #or() を使用して、条件を入れ子にする事も可能。
	 * <pre>
	 * {@code
	 * int ret = manager.execUpdate(
	 *     new UpdateQueryBuilder()
	 *         .update(TBL1)
	 *         .set(TBL1.COL1, val1)
	 *         .where(equal(TBL1.COL3, "value")));
	 *
	 * int ret = manager.execUpdate(
	 *     new UpdateQueryBuilder()
	 *         .update(TBL1)
	 *         .set(TBL1.COL1, val1)
	 *         .where(
	 *             and(
	 *                 equal(TBL1.COL1, val1),
	 *                 equal(TBL1.COL2, val2),
	 *                 or(
	 *                     equal(TBL1.COL3, "a"),
	 *                     equal(TBL1.COL3, "A")
	 *                 )
	 *             ))
	 * }
	 * </pre>
	 */
	public UpdateQueryBuilder where(IConditionElement condition) {
		rootWhere = condition;
		return this;
	}

	/**
	 * クエリー文字列を生成する.<br/>
	 * 通常このメソッドをユーザが呼び出す事は想定されて無い。
	 */
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

	/**
	 * クエリー（PreparedStatement）にバインドする値を取り出す.<br/>
	 * 通常このメソッドをユーザが呼び出す事は想定されて無い。
	 */
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
