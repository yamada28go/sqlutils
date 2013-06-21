package jp.gr.java_conf.sqlutils.core.persistorhandler;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jp.gr.java_conf.sqlutils.DBManager;
import jp.gr.java_conf.sqlutils.DBManager.PostProcess;
import jp.gr.java_conf.sqlutils.core.dto.IColumn;
import jp.gr.java_conf.sqlutils.core.dto.ITable;
import jp.gr.java_conf.sqlutils.core.dto.IDto.IPersistable;
import jp.gr.java_conf.sqlutils.core.exception.NoSuchColumnException;

public class InsertHandler extends PersistorHandler {


	public InsertHandler(DBManager manager) {
		super(manager);
	}


	/**
	 * オーバライド用のメソッド。
	 * 定型的な処理、例えばINSERT時に「登録者」にユーザ名を格納する、といった用途に。
	 * @param attrs キーはカラム名。テーブル横断的に処理するコードを書くには、IColumnは向かないため。
	 * @param tbl
	 */
	protected void modifyValues(Map<String, Object> attrs, ITable tbl) {
		// for override
	}


	public <T extends IPersistable> T exec(T dto) {

		try {
			ITable tbl = dto.getTableDefinition();

			@SuppressWarnings("unchecked")
			T clone = (T) dto.clone();

			Map<String, Object> attrs = new LinkedHashMap<String, Object>();
			IColumn<?> autoIncrementCol = null;
			for (IColumn<?> c : tbl.getCols()) {
				Object val = clone.get(c.name());

				// 値がNullなら、そのカラムに関してはSQL構築しない→default値の有無等はDBMSに委ねる
				// isIgnoreOnInsertの場合も無視する
				// AutoIncrement型は自動的にisIgnoreOnInsert
				if (val != null
				&& !c.isIgnoreOnInsert()) {
					attrs.put(c.name(), val);
				}
				if (c.isAutoIncrement()) {
					if (autoIncrementCol != null)
						throw new RuntimeException("Unexpected! More than one autoIncremented-col!");
					autoIncrementCol = c;
				}
			}

			// Handlerの継承によるモディフィケーション
			modifyValues(attrs, tbl);

			// サブSQLを投げるので、現状のPostProcessを待避してNONEを設定
			PostProcess prevPp = manager.getPostProcess();
			manager.setPostProcess(PostProcess.NONE);

//			IColumn<?> autoIncrementCol = null;
			for (IColumn<?> c : tbl.getCols()) {

				// シーケンスカラムの自動補完
				if (c.getSequenceTableName() != null) {
					Object nextVal = manager.getSequenceVal(c.getSequenceTableName());
					attrs.put(c.name(), nextVal);
					clone.set(c.name(), nextVal);
					continue;
				}
			}

			manager.setPostProcess(prevPp == PostProcess.NONE ? PostProcess.NONE : PostProcess.COMMIT_ONLY);

			// create sql
			String sql = createInsertSql(tbl.name(appendSchemaToSql), attrs);

			// exec
			List<Object> params = new ArrayList<Object>();
			for (Entry<String, Object> attr : attrs.entrySet())
				params.add(attr.getValue());
			int ret = manager.execUpdate(sql, params.toArray());
			if (ret == 0) {
				manager.setPostProcess(prevPp);
				if (prevPp == PostProcess.COMMIT_AND_CLOSE)
					manager.close();
				return null;
			}

			// 主キーで再度取得→default値が設定されたケースもふくめて戻り値に反映させる
			try {
				// AutoIncrements型カラムに、自動設定された値を取得
				if (autoIncrementCol != null) {
					clone.set(autoIncrementCol.name(), manager.getAutoIncrementedVal());
				}
				clone = manager.selectWithKey(clone);

			} catch (NoSuchColumnException e) {
				throw new RuntimeException("Unexpected", e);
			}


			manager.setPostProcess(prevPp);
			if (prevPp == PostProcess.COMMIT_AND_CLOSE)
				manager.close();
			return clone;


		} catch (NoSuchColumnException e) {
			throw new RuntimeException("unexpected!");
		}
	}

	protected static String createInsertSql(String tblName, Map<String, Object> attrs) {
		StringBuilder sb = new StringBuilder();
		sb.append("insert into ").append(tblName).append(" (");
		for (Entry<String, Object> c : attrs.entrySet()) {
			sb.append(c.getKey()).append(",");
		}
		sb.deleteCharAt(sb.length() - 1);
		sb.append(") values (");
		for (int i = 0; i < attrs.size(); i++) {
			sb.append("?,");
		}
		sb.deleteCharAt(sb.length() - 1);
		sb.append(")");
		return sb.toString();
	}
}
