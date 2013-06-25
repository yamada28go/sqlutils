package jp.gr.java_conf.sqlutils.core.builder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jp.gr.java_conf.sqlutils.common.ValueEnum.IValueEnum;
import jp.gr.java_conf.sqlutils.core.builder.BuilderElement.ColElement;
import jp.gr.java_conf.sqlutils.core.builder.ConditionBuilder.ConditionArray.ConditionType;
import jp.gr.java_conf.sqlutils.core.dto.IColumn;

import org.apache.commons.lang.StringUtils;

public class ConditionBuilder {

//	public static final Condition IGNORE_WITH_NULL = new Condition("DUMMY");


	public interface IConditionColumn<T> {
		String fullname(boolean appendSchema);
	}

	public static class Condition implements IConditionElement {
		private IConditionColumn<?> targetCol;
		private String condition;
		private Object[] argments;
		public Condition(String condition) {
			this.condition = condition;
		}
		public Condition(IConditionColumn<?> targetCol, String condition, Object... argments) {
			// まだ文字列展開しないで保持する
			this.targetCol = targetCol;
			this.condition = condition;
			this.argments = argments;
		}
		@Override
		public String toQuery(boolean appendSchema) {
			if (targetCol != null)
				return targetCol.fullname(appendSchema) + condition;
			else
				return condition;
		}
		public Object[] getArgs() {
			return argments;
		}
	}

	public static class ColumnEqualCondition implements IConditionElement {
		IConditionColumn<?> left;
		IConditionColumn<?> right;
		public ColumnEqualCondition(IConditionColumn<?> left, IConditionColumn<?> right) {
			this.left = left;
			this.right = right;
		}
		@Override
		public String toQuery(boolean appendSchema) {
			return left.fullname(appendSchema) + " = " + right.fullname(appendSchema);
		}
		@Override
		public Object[] getArgs() {
			return new Object[]{left, right};
		}
	}

	public static class IgnoredCondition implements IConditionElement {
		IConditionColumn<?> col;
		public IgnoredCondition(IConditionColumn<?> col) {
			this.col = col;
		}
		@Override
		public String toQuery(boolean appendSchema) {
			return "'" + col.fullname(appendSchema) + " is ignored' is not null";
		}
		@Override
		public Object[] getArgs() {
			return null;
		}
	}

	public static class NullCondition implements IConditionElement {
		@Override
		public String toQuery(boolean appendSchema) {
			return "'nullcondition' is not null";
		}
		@Override
		public Object[] getArgs() {
			return null;
		}
	}


	private static <T> IConditionElement createIgnoredCondition(IConditionColumn<T> col) {
//		return new Condition("'" + col.fullname() + " is ignored' is not null");
		return new IgnoredCondition(col);
	}

	public interface IConditionElement {
		String toQuery(boolean appendSchema);
		Object[] getArgs();
	}

	public static class ConditionArray implements IConditionElement {
		public enum ConditionType {AND, OR};
		ConditionType type;
		List<IConditionElement> childs;
		public ConditionArray(ConditionType type, IConditionElement...conditionElements) {
			this.type = type;
			this.childs = new ArrayList<IConditionElement>();
			for (IConditionElement e : conditionElements) {
				if (e == null)
					throw new RuntimeException("Nullをand()またはor()に渡さない！非成立条件ならIgnoreConditionを渡す事");
				this.childs.add(e);
			}
//			this.childs.addAll(Arrays.asList(conditionElements));
		}
		public void appendCondition(IConditionElement condition) {
			this.childs.add(condition);
		}
		@Override
		public String toQuery(boolean appendSchema) {
//			if (childs.isEmpty())
//				return "()";
			StringBuilder sb = new StringBuilder();
//			if (type == ConditionType.OR)
				sb.append("(");
			for (IConditionElement e : childs) {
				sb.append(e.toQuery(appendSchema));
				if (type == ConditionType.AND)
					sb.append(" and ");
				else
					sb.append(" or ");
			}
			if (type == ConditionType.AND)
				sb.delete(sb.length() - 5, sb.length());
			else
				sb.delete(sb.length() - 4, sb.length());
//			if (type == ConditionType.OR)
				sb.append(")");
			return sb.toString();
		}
		@Override
		public Object[] getArgs() {
			List<Object> args = new ArrayList<Object>();
			for (IConditionElement e : childs) {
				if (e.getArgs() != null)
					for (Object arg : e.getArgs()) {
						args.add(arg);
					}
			}
			return args.toArray();
		}
	}


//	private static class FixLenStr {
//		String str;
//		int len;
//		public FixLenStr(String str, int len) {
//			this.str = str;
//			this.len = len;
//		}
//		public String getString() {
//			return StringUtils.rightPad(str, len, ' ');
//		}
//	}


	public static Condition rawCondition(String condition) {
		return new Condition(condition);
	}

	public static Condition rawCondition(String condition, Object...params) {
		return new Condition(null, condition, params);
	}



	public static ConditionArray and(IConditionElement...conditionElements) {
		return new ConditionArray(ConditionType.AND, conditionElements);
	}

