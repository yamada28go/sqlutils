package jp.gr.java_conf.sqlutils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import jp.gr.java_conf.sqlutils.core.builder.QueryBuilder;
import jp.gr.java_conf.sqlutils.core.builder.UpdateQueryBuilder;
import jp.gr.java_conf.sqlutils.core.connection.IConnectionProvider;
import jp.gr.java_conf.sqlutils.core.dto.DtoSet.EightJoinned;
import jp.gr.java_conf.sqlutils.core.dto.DtoSet.FiveJoinned;
import jp.gr.java_conf.sqlutils.core.dto.DtoSet.FourJoinned;
import jp.gr.java_conf.sqlutils.core.dto.DtoSet.JoinnedDto;
import jp.gr.java_conf.sqlutils.core.dto.DtoSet.SevenJoinned;
import jp.gr.java_conf.sqlutils.core.dto.DtoSet.SixJoinned;
import jp.gr.java_conf.sqlutils.core.dto.DtoSet.ThreeJoinned;
import jp.gr.java_conf.sqlutils.core.dto.IDto;
import jp.gr.java_conf.sqlutils.core.dto.IDto.ILogicalDeleting;
import jp.gr.java_conf.sqlutils.core.dto.IDto.IOptimisticLocking;
import jp.gr.java_conf.sqlutils.core.dto.IDto.IPersistable;
import jp.gr.java_conf.sqlutils.core.exception.MoreThanOneResultsException;
import jp.gr.java_conf.sqlutils.core.exception.OptimisticLockingException;
import jp.gr.java_conf.sqlutils.core.exception.RuntimeSQLException;
import jp.gr.java_conf.sqlutils.core.handler.DtoFetchHandler;
import jp.gr.java_conf.sqlutils.core.handler.DtoListFetchHandler;
import jp.gr.java_conf.sqlutils.core.handler.DtoListHandler;
import jp.gr.java_conf.sqlutils.core.persistorhandler.DeleteHandler;
import jp.gr.java_conf.sqlutils.core.persistorhandler.InsertHandler;
import jp.gr.java_conf.sqlutils.core.persistorhandler.LogicalDeleteHandler;
import jp.gr.java_conf.sqlutils.core.persistorhandler.SelectHandler;
import jp.gr.java_conf.sqlutils.core.persistorhandler.UpdateHandler;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * コネクションの生成
 *
 * ■ 対象コネクションが一通りしかないなら、staticメソッドでの初期化＆インスタンス取得が簡単です。
 * ------------------------
 * // 初期化
 * DBManager.init(DBMS.Oracle, provider);
 * // Managerを取得
 * DBManager manager = DBManager.get();
 *
 *
 * ■ ConnectionProviderを使い分けたい場合は、それぞれコンストラクタの引数で指定します。
 * Factoryクラスを用意する事を推奨します。
 * ------------------------
 * DBManager oracleManager = new DBManager(DBMS.Oracle, oracleProvider);
 * DBManager mysqlManager = new DBManager(DBMS.MySQL, mysqlProvider);
 *
 * ※また、Insert/Update時の自動更新日設定といったカスタマイズをするために、
 * 独自拡張したDBManagerを使用する場合も、Factoryクラスを介してインスタンスを生成する手法となります。
 *
 */
public class DBManager {

	private static final Logger logger = LoggerFactory.getLogger(DBManager.class);

	public interface IDBManagerFactory {
		public DBManager create();
	}

	public static class DefaultDBManagerFactory implements IDBManagerFactory {
		private String defaultDbms;
		private IConnectionProvider defaultProvider;
		public DefaultDBManagerFactory(String defaultDbms, IConnectionProvider defaultProvider) {
			this.defaultDbms = defaultDbms;
			this.defaultProvider = defaultProvider;
		}
		@Override
		public DBManager create() {
			return new DBManager(
					defaultDbms,
					defaultProvider,
					PostProcess.COMMIT_AND_CLOSE,
					PostProcessOnException.CLOSE);
		}
	}

	public enum PostProcess { COMMIT_AND_CLOSE, COMMIT_ONLY, NONE }
	public enum PostProcessOnException { CLOSE, NONE }



	static IDBManagerFactory factory;


	public static void init(final String defaultDbms, final IConnectionProvider defaultProvider) {
		setFactory(new DefaultDBManagerFactory(defaultDbms, defaultProvider));
	};

