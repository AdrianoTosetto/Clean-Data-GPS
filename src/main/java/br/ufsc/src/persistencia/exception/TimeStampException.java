package br.ufsc.src.persistencia.exception;

public class TimeStampException extends Exception {

	private static final long serialVersionUID = 1L;
	
	String msg;
	
	public TimeStampException(String msg) {
		this.msg = msg;	
	}
	
	public String getMsg(){
		return this.msg;
	}
}