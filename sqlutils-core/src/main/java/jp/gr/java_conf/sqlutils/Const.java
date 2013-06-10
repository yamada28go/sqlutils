package jp.gr.java_conf.sqlutils;

public interface Const {

	@Deprecated
	public enum DBMS {ORACLE, POSTGRES, MYSQL, SQLSERVER, H2, SYMFOWARE, ELSE} // DB2, SQLITE,

	/**
	 * row_number関数で取得するカラムの別名
	 * select ..., row_number() as ROW_NUMBER__
	 * _から始まる別名は、Oracleではエラーになるので注意
	 */
	public static final String ROW_NUMBER_COL_NAME = "ROW_NUMBER__"; // Oracleで_から始まる別名はエラーになる

	/**
	 * 取得カラムの別名に、「テーブル名＆カラム名」を付加する際に使用するセパレータ
	 * select COL1 as TBL1__COL1 from TBL1
	 */
	public static final String TBL_COL_SEPARATOR = "___";

	/**
	 * テーブルを走査してDTOを生成する際に、Javaのキーワードと被った時に末尾付加する
	 * ex:カラム名「INTERFACE」 → DTOフィールド名「interface_」
	 */
	public static final String FIELDNAME_SUFFIX = "_";

	/**
	 * テーブルを走査してDTOを生成する際に、Javaで使用できない変数名（数字等で始まる）になった時に先頭付加する
	 * ex:カラム名「1COL」 → DTOフィールド名「_1col」
	 */
	public static final String FIELDNAME_PREFIX = "_";
}
