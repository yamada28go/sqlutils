package jp.gr.java_conf.sqlutils.core.dto;

import java.util.HashMap;

import jp.gr.java_conf.sqlutils.core.exception.DtoSetClassDuplicationException;
import jp.gr.java_conf.sqlutils.core.exception.NoSuchColumnException;

public class DtoSet extends HashMap<String, IDto> implements IDto {

	private static final long serialVersionUID = 1L;


	@SuppressWarnings("rawtypes")
	public static IDto create(Class<? extends IDto>... classes) {
		try {

			if (classes.length == 1)
				return classes[0].newInstance();

			DtoSet ret;
			switch(classes.length) {
			case 2:	ret = new JoinnedDto();	break;
			case 3:	ret = new ThreeJoinned();	break;
			case 4:	ret = new FourJoinned();	break;
			case 5:	ret = new FiveJoinned();	break;
			case 6:	ret = new SixJoinned();	break;
			case 7:	ret = new SixJoinned();	break;
			case 8:	ret = new EightJoinned();	break;

			default:
				throw new RuntimeException();
			}
			for (Class<? extends IDto> cls : classes) {
//				String key = cls.getCanonicalName();
				String key = cls.getName();
				/*
				 * for property-expression access, like wicket's propertymodel
				 * <pre>
				 * new PropertyModel<?>(this, "dtoSetName[some.dto.class.name].fieldName");
				 * new PropertyModel<?>(dtoSet, "[some.dto.class.name].fieldName");
				 * </pre>
				 */
				if (ret.containsKey(key))
					throw new DtoSetClassDuplicationException(
							"IDto class duplicated error. " +
							"If you want, please create alias-class, by extending original-class.");
				ret.put(key, cls.newInstance());
			}
			return ret;
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	public static class JoinnedDto<T1,T2> extends DtoSet {
		private static final long serialVersionUID = 1L;
	}

	public static class ThreeJoinned<T1,T2,T3> extends DtoSet {
		private static final long serialVersionUID = 1L;
	}

	public static class FourJoinned<T1,T2,T3,T4> extends DtoSet {
		private static final long serialVersionUID = 1L;
	}

	public static class FiveJoinned<T1,T2,T3,T4,T5> extends DtoSet {
		private static final long serialVersionUID = 1L;
	}

	public static class SixJoinned<T1,T2,T3,T4,T5,T6> extends DtoSet {
		private static final long serialVersionUID = 1L;
	}

	public static class SevenJoinned<T1,T2,T3,T4,T5,T6,T7> extends DtoSet {
		private static final long serialVersionUID = 1L;
	}

	public static class EightJoinned<T1,T2,T3,T4,T5,T6,T7,T8> extends DtoSet {
		private static final long serialVersionUID = 1L;
	}



	@SuppressWarnings("unchecked")
	public <T extends IDto> T get(Class<T> t) {
		return (T) super.get(t.getName());
	}

	@Deprecated
	@Override
	public void set(String colName, Object val) throws NoSuchColumnException {
		throw new RuntimeException("Use DtoUtil#set() instead.");
	}

	@Deprecated
	@Override
	public Object get(String colName) throws NoSuchColumnException {
		throw new RuntimeException("Use DtoUtil#get() instead.");
	}

	@Override
	@Deprecated
	public String getTableName() {
		throw new RuntimeException("UnAvaialble");
	}
}
