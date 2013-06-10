package jp.gr.java_conf.sqlutils.core.builder;


public class PostgresQueryBuilder extends QueryBuilder {


	public String getGetSequenceValSql(String seqName) {
		return "select nextval('" + seqName + "')";
	}
}
