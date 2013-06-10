package jp.gr.java_conf.sqlutils.core.exception;

import jp.gr.java_conf.sqlutils.core.dto.IDto;

public class NoSuchFieldOnDtoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public NoSuchFieldOnDtoException(Class<IDto> tblClass, String colName) {
		super(
			String.format("カラム名に対応するフィールドが、格納先DTOに見つかりません : DtoClass={%s},Column={%s}",
				tblClass.getSimpleName(), colName));
	}


}
