package br.ufsc.src.persistencia.exception;

public class UpdateGeomException extends Exception {

	private static final long serialVersionUID = 1L;
	
	String msg;
	
	public UpdateGeomException(String msg) {
		this.msg = msg;	
	}
	
	public String getMsg(){
		return this.msg;
	}
}