	public static ConditionArray or(IConditionElement...conditionElements) {
		return new ConditionArray(ConditionType.OR, conditionElements);
	}

	public interface IIteratorCallback<T> {
		IConditionElement get(T arg);
	};

	/**
	 * Usage
	 * <code>
	 * String[] values;
	 *
	 * qb.where(
	 * 		and(
	 * 			equal(TBL.COL1, val1)
	 * 			,iterate(ConditionType.OR, values, new IIteratorCallback<String>(){
	 * 				@Override
	 * 				public IConditionElement get(String arg) {
	 * 					return equal(TBL.COL1, arg);
	 * 				}})
	 * 			))
	 *
	 * </code>
	 * @param type
	 * @param args
	 * @param callback
	 * @return
	 */
	public static <T> IConditionElement iterate(ConditionType type, T[] args, IIteratorCallback<T> callback) {
		if (args == null)
			return callback.get(null);
		IConditionElement[] elems = new IConditionElement[args.length];
		int i = 0;
		for (T arg : args) {
			elems[i++] = callback.get(arg);
		}
		return new ConditionArray(type, elems);
	}




	public interface ICaseCallback {
		IConditionElement get();
	};


	public static IConditionElement caseIf(boolean caseif, ICaseCallback truecase, ICaseCallback falsecase) {
		if (caseif)
			return truecase.get();
		else
			return falsecase.get();
	}
	public static IConditionElement caseTrue(boolean caseif, ICaseCallback truecase) {
		if (caseif)
			return truecase.get();
		else
			return new NullCondition();
	}
	public static IConditionElement caseFalse(boolean caseif, ICaseCallback falsecase) {
		if (caseif)
			return new NullCondition();
		else
			return falsecase.get();
	}




	public static <T> IConditionElement equal(IConditionColumn<T> left, IConditionColumn<T> right) {
		/*
		 * このメソッドはJoin時の結合条件用であり、rightにNullが渡されるのは想定してない。
		 * この場合、このメソッドではなく
		 * equal(IConditionColumn<T>, T)
		 * が呼びたかったものとみなしてそちらに処理を委譲します。
		 */
		if (right == null) {
			T t = null;
			return equal(left, t);
		}
//		return new Condition(left.fullname() + " = " + right.fullname());
		return new ColumnEqualCondition(left, right);
	}

	/**
	 * valにNullを渡す場合は、一度T型で変数を宣言し、その変数を渡す事。
	 * そうしないと、コンパイラは判断できずに
	 * equal(IConditionColumn<T>, IConditionColumn<T>)
	 * が呼ばれてしまう
	 * ※誤ってそちらが呼ばれても、右辺がNullならこの関数をコールし直すようにはなっているが、
	 * 混乱のネタは取り除く事。
	 *
	 * <pre>
	 * Integer nullInt = null;
	 * queryBuilder.where(equal(TBL.COLUMN, nullInt));
	 * </pre>
	 */
	public static <T> IConditionElement equal(IConditionColumn<T> col, T val) {
		if (val == null)
			return createIgnoredCondition(col);
		else
			return new Condition(col, " = ?", toValue(col, val));
	}

	public static <T> IConditionElement equalWithNullCase(IConditionColumn<T> col, T val) {
		if (val == null)
			return new Condition(col, " is null");
		else
			return equal(col, val);
	}

	public static <T> IConditionElement isNull(IConditionColumn<T> col) {
		return equalWithNullCase(col, null);
	}

	@Deprecated // use isNull instead.
	public static <T> IConditionElement equalNull(IConditionColumn<T> col) {
		return equalWithNullCase(col, null);
	}

	public static <T> IConditionElement notEqual(IConditionColumn<T> col, T val) {
		if (val == null)
			return createIgnoredCondition(col);
		else
			return new Condition(col, " <> ?", toValue(col, val));
	}

	public static <T> IConditionElement notEqualWithNullCase(IConditionColumn<T> col, T val) {
		if (val == null)
			return new Condition(col, " is not null");
		else
			return notEqual(col, val);
	}

	@Deprecated // use isNotNull instead.
	public static <T> IConditionElement notEqualNull(IConditionColumn<T> col) {
		return notEqualWithNullCase(col, null);
	}

	public static <T> IConditionElement isNotNull(IConditionColumn<T> col) {
		return notEqualWithNullCase(col, null);
	}

	public static <T> IConditionElement like(IConditionColumn<T> col, T val) {
		if (val == null) return createIgnoredCondition(col);
		return new Condition(col, " like ?", "%" + toValue(col, val) + "%");
	}

	public static <T> IConditionElement notLike(IConditionColumn<T> col, T val) {
		if (val == null) return createIgnoredCondition(col);
		return new Condition(col, " not like ?", "%" + toValue(col, val) + "%");
	}

	public static <T> IConditionElement startWith(IConditionColumn<T> col, T val) {
		if (val == null) return createIgnoredCondition(col);
		return new Condition(col, " like ?", toValue(col, val) + "%");
	}

