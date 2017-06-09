package br.ufsc.src.control.exception;

public class BrokeTrajectoryException extends Exception {
	
	private static final long serialVersionUID = 1L;
	
	String msg;
	
	public BrokeTrajectoryException(String msg) {
		this.msg = msg;	
	}
	
	public String getMsg(){
		return this.msg;
	}

}