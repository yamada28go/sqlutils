package jp.gr.java_conf.sqlutils.core.dto;

import java.io.Serializable;

import jp.gr.java_conf.sqlutils.core.builder.BuilderElement.IGroupByColumn;
import jp.gr.java_conf.sqlutils.core.builder.BuilderElement.IOrderColumn;
import jp.gr.java_conf.sqlutils.core.builder.BuilderElement.ISelectColumn;
import jp.gr.java_conf.sqlutils.core.builder.ConditionBuilder.IConditionColumn;

public interface IColumn<T> extends ISelectColumn<T>, IOrderColumn, IConditionColumn<T>, IGroupByColumn {
	ITable getTable();
	Class<T> getDataType();
	boolean isPrimaryKey();
	boolean isFixedLenStr();
	boolean isAutoIncrement();
//	boolean isDelFlag();
//	boolean isOptimisticLockKey();
	String getSequenceTableName();
	String name();
	String fullname(boolean appendSchema);
	String fieldname();
	int getSize();


	public static class Column<T> implements IColumn<T>, Serializable {
		private static final long serialVersionUID = 1L;
		String name;
		String fieldname;
		ITable table;
		Class<T> dataType;
		boolean isPrimaryKey;
		boolean isFixedLenStr;
		boolean isAutoIncrement;
//		boolean isDelFlag;
//		boolean isOptimisticLockKey;
		String sequenceTableName;
		int size;

		public Column(
				String name,
				String fieldname,
				ITable table,
				Class<T> dataType,
				boolean isPrimaryKey,
				boolean isFixedLenStr,
				boolean isAutoIncrement,
//				boolean isDelFlag,
//				boolean isOptimisticLockKey,
				String sequenceTableName,
				int size) {

			this.name = name;
			this.fieldname = fieldname;
			this.table = table;
			this.dataType = dataType;
			this.isPrimaryKey = isPrimaryKey;
			this.isFixedLenStr = isFixedLenStr;
			this.isAutoIncrement = isAutoIncrement;
//			this.isDelFlag = isDelFlag;
//			this.isOptimisticLockKey = isOptimisticLockKey;
			this.sequenceTableName = sequenceTableName;
			this.size = size;
		}

		public ITable getTable() {
			return table;
		}
		public Class<T> getDataType() {
			return dataType;
		}
		public boolean isPrimaryKey() {
			return isPrimaryKey;
		}
		public boolean isFixedLenStr() {
			return isFixedLenStr;
		}
		public boolean isAutoIncrement() {
			return isAutoIncrement;
		}
//		public boolean isDelFlag() {
//			return isDelFlag;
//		}
//		public boolean isOptimisticLockKey() {
//			return isOptimisticLockKey;
//		}
		public String getSequenceTableName() {
			return sequenceTableName;
		}
		public int getSize() {
			return size;
		}
		public String name() {
			return name;
		}
		public String fullname(boolean appendSchema) {
			return table.name(appendSchema) + "." + name();
		}
		public String fieldname() {
			return fieldname;
		}
		public String toString() {
			return table.name(true) + "." + name();
		}
	}
}
