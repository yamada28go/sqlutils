package jp.gr.java_conf.sqlutils.core.exception;

public class TableNameRetrieveFailureException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public TableNameRetrieveFailureException(int num, String tblName, String colName) {
		super(String.format("テーブル名が不明なため、実行結果の格納先DTOが決定できません。" +
				"取得された名称：tblName={%s}, colName={%s} ,num={%d}", tblName, colName, num));
	}


}
