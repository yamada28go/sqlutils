package jp.gr.java_conf.sqlutils.generator.jdbc;

import java.util.ArrayList;
import java.util.List;

public class TableInfo {
	public String schema;
	public String name;
	public String remarks;
	public List<String> primaryKeys;
	public List<ColumnInfo> cols;
	public boolean isView;

	public String dtoClassName;

	// ex: public static final Master1Definition MASTER1 = new Master1Definition();
	public String definitionName;


	public String getSchema() {
		return schema;
	}
	public String getName() {
		return name;
	}
	public String getRemarks() {
		return remarks;
	}
	public List<String> getPrimaryKeys() {
		return primaryKeys;
	}
	public List<ColumnInfo> getCols() {
		return cols;
	}
	public boolean isView() {
		return isView;
	}
	public String getDtoClassName() {
		return dtoClassName;
	}
	public String getDefinitionName() {
		return definitionName;
	}

//	public boolean hasLogicalDeleteFlagCol() {
//		return getLogicalDeleteFlagCol() != null;
//	}

	public ColumnInfo getLogicalDeleteFlagCol() {
		for (ColumnInfo c : cols) {
			if (c.isLogicalDeleteFlag())
				return c;
		}
		return null;
	}

//	public boolean hasOptimisticLockKeyCol() {
//		return getOptimisticLockKeyCol() != null;
//	}

	public ColumnInfo getOptimisticLockKeyCol() {
		for (ColumnInfo c : cols) {
			if (c.isOptimisticLockKey())
				return c;
		}
		return null;
	}

	public List<ColumnInfo> getPrimaryKeyCols() {
		List<ColumnInfo> ret = new ArrayList<ColumnInfo>();
		for (ColumnInfo c : cols) {
			if (c.isPrimaryKey)
				ret.add(c);
		}
		return ret;
	}

	public boolean isPersistable() {
		return !isView && !primaryKeys.isEmpty();
	}

}
