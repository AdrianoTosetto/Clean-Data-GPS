package br.ufsc.src.igu.panel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.text.DefaultCaret;

import br.ufsc.src.control.ServiceControl;
import br.ufsc.src.control.dataclean.FeaturesConfig;
import br.ufsc.src.control.dataclean.TrajectoryFeatures;
import br.ufsc.src.persistencia.Persistencia;
import br.ufsc.src.persistencia.conexao.DBConfig;
import br.ufsc.src.persistencia.exception.DBConnectionException;
import br.ufsc.src.persistencia.exception.GetTableColumnsException;

public class Features extends AbstractPanel implements Observer{

	public Features(String title, ServiceControl control, JButton processButton) {
		super(title, control, processButton);
		// TODO Auto-generated constructor stub
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private TrajectoryFeatures trajFeatures; 
	
	
	private JPanel panelConfig = new JPanel(); //top panel of the window
	private JPanel panelConfigChild1 = new JPanel(); //top panel of the panelConfig
	private JPanel panelConfigChild2 = new JPanel(); // bottom panel of the panelConfig 
	private JPanel panelConfigChild3 = new JPanel(); //origin table columns to segment
	private JPanel panelOptions = new JPanel();
	
	private JPanel panelOptionsChild1 = new JPanel();
	
	private JPanel panelOptionsChild2 = new JPanel();
	private JPanel panelOptionsChild3 = new JPanel();
	
	private JTextField newTableNameInput;
	
	private JComboBox<String> tablesDataBase;
	private JComboBox<String> segTables; //tables that store broken trajctories
	
	private JProgressBar progressBar; //
	private JButton button;
	private JButton cancel;
	private JButton loadSegmentationOptions;
	
	private JLabel infoOldTable;
	private JLabel infoNewTable;
	
	
	private JLabel numberThreadsInfo;
	private JSpinner numberThreadsInput;
	
	private JLabel writeTableLabel;
	private JCheckBox writeTableCheckBox;
	
	JTable featuresAvailable;  // features available to be extracted
	JTable featuresChoosen; // features choosen to be extracted
	
	/*
	 * speed options
	 * */
	
	private JRadioButton kmPerHour = new JRadioButton("km/h");
	private JRadioButton metersPerSecond =  new JRadioButton("m/s");
	
	/*
	 * distance options
	 * */
	
	private JRadioButton meters =  new JRadioButton();
	private JRadioButton kilometers =  new JRadioButton();
	
	/*
	 * duration options
	 * */
	
	private JRadioButton seconds =  new JRadioButton();
	private JRadioButton minutes =  new JRadioButton();
	private JRadioButton hours =  new JRadioButton();
	
	private ButtonGroup  group0;
	private ButtonGroup  group1;
	private ButtonGroup  group2;
	
	JButton addFeature;
	JButton deleteFeature;
	
	private StatusTextArea status = new StatusTextArea("Status...");
	
	private Thread doWork = null;
	
	private FeaturesConfig fConfig = null;
	
	private String segTable;
	private String table;
	
	public void deleteThisMethodAfterTests() {
		DBConfig.banco   = "bovinos";
		DBConfig.senha   = "post123";
		DBConfig.usuario = "postgres";
	}
	
	public Features(ServiceControl control) throws SQLException {
		super("Segmentação", control, new JButton("rs"));
		this.control = control;
		trajFeatures = new TrajectoryFeatures(new Persistencia("org.postgresql.Driver","jdbc:postgresql://localhost/","bovinos","postgres", "post123"));
		defineComponents();
		adjustComponents();
		addListners();
		deleteThisMethodAfterTests();
		loadTables();
		trajFeatures.addObserver(status);
		trajFeatures.addObserver(this);
	}
	public ServiceControl getServiceControl() {
		return control;
	}
	public static void main(String[] args) throws SQLException {
		JFrame f = new JFrame();
		//f.setResizable(false);
		try{
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
		} catch(Exception e){
			e.printStackTrace();
		}
		f.setSize(550,700);
		Features panel;
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Persistencia p = new Persistencia("org.postgresql.Driver","jdbc:postgresql://localhost/","taxicab","postgres", "postgres");
		
		panel = new Features(new ServiceControl(p));
		p.addObserver(panel);
		f.add(panel);
		f.setVisible(true);
	}
	@Override
	public void defineComponents() {
		newTableNameInput = new JTextField("");
		
		addFeature = new JButton(">>");
		deleteFeature = new JButton("<<");
		
		featuresAvailable = new JTable(0,1);
		featuresChoosen   = new JTable(0,1);
		featuresAvailable.getColumnModel().getColumn(0).setHeaderValue("Features disponíveis");
		featuresChoosen.getColumnModel().getColumn(0).setHeaderValue("Features escolhidas");

		progressBar = new JProgressBar(0,100);
		progressBar.setStringPainted(true);
		progressBar.setSize(400, 50);
		button = new JButton("Extrair Features");
		cancel = new JButton("Cancelar");
		
		infoNewTable = new JLabel("Output 	 ");
		infoOldTable = new JLabel("tabela base             ");
		
		kmPerHour = new JRadioButton("km/h");
		metersPerSecond = new JRadioButton("m/s");
		
		meters = new JRadioButton("meters");
		kilometers = new JRadioButton("kilometros");
		
		seconds = new JRadioButton("secs");
		minutes = new JRadioButton("min");
		hours   = new JRadioButton("hours");
		
	
		//SpinnerNumberModel model1 = new SpinnerNumberModel(5.0, 0.0, 8.0, 1.0); 
		numberThreadsInput = new JSpinner(new SpinnerNumberModel(1.0, 1.0, 8.0, 1.0));
		numberThreadsInfo = new JLabel("Número de threads");
		
		writeTableLabel = new JLabel("Escrever na tabela original?");
		writeTableCheckBox = new JCheckBox("");
		
		group0 = new ButtonGroup();
		group1 = new ButtonGroup();
		group2 = new ButtonGroup();
		
		
		panelOptionsChild1.setBorder(BorderFactory.createTitledBorder("Speed Options"));
		panelOptionsChild2.setBorder(BorderFactory.createTitledBorder("Distance Options"));
		panelOptionsChild3.setBorder(BorderFactory.createTitledBorder("Duration Options "));
	}

	@Override
	public void adjustComponents() {
		
		segTables       = new JComboBox<String>();
		tablesDataBase  = new JComboBox<String>();
		setLayout(new GridLayout(5,1));
		
		panelConfigChild1.setLayout(new GridLayout(4,2,10,10));
		
		panelConfigChild1.add(infoOldTable);
		panelConfigChild1.add(segTables);
		panelConfigChild1.add(infoNewTable);
		panelConfigChild1.add(tablesDataBase);
		panelConfigChild1.add(numberThreadsInfo);
		panelConfigChild1.add(numberThreadsInput);
		panelConfigChild1.add(writeTableLabel);
		panelConfigChild1.add(writeTableCheckBox);
		
		DefaultTableModel model = (DefaultTableModel) featuresAvailable.getModel();
		model.addRow(new Object[]{"distance"}); // 1
		model.addRow(new Object[]{"speed"}); // 2
		model.addRow(new Object[]{"duration"});  // 3
		
		JScrollPane sp1 = new JScrollPane(featuresAvailable);
		JScrollPane sp2 = new JScrollPane(featuresChoosen);
		
		JPanel aux = new JPanel();
		aux.setLayout(new GridLayout(2,1));
		aux.add(addFeature);
		aux.add(deleteFeature);
		
		GridLayout gl = new GridLayout(1,3,10,10);
		gl.setHgap(40);
		panelConfigChild2.setLayout(gl);
		panelConfigChild2.add(sp1);
		panelConfigChild2.add(aux);
		panelConfigChild2.add(sp2);
		
		panelConfigChild3.setLayout(new GridLayout(1,2,10,10));
		panelConfigChild3.add(button);
		panelConfigChild3.add(cancel);
		JPanel x = new JPanel();
		x.setLayout(new GridLayout(2,1));
		x.add(panelConfigChild3);
		
		add(panelConfigChild1);
		add(panelConfigChild2);
		
		panelOptions.setLayout(new GridLayout(1,3));
		
		panelOptionsChild1.setVisible(false);
		panelOptionsChild2.setVisible(false);
		panelOptionsChild3.setVisible(false);
		
		group0.add(kmPerHour);
		group0.add(metersPerSecond);
		
		group1.add(meters);
		group1.add(kilometers);
		
		group2.add(seconds);
		group2.add(minutes);
		group2.add(hours);
		
		
		panelOptionsChild1.add(kmPerHour);
		panelOptionsChild1.add(metersPerSecond);
		
		panelOptionsChild2.add(meters);
		panelOptionsChild2.add(kilometers);
		
		panelOptionsChild3.add(seconds);
		panelOptionsChild3.add(minutes);
		panelOptionsChild3.add(hours);
		
		panelOptions.add(panelOptionsChild1);
		panelOptions.add(panelOptionsChild2);
		panelOptions.add(panelOptionsChild3);
		
		add(panelOptions);
		
		
		
		
		JPanel barPanel = new JPanel();
		barPanel.setLayout(new GridLayout(1,1));
		barPanel.add(progressBar);
		x.add(barPanel);
		add(x);
		status.setEditable(false);
		DefaultCaret caret = (DefaultCaret)status.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		JScrollPane sp = new JScrollPane(status);
		add(sp);
		
	}
	public void addListners(){
		segTables.addActionListener(this);
		addFeature.addActionListener(this);
		deleteFeature.addActionListener(this);
		button.addActionListener(this);
		cancel.addActionListener(this);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == segTables);
		
		if(e.getSource() == addFeature) {
			int index = featuresAvailable.getSelectedRow();
			
			if(featuresAvailable.getRowCount() == 0) {
				JOptionPane.showMessageDialog(null,"Não há colunas para serem removidas","Error", JOptionPane.ERROR_MESSAGE, null);
				return;
			}
			
			if(index == -1) {
				JOptionPane.showMessageDialog(null,"Selecione uma coluna","Error", JOptionPane.ERROR_MESSAGE, null);
				return;
			}
			String column = (String) featuresAvailable.getValueAt(index, 0);
			DefaultTableModel model = (DefaultTableModel) featuresAvailable.getModel();
			model.removeRow(index);
			model = (DefaultTableModel) featuresChoosen.getModel();
			model.addRow(new Object[]{column});
			checkFeatureSelected(column, true);
		}
		
		if(e.getSource() == deleteFeature) {
			int index = featuresChoosen.getSelectedRow();
			
			if(featuresChoosen.getRowCount() == 0) {
				JOptionPane.showMessageDialog(null,"Não há colunas para serem removidas","Error", JOptionPane.ERROR_MESSAGE, null);
				return;
			}
			
			if(index == -1) {
				JOptionPane.showMessageDialog(null,"Selecione uma coluna","Error", JOptionPane.ERROR_MESSAGE, null);
				return;
			}
			String column = (String) featuresChoosen.getValueAt(index, 0);
			DefaultTableModel model = (DefaultTableModel) featuresChoosen.getModel();
			model.removeRow(index);
			model = (DefaultTableModel) featuresAvailable.getModel();
			model.addRow(new Object[]{column});
			checkFeatureSelected(column, false);
		}
		if(e.getSource() == button) {
			
			if(hasUserInputErrors()) return;
			button.setEnabled(false);
			fConfig = new FeaturesConfig();
			fConfig.setWriteOriginal(writeTableCheckBox.isSelected());
			
			int nFeatures = featuresChoosen.getRowCount();
			
			for(int i = 0; i < nFeatures; i++) {
				String featureString = (String) featuresChoosen.getValueAt(i, 0);
				if(featureString.equals("distance")) fConfig.hasDistance = true;
				if(featureString.equals("speed")) fConfig.hasSpeed = true;
				if(featureString.equals("duration")) fConfig.hasDuration = true;
			}
			
			if(metersPerSecond.isSelected())  fConfig.argMetersPerSecond = true;
			if(kmPerHour.isSelected()) fConfig.argKMPerHour = true;
			
			if(seconds.isSelected()) fConfig.argSeconds = true;
			if(minutes.isSelected()) fConfig.argMinutes = true;
			if(hours.isSelected())   fConfig.argHours   = true;
			
			if(meters.isSelected()) fConfig.argMeters = true;
			if(kilometers.isSelected()) fConfig.argKM = true;
			
			
			System.out.println("Speed " + fConfig.hasSpeed);
			System.out.println("Duration:" + fConfig.hasDuration);
			System.out.println("Distance"+fConfig.hasDistance);
			
			System.out.println("Hours: " + fConfig.argHours);
			System.out.println("Minutes: "+fConfig.argMinutes);
			System.out.println("Seconds: "+fConfig.argSeconds);
			
			System.out.println("km:" + fConfig.argKM);
			System.out.println("meters: " + fConfig.argMeters);
			 
			System.out.println("m/s: " + fConfig.argMetersPerSecond);
			System.out.println("km/h: " + fConfig.argKMPerHour);
			
			segTable = (String) segTables.getSelectedItem();
			table    = (String) tablesDataBase.getSelectedItem();
			
			double nThreads = (Double)numberThreadsInput.getValue();
			System.out.println(numberThreadsInput.getValue());
			
			doWork = (new Thread(new Runnable(){

				@Override
				public void run() {
					
					try {
						trajFeatures.doItAll(table, segTable, fConfig, (int)nThreads);
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}	
				}
				
			}));
			doWork.start();
			button.setEnabled(true);
		}
		if(e.getSource() == cancel) {
			doWork.stop();
			
			/* stop all the threads that doWork spawns */
			trajFeatures.getThreadGroup().stop();
			
			if(fConfig == null) return;
			
			if(fConfig.hasDistance) 
				if(fConfig.isWriteOriginal())
					control.deleteColumn(table, "distance");
				else
					control.deleteColumn(segTable, "distance");
			
			if(fConfig.hasDuration)
				if(fConfig.isWriteOriginal())
					control.deleteColumn(table, "duration");
				else
					control.deleteColumn(segTable, "duration");
			
			if(fConfig.hasSpeed)
				if(fConfig.isWriteOriginal())
					control.deleteColumn(table, "speed");
				else
					control.deleteColumn(segTable, "speed");
			status.clearStatus();
			status.updateStatus("Cancelado...\n");
			progressBar.setValue(0);
			FeaturesConfig.clear();
		} 
	}
	@Override
	public void update(Observable o, Object arg) {
		String s = (String)arg;
		try {
			int value = Integer.parseInt(s);
			progressBar.setValue(value);
		}catch(Exception e ) {
			
		}
	}
	
