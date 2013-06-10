package jp.gr.java_conf.sqlutils.core.connection;

import java.sql.Connection;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Deprecated
public class TxManagableConnectionProvider implements IConnectionProvider {

	private static final Logger logger = LoggerFactory.getLogger(TxManagableConnectionProvider.class);

	private static class ConnectionCacher {
		public UnClosableConnection connection;
	}

	private static ThreadLocal<ConnectionCacher> connectionCacher =
			new ThreadLocal<ConnectionCacher>() {
				protected ConnectionCacher initialValue() {
					return new ConnectionCacher();
				}
			};

	private static ThreadLocal<Boolean> withinTxScope = new ThreadLocal<Boolean>() {
		protected Boolean initialValue() {
			return false;
		}
	};


	static void startTxScope() {
		withinTxScope.set(true);
	}

	static void rollbackTxScope() {
		endTxScope(false);
	}

	static void commitTxScope() {
		endTxScope(true);
	}

	private static void endTxScope(boolean success) {
		if (withinTxScope.get()) {
			UnClosableConnection conn = connectionCacher.get().connection;
			if (conn != null) {
				try {
					if (success)
						conn.commitNative();
					else
						conn.rollbackNative();
					conn.closeNative();
				} catch (SQLException e) {
					logger.error("Failed to commit or rollback or close", e);
				}
			}
		}
		withinTxScope.remove();
		connectionCacher.remove();
	}




	private IConnectionProvider baseProvider;

	public TxManagableConnectionProvider(IConnectionProvider baseProvider) {
		if (baseProvider instanceof ThreadLocalConnectionProvider)
			throw new RuntimeException("ThreadLocalConnectionProvider is not supported.");

		this.baseProvider = baseProvider;
	}

	public Connection createConnection() {
		if (withinTxScope.get() == false) {
			return baseProvider.createConnection(); // 素のConnectionを返却する

		} else {
			ConnectionCacher info = connectionCacher.get();
			if (info.connection == null)
				info.connection = new UnClosableConnection(baseProvider.createConnection());
			return info.connection; // DBManagerからはCloseできないConnectionを返却する
		}
	}


//	public static abstract class Transaction {
//
//		protected abstract void run();
//
//		public void execute() {
//			try {
//				startTxScope();
//				run();
//				commitTxScope();
//			} catch (RuntimeException e) {
//				rollbackTxScope();
//				throw e;
//			}
//		}
//	}

//	public static abstract class TransactionWithException<E extends Exception> {
//
//		protected abstract void run() throws E;
//
//		private Class<E> clazz;
//
//		public TransactionWithException(Class<E> clazz) {
//			this.clazz = clazz;
//		}
//
//		public void execute() throws E {
//			try {
//				startTxScope();
//				run();
//				commitTxScope();
//			} catch (RuntimeException e) {
//				rollbackTxScope();
//				throw e;
//			} catch (Exception e) {
//				rollbackTxScope();
//				throw clazz.cast(e);
//			}
//		}
//	}
}
