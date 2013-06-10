package jp.gr.java_conf.sqlutils.core.connection;

@Deprecated
public abstract class TransactionWithException<E extends Exception> {

	protected abstract void run() throws E;

	private Class<E> clazz;

	public TransactionWithException(Class<E> clazz) {
		this.clazz = clazz;
	}

	public void execute() throws E {
		try {
			TxManagableConnectionProvider.startTxScope();
			run();
			TxManagableConnectionProvider.commitTxScope();
		} catch (RuntimeException e) {
			TxManagableConnectionProvider.rollbackTxScope();
			throw e;
		} catch (Exception e) {
			TxManagableConnectionProvider.rollbackTxScope();
			throw clazz.cast(e);
		}
	}
}
