package jp.gr.java_conf.sqlutils.core.exception;

public class MissingDtoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public MissingDtoException(String tblName) {
		super("テーブル名に対応する格納先DTOが見つかりません : TableName={" + tblName + "}");
	}


}
