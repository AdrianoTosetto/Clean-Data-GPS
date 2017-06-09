package br.ufsc.src.persistencia.exception;

public class ExecuteBatchException extends Exception {

	private static final long serialVersionUID = 1L;
	
	String msg;
	
	public ExecuteBatchException(String msg) {
		this.msg = msg;	
	}
	
	public String getMsg(){
		return this.msg;
	}
}