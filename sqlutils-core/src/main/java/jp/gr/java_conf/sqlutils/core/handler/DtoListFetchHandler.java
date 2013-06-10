package jp.gr.java_conf.sqlutils.core.handler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import jp.gr.java_conf.sqlutils.core.dto.IDto;
import jp.gr.java_conf.sqlutils.core.dto.IDto.IVoidDto;

/*
 * DtoListHandlerは、全ての対象データをメモリ上に確保するので、
 * 大量データの処理を行う場合は このハンドラを使い、exec() メソッド内で一件ずつ順次処理を行う。
 * 使用例としては主に
 * ・DBから大量データを取得してCSVファイルに出力：一件ずつFetchして、ファイルに書き込んでゆく
 *
 */
public class DtoListFetchHandler<T extends IDto> extends DtoListHandler<IVoidDto> {

	DtoFetchHandler<T> handler;

//	public DtoFetchHandler(Class<? extends IDto>...classes) {
//		super(true, true, classes);
//	}

//	public DtoFetchHandler(boolean isFixedLenStrTrimming, boolean throwExcpOnMissingDto, Class<? extends IDto>...classes) {
//		super(isFixedLenStrTrimming, throwExcpOnMissingDto, classes);
//	}

//	@SuppressWarnings("unchecked")
	public DtoListFetchHandler(ResultSetParser parser, DtoFetchHandler<T> handler) {
		super(parser, handler.getClasses());
		this.handler = handler;
	}

////	@SuppressWarnings("unchecked")
//	public DtoFetchParentHandler(Class<? extends IDto> t1, Class<? extends IDto> t2) {
//		super(t1, t2);
//	}
//
////	@SuppressWarnings("unchecked")
//	public DtoFetchParentHandler(Class<? extends IDto> t1, Class<? extends IDto> t2, Class<? extends IDto> t3) {
//		super(t1, t2, t3);
//	}
//
////	@SuppressWarnings("unchecked")
//	public DtoFetchParentHandler(Class<? extends IDto> t1, Class<? extends IDto> t2, Class<? extends IDto> t3, Class<? extends IDto> t4) {
//		super(t1, t2, t3, t4);
//	}
//
////	@SuppressWarnings("unchecked")
//	public DtoFetchParentHandler(Class<? extends IDto> t1, Class<? extends IDto> t2, Class<? extends IDto> t3, Class<? extends IDto> t4, Class<? extends IDto> t5) {
//		super(t1, t2, t3, t4, t5);
//	}



	public final List<IVoidDto> handle(ResultSet rs) throws SQLException {
		handler.start();
        while (rs.next()) {
            @SuppressWarnings("unchecked")
			T dto = (T) super.handleRow(rs);
            handler.exec(dto);
        }
        handler.finish();
        return null;
	}
}
