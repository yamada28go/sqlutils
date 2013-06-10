package jp.gr.java_conf.sqlutils.core.exception;


public class NoSuchFieldOrMissingDtoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public NoSuchFieldOrMissingDtoException(String tblName, String colName) {
		super(
				String.format("格納先DTOが見つからないか、あるいはDTOに対応するフィールドが見つかりません : Table={%s},Column={%s}",
					tblName, colName));
	}


}
