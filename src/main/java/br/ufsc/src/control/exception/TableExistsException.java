package br.ufsc.src.control.exception;

public class TableExistsException extends Exception {
	
	private static final long serialVersionUID = 1L;
	
	String msg;
	
	public TableExistsException(String msg) {
		this.msg = msg;	
	}
	
	public String getMsg(){
		return this.msg;
	}

}