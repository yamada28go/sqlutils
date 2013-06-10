package jp.gr.java_conf.sqlutils.core.handler;

import java.sql.ResultSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Deprecated
public class SimpleSqlColNameProvider implements IColNameProvider {

	/*
	 * '\s' 空白
	 * '*' 直前Charが0回以上
	 * '+' 直前Charが1回以上
	 * '{0,}' 文字列が0回以上
	 *
	 */
	private static final String RGX = "^(select \\* from \\(select|SELECT|select)\\s+(DISTINCT|distinct){0,}(.+)\\s+(FROM|from)";
	private static Pattern p = Pattern.compile(RGX);

	private String[][] names;


	public SimpleSqlColNameProvider(String sql) {

		if (sql == null)
			throw new RuntimeException("Sql must not null.");

		String[] selectCols;
		Matcher m = p.matcher(sql);
		if (m.find())
			selectCols = m.group(3).trim().split("\\s*,\\s*");
		else
			throw new RuntimeException("Unable to parse select-cols : " + sql);

		if (selectCols.length == 1 && selectCols[0].equals("*"))
			throw new RuntimeException("Cannot parse sql with asterisc : " + sql);


		names = new String[selectCols.length][2];
		int i = -1;
		for (String s : selectCols) {
			i++;
			String[] tmp = s.split("\\s*\\.\\s*");
			if (tmp.length == 3) {
    			// <schema>.<tbl>.<col>
				names[i][0] = tmp[1];
				names[i][1] = tmp[2];
			} else if (tmp.length == 2) {
    			// <tbl>.<col>
				names[i][0] = tmp[0];
				names[i][1] = tmp[1];
			} else if (tmp.length == 1) {
    			// <col>
				names[i][0] = null;
				names[i][1] = tmp[0];
			} else {
    			throw new RuntimeException();
			}
		}
	}

	@Override
	public String getTableName(ResultSet rs, int num) {
		return names[num - 1][0];
	}

	@Override
	public String getColumnName(ResultSet rs, int num) {
		return names[num - 1][1];
	}
}
