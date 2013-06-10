package jp.gr.java_conf.sqlutils.core.exception;


/**
 *
 * Not runnable exception, please ready for catch.
 *
 * @author Hamanishi
 *
 */
public class OptimisticLockingException extends Exception {

	private static final long serialVersionUID = 1L;

	public OptimisticLockingException() {
		super("他のユーザにより更新されました。最新の状態を確認して下さい。");
	}
}
