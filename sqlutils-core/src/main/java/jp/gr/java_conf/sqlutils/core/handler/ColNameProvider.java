package jp.gr.java_conf.sqlutils.core.handler;

import java.sql.ResultSet;

@Deprecated
public class ColNameProvider implements IColNameProvider {

	private String[][] names;

	public ColNameProvider(String[] dd) {

		this.names = new String[dd.length][2];
		int i = -1;
		for (String s : dd) {
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
