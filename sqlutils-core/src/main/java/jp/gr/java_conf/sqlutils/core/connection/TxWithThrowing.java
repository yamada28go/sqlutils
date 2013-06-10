package jp.gr.java_conf.sqlutils.core.connection;

import jp.gr.java_conf.sqlutils.DBManager;
import jp.gr.java_conf.sqlutils.DBManager.PostProcess;

public abstract class TxWithThrowing<E extends Exception> {

	protected abstract void run(DBManager manager) throws E;


	@SuppressWarnings("unchecked")
	public void execute(DBManager manager) throws E {
		try {
			manager.setPostProcess(PostProcess.NONE); // TODO こやって指定しても、子プロセスの中で変更できてしまう
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
