package jp.gr.java_conf.sqlutils.core.connection;

import jp.gr.java_conf.sqlutils.DBManager;
import jp.gr.java_conf.sqlutils.DBManager.PostProcess;
import jp.gr.java_conf.sqlutils.DBManager.PostProcessOnException;

/**
 * トランザクション処理を実現するクラス.<br/>
 * 内部でException（RuntimeExceptionではない）スローする処理を実装する場合のクラス。
 *
 * {@link ThreadLocalConnectionProvider}を使用している場合は、スレッドがトランザクション境界になるので、
 * このクラスを使う意味は無い。
 *
 *
 */
public abstract class TxWithThrowing<E extends Exception> {

	protected abstract void run(DBManager manager) throws E;


	@SuppressWarnings("unchecked")
	public void execute(DBManager manager) throws E {
		try {
			// runメソッド内で複数回managerメソッドが呼ばれる使い方を想定しているので、一回毎のPostProcessをNONEに設定
			// 但しこうしても、run内で設定変更はできてしまうのだが、そこまでブロックはできないので実装任せ。
			manager.setPostProcess(PostProcess.NONE);
			manager.setPostProcessOnException(PostProcessOnException.NONE);
			run(manager);
			manager.commit();
		} catch (RuntimeException e) {
			manager.rollback();
			throw e;
		} catch (Exception e) {
			manager.rollback();
			throw (E)e;
		} finally {
			manager.close();
		}
	}
}
