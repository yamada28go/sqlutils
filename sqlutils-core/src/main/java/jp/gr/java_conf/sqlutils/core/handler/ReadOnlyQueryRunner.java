package jp.gr.java_conf.sqlutils.core.handler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import jp.gr.java_conf.sqlutils.Const.DBMS;

import org.apache.commons.dbutils.QueryRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Deprecated
public class ReadOnlyQueryRunner extends QueryRunner {

	private static final Logger logger = LoggerFactory.getLogger(ReadOnlyQueryRunner.class);

	private DBMS dbms;
	public int scrollType = ResultSet.TYPE_FORWARD_ONLY;
	public int cursorType = ResultSet.CONCUR_READ_ONLY;
//	public int maxRows = 0;
	public int fetchSize = 0; // Postgresの場合、0＝全件なので注意！
//	public int queryTimeout = 0;


	public ReadOnlyQueryRunner(DBMS dbms) {
        super();
		this.dbms = dbms;
	}

	public ReadOnlyQueryRunner(DBMS dbms, int fetchSize, boolean pmdKnownBroken) {
        super(pmdKnownBroken);
		this.dbms = dbms;
		this.fetchSize = fetchSize;
	}

//	public ReadOnlyQueryRunner(DBMS dbms, int fetchSize, int maxRows, int queryTimeout, boolean pmdKnownBroken) {
//        super(pmdKnownBroken);
//		this.dbms = dbms;
//		this.fetchSize = fetchSize;
//		this.maxRows = maxRows;
//		this.queryTimeout = queryTimeout;
//	}

	protected PreparedStatement prepareStatement(Connection conn, String sql)
	throws SQLException {
		if (dbms == DBMS.POSTGRES) {
			if (fetchSize == 0)
				logger.warn("フェッチサイズが’0’です。Postgresでは、0＝全件取得となり、対象データが大量にある場合、パフォーマンスに問題を与えます。'DBManager.setFetchSize(n)'で任意のフェッチサイズを明示する事をお勧めします。");
			conn.setAutoCommit(false); // PostgresでsetFetchSizeを有効にするための設定
		}
		PreparedStatement stmt = conn.prepareStatement(sql, scrollType, cursorType);
//		stmt.setMaxRows(maxRows);
		stmt.setFetchSize(fetchSize);
//		stmt.setQueryTimeout(queryTimeout);
		return stmt;
	}
}
