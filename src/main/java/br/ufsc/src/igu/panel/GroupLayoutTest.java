package br.ufsc.src.igu.panel;

import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;

import br.ufsc.src.control.ServiceControl;

public class GroupLayoutTest extends AbstractPanel{

	public GroupLayoutTest(String title, ServiceControl control,
			JButton processButton) {
		super(title, control, processButton);
		defineComponents();
		adjustComponents();
	}

	@Override
	public void defineComponents() {
		JComboBox databaseList = new JComboBox<>();
		JLabel label1 = new JLabel("label1");
		JLabel label2 = new JLabel("label2");
	}

	@Override
	public void adjustComponents() {}

	@Override
	public void actionPerformed(ActionEvent e) {}

}
