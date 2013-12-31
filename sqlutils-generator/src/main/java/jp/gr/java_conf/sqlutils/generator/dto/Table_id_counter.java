package jp.gr.java_conf.sqlutils.generator.dto;

// C++内部でDTOオブジェクトを識別するために連番を割り振る。
// 本クラスでは連番の管理を行う。
public class Table_id_counter {

	private int id;
	
	public Table_id_counter()
	{
		id = 0;
	}
	
	public int get_id()
	{
		id = id+1;
		return id;
	}
	
	
	
}
