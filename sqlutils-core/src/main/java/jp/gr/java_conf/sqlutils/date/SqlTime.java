package jp.gr.java_conf.sqlutils.date;

import java.sql.Time;
import java.util.Calendar;
import java.util.Date;


/**
 * java.sql.Time without milliseconds.
 * @see jp.gr.java_conf.sqlutils.date.SqlTimeMSec
 *
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
		cal.set(Calendar.MILLISECOND, 0);
		return new SqlTime(cal.getTimeInMillis());
	}


	private SqlTime(long date) {
		super(date);
	}

}
