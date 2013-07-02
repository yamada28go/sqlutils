package jp.gr.java_conf.sqlutils.core.builder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jp.gr.java_conf.sqlutils.common.ValueEnum.IValueEnum;
import jp.gr.java_conf.sqlutils.core.builder.BuilderElement.ColElement;
import jp.gr.java_conf.sqlutils.core.builder.ConditionBuilder.ConditionArray.ConditionType;
import jp.gr.java_conf.sqlutils.core.dto.IColumn;

import org.apache.commons.lang.StringUtils;

/**
 * スタティックメソッドを使って、Where条件句を構築する.<br/>
 * <p>
 * スタティックメソッドの使用の際は、スタティックインポートを使用する事で、コードの短略化が図れる。
 * <pre>
 * import static jp.gr.java_conf.sqlutils.core.builder.ConditionBuilder.*;
 * </pre>
 */
public class ConditionBuilder {


	/**
	 * {@link ConditionBuilder#equal(IConditionColumn)}等の条件句構成構文に指定できるカラム定義型である事を示す。
	 */
	public interface IConditionColumn<T> {
		String fullname(boolean appendSchema);
	}

	static class Condition implements IConditionElement {
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

	static class ColumnEqualCondition implements IConditionElement {
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

	static class IgnoredCondition implements IConditionElement {
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

	static class NullCondition implements IConditionElement {
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

	/**
	 * 条件句構成要素である事を示す。
	 */
	public interface IConditionElement {
		String toQuery(boolean appendSchema);
		Object[] getArgs();
	}

	static class ConditionArray implements IConditionElement {
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


	/**
	 * 任意の文字列で条件句を構築する.<br/>
	 */
	public static Condition rawCondition(String condition) {
		return new Condition(condition);
	}

	/**
	 * 任意の文字列で条件句を構築する.<br/>
	 */
	public static Condition rawCondition(String condition, Object...params) {
		return new Condition(null, condition, params);
	}


	/**
	 * 条件句をANDで入れ子にする.<br/>
	 */
	public static ConditionArray and(IConditionElement...conditionElements) {
		return new ConditionArray(ConditionType.AND, conditionElements);
	}

	/**
	 * 条件句をORで入れ子にする.<br/>
	 */
	public static ConditionArray or(IConditionElement...conditionElements) {
		return new ConditionArray(ConditionType.OR, conditionElements);
	}

	/**
	 * コールバック
	 * @see {@link ConditionBuilder#iterate(ConditionType, Object[], IIteratorCallback)}
	 */
	public interface IIteratorCallback<T> {
		IConditionElement get(T arg);
	};

	/**
	 * 不定長のコレクションから動的に条件句を構築する.<br/>
	 *
	 * <pre>
	 * {@code
	 * builder.where(
	 *     iterate(ConditionType.OR, values, new IIteratorCallback{@code<String>}(){
	 *         {@code @Override}
	 *         public IConditionElement get(String arg) {
	 *             return equal(TBL.COL1, arg);
	 *         }})
	 *     )
	 * }
	 * </pre>
	 * 上記例は、valuesの個数が固定ならば、以下の記述と同義になる。
	 * <pre>
	 * {@code
	 * builder.where(
	 *     or(
	 *         equal(TBL.COL1, values[0]),
	 *         equal(TBL.COL1, values[1]),
	 *         equal(TBL.COL1, values[2]),
	 *         equal(TBL.COL1, values[3])
	 *     )
	 * }
	 * </pre>
	 *
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



	/**
	 * コールバック
	 * @see {@link ConditionBuilder#caseIf(boolean, ICaseCallback, ICaseCallback)}
	 * @see {@link ConditionBuilder#caseTrue(boolean, ICaseCallback)}
	 * @see {@link ConditionBuilder#caseFalse(boolean, ICaseCallback)}
	 */
	public interface ICaseCallback {
		IConditionElement get();
	};


	/**
	 * 条件句の構築を動的に制御する.<br/>
	 * 条件に応じて、2つのコールバックのうちのいずれかが呼ばれる。
	 *
	 * <pre>
	 * {@code
	 * builder.where(
	 *     caseIf(flag,
	 *         new ICaseCallback(){
	 *             {@code @Override}
	 *             public IConditionElement get() {
	 *                 // when flag=true
	 *                 return equal(TBL.COL1, val1);
	 *             }},
	 *         new ICaseCallback(){
	 *             {@code @Override}
	 *             public IConditionElement get() {
	 *                 // when flag=false
	 *                 return equal(TBL.COL2, val2);
	 *             }})
	 *     )
	 * }
	 * </pre>
	 *
	 */
	public static IConditionElement caseIf(boolean caseif, ICaseCallback truecase, ICaseCallback falsecase) {
		if (caseif)
			return truecase.get();
		else
			return falsecase.get();
	}

	/**
	 * 条件句の構築を動的に制御する.<br/>
	 * 条件=trueの場合に、コールバックが呼ばれる。
	 * @see {@link #caseIf(boolean, ICaseCallback, ICaseCallback)}
	 */
	public static IConditionElement caseTrue(boolean caseif, ICaseCallback truecase) {
		if (caseif)
			return truecase.get();
		else
			return new NullCondition();
	}

