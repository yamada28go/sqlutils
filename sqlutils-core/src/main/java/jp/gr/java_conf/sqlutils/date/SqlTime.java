package jp.gr.java_conf.sqlutils.date;

import java.sql.Time;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.StringUtils;


/**
 * java.sql.Timeが、millisecondを含むのか含まないのかが明瞭でない＆実装も中途半端で混乱したので、
 * 新たにクラス定義したもの。
 *
 * SQL99のTIME型は、ミリ秒も含む（らしい）。
 * java.sql.Time型は、コンストラクタはjava.util.Date型と同じなのでミリ秒に対応するが、
 * valueOfやtoSringといったメソッドでは何故かミリ秒に非対応。
 *
 * ちなみに
 * java.util.Date型は、コンストラクタにlongを取る事でミリ秒に対応する。
 * java.sql.Timestamp型は、Dateを拡張してナノ秒フィールドを別に持つ。
 *
 * よってjava.sql.Time型をそのままDTOとして使用すると
 * 1.データを取得する際にミリ秒が欠落する。←JDBCドライバの実装が、valueOfメソッドを使ってインスタンス生成されている場合
 * 2.PreparedStatementに、ミリ秒までセットしたTime型の値を渡しても、toString()がミリ秒を無視するため、
 * 結果イコールにならない条件になってしまう。
 * といった問題が発生する。
 *
 *
 * 1.に関しては、JDBCドライバ内の実装次第なのでどうしようもないが、
 * 2.に関しては、java.sql.Time型でなく当クラスを使用する事で防げる、というかそれが目的。
 */
public class SqlTime extends Time {

	private static final long serialVersionUID = 1L;


	public static SqlTime getInstance(Date date) {
		return getInstance(date.getTime());
	}

	public static SqlTime getInstance(long millisec) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(millisec);
		cal.set(Calendar.YEAR, 1970);
		cal.set(Calendar.MONTH, Calendar.JANUARY);
		cal.set(Calendar.DATE, 1);
		//cal.set(Calendar.MILLISECOND, 0);
		return new SqlTime(cal.getTimeInMillis());
	}

	public static SqlTime getInstance(int hour, int minute, int second) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, 1970);
		cal.set(Calendar.MONTH, Calendar.JANUARY);
		cal.set(Calendar.DATE, 1);
		cal.set(Calendar.HOUR_OF_DAY, hour);
		cal.set(Calendar.MINUTE, minute);
		cal.set(Calendar.SECOND, second);
		cal.set(Calendar.MILLISECOND, 0);
		return new SqlTime(cal.getTimeInMillis());
	}

	public static SqlTime getInstance(int hour, int minute, int second, int msec) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, 1970);
		cal.set(Calendar.MONTH, Calendar.JANUARY);
		cal.set(Calendar.DATE, 1);
		cal.set(Calendar.HOUR_OF_DAY, hour);
		cal.set(Calendar.MINUTE, minute);
		cal.set(Calendar.SECOND, second);
		cal.set(Calendar.MILLISECOND, msec);
		return new SqlTime(cal.getTimeInMillis());
	}

	private SqlTime(long date) {
		super(date);
	}

	public String toString() {
		String ret = super.toString();
		String ms = StringUtils.right(String.valueOf(super.getTime()), 3);
		if (!"000".equals(ms))
			ret = ret + "." + ms;
		return ret;
	}
}