	public static void setFactory(IDBManagerFactory factory) {
		DBManager.factory = factory;
	}

	public static DBManager get() {
		return factory.create();
	}





	/***********************************************************************************/

	/**
	 * SimpleConnectionProvider、またはThreadLocalConnectionProvider
	 */
	protected IConnectionProvider provider;

	/**
	 * SimpleConnectionProviderを使用している場合は、
	 * DBManagerインスタンス一つに対して、コネクションが一つ割り当てられます。
	 *
	 * ThreadLocalConnectionProviderを使用している場合は、コネクションはDBManagerインスタンスに関係無く、
	 * リクエストスレッド毎に一つが割り当てられます。
	 * この場合、このコネクションは、DBManager#commit()やDBManager#close()を呼んでも反映されません。
	 * 代わりにスレッドの終端で、ThreadLocalConnectionProviderのメソッドを呼び出す必要があります。
	 *
	 * ThreadLocalConnectionProvider#setTLConnectionRollback
	 * ThreadLocalConnectionProvider#closeTLConnection
	 *
	 */
	protected Connection conn;


	public String dbms;
	protected int fetchSize;

	/**
	 * PersistorHandlerのFactory。
	 * Handlerをカスタマイズする場合は、カスタマイズドHandlerを生成するFactoryを設定する事で可能。
	 */


	/** 検索・更新処理後の処置 */
	protected PostProcess postProcess;

	/** 検索・更新処理でException発生した際の処置 */
	protected PostProcessOnException postProcessOnException;



	public DBManager(
			String dbms,
			IConnectionProvider provider,
			PostProcess postProcess,
			PostProcessOnException postProcessOnException) {

		if (dbms == null || provider == null)
			throw new RuntimeException("DBManager instantiation failed. Param is null.");

		this.dbms = dbms;
		this.provider = provider;
		this.postProcess = postProcess;
		this.postProcessOnException = postProcessOnException;
		clear();
	}


	protected void clear() {
		fetchSize = 0;
	}

	/**
	 * コネクションをコミットする。
	 * デフォルトのpostProcessであれば、一回結果・更新処理を呼ぶと内部で自動的にコミット＆クローズされるので、
	 * このメソッドを呼ぶ必要は無い。
	 * postProcessを変更した場合は、自身でコミット及びクローズをする必要がある。
	 *
	 * ThreadLocalConnectionProviderを使用している場合は、リクエストスレッドの処理終端で
	 * コミット＆クローズが呼ばれる（←FWレベルでそのように実装されている必要があるが）ので、
	 * 仮にpostProcessを変更した場合でも、個々の処理でコミットやクローズをする必要は無い。
	 *
	 */
	public void commit() {
		if (conn != null) {
			try {
				logger.debug("commit!");
				conn.commit();
			} catch (SQLException e) {
				throw new RuntimeSQLException(e);
			}
		}
	}

	/**
	 * コネクションをロールバックする。
	 * 通常この処理を明示的に呼び出すケースは想定されない。
	 *
	 * ThreadLocalConnectionProviderを使用している場合は、リクエストスレッドの処理終端で
	 * Exception発生時にはロールバック＆クローズされる。
	 *
	 * SimpleConnectionProviderを使用している場合は、
	 * jp.gr.java_conf.sqlutils.core.connection.Txクラスを使用すれば、
	 * Exception発生時にはロールバック＆クローズされる。
	 *
	 * @see jp.gr.java_conf.sqlutils.core.connection.Tx
	 */
	public void rollback() {
		if (conn != null) {
			try {
				logger.debug("rollback!");
				conn.rollback();
			} catch (SQLException e) {
				throw new RuntimeSQLException(e);
			}
		}
	}

	/**
	 * コネクションをクローズする。
	 * 挙動に関しては、コミット処理と同様。
	 */
	public void close() {
		if (conn != null) {
			try {
				logger.debug("close!");
				conn.close();
				conn = null;
			} catch (SQLException e) {
				throw new RuntimeSQLException(e);
			}
		}
	}

	/**
	 * インスタンスに紐付いたコネクションを取得する。
	 *
	 * 通常はこのメソッドを外部から直接利用する事は想定されない。
	 */
	public Connection getConnection() {
		if (conn == null) {
			conn = provider.createConnection();
			// AutoCommitをオフにする
			try {
				if (conn.getAutoCommit()) {
					try {
						conn.setAutoCommit(false);
					} catch (SQLException e) {
						logger.warn("Failed to set Connection's auto-commit function to false." +
								" So, designation by DBManager#setAutoCommit maybe not work.");
					}
				}
			} catch (SQLException e) {
				logger.warn("Failed to check Connection's auto-commit state.");
			}
		}
		return conn;
	}

