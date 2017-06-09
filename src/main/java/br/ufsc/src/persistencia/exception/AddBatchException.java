package br.ufsc.src.persistencia.exception;

public class AddBatchException extends Exception {

	private static final long serialVersionUID = 1L;
	
	String msg;
	
	public AddBatchException(String msg) {
		this.msg = msg;	
	}
	
	public String getMsg(){
		return this.msg;
	}

}