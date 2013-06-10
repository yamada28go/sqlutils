package jp.gr.java_conf.sqlutils.core.connection;

import java.sql.Connection;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadLocalConnectionProvider implements IConnectionProvider {

	private static final Logger logger = LoggerFactory.getLogger(ThreadLocalConnectionProvider.class);


	public static class ConnectionInfo {
		public UnClosableConnection connection;
		public boolean exceptionThrown;
	}

	private static ThreadLocal<ConnectionInfo> threadlocalConnectionInfo =
			new ThreadLocal<ConnectionInfo>() {
				protected ConnectionInfo initialValue() {
					return new ConnectionInfo();
				}
			};


	/**
	 * Exception発生フラグを立てる。
	 * フラグが立っていれば、リクエスト完了時にRollbackする。立ってなければCommitする。
	 */
	public static void setTLConnectionRollback() {
		threadlocalConnectionInfo.get().exceptionThrown = true;
	}

	/**
	 * ThreadLocalなConnectionをCommitまたはRollbackする
	 * リクエストの処理完了時に呼び出す
	 */
	public static void closeTLConnection() {
		ConnectionInfo info = threadlocalConnectionInfo.get();
		if (info.connection != null) {
			try {
				if (info.exceptionThrown) {
					info.connection.rollbackNative();
					logger.debug("rollback():ThreadId=" + Thread.currentThread().getId());
				} else {
					info.connection.commitNative();
					logger.debug("commit():ThreadId=" + Thread.currentThread().getId());
				}
				info.connection.closeNative();
				logger.debug("close():ThreadId=" + Thread.currentThread().getId());
			} catch (SQLException e1) {}

			// ThreadLocalから削除
			threadlocalConnectionInfo.remove();
		}
	}




	private IConnectionProvider baseProvider;

	public ThreadLocalConnectionProvider(IConnectionProvider baseProvider) {
		this.baseProvider = baseProvider;
//		this.baseProvider.setDefaultAutoCommit(false);
	}


	public Connection createConnection() {
		ConnectionInfo info = threadlocalConnectionInfo.get();
		if (info.connection == null)
			info.connection = new UnClosableConnection(baseProvider.createConnection());
		return info.connection; // DBManagerからはCloseできないConnectionを返却する
	}

//	public void setDefaultAutoCommit(boolean autoCommit) {
//		// NOP. Always false
//	}
//
//	public boolean getDefaultAutoCommit() {
//		return false;
//	}
}
