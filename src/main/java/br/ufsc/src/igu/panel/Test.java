package br.ufsc.src.igu.panel;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Checkbox;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import br.ufsc.src.control.ServiceControl;
import br.ufsc.src.persistencia.Persistencia;
import br.ufsc.src.persistencia.conexao.DBConnectionProvider;

public class Test extends AbstractPanel{
	/*private JComboBox<String> listConnections;
	JPanel topPanel = new JPanel();
	JPanel bottomPanel = new JPanel();
	JScrollPane sPane = new JScrollPane(); //lista com o nome de tabelas
	DefaultListModel<String> TableList = new DefaultListModel<>();*/
	private JPanel rootPanel;
	private JPanel leftPanel;
	private JPanel rightPanel;
	
	private JComboBox<String> listConnections;
	private JComboBox<String> tableList;
	private JCheckBox[] viewList;
	
	private JTable outputTable;
	
	private JPanel viewOptionsPanel; //opcoes para formatar o view(e.g limite de linhas extraidas de uma tabela)
	
	JCheckBox setLimitCheckBox = new JCheckBox("limite");
	JTextField limitRowsJTextField = new JTextField();
	
	String[] columnsCheckBoxes;
	ArrayList<JCheckBox> columnsJ = new ArrayList<JCheckBox>(); 
	
	
	private JButton generateView = new JButton("Gerar view");
	
	public Test(String title, ServiceControl control, JButton processButton) {
		super(null,null,new JButton(""));
		defineComponents();
		adjustComponents();
		setAllListeners();
		DBConnectionProvider dbConnProvider = DBConnectionProvider.getInstance();
		dbConnProvider.getGeoDatabases();
		ResultSet rs = dbConnProvider.getColumnsValues("taxicab", "select * from testview limit 10");
		//dbConnProvider.generateView("viewTest","table", new String[]{"campo1","campo2"});
		this.setSize(new Dimension(800,800));
	}
	private static final long serialVersionUID = 1L;

	@Override
	public void defineComponents() {
		rootPanel  = new JPanel();
		leftPanel  = new JPanel();
		rightPanel = new JPanel();
		leftPanel.setLayout(new GridLayout(10, 1));
		rightPanel.setLayout(new BorderLayout());
		viewOptionsPanel = new JPanel();
		viewOptionsPanel.setLayout(new GridLayout(1,2));
		viewOptionsPanel.add(limitRowsJTextField);
		viewOptionsPanel.add(setLimitCheckBox);
		DBConnectionProvider db = DBConnectionProvider.getInstance();
		try{
			ArrayList<String> databases = db.getGeoDatabases();
			String[] databasesArray = new String[databases.size()];
			databases.toArray(databasesArray);
			listConnections = new JComboBox<String>(databasesArray);
		}catch(Exception e){
			System.out.println(e.getMessage());
		}
		try{
			System.out.println(db.getTableColumns("taxicab2"));
		}catch(Exception e){
			System.out.println(e.getMessage());
		}
		tableList = new JComboBox<String>(new String[]{"tabela1","tablea2"});
		viewList = new JCheckBox[5];
		leftPanel.add(listConnections);
		leftPanel.add(tableList);
		for(int i = 0; i < 5; i++){
			viewList[i] = new JCheckBox("opt " + i);
		//	viewOptionsPanel.add(viewList[i]);
		}
		leftPanel.add(viewOptionsPanel);
		rootPanel.setLayout(new GridLayout(2, 1));
		String[] columnNames = {"First Name",
                "Last Name",
                "Sport",
                "# of Years",
                "Vegetarian"};
		Object[][] data = {
			    {"Kathy", "Smith",
			     "Snowboarding", new Integer(5), new Boolean(false)},
			    {"John", "Doe",
			     "Rowing", new Integer(3), new Boolean(true)},
			    {"Sue", "Black",
			     "Knitting", new Integer(2), new Boolean(false)},
			    {"Jane", "White",
			     "Speed reading", new Integer(20), new Boolean(true)},
			    {"Joe", "Brown",
			     "Pool", new Integer(10), new Boolean(false)}
		};
		outputTable = new JTable(data,columnNames);
		rightPanel.add(outputTable);
		rootPanel.add(leftPanel);
		rootPanel.add(rightPanel);
		add(rootPanel);
	}
	@Override
	public void adjustComponents() {}
	@Override
	public void actionPerformed(ActionEvent e) {}
	
	public void setAllListeners(){
		listConnections.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED){
					
					String dbName = (String)listConnections.getSelectedItem();
					DBConnectionProvider db = DBConnectionProvider.getInstance();
					try {
						ArrayList<String> list = db.getTableList(dbName);
						tableList.removeAllItems();
						for(int i = 0; i < list.size(); i++) {
							tableList.addItem(list.get(i));
						}
					} catch (SQLException e1) {
						e1.printStackTrace();
					}
				}
			}
		});
		tableList.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED){
					DBConnectionProvider db = DBConnectionProvider.getInstance();
					
					try {
						ArrayList<String> columns = db.getTableColumns(e.getItem().toString());			
						viewOptionsPanel.removeAll();
						columnsJ.clear();
						System.out.println(columns);
						columnsCheckBoxes = new String[columns.size()];
						for(int i = 0; i < columns.size(); i++){
							columnsCheckBoxes[i] = columns.get(i);
							columnsJ.add(new JCheckBox(columns.get(i)));
							viewOptionsPanel.add(columnsJ.get(i));
						}
						viewOptionsPanel.add(limitRowsJTextField);
						viewOptionsPanel.add(setLimitCheckBox);
						viewOptionsPanel.add(generateView);
						viewOptionsPanel.revalidate();
						
					} catch (SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		});
		generateView.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				String database  = String.valueOf(listConnections.getSelectedItem());
				String tableName = String.valueOf(tableList.getSelectedItem());
				ArrayList<String> selectedColumns = new ArrayList<String>();

				for(int i = 0; i < columnsJ.size(); i++){
					if(columnsJ.get(i).isSelected()){
						selectedColumns.add(columnsJ.get(i).getText());
						//System.out.println(columnsJ.get(i).getText());
					}
				}
				DBConnectionProvider db = DBConnectionProvider.getInstance();
				String[] selectedColumnsArray = new String[selectedColumns.size()];
				selectedColumnsArray = selectedColumns.toArray(selectedColumnsArray);
				db.generateView("testview",database,tableName,selectedColumnsArray);
				String sql = "Select *";
				sql = sql + " from testview limit 10"; 
				//System.out.println(sql);
				ArrayList<ArrayList<Object>> data = new ArrayList<>();
				try {
					db.open();
					ResultSet rs = db.getColumnsValues(database, sql);
					while(rs.next()){
						ArrayList<Object> aux = new ArrayList<Object>();
						for(int i = 1; i < selectedColumnsArray.length + 1; i++){
							aux.add(rs.getString(i));
						}
						data.add(aux);
					}
					rightPanel.removeAll();
					Object dataArray[][] = new Object[data.size()][data.get(0).size()];
					for(int i = 0; i < data.size(); i++){
						for(int j = 0; j < data.get(0).size(); j++){
							System.out.print(data.get(i).get(j));
							dataArray[i][j] = (Object)data.get(i).get(j);
						}
						System.out.print("\n");
					}
					rightPanel.add(new JTable(dataArray,selectedColumnsArray));
					
					rightPanel.revalidate();
					rightPanel.repaint();
					
					db.close();
				} catch (SQLException e1) {
					//TODO Auto-generated catch block
					e1.printStackTrace();
				}

			}
		});
	}
}