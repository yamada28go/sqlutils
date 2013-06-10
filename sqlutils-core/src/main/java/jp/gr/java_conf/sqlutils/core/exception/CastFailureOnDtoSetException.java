package jp.gr.java_conf.sqlutils.core.exception;

import jp.gr.java_conf.sqlutils.core.dto.IDto;

public class CastFailureOnDtoSetException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public CastFailureOnDtoSetException(Class<IDto> tblClass, String colName, Object val) {
		super(
			String.format("DTOのフィールドへの値の格納に失敗しました。ClassCastException : DtoClass={%s},Column={%s},val={%s}",
				tblClass.getSimpleName(), colName, val.toString()));
	}


}
