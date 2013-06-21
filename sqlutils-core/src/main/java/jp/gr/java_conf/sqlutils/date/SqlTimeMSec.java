package jp.gr.java_conf.sqlutils.date;

import java.sql.Time;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.StringUtils;

/**
 * <s>
 * java.sql.Time with milliseconds.
 * java.sql.Timeが、millisecondを含むのか含まないのかが明瞭でないため、
 * 明確化するためにクラスを二分したもの。
 *
 * java.util.Date型は、コンストラクタにlongを取る事でミリ秒に対応する。
 *  'new Date()'は'new Date(System.currentTimeMillis())'と同意
 * java.sql.Timestamp型は、Dateを拡張してナノ秒フィールドを別に持つ。
 *
 * ややこしいのは、java.sql.Time型がミリ秒に対応するのか否か。
 * java.sql.Time型は、コンストラクタはjava.util.Date型と同じなのでミリ秒に対応するが、
 * valueOfやtoSringといったメソッドではミリ秒は非対応。これはJDBCのTIME型が、ミリ秒を持たない型として定義されているから。
 * 従ってJava的にはミリ秒まで保持できても、JDBC経由で使用する際にはミリ秒は無視される。
 *
 *
 * ただしDBMS（JDBCドライバ）によっては、ミリ秒まで保持するデータ型が、JDBCのTIME型にマッピングされる場合もあり、
 * この場合、JDBCの実装次第ではデータが欠落する（valueOf使ってたらアウト）。
 *
 *
 * このクラスを使用する事で、PreparedStatementへのバインディング時の欠落は防げるが、
 * データを取得して来た際の欠落が発生するかどうかはJDBCの実装による。
 * </s>
 */
@Deprecated // 誤った理解に基づいていたため。不要のはず
public class SqlTimeMSec extends Time {

	private static final long serialVersionUID = 1L;


	public static SqlTimeMSec getInstance(Date date) {
		return getInstance(date.getTime());
	}

	public static SqlTimeMSec getInstance(long millisec) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(millisec);
		cal.set(Calendar.YEAR, 1970);
		cal.set(Calendar.MONTH, Calendar.JANUARY);
		cal.set(Calendar.DATE, 1);
		return new SqlTimeMSec(cal.getTimeInMillis());
	}


	private SqlTimeMSec(long date) {
		super(date);
	}
	public String toString() {
		String ret = super.toString();
		String ms = StringUtils.right(String.valueOf(super.getTime()), 3);
		return ret + "." + ms;
	}

}
