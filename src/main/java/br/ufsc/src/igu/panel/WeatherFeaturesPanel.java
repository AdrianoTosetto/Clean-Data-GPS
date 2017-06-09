package br.ufsc.src.igu.panel;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import br.ufsc.src.control.ServiceControl;
import br.ufsc.src.control.dataclean.WeatherFeaturesConfig;
import br.ufsc.src.control.datafeatures.WeatherFeatures;
import br.ufsc.src.persistencia.Persistencia;
import br.ufsc.src.persistencia.conexao.DBConfig;

public class WeatherFeaturesPanel extends AbstractPanel implements ActionListener, Observer, Runnable{

	
	public void deleteThisMethodAfterTests() {
		DBConfig.banco   = "bovinos";
		DBConfig.senha   = "post123";
		DBConfig.usuario = "postgres";
	}
	
	public WeatherFeaturesPanel()  throws SQLException {
		super(null,null,new JButton());
		control = new ServiceControl(new Persistencia("org.postgresql.Driver","jdbc:postgresql://localhost/","bovinos","postgres", "post123"));

		deleteThisMethodAfterTests();
		defineComponents();
		loadDatabases();
		adjustComponents();
		addListeners();
		weatherFeatures.addObserver(this);
		weatherFeatures.addObserver(status);
	}

	@Override
	public void defineComponents() {
		topPanel         = new JPanel();
		featuresPanel    = new JPanel();
		startButtonPanel = new JPanel();
		statusPanel      = new JPanel();
		cities           = new JComboBox<String>(citiesNames);
		tables           = new JComboBox<String>();
		citiesLabel      = new JLabel("Cidades");
		tableLabel       = new JLabel("Tabela");
		temperature      = new JRadioButton("Temperature");
		conds            = new JRadioButton("Condition");
		hail             = new JRadioButton("Hail");
		hum              = new JRadioButton("Hum");
		rain             = new JRadioButton("Rain");
		thunder          = new JRadioButton("Thunder");
		tornado          = new JRadioButton("Tornado");
		start            = new JButton("Extrair");
		status           = new StatusTextArea("...");
		progress         = new JProgressBar();
		
		topPanel.setLayout(new GridLayout(2,2));
		topPanel.add(tableLabel);
		topPanel.add(tables);
		topPanel.add(citiesLabel);
		topPanel.add(cities);
		
		featuresPanel.setLayout(new GridLayout(2,4));
		
		featuresPanel.add(temperature);
		featuresPanel.add(conds);
		featuresPanel.add(hum);
		featuresPanel.add(rain);
		featuresPanel.add(hail);
		featuresPanel.add(thunder);
		featuresPanel.add(tornado);
		
		startButtonPanel.add(start);
		startButtonPanel.add(new JButton("Cancelar"));
		
		statusPanel.setLayout(new GridLayout(2,2));
		statusPanel.add(progress);
		statusPanel.add(status);
		
		setLayout(new GridLayout(4,1));
		
		add(topPanel);
		add(featuresPanel);
		add(startButtonPanel);
		add(statusPanel);
	
	}

	
	private void loadDatabases() {
		try {
			ArrayList<String> tablesList = control.getTableList();
			System.out.println(tablesList);
			for(int i = 0; i < tablesList.size(); i++)
				tables.addItem(tablesList.get(i));					
		} catch (SQLException e) {
			new JOptionPane("Não foi possível carregar as tabelas segmentadas", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
		
	}
	
	@Override
	public void adjustComponents() {
		progress.setStringPainted(true);
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == start) {
			
			WeatherFeaturesConfig config = new WeatherFeaturesConfig();
			boolean featureSelected = false;
			String city  = (String) cities.getSelectedItem();
			String table = (String) tables.getSelectedItem();
			if(hail.isSelected()) {config.setHasHail(true); featureSelected = true;}
			if(temperature.isSelected()) {config.setHasTemperature(true); featureSelected = true;}
			if(conds.isSelected()) {config.setHasCond(true); featureSelected = true;}
			
			if(thunder.isSelected()) {config.setHasThunder(true); featureSelected = true;}
			if(tornado.isSelected()) {config.setHasTornado(true); featureSelected = true;}
			if(rain.isSelected()) {config.setHasRain(true); featureSelected = true;}
			if(hum.isSelected()) {config.setHasHum(true); featureSelected = true;}
			
			if(!featureSelected) {
				JOptionPane.showMessageDialog(this, "Select a feature to be extracted!", "", JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			config.setTable(table);
			config.setCity(city);
		    Runnable runner = new Runnable()
		    {
		        public void run() {
					try {
						weatherFeatures.extractFeatures(config);
						JOptionPane.showMessageDialog(null, "Terminou");
					} catch (SQLException e) {
						JOptionPane.showMessageDialog(null, "Impossible to extract features", "", JOptionPane.ERROR_MESSAGE);
						e.printStackTrace();
					}
		        }
		    };
		    Thread t = new Thread(runner, "Code Executer");
		    t.start();
		    
		    progress.setValue(0);
		    repaint();
		}
	}
	
	public void addListeners() {
		start.addActionListener(this);
	}
	
	public static void main(String[] args) throws SQLException {
		try{
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
		} catch(Exception e){
			e.printStackTrace();
		}
		JFrame f = new JFrame();
		f.setVisible(true);
		f.setSize(500,500);
		
		f.add(new WeatherFeaturesPanel());
	}
	private JPanel topPanel;
	private JPanel featuresPanel;
	private JPanel startButtonPanel;
	private JPanel statusPanel;
	
	
	private JLabel citiesLabel;
	private JLabel tableLabel;
	private JComboBox<String> cities;
	private JComboBox<String> tables;
	
	private JRadioButton temperature;
	private JRadioButton conds;
	private JRadioButton hum;
	private JRadioButton rain;
	private JRadioButton hail;
	private JRadioButton thunder;
	private JRadioButton tornado;
	
	private JButton start;
	
	
	private StatusTextArea status;
	private JProgressBar progress;
	
	WeatherFeatures weatherFeatures = new WeatherFeatures(new Persistencia("org.postgresql.Driver","jdbc:postgresql://localhost/","bovinos","postgres", "post123"));
	Thread workerThread; /* it will extract the features */
	
	private String[] citiesNames = {
			"San Francisco"
	};

	@Override
	public void update(Observable o, Object arg) {
		String s = (String)arg;
		try {
			int value = Integer.parseInt(s);
			progress.setValue(value);
		}catch(Exception e ) {
			
		}
	}

	@Override
	public void run() {
	}
}
