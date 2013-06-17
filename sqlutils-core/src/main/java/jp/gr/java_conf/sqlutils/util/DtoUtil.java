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

//	private static RuntimeException newMissingDtoClassException(Class<? extends IDto> clazz) {
//		return new RuntimeException(
//				String.format("Missing dto class {%s} in DtoSet. " +
//						"IF dto is not genereted, use DtoPropertyModel instead.", clazz.getCanonicalName()));
//
//	}

//	@Deprecated
//	public static IDto getSingleDto(IDto dto, Class<? extends IDto> clazz) {
//		if (dto instanceof DtoSet) {
//			IDto ret = ((DtoSet)dto).get(clazz);
////			if (ret == null)
////				throw newMissingDtoClassException(clazz);
//			return ret;
//		} else
//			return dto;
//	}
//
//	@Deprecated
//	public static Object get(IDto singleDto, String colName) throws NoSuchColumnException {
//		return singleDto.get(colName);
//	}
//
//	@Deprecated
//	public static Object get(DtoSet dto, Class<? extends IDto> clazz, String colName) throws NoSuchColumnException {
//		IDto singleDto = getSingleDto(dto, clazz);
//		return singleDto.get(colName);
//	}
//
//	@Deprecated
//	public static void set(IDto singleDto, String colName, Object value) throws NoSuchColumnException {
//		singleDto.set(colName, value);
//	}
//
//	@Deprecated
//	public static void set(DtoSet dto, Class<? extends IDto> clazz, String colName, Object value) throws NoSuchColumnException {
//		IDto singleDto = getSingleDto(dto, clazz);
//		singleDto.set(colName, value);
//	}

//	public static Object get(IDto dto, IColumn<?> col) throws NoSuchColumnException {
//		if (dto instanceof DtoSet) {
//			Class<? extends IGeneratedDto> clazz = col.getTable().getDtoClass();
//			dto = ((DtoSet)dto).get(clazz);
//			if (dto == null)
//				throw newMissingDtoClassException(clazz);
//		}
//		return dto.get(col.name());
//	}
//
//	public static void set(IDto dto, IColumn<?> col, Object value) throws NoSuchColumnException {
//		if (dto instanceof DtoSet) {
//			Class<? extends IGeneratedDto> clazz = col.getTable().getDtoClass();
//			dto = ((DtoSet)dto).get(clazz);
//			if (dto == null)
//				throw newMissingDtoClassException(clazz);
//		}
//		dto.set(col.name(), value);
//	}

//	@SuppressWarnings("deprecation")
//	public static Object get(IDto dto, String expression) {
//
//		try {
//			if (!expression.contains(".")) {
//				Field f = dto.getClass().getField(expression);
//				return f.get(dto);
//			}
//
//			Object val = null;
//			boolean propertyValueGet = true;
//			IDto target = dto;
//			String[] exps = expression.split("\\.");
//			for (int i = 0; i < exps.length; i++) {
//				if (i == 0 && dto instanceof DtoSet) {
//					// expressionはDtoの指定
//					target = ((DtoSet)dto).get(exps[0]);
//					continue;
//				}
//
//				if (target == null)
//					return null;
//
//				// expressionはDtoのPropertyの指定
//				if (propertyValueGet) {
//					Field f = target.getClass().getField(exps[i]);
//					val = f.get(target);
//					propertyValueGet = false;
//					continue;
//				}
//
//				if (val == null) {
//					return null;
//				} else {
//					// expressionはObjectのゲッター指定
//					Method m = val.getClass()
//								.getMethod("get" + StringUtils.capitalize(exps[i]));
//					val = m.invoke(val);
//				}
//			}
//			return val;
//
//		} catch (SecurityException e) {
//			throw new RuntimeException(e);
//		} catch (IllegalArgumentException e) {
//			throw new RuntimeException(e);
//		} catch (IllegalAccessException e) {
//			throw new RuntimeException(e);
//		} catch (NoSuchFieldException e) {
//			throw new RuntimeException(e);
//		} catch (NoSuchMethodException e) {
//			throw new RuntimeException(e);
//		} catch (InvocationTargetException e) {
//			throw new RuntimeException(e);
//		}
//	}

