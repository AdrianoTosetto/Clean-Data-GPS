package br.ufsc.src.persistencia.exception;

public class DBConnectionException extends Exception {

	private static final long serialVersionUID = 1L;
	
	String msg;
	
	public DBConnectionException(String msg) {
		this.msg = msg;	
	}
	
	public String getMsg(){
		return this.msg;
	}
}