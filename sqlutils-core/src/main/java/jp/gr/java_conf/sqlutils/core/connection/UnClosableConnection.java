package jp.gr.java_conf.sqlutils.core.connection;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * コネクションクラスのラッパー<br/>
 * {@link ThreadLocalConnectionProvider}が返却するクラスで、{@link Connection#commit()}や{@link Connection#close()}などを無視する。
 * <p>
 * このクラスのコネクションの制御には、以下のstaticメソッドを呼ぶ必要がある。
 * <li>{@link ThreadLocalConnectionProvider#setTLConnectionRollback()}
 * <li>{@link ThreadLocalConnectionProvider#closeTLConnection()}
 */
public class UnClosableConnection implements Connection {

	private static final Logger logger = LoggerFactory.getLogger(UnClosableConnection.class);


	private Connection base;

	public UnClosableConnection(Connection base) {
		this.base = base;
	}

	// ===============================================================
	// 以下のNativeメソッドはマスクする

	@Deprecated
	public void close() throws SQLException {
		logger.debug("Connection-close has called, but not closed actually.");
	}

	@Deprecated
	public void commit() throws SQLException {
		logger.debug("Connection-commit has called, but not commited actually.");
	}

	@Deprecated
	public void rollback() throws SQLException {
		logger.debug("Connection-rollback has called, but not rollbacked actually.");
	}



	// ===============================================================
	// Nativeメソッドの代りに用意

	public void closeNative() throws SQLException {
		base.close();
		logger.debug("Connection has closed actually.");
	}

	public void commitNative() throws SQLException {
		base.commit();
	}

	public void rollbackNative() throws SQLException {
		base.rollback();
	}






	@Override
	public void clearWarnings() throws SQLException {
		base.clearWarnings();
	}

	@Override
	public Statement createStatement() throws SQLException {
		return base.createStatement();
	}

	@Override
	public Statement createStatement(int arg0, int arg1) throws SQLException {
		return base.createStatement(arg0, arg1);
	}

	@Override
	public Statement createStatement(int arg0, int arg1, int arg2) throws SQLException {
		return base.createStatement(arg0, arg1, arg2);
	}

	@Override
	public boolean getAutoCommit() throws SQLException {
		return base.getAutoCommit();
	}

	@Override
	public String getCatalog() throws SQLException {
		return base.getCatalog();
	}

	@Override
	public int getHoldability() throws SQLException {
		return base.getHoldability();
	}

	@Override
	public DatabaseMetaData getMetaData() throws SQLException {
		return base.getMetaData();
	}

	@Override
	public int getTransactionIsolation() throws SQLException {
		return base.getTransactionIsolation();
	}

	@Override
	public Map<String, Class<?>> getTypeMap() throws SQLException {
		return base.getTypeMap();
	}

	@Override
	public SQLWarning getWarnings() throws SQLException {
		return base.getWarnings();
	}

	@Override
	public boolean isClosed() throws SQLException {
		return base.isClosed();
	}

	@Override
	public boolean isReadOnly() throws SQLException {
		return base.isReadOnly();
	}

	@Override
	public String nativeSQL(String arg0) throws SQLException {
		return base.nativeSQL(arg0);
	}

	@Override
	public CallableStatement prepareCall(String arg0) throws SQLException {
		return base.prepareCall(arg0);
	}

	@Override
	public CallableStatement prepareCall(String arg0, int arg1, int arg2) throws SQLException {
		return base.prepareCall(arg0, arg1, arg2);
	}

	@Override
	public CallableStatement prepareCall(String arg0, int arg1, int arg2, int arg3) throws SQLException {
		return base.prepareCall(arg0, arg1, arg2, arg3);
	}

	@Override
	public PreparedStatement prepareStatement(String arg0) throws SQLException {
		return base.prepareStatement(arg0);
	}

	@Override
	public PreparedStatement prepareStatement(String arg0, int arg1) throws SQLException {
		return base.prepareStatement(arg0, arg1);
	}

	@Override
	public PreparedStatement prepareStatement(String arg0, int[] arg1) throws SQLException {
		return base.prepareStatement(arg0, arg1);
	}

	@Override
	public PreparedStatement prepareStatement(String arg0, String[] arg1) throws SQLException {
		return base.prepareStatement(arg0, arg1);
	}

	@Override
	public PreparedStatement prepareStatement(String arg0, int arg1, int arg2) throws SQLException {
		return base.prepareStatement(arg0, arg1, arg2);
	}

	@Override
	public PreparedStatement prepareStatement(String arg0, int arg1, int arg2, int arg3) throws SQLException {
		return base.prepareStatement(arg0, arg1, arg2, arg3);
	}

	@Override
	public void rollback(Savepoint arg0) throws SQLException {
		base.rollback(arg0);
	}

	@Override
	public void releaseSavepoint(Savepoint arg0) throws SQLException {
		base.releaseSavepoint(arg0);
	}

	@Override
	public void setAutoCommit(boolean arg0) throws SQLException {
		base.setAutoCommit(arg0);
	}

	@Override
	public void setCatalog(String arg0) throws SQLException {
		base.setCatalog(arg0);
	}

	@Override
	public void setHoldability(int arg0) throws SQLException {
		base.setHoldability(arg0);
	}

	@Override
	public void setReadOnly(boolean arg0) throws SQLException {
		base.setReadOnly(arg0);
	}

	@Override
	public Savepoint setSavepoint() throws SQLException {
		return base.setSavepoint();
	}

	@Override
	public Savepoint setSavepoint(String arg0) throws SQLException {
		return base.setSavepoint(arg0);
	}

	@Override
	public void setTransactionIsolation(int arg0) throws SQLException {
		base.setTransactionIsolation(arg0);
	}

	@Override
	public void setTypeMap(Map<String, Class<?>> arg0) throws SQLException {
		base.setTypeMap(arg0);
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		return base.unwrap(iface);
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return base.isWrapperFor(iface);
	}

	@Override
	public Clob createClob() throws SQLException {
		return base.createClob();
	}

	@Override
	public Blob createBlob() throws SQLException {
		return base.createBlob();
	}

	@Override
	public NClob createNClob() throws SQLException {
		return base.createNClob();
	}

	@Override
	public SQLXML createSQLXML() throws SQLException {
		return base.createSQLXML();
	}

	@Override
	public boolean isValid(int timeout) throws SQLException {
		return base.isValid(timeout);
	}

	@Override
	public void setClientInfo(String name, String value) throws SQLClientInfoException {
		base.setClientInfo(name, value);
	}

	@Override
	public void setClientInfo(Properties properties) throws SQLClientInfoException {
		base.setClientInfo(properties);
	}

	@Override
	public String getClientInfo(String name) throws SQLException {
		return base.getClientInfo(name);
	}

	@Override
	public Properties getClientInfo() throws SQLException {
		return base.getClientInfo();
	}

	@Override
	public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
		return base.createArrayOf(typeName, elements);
	}

	@Override
	public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
		return base.createStruct(typeName, attributes);
	}
}