	/**
	 * DB検索処理を実施する際のフェッチサイズを設定する。
	 *
	 * 大量の検索結果がヒットする事が想定される場合、適切なフェッチサイズを設定する事を推奨。
	 * 例えばPostgresの場合、デフォルト値の"0"＝全件フェッチするので、サーバに多大な負荷がかかる可能性あり。
	 *
	 */
	public DBManager setFetchSize(int fetchSize) {
		this.fetchSize = fetchSize;
		return this;
	}

	/**
	 * 検索・更新処理実施後の処置を設定する。
	 * デフォルトは「コミット＆クローズ」
	 *
	 * 単一のDBManagerインスタンスで複数の検索・更新処理を実行するには、設定を変更する必要あり（←クローズされてしまわないよう）
	 *
	 */
	public DBManager setPostProcess(PostProcess postProcess) {
		this.postProcess = postProcess;
		return this;
	}

	/**
	 * 検索・更新処理実施時にExceptionが発生した場合の処置を設定する。
	 * デフォルトは「クローズ」
	 *
	 * rollback()メソッドと同様に、通常この処理を明示的に呼び出すケースは想定されない。
	 * ThreadLocalConnectionProviderや、
	 * jp.gr.java_conf.sqlutils.core.connection.Txクラスに任せれば良い。
	 *
	 */
	public DBManager setPostProcessOnException(PostProcessOnException postProcessOnException) {
		this.postProcessOnException = postProcessOnException;
		return this;
	}

	public PostProcess getPostProcess() {
		return postProcess;
	}

	public PostProcessOnException getPostProcessOnException() {
		return postProcessOnException;
	}



	// ===============================================================
	// for Override

	protected QueryRunner newQueryRunner() {
		return new QueryRunner() {
			protected PreparedStatement prepareStatement(Connection conn, String sql) throws SQLException {
//				if (dbms == DBMS.POSTGRES) {
				if ("POSTGRES".equals(dbms)) {
					if (fetchSize == 0)
						logger.warn("フェッチサイズが’0’です。Postgresでは、0＝全件取得となり、対象データが大量にある場合、パフォーマンスに問題を与えます。'DBManager.setFetchSize(n)'で任意のフェッチサイズを明示する事をお勧めします。");
					conn.setAutoCommit(false); // PostgresでsetFetchSizeを有効にするための設定
				}
				PreparedStatement stmt = conn.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
				stmt.setFetchSize(fetchSize);
//				stmt.setMaxRows(maxRows);
//				stmt.setQueryTimeout(queryTimeout);
				return stmt;
			}
		};
	}



	protected InsertHandler newInsertHandler(DBManager manager) {
		return new InsertHandler(manager);
	}

	protected UpdateHandler newUpdateHandler(DBManager manager) {
		return new UpdateHandler(manager);
	}

	protected DeleteHandler newDeleteHandler(DBManager manager) {
		return new DeleteHandler(manager);
	}

	protected SelectHandler newSelectHandler(DBManager manager) {
		return new SelectHandler(manager);
	}

	protected LogicalDeleteHandler newLogicalDeleteHandler(DBManager manager) {
		return new LogicalDeleteHandler(manager);
	}



	// ==================================================================
	// Select-query functions

	/**
	 * 検索処理。
	 * 結果をListで返却する。
	 */
	public <T extends IDto> List<T> getList(QueryBuilder qw, Class<T> t) {
		DtoListHandler<T> handler = new DtoListHandler<T>(qw.getResultSetParser(), t);
		return execQuery(qw, handler);
	}

	/**
	 * 検索処理。
	 * 2つのテーブルをJoinした結果をListで返却する。
	 */
	public <T1 extends IDto,T2 extends IDto>
	List<JoinnedDto<T1,T2>> getList(QueryBuilder qw, Class<T1> t1, Class<T2> t2) {
		DtoListHandler<JoinnedDto<T1,T2>> handler = new DtoListHandler<JoinnedDto<T1,T2>>(qw.getResultSetParser(), t1,t2);
		return execQuery(qw, handler);
	}

