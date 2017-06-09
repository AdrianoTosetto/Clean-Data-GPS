package br.ufsc.src.igu.panel;

public enum EnumTypes {
	INTEGER ("integer")
	,SMALLINT ("smallint")
	,VARCHAR ("varchar")
	,NUMERIC ("numeric")
	,DECIMAL ("decimal")
	,SERIAL ("serial")
	,REAL ("real")
	,CHARACTERVARYING ("character varying")
	,TIMESTAMP ("timestamp without time zone")
	,POINT ("geometry(Point)");
	
	
	private String type;
	
	EnumTypes(String tp){
		this.type = tp;
	}
	
	public String toString(){
		return this.type;
	}
}