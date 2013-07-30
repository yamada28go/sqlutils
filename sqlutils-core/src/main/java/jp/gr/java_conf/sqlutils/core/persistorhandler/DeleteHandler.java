package jp.gr.java_conf.sqlutils.core.persistorhandler;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import jp.gr.java_conf.sqlutils.DBManager;
import jp.gr.java_conf.sqlutils.core.dto.IColumn;
import jp.gr.java_conf.sqlutils.core.dto.ITable;
import jp.gr.java_conf.sqlutils.core.dto.IDto.IPersistable;
import jp.gr.java_conf.sqlutils.core.exception.NoSuchColumnException;


/**
 * DTOインスタンスからDelete文を生成・実行するハンドラ
 *
 */
public class DeleteHandler extends AbstractUpdateHandler {


	public DeleteHandler(DBManager manager) {
		super(manager);
	}

	public <T extends IPersistable> int exec(T dto) {

		try {
			ITable tbl = dto.getTableDefinition();
			IColumn<?> optLockKeyCol = tbl.getOptimisticLockingKeyCol();

			// Where条件カラム
			LinkedHashSet<IColumn<?>> whereCols = new LinkedHashSet<IColumn<?>>();
			if (optLockKeyCol != null)
				whereCols.add(optLockKeyCol);
			for (IColumn<?> c : tbl.getCols()) {
				if (c.isPrimaryKey() /*|| c.isOptimisticLockKey()*/) {
					whereCols.add(c);
				}
			}

			if (whereCols.size() == 0)
				throw new RuntimeException("No key columns. This will update all records.");


			@SuppressWarnings("unchecked")
			T clone = (T) dto.clone();


			// exec sql
			String sql = createDeleteSql(tbl.name(appendSchemaToSql), whereCols);

			List<Object> params = new ArrayList<Object>();
			for (IColumn<?> c : whereCols) {
				params.add(clone.get(c.name()));
			}

			return manager.execUpdate(sql, params.toArray());

		} catch (NoSuchColumnException e) {
			throw new RuntimeException("unexpected!");
		}
	}
}