	/**
	 * 検索処理。
	 * 3つのテーブルをJoinした結果をListで返却する。
	 */
	public <T1 extends IDto,T2 extends IDto,T3 extends IDto>
	List<ThreeJoinned<T1,T2,T3>> getList(QueryBuilder qw, Class<T1> t1, Class<T2> t2, Class<T3> t3) {
		DtoListHandler<ThreeJoinned<T1,T2,T3>> handler = new DtoListHandler<ThreeJoinned<T1,T2,T3>>(qw.getResultSetParser(), t1,t2,t3);
		return execQuery(qw, handler);
	}

	/**
	 * 検索処理。
	 * 4つのテーブルをJoinした結果をListで返却する。
	 */
	public <T1 extends IDto,T2 extends IDto,T3 extends IDto,T4 extends IDto>
	List<FourJoinned<T1,T2,T3,T4>> getList(QueryBuilder qw, Class<T1> t1, Class<T2> t2, Class<T3> t3, Class<T4> t4) {
		DtoListHandler<FourJoinned<T1,T2,T3,T4>> handler = new DtoListHandler<FourJoinned<T1,T2,T3,T4>>(qw.getResultSetParser(), t1,t2,t3,t4);
		return execQuery(qw, handler);
	}

	/**
	 * 検索処理。
	 * 5つのテーブルをJoinした結果をListで返却する。
	 */
	public <T1 extends IDto,T2 extends IDto,T3 extends IDto,T4 extends IDto,T5 extends IDto>
	List<FiveJoinned<T1,T2,T3,T4,T5>> getList(QueryBuilder qw, Class<T1> t1, Class<T2> t2, Class<T3> t3, Class<T4> t4, Class<T5> t5) {
		DtoListHandler<FiveJoinned<T1,T2,T3,T4,T5>> handler = new DtoListHandler<FiveJoinned<T1,T2,T3,T4,T5>>(qw.getResultSetParser(), t1,t2,t3,t4,t5);
		return execQuery(qw, handler);
	}

	/**
	 * 検索処理。
	 * 6つのテーブルをJoinした結果をListで返却する。
	 */
	public <T1 extends IDto,T2 extends IDto,T3 extends IDto,T4 extends IDto,T5 extends IDto,T6 extends IDto>
	List<SixJoinned<T1,T2,T3,T4,T5,T6>> getList(QueryBuilder qw, Class<T1> t1, Class<T2> t2, Class<T3> t3, Class<T4> t4, Class<T5> t5, Class<T6> t6) {
		DtoListHandler<SixJoinned<T1,T2,T3,T4,T5,T6>> handler = new DtoListHandler<SixJoinned<T1,T2,T3,T4,T5,T6>>(qw.getResultSetParser(), t1,t2,t3,t4,t5,t6);
		return execQuery(qw, handler);
	}

	/**
	 * 検索処理。
	 * 7つのテーブルをJoinした結果をListで返却する。
	 */
	public <T1 extends IDto,T2 extends IDto,T3 extends IDto,T4 extends IDto,T5 extends IDto,T6 extends IDto,T7 extends IDto>
	List<SevenJoinned<T1,T2,T3,T4,T5,T6,T7>> getList(QueryBuilder qw, Class<T1> t1, Class<T2> t2, Class<T3> t3, Class<T4> t4, Class<T5> t5, Class<T6> t6, Class<T7> t7) {
		DtoListHandler<SevenJoinned<T1,T2,T3,T4,T5,T6,T7>> handler = new DtoListHandler<SevenJoinned<T1,T2,T3,T4,T5,T6,T7>>(qw.getResultSetParser(), t1,t2,t3,t4,t5,t6,t7);
		return execQuery(qw, handler);
	}

	/**
	 * 検索処理。
	 * 8つのテーブルをJoinした結果をListで返却する。
	 */
	public <T1 extends IDto,T2 extends IDto,T3 extends IDto,T4 extends IDto,T5 extends IDto,T6 extends IDto,T7 extends IDto,T8 extends IDto>
	List<EightJoinned<T1,T2,T3,T4,T5,T6,T7,T8>> getList(QueryBuilder qw, Class<T1> t1, Class<T2> t2, Class<T3> t3, Class<T4> t4, Class<T5> t5, Class<T6> t6, Class<T7> t7, Class<T8> t8) {
		DtoListHandler<EightJoinned<T1,T2,T3,T4,T5,T6,T7,T8>> handler = new DtoListHandler<EightJoinned<T1,T2,T3,T4,T5,T6,T7,T8>>(qw.getResultSetParser(), t1,t2,t3,t4,t5,t6,t7,t8);
		return execQuery(qw, handler);
	}

