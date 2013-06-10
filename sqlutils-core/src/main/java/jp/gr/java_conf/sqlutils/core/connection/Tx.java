package jp.gr.java_conf.sqlutils.core.connection;

import jp.gr.java_conf.sqlutils.DBManager;
import jp.gr.java_conf.sqlutils.DBManager.PostProcess;
import jp.gr.java_conf.sqlutils.DBManager.PostProcessOnException;

public abstract class Tx {

	protected abstract void run(DBManager manager);


	public void execute(DBManager manager) {
		try {
			manager.setPostProcess(PostProcess.NONE); // TODO こやって指定しても、子プロセスの中で変更できてしまう
			manager.setPostProcessOnException(PostProcessOnException.NONE); // TODO こやって指定しても、子プロセスの中で変更できてしまう
			run(manager);
			manager.commit();
		} catch (RuntimeException e) {
			manager.rollback();
			throw e;
		} finally {
			manager.close();
		}
	}
}
