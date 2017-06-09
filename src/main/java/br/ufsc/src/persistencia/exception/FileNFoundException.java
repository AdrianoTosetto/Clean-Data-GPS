package br.ufsc.src.persistencia.exception;

public class FileNFoundException extends Exception {

	private static final long serialVersionUID = 1L;
	
	String msg;
	
	public FileNFoundException(String msg) {
		this.msg = msg;	
	}
	
	public String getMsg(){
		return this.msg;
	}
}