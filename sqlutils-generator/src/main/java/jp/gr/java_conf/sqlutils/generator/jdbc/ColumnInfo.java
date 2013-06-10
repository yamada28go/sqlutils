package jp.gr.java_conf.sqlutils.generator.jdbc;

import java.sql.Types;

import jp.gr.java_conf.sqlutils.generator.dto.config.ColValueConverter;
import jp.gr.java_conf.sqlutils.generator.dto.config.DtoGeneratorConfig.ColumnSetting;
import jp.gr.java_conf.sqlutils.generator.dto.config.DtoGeneratorConfig.EnumRelation;
import jp.gr.java_conf.sqlutils.generator.dto.config.DtoGeneratorConfig.SequenceRelation;

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

	public ColValueConverter converter;

	public ColumnSetting setting;


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



	public boolean isDataTypeChar() {
		return dataType == Types.CHAR;
	}

	public boolean isLogicalDeleteFlag() {
		return setting == null ? false : setting.isLogicalDeleteFlag;
	}
	public boolean isOptimisticLockKey() {
		return setting == null ? false : setting.isOptimisticLockKey;
	}
//	public boolean isSequenceRelated() {
//		return setting == null ? false :
//			setting.sequenceRelation == null ? false : true;
//	}
	public SequenceRelation getRelatedSequence() {
		if (setting != null)
			return setting.sequenceRelation;
		return null;
	}
//	public boolean isEnumRelated() {
//		return setting == null ? false :
//			setting.enumRelation == null ? false : true;
//	}
	public EnumRelation getRelatedEnum() {
		if (setting != null)
			return setting.enumRelation;
		return null;
	}


	public String getDtoFieldClassType() {
		return converter.dtoFieldClassType;
	}
	public String getSetToDtoConversion() {
		return converter.setToDtoConversion;
	}
	public String getGetFromDtoConversion() {
		return converter.getFromDtoConversion.replace(ColValueConverter.FIELDNAME_PLACEHOLDER, dtoFieldName);
	}

}
