package jp.gr.java_conf.sqlutils.core.persistorhandler;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jp.gr.java_conf.sqlutils.DBManager;
import jp.gr.java_conf.sqlutils.core.dto.IColumn;
import jp.gr.java_conf.sqlutils.core.dto.ITable;
import jp.gr.java_conf.sqlutils.core.dto.IDto.IPersistable;
import jp.gr.java_conf.sqlutils.core.exception.NoSuchColumnException;

/**
 * DTOインスタンスから、その内容に沿ったUpdate文を生成・実行するハンドラ
 *
 */
public class UpdateHandler extends AbstractUpdateHandler {


	public UpdateHandler(DBManager manager) {
		super(manager);
	}

	/**
	 * 拡張ポイント.<br/>
	 * 定型的な処理、例えばUPDATE時に「更新者」にユーザ名を格納する、といった用途に。
	 * @param attrs キーはカラム名。テーブル横断的に処理するコードを書くには、IColumnは向かないため。
	 */
	protected void modifyAttrColValues(Map<String, Object> attrs, ITable tbl) {
		// for override
	}


	public <T extends IPersistable> T exec(T dto) {

		try {
			ITable tbl = dto.getTableDefinition();
			IColumn<?> optLockKeyCol = tbl.getOptimisticLockingKeyCol();

			// Where条件カラム
			LinkedHashSet<IColumn<?>> whereCols = new LinkedHashSet<IColumn<?>>();
			for (IColumn<?> c : tbl.getCols()) {
				if (c.isPrimaryKey() /*|| c.isOptimisticLockKey()*/) {
					whereCols.add(c);
				}
			}
			if (optLockKeyCol != null)
				whereCols.add(optLockKeyCol);

			// Setカラム
			LinkedHashSet<IColumn<?>> attrCols = new LinkedHashSet<IColumn<?>>();
			for (IColumn<?> c : tbl.getCols()) {
				if (!c.isPrimaryKey()) {
					attrCols.add(c);
				}
			}


			if (whereCols.size() == 0)
				throw new RuntimeException("No key columns. This will update all records.");

			if (attrCols.size() == 0)
				throw new RuntimeException("No update columns.");


			@SuppressWarnings("unchecked")
			T clone = (T) dto.clone();

			// 排他ロックカラムの現状値を保持
			Map<String, Object> optLockCurrVals = new LinkedHashMap<String, Object>();
			if (optLockKeyCol != null)
				optLockCurrVals.put(optLockKeyCol.name(), dto.get(optLockKeyCol.name()));
//			for (IColumn<?> c : attrCols) {
//				if (c.isOptimisticLockKey()) {
//					optLockCurrVals.put(c.name(), dto.get(c.name()));
//				}
//			}

			// Modification用のMapを作成
			Map<String, Object> attrs = new LinkedHashMap<String, Object>();
			for (IColumn<?> c : attrCols) {
				if (optLockKeyCol == null
				|| c != optLockKeyCol)
					attrs.put(c.name(), clone.get(c.name()));
//				if (!c.isOptimisticLockKey())
//					attrs.put(c.name(), clone.get(c.name()));
			}
			modifyAttrColValues(attrs, tbl);

			// cloneの値を更新
			try {
				for (Entry<String, Object> set : attrs.entrySet()) {
					clone.set(set.getKey(), set.getValue());
				}
//				for (IColumn<?> c : attrCols) {
//					if (c.isOptimisticLockKey()) {
//						clone.set(c.name(), getOptLockNewValue(c, clone.get(c.name())));
//					}
//				}
				if (optLockKeyCol != null)
					clone.set(optLockKeyCol.name(), getOptimisticLockKeyNewValue(optLockKeyCol, clone.get(optLockKeyCol.name())));
			} catch (NoSuchColumnException e) {
				throw new RuntimeException("Unexpected", e);
			}


			// exec sql
			String sql = createUpdateSql(tbl.name(appendSchemaToSql), whereCols, attrCols, clone, false);

			List<Object> params = new ArrayList<Object>();
			for (IColumn<?> c : attrCols) {
				params.add(clone.get(c.name()));
			}
			for (IColumn<?> c : whereCols) {
				if (c.isPrimaryKey())
					params.add(clone.get(c.name()));
				else
					params.add(optLockCurrVals.get(c.name()));
			}

			int ret = manager.execUpdate(sql, params.toArray());
			if (ret == 0)
				return null;
			return clone;



		} catch (NoSuchColumnException e) {
			throw new RuntimeException("unexpected!");
		}
	}
}