	public <T extends IDto> List<T> execQuery(QueryBuilder qw, DtoListHandler<T> handler) {
		String sql = qw.buildQuery();
		return execQuery(handler, sql, qw.getQueryPrms());
	}



	/**
	 * 検索処理。
	 * 結果を返却せず、一件毎にハンドラを呼び出す。
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void fetchDto(QueryBuilder qw, DtoFetchHandler<? extends IDto> handler) {
//		handler.setColNameParser(new ColNameProvider(qw.getQueryGetColNames()));
		execQuery(qw, new DtoListFetchHandler(qw.getResultSetParser(), handler));
	}

	private <T extends IDto> T getSingleDto(QueryBuilder qw, DtoListHandler<T> handler) {
//		List<T> ret = (List<T>) execQuery(handler, qw.buildQuery(dbms), qw.getQueryPrms());
		List<T> ret = execQuery(qw, handler);
		if (ret.size() == 0)
			return null;
		else if (ret.size() == 1)
			return ret.get(0);
		else
			throw new MoreThanOneResultsException();
	}

	/**
	 * 検索処理。
	 * 結果が一件しか無い場合に、ListではなくDto単体で返却する。
	 * 結果が一件以上ある場合にはExceptionをスローする。
	 */
	public <T extends IDto> T getSingleDto(QueryBuilder qw, Class<T> t) {
		DtoListHandler<T> handler = new DtoListHandler<T>(qw.getResultSetParser(), t);
		return getSingleDto(qw, handler);
	}

	/**
	 * 検索処理。
	 * 2つのテーブルをJoinした単一結果をDtoで返却する。
	 */
	public <T1 extends IDto,T2 extends IDto>
	JoinnedDto<T1,T2> getSingleDto(QueryBuilder qw, Class<T1> t1, Class<T2> t2) {
		DtoListHandler<JoinnedDto<T1,T2>> handler = new DtoListHandler<JoinnedDto<T1,T2>>(qw.getResultSetParser(), t1,t2);
		return getSingleDto(qw, handler);
	}

	/**
	 * 検索処理。
	 * 3つのテーブルをJoinした単一結果をDtoで返却する。
	 */
	public <T1 extends IDto,T2 extends IDto,T3 extends IDto>
	ThreeJoinned<T1,T2,T3> getSingleDto(QueryBuilder qw, Class<T1> t1, Class<T2> t2, Class<T3> t3) {
		DtoListHandler<ThreeJoinned<T1,T2,T3>> handler = new DtoListHandler<ThreeJoinned<T1,T2,T3>>(qw.getResultSetParser(), t1,t2,t3);
		return getSingleDto(qw, handler);
	}


	/**
	 * シーケンスを採番し、結果を取得する。
	 * 通常はこのメソッドを外部から直接利用する事は想定されない。
	 */
	public Number getSequenceVal(String seqName) {
		String sql = QueryBuilder.get(dbms).getGetSequenceValSql(seqName);
//		switch(dbms) {
//		case POSTGRES:	sql = "select nextval('" + seqName + "')"; break;
//		case ORACLE:	sql = "select " + seqName + ".nextval from dual"; break;
//		case H2:		sql = "select " + seqName + ".nextval from dual"; break;
//		case SYMFOWARE:	sql = "select " + seqName + ".nextval from RDBII_SYSTEM.RDBII_ASSISTTABLE"; break;
//		default:
//			throw new RuntimeException("Not supported. Or please override.");
//		}
		return (Number) execQuery(new ScalarHandler(1), sql);
	}

	/**
	 * オートインクリメントされた値を取得する。
	 * 通常はこのメソッドを外部から直接利用する事は想定されない。
	 */
	public Number getAutoIncrementedVal() {
		String sql = QueryBuilder.get(dbms).getGetAutoIncrementedValSql();;
//		switch(dbms) {
//		case MYSQL:		sql = "select last_insert_id()"; break;
//		case SQLSERVER:	sql = "select scope_identity()"; break; // TODO 未検証
//		default:
//			throw new RuntimeException("Unexpected, or please override.");
//		}
		return (Number) execQuery(new ScalarHandler(1), sql);
	}


