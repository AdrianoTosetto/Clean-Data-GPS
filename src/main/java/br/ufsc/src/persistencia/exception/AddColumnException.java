package br.ufsc.src.persistencia.exception;

public class AddColumnException extends Exception {

	private static final long serialVersionUID = 1L;
	
	String msg;
	
	public AddColumnException(String msg) {
		this.msg = msg;	
	}
	
	public String getMsg(){
		return this.msg;
	}

}