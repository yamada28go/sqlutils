package jp.gr.java_conf.sqlutils.core.handler;

import jp.gr.java_conf.sqlutils.core.dto.IDto;

/*
 * DtoListHandlerは、全ての対象データをメモリ上に確保するので、
 * 大量データの処理を行う場合は このハンドラを使い、exec() メソッド内で一件ずつ順次処理を行う。
 * 使用例としては主に
 * ・DBから大量データを取得してCSVファイルに出力：一件ずつFetchして、ファイルに書き込んでゆく
 *
 */
public abstract class DtoFetchHandler<T extends IDto> {
	abstract protected void start();
	abstract protected void exec(T dto);
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


	public Class<? extends IDto>[] getClasses() {
		return classes;
	}
}
