package jp.gr.java_conf.sqlutils.date;

import java.util.Calendar;
import java.util.Date;


/**
 * 以下の目的で用意したラッパー
 * 同名クラスはインポート上の面倒が常に伴うので避けたい
 * 既存のコンストラクタが相応しいのが無い（Calendarクラスでやれという事かもしれないが）ため、
 * インスタンス取得用のstaticメソッドの置き場として。
 *
 */
public class SqlDate extends java.sql.Date {

	private static final long serialVersionUID = 1L;


	public static SqlDate getInstance(Date date) {
		return getInstance(date.getTime());
	}

	public static SqlDate getInstance(long millisec) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(millisec);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return new SqlDate(cal.getTimeInMillis());
	}

	/**
	 * @param year
	 * @param month 1-base
	 * @param day
	 * @return
	 */
	public static SqlDate getInstance(int year, int month, int day) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH, month - 1);
		cal.set(Calendar.DATE, day);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return new SqlDate(cal.getTimeInMillis());
	}

	private SqlDate(long date) {
		super(date);
	}

}
