package jp.gr.java_conf.sqlutils.core.connection;

import java.sql.Connection;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * コネクション生成クラス.<br/>
 * <p>
 * WEBサーブレット向け。ThreadLocalを使用する事で、常に同一スレッドに対して同一のコネクションを割り振る。<br/>
 * ラッパー構造となっており、実際にコネクションを生成するのは子のIConnectionProviderクラス。<br/>
 * 但し生成されるコネクションクラスもラップされており、通常の{@link Connection#commit()}や{@link Connection#close()}などを無視する作りになっている。<br/>
 * <p>
 * コネクションの制御には、以下のstaticメソッドを呼ぶ必要がある。
 * <li>{@link ThreadLocalConnectionProvider#setTLConnectionRollback()}
 * <li>{@link ThreadLocalConnectionProvider#closeTLConnection()}
 *
 */
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
	 * カレントスレッドに割り当てられているコネクションに対して、Exception発生フラグを立てる。
	 */
	public static void setTLConnectionRollback() {
		threadlocalConnectionInfo.get().exceptionThrown = true;
	}

	/**
	 * カレントスレッドに割り当てられているコネクションをクローズする。
	 * Exception発生フラグに応じて、コミットあるいはロールバックを行う。
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

	/**
	 * コンストラクタ.<br/>
	 * @param baseProvider 実際にコネクションを生成するプロバイダ
	 */
	public ThreadLocalConnectionProvider(IConnectionProvider baseProvider) {
		this.baseProvider = baseProvider;
	}

	public Connection createConnection() {
		ConnectionInfo info = threadlocalConnectionInfo.get();
		if (info.connection == null) {
			info.connection = new UnClosableConnection(baseProvider.createConnection());
//			info.connection.setAutoCommit(false);
		}
		return info.connection; // DBManagerからはCloseできないConnectionを返却する
	}
}
