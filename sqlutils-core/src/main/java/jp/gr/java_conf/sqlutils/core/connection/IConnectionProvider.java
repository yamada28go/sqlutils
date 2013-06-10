package jp.gr.java_conf.sqlutils.core.connection;

import java.sql.Connection;

public interface IConnectionProvider {

//	void setDefaultAutoCommit(boolean autoCommit);
//	boolean getDefaultAutoCommit();
	Connection createConnection();
}
