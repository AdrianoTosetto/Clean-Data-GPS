package br.ufsc.src.igu.panel;

import java.awt.Color;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;

import br.ufsc.src.control.ServiceControl;

public abstract class AbstractPanel extends JPanel implements ActionListener{
	private static final long serialVersionUID = 1L;
	private String title;
	protected JButton processButton;
	protected ServiceControl control;
	protected Container screen;

	public AbstractPanel(String title, ServiceControl control, JButton processButton) {
		this.title = title;
		this.control = control;
		this.processButton = processButton;
		defineLook();
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

	private void defineLook() {
		setBackground(Color.LIGHT_GRAY);
		setBorder(BorderFactory.createTitledBorder(title));
		processButton.addActionListener(this);
	}

	public abstract void defineComponents();

	public abstract void adjustComponents();

	public abstract void actionPerformed(ActionEvent e);

	protected void clearWindow() {
		getRootPane().setBackground(Color.LIGHT_GRAY);
		setVisible(false);
	}

}