	/**
	 * 検索処理。
	 * 検索結果の先頭行の先頭カラムの値をNumber型で取得する。
	 * 当然ながら先頭行の先頭カラムの値が数値でない場合はExceptionになる。
	 *
	 * Number型なのは、使用する関数により、またDBMSのJDBCドライバにより、型が不明なため（Int/Longあるいはその他）。
	 *
	 */
	public Number getNum(QueryBuilder qw) {
		return (Number) execQuery(qw, new ScalarHandler(1));
	}

	/**
	 * 検索処理。
	 * 検索結果の先頭行の先頭カラムの値を取得する。
	 *
	 */
	public <T> T getColValue(QueryBuilder qw, Class<T> clazz) {
		return getColValue(qw, 1, clazz);
	}

	/**
	 * 検索処理。
	 * 検索結果の先頭行の、任意のカラムの値を取得する。
	 *
	 * @param colNum 1 based index.
	 */
	public <T> T getColValue(QueryBuilder qw, int colNum, Class<T> clazz) {
		Object val = execQuery(qw, new ScalarHandler(colNum));
		logger.debug(String.format("getColValue: colNum={%d} class={%s}", colNum, val.getClass().getCanonicalName()));
		return clazz.cast(val);
	}

	/**
	 * 検索処理。
	 * 検索結果の先頭行を、カラム名をキーにしたMapで取得する。
	 *
	 * @see org.apache.commons.dbutils.handlers.MapHandler.MapHandler()
	 */
	public Map<String,Object> getAsMap(QueryBuilder qw) {
		return execQuery(qw, new MapHandler());
	}

	/**
	 * 検索処理。
	 * 検索結果を、カラム名をキーにしたMapのListで取得する。
	 *
	 * @see org.apache.commons.dbutils.handlers.MapListHandler.MapListHandler()
	 */
	public List<Map<String,Object>> getAsMapList(QueryBuilder qw) {
		return execQuery(qw, new MapListHandler());
	}

	/**
	 * 検索処理。
	 * 検索結果を、任意の型で取得する。
	 *
	 * @see org.apache.commons.dbutils.ResultSetHandler<T>
	 */
	public <T> T execQuery(QueryBuilder qw, ResultSetHandler<T> handler) {
		return execQuery(handler, qw.buildQuery(/*dbms*/), qw.getQueryPrms());
	}


	/**
	 * 検索処理。
	 * 検索結果を、任意の型で取得する。
	 *
	 * @see org.apache.commons.dbutils.ResultSetHandler<T>
	 */
	public <T> T execQuery(ResultSetHandler<T> handler, String sql, Object...params) {
		if (params != null && params.length != 0 && params[0] instanceof Collection<?>)
			throw new RuntimeException("Params are collection! Please extract to Object[]");
//		return execQuery(new ReadOnlyQueryRunner(dbms, fetchSize, queryRunnerPmdKnownBroken), handler, sql, params);
		return execQuery(newQueryRunner(), handler, sql, params);
	}


	/**
	 * 検索処理。
	 * 検索結果を、任意の型で取得する。
	 * 取得後、postProcess設定に応じて処置を行う。
	 *
	 * @see org.apache.commons.dbutils.ResultSetHandler<T>
	 */
	public <T> T execQuery(QueryRunner qr, ResultSetHandler<T> handler, String sql, Object...params) {
		try {
			infoSqlAndParams(dbms, sql, params);
			T ret = qr.query(getConnection(), sql, handler, params);
			debugReturnValue(ret);
			if (postProcess == PostProcess.COMMIT_AND_CLOSE) {
				commit();
				close();
			} else if (postProcess == PostProcess.COMMIT_ONLY) {
				commit();
			}
			clear();
			return ret;
		} catch (SQLException e) {
			if (postProcessOnException == PostProcessOnException.CLOSE)
				close();
			throw new RuntimeSQLException(e);
		}
	}



	// ==================================================================
	// Persistor functions


	/**
	 * 検索処理。
	 * 引数のDTOの主キーフィールドに設定されている値を元に、テーブルから一件のレコードを取得する。
	 */
	public <T extends IPersistable> T selectWithKey(T dto) {
		return selectWithKey(dto, false);
	}

	/**
	 * 検索処理。
	 * 引数のDTOの主キーフィールドに設定されている値を元に、テーブルから一件のレコードを取得する。
	 * 同時に取得されたレコードに対する行ロックを取得する。
	 */
	public <T extends IPersistable> T selectWithKey(T dto, boolean forUpdate) {
		return selectWithKey(dto, newSelectHandler(this), forUpdate);
	}
	public <T extends IPersistable> T selectWithKey(T dto, SelectHandler handler, boolean forUpdate) {
		return handler.exec(dto, forUpdate);
	}

