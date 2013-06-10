package jp.gr.java_conf.sqlutils.date;

import java.sql.Time;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.StringUtils;

/**
 * java.sql.Time with milliseconds.
 * java.sql.Timeが、millisecondを含むのか含まないのかが明瞭でないため、
 * 明確化するためにクラスを二分したもの。
 *
 * Date型はミリ秒までを保持するが、コンストラクタやtoSring、valueOfではミリ秒に対応していないため、
 * 混乱を招く元であり、更にこの関連はDBMSとも関わってくると一層の混乱を招きかねないため、
 * 当FWでは根本的に区別するものとする
 */
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