	public static <T> IConditionElement notStartWith(IConditionColumn<T> col, T val) {
		if (val == null) return createIgnoredCondition(col);
		return new Condition(col, " not like ?", toValue(col, val) + "%");
	}

	public static <T> IConditionElement endWith(IConditionColumn<T> col, T val) {
		if (val == null) return createIgnoredCondition(col);
		return new Condition(col, " like ?", "%" + toValue(col, val));
	}

	public static <T> IConditionElement notEndWith(IConditionColumn<T> col, T val) {
		if (val == null) return createIgnoredCondition(col);
		return new Condition(col, " not like ?", "%" + toValue(col, val));
	}

	public static <T> IConditionElement grThan(IConditionColumn<T> col, T val) {
		if (val == null) return createIgnoredCondition(col);
		return new Condition(col, " > ?", toValue(col, val));
	}

	public static <T> IConditionElement grEqual(IConditionColumn<T> col, T val) {
		if (val == null) return createIgnoredCondition(col);
		return new Condition(col, " >= ?", toValue(col, val));
	}

	public static <T> IConditionElement lsThan(IConditionColumn<T> col, T val) {
		if (val == null) return createIgnoredCondition(col);
		return new Condition(col, " < ?", toValue(col, val));
	}

	public static <T> IConditionElement lsEqual(IConditionColumn<T> col, T val) {
		if (val == null) return createIgnoredCondition(col);
		return new Condition(col, " <= ?", toValue(col, val));
	}



	/**
	 * 境界値も含まれるので注意
	 */
	public static <T> IConditionElement between(IConditionColumn<T> col, T from, T to) {
		if (from == null && to != null) throw new RuntimeException("Between 'from'-param is null.");
		if (from != null && to == null) throw new RuntimeException("Between 'to'-param is null.");
		if (from == null && to == null) return createIgnoredCondition(col);
		return new Condition(col, " between ? and ?", toValue(col, from), toValue(col, to));
	}

	/**
	 * Betweenの拡張メソッド
	 * FromがNullの場合はLesserEqaulと同義
	 * ToがNullの場合はGreaterEqualと同義
	 * どの場合も境界値を含む
	 */
	public static <T> IConditionElement range(IConditionColumn<T> col, T from, T to) {
		if (from == null && to == null)
			return createIgnoredCondition(col);
		if (from != null && to == null)
			return grEqual(col, from);
		if (from == null && to != null)
			return lsEqual(col, to);
		return between(col, from, to);
//		return new Condition(col.fullname() + " >= ? and " + col.fullname() + " <= ?", toValue(col, from), toValue(col, to));
	}


	public static <T> IConditionElement in(IConditionColumn<T> col, List<T> vals) {
		if (vals == null || vals.isEmpty()) return createIgnoredCondition(col);
		return new Condition(col, createInStmt(vals, false), toValueArgs(col, vals));
	}

	public static <T> IConditionElement in(IConditionColumn<T> col, T...vals) {
		if (vals == null) return createIgnoredCondition(col);
		return in(col, Arrays.asList(vals));
	}

	public static <T> IConditionElement notIn(IConditionColumn<T> col, List<T> vals) {
		if (vals == null || vals.isEmpty()) return createIgnoredCondition(col);
		return new Condition(col, createInStmt(vals, true), toValueArgs(col, vals));
	}

	public static <T> IConditionElement notIn(IConditionColumn<T> col, T...vals) {
		if (vals == null) return createIgnoredCondition(col);
		return notIn(col, Arrays.asList(vals));
	}


	private static <T> String createInStmt(/*IConditionColumn<T> col, */List<T> vals, boolean not) {
		StringBuilder sb = new StringBuilder();
//		sb.append(col.fullname());
		sb.append(not ? " not" : "");
		sb.append(" in (");
		for (int size = 0; size < vals.size(); size++) {
			sb.append("?,");
		}
		sb.deleteCharAt(sb.length() - 1).append(")");
		return sb.toString();
	}



	private static <T> Object[] toValueArgs(IConditionColumn<T> col, List<T> vals) {
		Object[] ret = new Object[vals.size()];
		for (int i = 0; i < vals.size(); i++) {
			ret[i] = toValue(col, vals.get(i));
		}
		return ret;
	}


	private static <T> IColumn<T> unwrap(IConditionColumn<T> col) {
		if (col instanceof ColElement)
			return ((ColElement<T>)col).col;
		else
			return ((IColumn<T>)col);
	}

//	private static <T> Object toValue(IConditionColumn<T> col, T value) {
//		return toValue(col, value, false);
//	}

	@SuppressWarnings({ "rawtypes" })
	private static <T> Object toValue(IConditionColumn<T> col, T value/*, boolean noFixLenPadding*/) {
		IColumn<T> column = unwrap(col);
//		if (noFixLenPadding == false && column.isFixedLenStr())
//			return new FixLenStr((String)value, column.getSize());
		if (column.isFixedLenStr())
			return StringUtils.rightPad((String)value, column.getSize(), ' ');
		else if (value instanceof IValueEnum)
			return ((IValueEnum)value).getValue();
		else
			return value;
	}
}
