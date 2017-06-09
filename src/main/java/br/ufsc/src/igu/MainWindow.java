package br.ufsc.src.igu;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.UIManager;

import br.ufsc.src.control.ServiceControl;
import br.ufsc.src.igu.panel.AbstractPanel;
import br.ufsc.src.igu.panel.GroupLayoutTest;
import br.ufsc.src.igu.panel.SegmentationTrajectoryPanel;
import br.ufsc.src.igu.panel.LoadPanelDSV;
import br.ufsc.src.igu.panel.RemoveNoisePanel;
import br.ufsc.src.igu.panel.SegmentationTrajectoryPanel1;
import br.ufsc.src.igu.panel.Test;
import br.ufsc.src.igu.panel.TrajNearPointPanel;
import br.ufsc.src.igu.panel.ConnectionPanel;
import br.ufsc.src.igu.panel.ExportTablePanel;
import br.ufsc.src.igu.panel.LoadPanel;


public class MainWindow extends JFrame implements ActionListener {
	private static final long serialVersionUID = 1L;
	ServiceControl control;

	public MainWindow(ServiceControl control) {
		super("Clean Data GPS");
		this.control = control;
		configure();
	}

	private void configure() {
		this.setInterfaceLayout();
		JLabel text;
		text = new JLabel();
		//texto.setText("-- Data GPS --");
		text.setFont(new Font(Font.DIALOG_INPUT, Font.BOLD, 40));
		text.setHorizontalAlignment(NORMAL);
		add(text);
		getContentPane().setBackground(Color.LIGHT_GRAY);
		setJMenuBar(new MenuBar(this));
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setPreferredSize(new Dimension(645, 600));
		
		pack();
		setLocationRelativeTo(null);
	}

	public void interact() {
		setVisible(true);
	}
	
	private void setInterfaceLayout() {
		try{
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
		} catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	public void actionPerformed(ActionEvent e) {
		EnumMenuOption option = EnumMenuOption.valueOf(e.getActionCommand());
		System.out.println(option);
		AbstractPanel panel = null;
		
		switch (option) {
		case OPTIONCONECTION:
			panel = new ConnectionPanel(control);
			break;
		case OPTIONOPENDSV:
			panel = new LoadPanelDSV(control);
			break;
		case OPTIONOPEN:
			panel = new LoadPanel(control);
			break;
		case OPTIONDBROKETRAJECTORY:
			panel = new SegmentationTrajectoryPanel1(control);
			break;
		case OPTIONREMOVENOISE:
			panel = new RemoveNoisePanel(control);
			
			break;
		case OPTIONEXPORTCSV:
			panel = new ExportTablePanel(control);
			break;
		case OPTIONTRAJNEARPOINT:
			panel = new TrajNearPointPanel(control);
			break;
		case TEST:
			
			break;
		}
		//panel = new Test(null, control, new JButton("teste"));
		//panel = new SegmentationTrajectoryPanel1("",control, new JButton(""));

		setContentPane(panel);
		pack(); 
	}
}