	/**
	 * 更新処理。
	 * 引数のDTOを元に、テーブルにレコードをInsertする。
	 */
	public <T extends IPersistable> T insert(T dto) {
		return insert(dto, newInsertHandler(this));
	}
	public <T extends IPersistable> T insert(T dto, InsertHandler handler) {
		return handler.exec(dto);
	}

	/**
	 * 更新処理。
	 * 引数のDTOを元に、テーブルにレコードをupdateする。
	 */
	public <T extends IPersistable> T update(T dto) {
		return update(dto, newUpdateHandler(this));
	}
	public <T extends IPersistable> T update(T dto, UpdateHandler handler) {
		return newUpdateHandler(this).exec(dto);
	}

	/**
	 * 更新処理。
	 * 引数のDTOを元に、テーブルにレコードをupdateする。
	 * 対象テーブルがIOptimisticLockingの場合に、
	 * 更新結果が0件だった時にはExceptionをスローする（＝他者から先に更新されたものとみなす）。
	 *
	 */
	public <T extends IOptimisticLocking> T update(T dto) throws OptimisticLockingException {
		return update(dto, newUpdateHandler(this));
	}
	public <T extends IOptimisticLocking> T update(T dto, UpdateHandler handler) throws OptimisticLockingException {
		T ret = newUpdateHandler(this).exec(dto);
		if (ret != null)
			return ret;
		else
			throw new OptimisticLockingException();
	}

	/**
	 * 更新処理。
	 * 引数のDTOの主キーフィールドに設定されている値を元に、テーブルにレコードをdeleteする。
	 */
	public <T extends IPersistable> int delete(T dto) {
		return deleteInner(dto);
	}

	/**
	 * 更新処理。
	 * 引数のDTOの主キーフィールドに設定されている値を元に、テーブルにレコードをdeleteする。
	 * 対象テーブルがIOptimisticLockingの場合に、
	 * 更新結果が0件だった時にはExceptionをスローする（＝他者から先に更新されたものとみなす）。
	 */
	public <T extends IOptimisticLocking> int delete(T dto)
	throws OptimisticLockingException {
		int ret = deleteInner(dto);
		if (ret != 0)
			return ret;
		else
			throw new OptimisticLockingException();
	}

	private <T extends IPersistable> int deleteInner(T dto) {
		if (dto instanceof ILogicalDeleting)
			return newLogicalDeleteHandler(this).exec(dto);
		else
			return newDeleteHandler(this).exec(dto);
	}



	// ==================================================================
	// Update-query functions (with no-persistor)

	/**
	 * 更新処理。
	 * 処理後、postProcess設定に応じて処置を行う。
	 *
	 * @see org.apache.commons.dbutils.ResultSetHandler<T>
	 */
	public int execUpdate(UpdateQueryBuilder updator) {
		return execUpdate(updator.createSql(/*dbms*/), updator.getUpdatePrms());
	}

	/**
	 * 更新処理。
	 * 処理後、postProcess設定に応じて処置を行う。
	 *
	 * @see org.apache.commons.dbutils.ResultSetHandler<T>
	 */
	public int execUpdate(String query, Object...params) {
		try {
			infoSqlAndParams(dbms, query, params);
			int ret = newQueryRunner().update(getConnection(), query, params);
			debugReturnValue(ret);
			if (postProcess == PostProcess.COMMIT_AND_CLOSE) {
				commit();
				close();
			} else if (postProcess == PostProcess.COMMIT_ONLY) {
				commit();
			}
			clear();
			return ret;
		} catch (SQLException e) {
			if (postProcessOnException == PostProcessOnException.CLOSE)
				close();
			throw new RuntimeSQLException(e);
		}
	}



	private static void infoSqlAndParams(String dbms, String sql, Object[] params) {
		List<Object> args = null;
		if (params != null)
			args = Arrays.asList(params);
		logger.info(
				String.format("Sql(%s)={%s}, Params=%s", dbms, sql, args));
	}

	private static void debugReturnValue(Object ret) {
		if (ret != null)
			logger.debug("Return={" + ret.toString() + "}");
		else
			logger.debug("Return is nothing.");
	}
}