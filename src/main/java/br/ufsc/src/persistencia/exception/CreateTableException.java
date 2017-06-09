package br.ufsc.src.persistencia.exception;

public class CreateTableException extends Exception {

	private static final long serialVersionUID = 1L;
	
	String msg;
	
	public CreateTableException(String msg) {
		this.msg = msg;	
	}
	
	public String getMsg(){
		return this.msg;
	}
}