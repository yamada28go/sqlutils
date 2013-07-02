package jp.gr.java_conf.sqlutils.core.handler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import jp.gr.java_conf.sqlutils.core.dto.IDto;
import jp.gr.java_conf.sqlutils.core.dto.IDto.IVoidDto;

/**
 * DtoListHandlerとDtoFetchHandlerを繋ぐラッパー。
 * ユーザが直接このクラスにアクセスする事は想定されていない。
 */
public class DtoListFetchHandler<T extends IDto> extends DtoListHandler<IVoidDto> {

	private DtoFetchHandler<T> handler;

	public DtoListFetchHandler(ResultSetParser parser, DtoFetchHandler<T> handler) {
		super(parser, handler.getClasses());
		this.handler = handler;
	}

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
