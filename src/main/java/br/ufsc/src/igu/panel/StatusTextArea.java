package br.ufsc.src.igu.panel;

import java.util.Observable;
import java.util.Observer;

import javax.swing.JTextArea;

public class StatusTextArea extends JTextArea implements Observer{

	private StringBuilder statusText;
	
	
	public StatusTextArea(String initStatus){
		statusText = new StringBuilder(initStatus);
	}
	
	public void updateStatus(String newStatus) {
		statusText.append(newStatus);
		this.setText(statusText.toString());
	}
	
	public void clearStatus() {
		statusText = new StringBuilder("");
		this.setText(statusText.toString());
	}
	
	@Override
	public void update(Observable o, Object arg) {
		String sArg = (String) arg;
		try {
			Integer.parseInt(sArg);
		}catch(Exception e ) {
			updateStatus(sArg);
		}
	}
}
