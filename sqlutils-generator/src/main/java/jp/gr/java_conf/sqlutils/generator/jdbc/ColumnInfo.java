package jp.gr.java_conf.sqlutils.generator.jdbc;

import java.sql.Types;

import jp.gr.java_conf.sqlutils.generator.dto.config.ColumnConfig;
import jp.gr.java_conf.sqlutils.generator.dto.config.SequenceRelation;

public class ColumnInfo {

	public String schema;
	public String tblName;
	public String name;
	public String remarks;
	public String nullable;
	public int dataType;
	public String dataTypeName;
	public int size;
	public String defaultValue;
	public boolean isPrimaryKey;
	public boolean isAutoIncrement;

	public String dtoFieldName;
	public String definitionName;

//	public ColValueConverter converter;
	public String dtoFieldClassType;
	public String setToDtoConversion;
	public String getFromDtoConversion;
	
	//! C++ 用 C++内部でデータラッパーに使用する型情報を保持する
	public String getWrapperType;

	// TODO templateエンジンをVelocityから変更する。候補はmustache.javaあたり。
	// VelocityはBeansにしか対応してないので、いちいちgetterを用意しないといけないのが面倒
	// フィールドアクセスできるようになれば、config内へのアクセスも直で書ける

	public ColumnConfig config;


	public String getSchema() {
		return schema;
	}
	public String getTblName() {
		return tblName;
	}
	public String getName() {
		return name;
	}
	public String getRemarks() {
		return remarks;
	}
	public String getDefaultValue() {
		return defaultValue;
	}
	public String getDtoFieldName() {
		return dtoFieldName;
	}
	public String getDefinitionName() {
		return definitionName;
	}
	public String getNullable() {
		return nullable;
	}
	public int getDataType() {
		return dataType;
	}
	public String getDataTypeName() {
		return dataTypeName;
	}
	public int getSize() {
		return size;
	}
	public boolean isPrimaryKey() {
		return isPrimaryKey;
	}
	public boolean isAutoIncrement() {
		return isAutoIncrement;
	}
	public String getDtoFieldClassType() {
		return dtoFieldClassType;
	}
	public String getSetToDtoConversion() {
		return setToDtoConversion;
	}
	public String getGetFromDtoConversion() {
		return getFromDtoConversion;
	}


	public String getGetWrapperType()
	{
		return getWrapperType;
	};

	public boolean isDataTypeChar() {
		return dataType == Types.CHAR;
	}

	public boolean isLogicalDeleteFlag() {
		return config == null ? false : config.logicalDeleteFlag != null;
	}
	public String getLogicalDeletedValue() {
		return config == null ? null :
				config.logicalDeleteFlag == null ? null :
					config.logicalDeleteFlag.deletedValueExpression;
	}
	public String getLogicalUnDeletedValue() {
		return config == null ? null :
				config.logicalDeleteFlag == null ? null :
					config.logicalDeleteFlag.undeletedValueExpression;
	}
	public boolean isOptimisticLockKey() {
		return config == null ? false : config.optimisticLockKey != null;
	}
//	public boolean isOptimisticLockCounter() {
//		return setting == null ? false : setting.optimisticLockCounter != null;
//	}
	public boolean isIgnoreOnInsert() {
		return config == null ? false : config.ignoreOnInsert != null;
	}
	public boolean isIgnoreOnUpdate() {
		return config == null ? false : config.ignoreOnUpdate != null;
	}
	public boolean isIgnoreOnLogicalDelete() {
		return config == null ? false : config.ignoreOnLogicalDelete != null;
	}

	public SequenceRelation getRelatedSequence() {
		if (config != null)
			return config.sequenceRelation;
		return null;
	}
}
