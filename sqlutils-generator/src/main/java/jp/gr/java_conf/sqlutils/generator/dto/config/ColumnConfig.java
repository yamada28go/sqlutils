package jp.gr.java_conf.sqlutils.generator.dto.config;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;

public class ColumnConfig {

	public static class IgnoreFromQuery {
	}

	/**
	 * Update時の排他チェック用のカウンタ（Versionとか言う場合が多い？）またはタイムスタンプのカラム。
	 * Timestamp型またはNumber型を想定し、更新時に自動でカウントアップあるいはシステム日付で更新するが、
	 * Javaでシステム日付を取得するとミリ秒までしか無いので、レコードのアクセスタイムスタンプとしては不足する恐れがあるので、
	 * DBMS側のデフォルト値設定等で設定する事を推奨する。
	 * その場合、IgnoreOnUpdateも併せて付加すれば、InsertやUpdate文の更新対象に含まれないのでDBMSデフォルト設定が効く。
	 *
	 */
	public static class OptimisticLockKey {
//		@XmlAttribute(name="newValue")
//		public String newValueExpression = "currentValue == null ? 1 : currentValue.intValue() + 1";
	}

	public static class LogicalDeleteFlag {
		@XmlAttribute(name="deletedValue")
		public String deletedValueExpression = "true";
		@XmlAttribute(name="undeletedValue")
		public String undeletedValueExpression = "false";
	}



	@XmlAttribute(name="name")
	public String name;

//	@XmlAttribute(name="isLogicalDeleteFlag")
//	public boolean isLogicalDeleteFlag;
//
//	@XmlAttribute(name="isOptimisticLockKey")
//	public boolean isOptimisticLockKey;

	public LogicalDeleteFlag logicalDeleteFlag;

	public OptimisticLockKey optimisticLockKey;

	public IgnoreFromQuery ignoreOnInsert;

	// TODO ignoreOnUpdate対応実装
	public IgnoreFromQuery ignoreOnUpdate;

	// TODO ignoreOnLogicalDelete対応実装
	public IgnoreFromQuery ignoreOnLogicalDelete;


	public ColumnNameResolvers colNameResolver;

	@XmlElements({
		@XmlElement(name="converter", type=IColValueConverter.ColValueConverter.class ),
		@XmlElement(name="strTrimConverter", type=IColValueConverter.StrTrimConverter.class),
		@XmlElement(name="intBoolConverter", type=IColValueConverter.IntBoolConverter.class),
		@XmlElement(name="enumConverter", type=IColValueConverter.EnumConverter.class)
	})
	public IColValueConverter converter;

	public SequenceRelation sequenceRelation;


	// ex: pos = "table[n]/col[n]"
	public void validate(String pos) {
//		if (isLogicalDeleteFlag == true && isOptimisticLockKey == true)
//		if (logicalDeleteFlag != null && optimisticLockKey != null)
//			Config.throwValidateError("logicalDeleteFlag と optimisticLockKey は同時に設定できません。");
//		if (logicalDeleteFlag != null && optimisticLockCounter != null)
//			Config.throwValidateError("logicalDeleteFlag と optimisticLockCounter は同時に設定できません。");
//		if (optimisticLockKey != null && optimisticLockCounter != null)
//			Config.throwValidateError("optimisticLockKey と optimisticLockCounter は同時に設定できません。");
		if (ignoreOnUpdate != null && logicalDeleteFlag != null)
			Config.throwValidateError("ignoreOnUpdate と logicalDeleteFlag は同時に設定できません。");
//		if (ignoreOnUpdate != null && optimisticLockCounter != null)
//			Config.throwValidateError("ignoreOnUpdate と optimisticLockCounter は同時に設定できません。");
		if (colNameResolver != null) colNameResolver.validate(pos);
		if (converter != null) converter.validate(pos);
//		if (enumRelation != null) enumRelation.validate(pos);
		if (sequenceRelation != null) sequenceRelation.validate(pos);
	}


	void merge(ColumnConfig gc) {
		if (colNameResolver == null) colNameResolver = gc.colNameResolver;
		if (converter == null) converter = gc.converter;
		if (ignoreOnInsert == null) ignoreOnInsert = gc.ignoreOnInsert;
		if (ignoreOnUpdate == null) ignoreOnUpdate = gc.ignoreOnUpdate;
		if (ignoreOnLogicalDelete == null) ignoreOnLogicalDelete = gc.ignoreOnLogicalDelete;
		if (logicalDeleteFlag == null) logicalDeleteFlag = gc.logicalDeleteFlag;
		if (optimisticLockKey == null) optimisticLockKey = gc.optimisticLockKey;
		if (sequenceRelation == null) sequenceRelation = gc.sequenceRelation;
	}
}
