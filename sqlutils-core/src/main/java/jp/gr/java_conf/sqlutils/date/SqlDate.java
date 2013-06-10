package jp.gr.java_conf.sqlutils.date;

import java.util.Calendar;
import java.util.Date;


/**
 * 以下の目的で用意したラッパー
 * 同名クラス（「Date」）を避けたい
 * 既存のコンストラクタが相応しいのが無い←Calendarクラスでやれという事かもしれないが
 * Time型の2つと合わせた
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

	private SqlDate(long date) {
		super(date);
	}

}
