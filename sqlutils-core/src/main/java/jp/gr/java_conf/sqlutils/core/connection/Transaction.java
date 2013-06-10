package jp.gr.java_conf.sqlutils.core.connection;

@Deprecated
public abstract class Transaction {

	protected abstract void run();

	public void execute() {
		try {
			TxManagableConnectionProvider.startTxScope();
			run();
			TxManagableConnectionProvider.commitTxScope();
		} catch (RuntimeException e) {
			TxManagableConnectionProvider.rollbackTxScope();
			throw e;
		}
	}
}