	public boolean hasUserInputErrors() {
		int numFeaturesChoosen = featuresChoosen.getRowCount();
		if(numFeaturesChoosen == 0) {
			JOptionPane.showMessageDialog(this, "Escolha pelo menos uma feature! ", "", JOptionPane.ERROR_MESSAGE);
			return true;
		}
		for(int i = 0; i < numFeaturesChoosen; i++) {
			String featureString = (String) featuresChoosen.getValueAt(i, 0);
			if(featureString.equals("distance")) {
				if(!kilometers.isSelected() && !meters.isSelected()) {
					JOptionPane.showMessageDialog(this, "Escolha pelo menos uma das opções de distância! ", "", JOptionPane.ERROR_MESSAGE);
					return true;
				}
			}
			if(featureString.equals("speed")) {
				if(!kmPerHour.isSelected() && !metersPerSecond.isSelected()) {
					JOptionPane.showMessageDialog(this, "Escolha pelo menos uma das opções de velocidade! ", "", JOptionPane.ERROR_MESSAGE);
					return true;
				}
			}
			if(featureString.equals("duration")) {
				if(!seconds.isSelected() && !minutes.isSelected() && !hours.isSelected()) {
					JOptionPane.showMessageDialog(this, "Escolha pelo menos uma das opções de duração! ", "", JOptionPane.ERROR_MESSAGE);
					
					return true;
				}
			}
		}
		return false;
	}
	
	void checkFeatureSelected(String feature, boolean visible) {
		if(feature.equals("speed")) panelOptionsChild1.setVisible(visible);
		if(feature.equals("distance")) panelOptionsChild2.setVisible(visible);
		if(feature.equals("duration")) panelOptionsChild3.setVisible(visible);
	}
	
	public void loadTables() {
		try {
			ArrayList<String> tables = control.getTableList();
			ArrayList<String> t = new ArrayList<String>();
			String[] filter = {"start_gid","final_gid"};
			System.out.println(tables);
			/*
			 * only broken trajectories tables
			 * */
			for(int i = 0; i < tables.size(); i++)
				if(control.tableExists(tables.get(i), filter))
					segTables.addItem(tables.get(i));
				else
					tablesDataBase.addItem(tables.get(i));
					
					
		} catch (SQLException e) {
			new JOptionPane("Não foi possível carregar as tabelas segmentadas", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}
}
