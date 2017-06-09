package br.ufsc.src.persistencia.exception;

public class GetSequenceException extends Exception {

	private static final long serialVersionUID = 1L;
	
	String msg;
	
	public GetSequenceException(String msg) {
		this.msg = msg;	
	}
	
	public String getMsg(){
		return this.msg;
	}
}