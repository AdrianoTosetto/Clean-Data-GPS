package br.ufsc.src.persistencia.exception;

public class SyntaxException extends Exception {

	private static final long serialVersionUID = 1L;
	
	String msg;
	
	public SyntaxException(String msg) {
		this.msg = msg;	
	}
	
	public String getMsg(){
		return this.msg;
	}
}