	/**
	 * 条件句の構築を動的に制御する.<br/>
	 * 条件=falseの場合に、コールバックが呼ばれる。
	 * @see {@link #caseIf(boolean, ICaseCallback, ICaseCallback)}
	 */
	public static IConditionElement caseFalse(boolean caseif, ICaseCallback falsecase) {
		if (caseif)
			return new NullCondition();
		else
			return falsecase.get();
	}



	/**
	 * テーブルJOIN用のイコール条件を構築する.<br/>
	 */
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
	 * イコール条件を構築する.<br/>
	 * 第2引数の値がNullの場合、条件句そのものが構築されない。<br/>
	 * これは、条件指定が省略された場合には、絞り込み条件から除外される事が一般的であるため。<br/>
	 * 値がNullの場合にも"is null"条件が採用されたい場合には{@link #equalWithNullCase(IConditionColumn, Object) }を使用。
	 */
	public static <T> IConditionElement equal(IConditionColumn<T> col, T val) {
		if (val == null)
			return createIgnoredCondition(col);
		else
			return new Condition(col, " = ?", toValue(col, val));
	}

	/**
	 * イコール条件を構築する.<br/>
	 * 第2引数の値がNullの場合、条件句は"is null"で構築される。
	 * <p>
	 * 常に"is null"で条件構築したい場合は、このメソッドではなく{@link #isNull(IConditionColumn)}を使用する事。<br/>
	 * 第2引数にNullをそのまま渡すと、コンパイラがT型の判断ができずに
	 * {@link #equal(IConditionColumn, IConditionColumn)}が呼ばれてしまうため。<br/>
	 * ※誤ってそちらが呼ばれても、右辺がNullならこの関数をコールし直すようにはなっているが、
	 * 混乱のネタは取り除く事。
	 */
	public static <T> IConditionElement equalWithNullCase(IConditionColumn<T> col, T val) {
		if (val == null)
			return new Condition(col, " is null");
		else
			return equal(col, val);
	}

	/**
	 * "is null"条件を構築する.<br/>
	 */
	public static <T> IConditionElement isNull(IConditionColumn<T> col) {
		return equalWithNullCase(col, null);
	}

	/**
	 * "is null"条件を構築する.<br/>
	 * @Deprecated use {@link #isNull(IConditionColumn)} instead.
	 */
	@Deprecated // use isNull instead.
	public static <T> IConditionElement equalNull(IConditionColumn<T> col) {
		return equalWithNullCase(col, null);
	}

	/**
	 * ノットイコール条件を構築する.<br/>
	 * 第2引数の値がNullの場合、条件句そのものが構築されない。
	 */
	public static <T> IConditionElement notEqual(IConditionColumn<T> col, T val) {
		if (val == null)
			return createIgnoredCondition(col);
		else
			return new Condition(col, " <> ?", toValue(col, val));
	}

	/**
	 * ノットイコール条件を構築する.<br/>
	 * 第2引数の値がNullの場合、"is not null"条件が構築される。
	 * <p>
	 * 常に"is not null"で条件構築したい場合は、このメソッドではなく{@link #isNotNull(IConditionColumn)}を使用する事。<br/>
	 */
	public static <T> IConditionElement notEqualWithNullCase(IConditionColumn<T> col, T val) {
		if (val == null)
			return new Condition(col, " is not null");
		else
			return notEqual(col, val);
	}

	/**
	 * "is not null"条件を構築する.<br/>
	 * @Deprecated use {@link #isNotNull(IConditionColumn)} instead.
	 */
	@Deprecated // use isNotNull instead.
	public static <T> IConditionElement notEqualNull(IConditionColumn<T> col) {
		return notEqualWithNullCase(col, null);
	}

	/**
	 * "is not null"条件を構築する.<br/>
	 */
	public static <T> IConditionElement isNotNull(IConditionColumn<T> col) {
		return notEqualWithNullCase(col, null);
	}

	/**
	 * like(中間一致)条件を構築する.<br/>
	 * 第2引数の値がNullの場合、条件句そのものが構築されない。
	 */
	public static <T> IConditionElement like(IConditionColumn<T> col, T val) {
		if (val == null) return createIgnoredCondition(col);
		return new Condition(col, " like ?", "%" + toValue(col, val) + "%");
	}

	/**
	 * not like(中間一致)条件を構築する.<br/>
	 * 第2引数の値がNullの場合、条件句そのものが構築されない。
	 */
	public static <T> IConditionElement notLike(IConditionColumn<T> col, T val) {
		if (val == null) return createIgnoredCondition(col);
		return new Condition(col, " not like ?", "%" + toValue(col, val) + "%");
	}

	/**
	 * like(前方一致)条件を構築する.<br/>
	 * 第2引数の値がNullの場合、条件句そのものが構築されない。
	 */
	public static <T> IConditionElement startWith(IConditionColumn<T> col, T val) {
		if (val == null) return createIgnoredCondition(col);
		return new Condition(col, " like ?", toValue(col, val) + "%");
	}

	/**
	 * not like(前方一致)条件を構築する.<br/>
	 * 第2引数の値がNullの場合、条件句そのものが構築されない。
	 */
	public static <T> IConditionElement notStartWith(IConditionColumn<T> col, T val) {
		if (val == null) return createIgnoredCondition(col);
		return new Condition(col, " not like ?", toValue(col, val) + "%");
	}

	/**
	 * like(後方一致)条件を構築する.<br/>
	 * 第2引数の値がNullの場合、条件句そのものが構築されない。
	 */
	public static <T> IConditionElement endWith(IConditionColumn<T> col, T val) {
		if (val == null) return createIgnoredCondition(col);
		return new Condition(col, " like ?", "%" + toValue(col, val));
	}

	/**
	 * not like(後方一致)条件を構築する.<br/>
	 * 第2引数の値がNullの場合、条件句そのものが構築されない。
	 */
	public static <T> IConditionElement notEndWith(IConditionColumn<T> col, T val) {
		if (val == null) return createIgnoredCondition(col);
		return new Condition(col, " not like ?", "%" + toValue(col, val));
	}

	/**
	 * 大小比較(>)条件を構築する.<br/>
	 * 第2引数の値がNullの場合、条件句そのものが構築されない。
	 */
	public static <T> IConditionElement grThan(IConditionColumn<T> col, T val) {
		if (val == null) return createIgnoredCondition(col);
		return new Condition(col, " > ?", toValue(col, val));
	}

	/**
	 * 大小比較(>=)条件を構築する.<br/>
	 * 第2引数の値がNullの場合、条件句そのものが構築されない。
	 */
	public static <T> IConditionElement grEqual(IConditionColumn<T> col, T val) {
		if (val == null) return createIgnoredCondition(col);
		return new Condition(col, " >= ?", toValue(col, val));
	}

	/**
	 * 大小比較(<)条件を構築する.<br/>
	 * 第2引数の値がNullの場合、条件句そのものが構築されない。
	 */
	public static <T> IConditionElement lsThan(IConditionColumn<T> col, T val) {
		if (val == null) return createIgnoredCondition(col);
		return new Condition(col, " < ?", toValue(col, val));
	}

	/**
	 * 大小比較(<=)条件を構築する.<br/>
	 * 第2引数の値がNullの場合、条件句そのものが構築されない。
	 */
	public static <T> IConditionElement lsEqual(IConditionColumn<T> col, T val) {
		if (val == null) return createIgnoredCondition(col);
		return new Condition(col, " <= ?", toValue(col, val));
	}


	/**
	 * 範囲比較条件を構築する.<br/>
	 * from、toの双方が値を持たない場合Exception。<br/>
	 * 双方とも値がNullの場合、条件句そのものが構築されない。<br/>
	 * 境界値も含まれる。
	 */
	public static <T> IConditionElement between(IConditionColumn<T> col, T from, T to) {
		if (from == null && to != null) throw new RuntimeException("Between 'from'-param is null.");
		if (from != null && to == null) throw new RuntimeException("Between 'to'-param is null.");
		if (from == null && to == null) return createIgnoredCondition(col);
		return new Condition(col, " between ? and ?", toValue(col, from), toValue(col, to));
	}

	/**
	 * 範囲比較条件を構築する.<br/>
	 * from、toの双方が値を持つ場合は{@link #between(IConditionColumn, Object, Object)}と同様に振る舞う。<br/>
	 * fromのみの場合は{@link #grEqual(IConditionColumn, Object)}と、<br/>
	 * toのみの場合は {@link #lsThan(IConditionColumn, Object)}と同義。<br/>
	 * 双方とも値がNullの場合、条件句そのものが構築されない。<br/>
	 * どの場合も境界値を含む。
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


	/**
	 * IN条件を構築する.<br/>
	 * 第2引数の値がNullまたは空の場合、条件句そのものが構築されない。
	 */
	public static <T> IConditionElement in(IConditionColumn<T> col, List<T> vals) {
		if (vals == null || vals.isEmpty()) return createIgnoredCondition(col);
		return new Condition(col, createInStmt(vals, false), toValueArgs(col, vals));
	}

	/**
	 * IN条件を構築する.<br/>
	 * 第2引数の値がNullまたは空の場合、条件句そのものが構築されない。
	 */
	public static <T> IConditionElement in(IConditionColumn<T> col, T...vals) {
		if (vals == null) return createIgnoredCondition(col);
		return in(col, Arrays.asList(vals));
	}

	/**
	 * NOT IN条件を構築する.<br/>
	 * 第2引数の値がNullまたは空の場合、条件句そのものが構築されない。
	 */
	public static <T> IConditionElement notIn(IConditionColumn<T> col, List<T> vals) {
		if (vals == null || vals.isEmpty()) return createIgnoredCondition(col);
		return new Condition(col, createInStmt(vals, true), toValueArgs(col, vals));
	}

	/**
	 * NOT IN条件を構築する.<br/>
	 * 第2引数の値がNullまたは空の場合、条件句そのものが構築されない。
	 */
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
