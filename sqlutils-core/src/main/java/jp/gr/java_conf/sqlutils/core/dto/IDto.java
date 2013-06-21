package jp.gr.java_conf.sqlutils.core.dto;

import java.io.Serializable;

import jp.gr.java_conf.sqlutils.core.exception.NoSuchColumnException;

public interface IDto extends Serializable {


	/**
	 * DBからJDBCで取得したデータを、DTOインスタンスに格納するためのIF。<br>
	 * IDtoクラスを自作した場合は、ここに任意の実装を行って下さい。<br>
	 * 通常は変換処理を行うため、ユーザロジックからこのメソッドにアクセスする事は想定されていません。<br>
	 * ユーザロジックからは、直接フィールドにアクセスして下さい。
	 *
	 * @param colName
	 * @param val
	 * @throws NoSuchColumnException
	 */
	void set(String colName, Object val) throws NoSuchColumnException;

	/**
	 * DBへのInsert処理等の際に、DTOインスタンスから値を取り出すためのIF.<br>
	 * IDtoクラスを自作した場合は、ここに任意の実装を行って下さい。<br>
	 * 通常は変換処理を行うため、ユーザロジックからこのメソッドにアクセスする事は想定されていません。<br>
	 * ユーザロジックからは、直接フィールドにアクセスして下さい。
	 *
	 * @param colName
	 * @return
	 * @throws NoSuchColumnException
	 */
	Object get(String colName) throws NoSuchColumnException;

	/**
	 * データ取得処理において、どのテーブルの受け皿として使用されるかが、この戻り値により決定されます。<br>
	 * IDtoクラスを自作した場合は、ここに任意の実装を行って下さい。<br>
	 * 自己結合したSQLによる取得処理を実行する際は、元Dtoを継承し、この戻り値をオーバーライドして受け皿Dtoを複製する事で
	 * それぞれのDTOにデータを受け取る事が可能です。
	 *
	 * <pre>
	 * public static class Master1Alias extends Master1 {
	 *     private static final long serialVersionUID = 1L;
	 *     @Override
	 *     public String getTableName() {
	 *         return "M1_ALIAS";
	 *     }
	 * }
	 * </pre>
	 *
	 * @return
	 */
	String getTableName();


	public interface IVoidDto extends IDto {
	}


	/**
	 * FWによって自動生成されたDTO
	 */
	public interface IGeneratedDto extends IDto, Cloneable {
		ITable getTableDefinition();
		boolean isNull();
		IGeneratedDto clone();
	}

	/**
	 * プライマリキーを持つテーブルのDTOである事を示すマーカーIF<br>
	 * DBManagerのPersistor系の機能は、IPersistableなDTOに対してのみ利用可能
	 */
	public interface IPersistable extends IGeneratedDto {
	}

	/**
	 * FWの楽観排他機能が有効になっている事を示すマーカーIF<br>
	 */
	public interface IOptimisticLocking extends IPersistable {
	}

	/**
	 * FWの論理削除機能が有効になっている事を示すマーカーIF<br>
	 */
	public interface ILogicalDeleting extends IPersistable {
	}

//	public interface IDtoField {
//		String name();
//		String fullname();
//	}
}
