package jp.gr.java_conf.sqlutils.core.handler;

import jp.gr.java_conf.sqlutils.core.dto.IDto;

/**
 *
 * QueryBuilderを使用した検索処理に使用するハンドラ.<br/>
 * 検索結果一件毎にコールバックハンドラとして呼び出される。
 *
 * <pre>
 * manager
 *     .setFetchSize(10)
 *     .fetchDto(
 *         builder
 *             .select()
 *             .from(TBL1),
 *         new DtoFetchHandler{@code<Tbl1>}(Tbl1.class) {
 *             {@code @Override}
 *             protected void start() {
 *             }
 *             {@code @Override}
 *             protected void exec(Tbl1 dto) {
 *             }
 *             {@code @Override}
 *             protected void finish() {
 *             }
 *         });
 * </pre>
 */
public abstract class DtoFetchHandler<T extends IDto> {

	/**
	 * コールバックの最初に一度だけ呼ばれる処理.<br/>
	 * 結果が0件でも呼ばれる。
	 */
	abstract protected void start();

	/**
	 * 結果一件毎に呼ばれる処理.<br/>
	 */
	abstract protected void exec(T dto);

	/**
	 * コールバックの最後に一度だけ呼ばれる処理.<br/>
	 * 結果が0件でも呼ばれる。
	 */
	abstract protected void finish();


	private Class<? extends IDto>[] classes;

	public DtoFetchHandler(Class<? extends IDto> t) {
		this(new Class<?>[]{t});
	}

	public DtoFetchHandler(Class<? extends IDto> t1, Class<? extends IDto> t2) {
		this(new Class<?>[]{t1,t2});
	}

	public DtoFetchHandler(Class<? extends IDto> t1, Class<? extends IDto> t2, Class<? extends IDto> t3) {
		this(new Class<?>[]{t1,t2,t3});
	}

	public DtoFetchHandler(Class<? extends IDto> t1, Class<? extends IDto> t2, Class<? extends IDto> t3, Class<? extends IDto> t4) {
		this(new Class<?>[]{t1,t2,t3,t4});
	}

	public DtoFetchHandler(Class<? extends IDto> t1, Class<? extends IDto> t2, Class<? extends IDto> t3, Class<? extends IDto> t4, Class<? extends IDto> t5) {
		this(new Class<?>[]{t1,t2,t3,t4,t5});
	}

	@SuppressWarnings("unchecked")
	private DtoFetchHandler(Class<?>[] ts) {
		classes = (Class<? extends IDto>[]) ts;
	}


	Class<? extends IDto>[] getClasses() {
		return classes;
	}
}
