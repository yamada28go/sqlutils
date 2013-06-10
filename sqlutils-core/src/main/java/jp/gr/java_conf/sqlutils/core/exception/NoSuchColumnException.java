package jp.gr.java_conf.sqlutils.core.exception;


public class NoSuchColumnException extends Exception {

	private static final long serialVersionUID = 1L;


	public NoSuchColumnException(String colName) {
		super("ColumnName={" + colName + "}");
	}
}