//	public static void set(IDto dto, String expression, Object value) {
//		try {
//
//			// SetはDtoSetに対応しない。必要無いと思うので。
//			if (dto instanceof DtoSet) {
//				throw new RuntimeException(
//						"DtoSetには対応していません。Dto個々にsetしてください。");
//			}
//
//			// Fieldのセッターにも対応しない。KeyValueEnumへのセットはどのみち無理なので。
//			if (expression.contains(".")) {
//				throw new RuntimeException(
//						"Multi-expressionには対応していません。" +
//						"DtoのProperty名のみを指定して下さい。" +
//						"KeyValueEnumを格納したい場合、KeyValueEnumDispNameConverter等を使って事前にインスタンスに変換して下さい");
//			}
//
//			Field f = dto.getClass().getField(expression);
//			f.set(dto, value);
//			return;
//
//
////			if (!expression.contains(".")) {
////				Field f = dto.getClass().getField(expression);
////				f.set(dto, value);
////				return;
////			}
////
////			IDto target = dto;
////			String[] exps = expression.split("\\.");
////			int offset = 0;
////
////			if (dto instanceof DtoSet) {
////				// expressionはDtoを指定
////				target = ((DtoSet)dto).get(exps[0]);
////				offset = 1;
////			}
////
////			// expressionはDtoのPropertyを指定
////			Field f = target.getClass().getField(exps[0 + offset]);
////			if (exps.length - offset == 1) {
////				f.set(dto, value);
////				return;
////			}
////
////			Object obj = f.get(dto);
////			Method m = obj.getClass().getMethod("set" + ConverterUtil.lowerToUpperCamelize(exps[1 + offset]), Object.class);
////			m.invoke(value);
//
//		} catch (SecurityException e) {
//			throw new RuntimeException(e);
//		} catch (NoSuchFieldException e) {
//			throw new RuntimeException(e);
//		} catch (IllegalArgumentException e) {
//			throw new RuntimeException(e);
//		} catch (IllegalAccessException e) {
//			throw new RuntimeException(e);
////		} catch (NoSuchMethodException e) {
////			throw new RuntimeException(e);
////		} catch (InvocationTargetException e) {
////			throw new RuntimeException(e);
//		}
//	}

//	public static void copy(IDto src, IDto dest) {
//		copy(src, dest, true);
//	}
//
//	public static void copy(IDto src, IDto dest, boolean ignoreDestFieldNotExist) {
//		Class<? extends IDto> clazz = src.getClass();
//		for (Field fGet : clazz.getFields()) {
//			try {
//				String fieldName = fGet.getName();
//				Object v = fGet.get(src);
//				Field fSet = dest.getClass().getField(fieldName);
//				fSet.set(dest, v);
//
//			} catch (NoSuchFieldException e) {
//				if (ignoreDestFieldNotExist)
//					continue;
//				else
//					throw new RuntimeException(e);
//			} catch (IllegalArgumentException e) {
//				throw new RuntimeException(e);
//			} catch (IllegalAccessException e) {
//				throw new RuntimeException(e);
//			}
//		}
//	}
//
//	public static Object getProperty(IDto dto, String propName) {
//		try {
//			return dto.getClass().getField(propName).get(dto);
//		} catch (IllegalArgumentException e) {
//			throw new RuntimeException(e);
//		} catch (SecurityException e) {
//			throw new RuntimeException(e);
//		} catch (IllegalAccessException e) {
//			throw new RuntimeException(e);
//		} catch (NoSuchFieldException e) {
//			throw new RuntimeException(e);
//		}
//	}
//
//	public static void fillWithNull(IDto dto) {
//		Class<? extends IDto> clazz = dto.getClass();
//		for (Field f : clazz.getFields()) {
//			try {
//				f.set(dto, null);
//			} catch (IllegalArgumentException e) {
//				continue;
//			} catch (IllegalAccessException e) {
//				continue;
//			}
//		}
//	}
//
//	public static boolean isEqualCol(Object o1, Object o2) {
//		if (o1 == null && o2 == null)
//			return true;
//		else if (o1 != null && o2 != null)
//			return o1.equals(o2);
//		else
//			return false;
//	}
//
////	public static IGeneratedDtoHelper getHelper(IGeneratedDto dto) {
////		try {
////			return dto.getHelperClass().newInstance();
////		} catch (InstantiationException e) {
////			throw new RuntimeException(e);
////		} catch (IllegalAccessException e) {
////			throw new RuntimeException(e);
////		}
////	}

	public static boolean isEqualCol(Object o1, Object o2) {
		if (o1 == null && o2 == null)
			return true;
		else if (o1 != null && o2 != null)
			return o1.equals(o2);
		else
			return false;
	}
}
