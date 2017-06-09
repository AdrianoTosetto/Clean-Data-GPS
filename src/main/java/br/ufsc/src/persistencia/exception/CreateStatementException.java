package br.ufsc.src.persistencia.exception;

public class CreateStatementException extends Exception {

	private static final long serialVersionUID = 1L;
	
	String msg;
	
	public CreateStatementException(String msg) {
		this.msg = msg;	
	}
	
	public String getMsg(){
		return this.msg;
	}
}