package jp.gr.java_conf.sqlutils.core.builder;


public class MySqlQueryBuilder extends QueryBuilder {


	public String getGetSequenceValSql(String seqName) {
		throw new RuntimeException("Not supported!");
	}

	public String getGetAutoIncrementedValSql() {
		return "select last_insert_id()";
	}
}
