package jp.gr.java_conf.sqlutils.core.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import jp.gr.java_conf.sqlutils.core.exception.RuntimeSQLException;



/**
 *
 * ■ コネクションプーリングを使わない場合
 * IConnectionProvider　provider = new SimpleConnectionProvider(driverName, url, user, pass);
 *
 * ■ コネクションプーリングを使う場合
 * IConnectionProvider　provider = new SimpleConnectionProvider(datasourceName);
 *
 *
 */
public class SimpleConnectionProvider implements IConnectionProvider {

	private interface IFactory {
		Connection create() throws Exception;
	}

	private class DefaultFactory implements IFactory {
		private String driverName;
		private String url;
		private Properties props;
//		private String user;
//		private String pass;
		public DefaultFactory(String driverName, String url, Properties props) {
			this.driverName = driverName;
			this.url = url;
			this.props = props;
		}
		public Connection create() throws Exception {
			Class.forName(driverName);
			return DriverManager.getConnection(url, props);
		}
	}

	private class PoolingFactory implements IFactory {
		private String datasourceName;
		public PoolingFactory(String datasourceName) {
			this.datasourceName = datasourceName;
		}
		public Connection create() throws Exception {
			final String prefix = "java:/comp/env/";
			InitialContext ctx = new InitialContext();
			DataSource ds = (DataSource)ctx.lookup(prefix + datasourceName);
			return ds.getConnection();
		}
	}


	private IFactory factory;

	/**
	 * @param driverName
	 * @param url
	 * @param user
	 * @param pass
	 */
	public SimpleConnectionProvider(String driverName, String url, String user, String pass) {
		Properties props = new Properties();
		props.put("user", user);
		props.put("password", pass);
		this.factory = new DefaultFactory(driverName, url, props);
	}

	/**
	 * @param driverName
	 * @param url
	 * @param props
	 */
	public SimpleConnectionProvider(String driverName, String url, Properties props) {
		this.factory = new DefaultFactory(driverName, url, props);
	}

	/**
	 * @param datasourceName
	 */
	public SimpleConnectionProvider(String datasourceName) {
		this.factory = new PoolingFactory(datasourceName);
	}

	public Connection createConnection() {
		try {
			Connection conn = factory.create();
			conn.setAutoCommit(false); // JDBC-Connection側でのAutoCommitは常に無効にする（DBManager側のAutoCommitと喧嘩するので）
			return conn;

		} catch (Exception e) {
			if (e instanceof NamingException) {
				throw new RuntimeException(e);
			}
			if (e instanceof ClassNotFoundException) {
				throw new RuntimeException(e);
			}
			if (e instanceof SQLException) {
				throw getConnectionFailure((SQLException) e);
			}
			else {
				throw new RuntimeException(e);
			}
		}
	}

	protected RuntimeException getConnectionFailure(SQLException e) {
		return new RuntimeSQLException("Create db connection failure.", e);
	}

}
