package jp.gr.java_conf.sqlutils.common.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@Deprecated
public class SqlDateUtil {

	public static java.sql.Timestamp getTimestamp() {
		return getTimestamp(new Date());
	}

	public static java.sql.Timestamp getTimestamp(Date date) {
		return new java.sql.Timestamp(date.getTime());
	}

	public static java.sql.Date getDate() {
		return getDate(new Date());
	}

	public static java.sql.Date getDate(int y, int m, int d) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, y);
		cal.set(Calendar.MONTH, m - 1);
		cal.set(Calendar.DAY_OF_MONTH, d);
		return getDate(cal.getTime());
	}

	public static java.sql.Date getDate(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return new java.sql.Date(cal.getTimeInMillis());
	}

	public static java.sql.Time getTime() {
		return getTime(new Date());
	}

	public static java.sql.Time getTime(int h, int m, int s) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, h);
		cal.set(Calendar.MINUTE, m);
		cal.set(Calendar.SECOND, s);
		cal.set(Calendar.MILLISECOND, 0);
		return getTime(cal.getTime());
	}

	public static java.sql.Time getTime(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.YEAR, 1970);
		cal.set(Calendar.MONTH, Calendar.JANUARY);
		cal.set(Calendar.DATE, 1);

		/*
		 * Time型ではミリ秒は扱わないので削除する
		 * 正式に謳われてる訳では無いようだが（というか明記されてない）、実際問題として
		 * java.sql.Time#toStringや#valueOfなども秒までしか対応してないため
		 */
		cal.set(Calendar.MILLISECOND, 0);
		return new java.sql.Time(cal.getTimeInMillis());
	}

	public static java.sql.Time getTimeMSec(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.YEAR, 1970);
		cal.set(Calendar.MONTH, Calendar.JANUARY);
		cal.set(Calendar.DATE, 1);
		return new java.sql.Time(cal.getTimeInMillis());
	}

	/**
	 * @return
	 * 		>0	日付1>日付2
	 * 		=0	日付1=日付2
	 * 		<0	日付1<日付2
	 */
	public static int compare(java.sql.Date d1, java.sql.Date d2) {
		SimpleDateFormat f = new SimpleDateFormat("yyyyMMdd");
		String s1 = f.format(d1);
		String s2 = f.format(d2);
		return s1.compareTo(s2);
	}

	public static int compare(java.sql.Time d1, java.sql.Time d2) {
		SimpleDateFormat f = new SimpleDateFormat("HHmmss");
		String s1 = f.format(d1);
		String s2 = f.format(d2);
		return s1.compareTo(s2);
	}

	public static int getHour(java.sql.Time t) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(t);
		return cal.get(Calendar.HOUR_OF_DAY);
	}
}
