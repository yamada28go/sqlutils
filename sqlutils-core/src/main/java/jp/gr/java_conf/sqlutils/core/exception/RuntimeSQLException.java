package jp.gr.java_conf.sqlutils.core.exception;

import java.sql.SQLException;

public class RuntimeSQLException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public RuntimeSQLException(String msg, SQLException cause) {
        super(msg, cause);
    }

	public RuntimeSQLException(SQLException cause) {
        super(cause);
    }

	public RuntimeSQLException(String msg) {
		super(msg);
	}
}
