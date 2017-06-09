package br.ufsc.src.igu;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

public class MenuBar extends JMenuBar {

	private static final long serialVersionUID = 1L;

	public MenuBar(MainWindow mainWindow) {
		organizeMenu(mainWindow);
	}

	private void organizeMenu(MainWindow mainWindow) {
		JMenu menu;
		JMenuItem item;

		menu = new JMenu("File");
		add(menu);
		
		item = new JMenuItem("Load DSV");
		menu.add(item);
		item.setActionCommand(EnumMenuOption.OPTIONOPENDSV.name());
		item.addActionListener(mainWindow);
		
		item = new JMenuItem("Load JSON/GPX/KML/WKT");
		menu.add(item);
		item.setActionCommand(EnumMenuOption.OPTIONOPEN.name());
		item.addActionListener(mainWindow);
		
		menu.addSeparator();
		
		item = new JMenuItem("Export table to CSV");
		menu.add(item);
		item.setActionCommand(EnumMenuOption.OPTIONEXPORTCSV.name());
		item.addActionListener(mainWindow);
		
		item = new JMenuItem("Verificar bancos");
		menu.add(item);
		item.setActionCommand(EnumMenuOption.TEST.name());
		item.addActionListener(mainWindow);
		
		menu = new JMenu("Data Clean");
		add(menu);
		
		item = new JMenuItem("Broke Trajectories");
		menu.add(item);
		item.setActionCommand(EnumMenuOption.OPTIONDBROKETRAJECTORY.name());
		item.addActionListener(mainWindow);
		
		item = new JMenuItem("Traj Near Point");
		menu.add(item);
		item.setActionCommand(EnumMenuOption.OPTIONTRAJNEARPOINT.name());
		item.addActionListener(mainWindow);
		
		item = new JMenuItem("Remove Noise");
		menu.add(item);
		item.setActionCommand(EnumMenuOption.OPTIONREMOVENOISE.name());
		item.addActionListener(mainWindow);
		
		menu = new JMenu("Configuration");
		add(menu);

		item = new JMenuItem("Connection");
		menu.add(item);
		item.setActionCommand(EnumMenuOption.OPTIONCONECTION.name());
		item.addActionListener(mainWindow);		
	}
}