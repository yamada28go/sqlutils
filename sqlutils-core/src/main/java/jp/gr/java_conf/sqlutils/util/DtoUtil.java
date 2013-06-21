package jp.gr.java_conf.sqlutils.util;

import jp.gr.java_conf.sqlutils.core.dto.DtoSet;
import jp.gr.java_conf.sqlutils.core.dto.IColumn;
import jp.gr.java_conf.sqlutils.core.dto.IDto;
import jp.gr.java_conf.sqlutils.core.exception.NoSuchColumnException;

public class DtoUtil {

	@SuppressWarnings("unchecked")
	public static <T> T get(IDto dto, IColumn<T> col) throws NoSuchColumnException {
		IDto singleDto = dto;
		if (dto instanceof DtoSet) {
			singleDto = ((DtoSet)dto).get(col.getTable().getDtoClass());
		}
		return (T) singleDto.get(col.name());
	}

	public static <T> void set(IDto dto, IColumn<T> col, T val) throws NoSuchColumnException {
		IDto singleDto = dto;
		if (dto instanceof DtoSet) {
			singleDto = ((DtoSet)dto).get(col.getTable().getDtoClass());
		}
		singleDto.set(col.name(), val);
	}

	public static boolean isEqualCol(Object o1, Object o2) {
		if (o1 == null && o2 == null)
			return true;
		else if (o1 != null && o2 != null)
			return o1.equals(o2);
		else
			return false;
	}
}
