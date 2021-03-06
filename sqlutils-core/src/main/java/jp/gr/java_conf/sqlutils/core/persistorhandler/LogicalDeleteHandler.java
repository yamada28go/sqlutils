package jp.gr.java_conf.sqlutils.core.persistorhandler;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import jp.gr.java_conf.sqlutils.DBManager;
import jp.gr.java_conf.sqlutils.core.dto.IColumn;
import jp.gr.java_conf.sqlutils.core.dto.IDto.ILogicalDeleting;
import jp.gr.java_conf.sqlutils.core.dto.ITable;
import jp.gr.java_conf.sqlutils.core.exception.NoSuchColumnException;


/**
 * DTOインスタンスからUpdate文（論理削除）を生成・実行するハンドラ
 *
 */
public class LogicalDeleteHandler extends AbstractUpdateHandler {


	public LogicalDeleteHandler(DBManager manager) {
		super(manager);
	}


	public <T extends ILogicalDeleting> int exec(T dto) {

		try {
			ITable tbl = dto.getTableDefinition();
			IColumn<?> optLockKeyCol = tbl.getOptimisticLockingKeyCol();
			IColumn<?> logicalDelKeyCol = tbl.getLogicalDeleteFlagCol();

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
			if (optLockKeyCol != null)
				attrCols.add(optLockKeyCol);
			if (logicalDelKeyCol != null)
				attrCols.add(logicalDelKeyCol);

			if (whereCols.size() == 0)
				throw new RuntimeException("No key columns. This will update all records.");

			if (attrCols.size() == 0)
				throw new RuntimeException("No update columns ( = no del-flag column is defined).");

			// 排他ロックカラムの現状値を保持
			Map<String, Object> optLockCurrVals = new LinkedHashMap<String, Object>();
			if (optLockKeyCol != null)
				optLockCurrVals.put(optLockKeyCol.name(), dto.get(optLockKeyCol.name()));

			String sql = createUpdateSql(tbl.name(appendSchemaToSql), whereCols, attrCols, dto, false);

			List<Object> params = new ArrayList<Object>();
			if (optLockKeyCol != null)
				params.add(getOptimisticLockKeyNewValue(optLockKeyCol, dto.get(optLockKeyCol.name())));
			if (logicalDelKeyCol != null)
//				params.add(getDeletedFlagValue());
				params.add(logicalDelKeyCol.getLogicalDeletedValue());

			for (IColumn<?> c : whereCols) {
				if (c.isPrimaryKey())
					params.add(dto.get(c.name()));
				else /*if (c.isOptimisticLockKey())*/
					params.add(optLockCurrVals.get(c.name()));
			}

			// exec sql
			return manager.execUpdate(sql, params.toArray());


		} catch (NoSuchColumnException e) {
			throw new RuntimeException("unexpected!");
		}
	}
}
