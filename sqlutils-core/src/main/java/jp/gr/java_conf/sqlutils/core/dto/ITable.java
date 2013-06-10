package jp.gr.java_conf.sqlutils.core.dto;

import java.io.Serializable;

import jp.gr.java_conf.sqlutils.core.builder.BuilderElement.ITblElement;
import jp.gr.java_conf.sqlutils.core.dto.IDto.IGeneratedDto;


public interface ITable extends ITblElement {
	String getSchemaName();
	IColumn<?>[] getCols();
	String name(boolean appendSchema);
//	boolean isLogicalDeleting();
//	boolean isOptimisticLocking();
	IColumn<?> getLogicalDeleteFlagCol();
	IColumn<?> getOptimisticLockingKeyCol();
	Class<? extends IGeneratedDto> getDtoClass();


	public static abstract class Table implements ITable, Serializable {
		private static final long serialVersionUID = 1L;
		String name;
		String schemaName;
//		boolean isLogicalDeleting;
//		boolean isOptimisticLocking;
		Class<? extends IGeneratedDto> dtoClass;

		public Table(
				String name,
				String schemaName,
//				boolean isOptimisticLocking,
//				boolean isLogicalDeleting,
				Class<? extends IGeneratedDto> dtoClass) {

			this.name = name;
			this.schemaName = schemaName;
//			this.isOptimisticLocking = isOptimisticLocking;
//			this.isLogicalDeleting = isLogicalDeleting;
			this.dtoClass = dtoClass;
		}

//		public boolean isOptimisticLocking() {
//			return isOptimisticLocking;
//		}
//		public boolean isLogicalDeleting() {
//			return isLogicalDeleting;
//		}
		public String name(boolean appendSchema) {
			if (appendSchema)
				return schemaName + "." + name;
			else
				return name;
		}
		public String getSchemaName() {
			return schemaName;
		}
		public String toString() {
			return schemaName + "." + name;
		}
		public Class<? extends IGeneratedDto> getDtoClass() {
			return dtoClass;
		}
	}
}
