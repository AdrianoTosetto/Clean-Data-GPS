package br.ufsc.src.persistencia.exception;

public class GetTableColumnsException extends Exception {

	private static final long serialVersionUID = 1L;
	
	String msg;
	
	public GetTableColumnsException(String msg) {
		this.msg = msg;	
	}
	
	public String getMsg(){
		return this.msg;
	}

}