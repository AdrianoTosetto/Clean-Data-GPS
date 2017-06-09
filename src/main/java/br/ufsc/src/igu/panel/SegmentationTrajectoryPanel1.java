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

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;

import br.ufsc.src.control.ServiceControl;
import br.ufsc.src.persistencia.Persistencia;
import br.ufsc.src.persistencia.exception.AddColumnException;
import br.ufsc.src.persistencia.exception.DBConnectionException;
import br.ufsc.src.persistencia.exception.GetTableColumnsException;

public class SegmentationTrajectoryPanel1 extends AbstractPanel implements Observer, Runnable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private JPanel panelConfig = new JPanel(); //top panel of the window
	private JPanel panelConfigChild1 = new JPanel(); //top panel of the panelConfig
	private JPanel panelConfigChild2 = new JPanel(); // bottom panel of the panelConfig 
	private JPanel panelConfigChild3 = new JPanel(); //origin table columns to segment
	
	private JTextField newTableNameInput;
	
	
	private JComboBox<String> tablesDataBase;
	
	private JProgressBar progressBar;
	private JButton button;
	private JButton loadSegmentationOptions;
	
	private JLabel infoOldTable;
	private JLabel infoNewTable;
	
	JTable columnsTable;  //colunas disponíveis
	JTable columnsSeg; //colunas usadas para segmentar
	
	JButton addColumn;
	JButton deleteColumn;
	
	private ArrayList<JRadioButton> segmentationOptionsArray = new ArrayList<JRadioButton>();
	
	private String fieldSeg = null;
	
	public SegmentationTrajectoryPanel1(ServiceControl control) {
		super("Segmentação", control, new JButton("rs"));
		this.control = control;
		defineComponents();
		adjustComponents();
		button.addActionListener(this);
		tablesDataBase.addActionListener(this);
		this.addColumn.addActionListener(this);
		this.deleteColumn.addActionListener(this);
		//loadSegmentationOptions.addActionListener(this);
		this.control.persistencia2.addObserver(this);
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
		f.setSize(500,500);
		SegmentationTrajectoryPanel1 panel;
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Persistencia p = new Persistencia("org.postgresql.Driver","jdbc:postgresql://localhost/","taxicab","postgres", "postgres");
		
		panel = new SegmentationTrajectoryPanel1(new ServiceControl(p));
		p.addObserver(panel);
		f.add(panel);
		f.setVisible(true);
	}
	@Override
	public void defineComponents() {
		newTableNameInput = new JTextField("");
		
		addColumn = new JButton(">>");
		deleteColumn = new JButton("<<");
		
		columnsTable = new JTable(0,1);
		columnsSeg   = new JTable(0,1);
		
		progressBar = new JProgressBar(0,100);
		progressBar.setStringPainted(true);
		progressBar.setSize(400, 50);
		button = new JButton("Segmentar");
		
		infoNewTable = new JLabel("Nome da tabela a ser segmentada ");
		infoOldTable = new JLabel("Nome da nova tabela             ");
	}

	@Override
	public void adjustComponents() {
		
		String[] tables = new String[10];
		
		try {
			ArrayList<String> aux = new ArrayList<String>(5);
			aux = control.getTableList();
			tables = new String[aux.size()];
			tables = aux.toArray(tables);
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			JOptionPane.showMessageDialog(null,"Não foi possível coletar as tabelas dessa base","Error", JOptionPane.ERROR_MESSAGE, null);
			e.printStackTrace();
			System.exit(ERROR);
		}
		tablesDataBase = new JComboBox<String>(tables);
		setLayout(new GridLayout(4,1));
		
		panelConfigChild1.setLayout(new GridLayout(2,2,10,10));
		
		panelConfigChild1.add(infoOldTable);
		panelConfigChild1.add(tablesDataBase);
		panelConfigChild1.add(infoNewTable);
		panelConfigChild1.add(newTableNameInput);
		
		JScrollPane sp1 = new JScrollPane(columnsTable);
		JScrollPane sp2 = new JScrollPane(columnsSeg);
		
		JPanel aux = new JPanel();
		aux.setLayout(new GridLayout(2,1));
		aux.add(addColumn);
		aux.add(deleteColumn);
		
		GridLayout gl = new GridLayout(1,3,10,10);
		gl.setHgap(40);
		panelConfigChild2.setLayout(gl);
		panelConfigChild2.add(sp1);
		panelConfigChild2.add(aux);
		panelConfigChild2.add(sp2);
		
		//panelConfigChild3.setLayout(new GridLayout(1,2,10,10));
		panelConfigChild3.add(button);
		panelConfigChild3.add(new JButton("Cancelar"));
		add(panelConfigChild1);
		add(panelConfigChild2);
		add(panelConfigChild3);
		
		JPanel barPanel = new JPanel();
		barPanel.setLayout(new GridLayout(1,1));
		barPanel.add(progressBar);
		add(barPanel);
		
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		
		final ArrayList<String> columnsSegArg = new ArrayList<String>();
		
		if(e.getSource() == button) {
			for(int i = 0; i < columnsSeg.getModel().getRowCount(); i++) {
				 columnsSegArg.add((String) columnsSeg.getModel().getValueAt(i, 0));
			}
			final String newTableName = newTableNameInput.getText().length() == 0 ? null:newTableNameInput.getText();
			final String oldTableName = tablesDataBase.getSelectedItem().toString();
			
			if(newTableName == null || oldTableName == null) {
				JOptionPane.showMessageDialog(null, "Os dois nomes da tabela são necessários");
				return;
			}
			
			if(!control.tableExists(oldTableName)) {
				JOptionPane.showMessageDialog(null, "Tabela não existe");
				return;
			}
			if(control.tableExists(newTableName)) {
				JOptionPane.showMessageDialog(null,"Essa tabela já existe :/","Error", JOptionPane.ERROR_MESSAGE, null);
				newTableNameInput.setText("");
				return;
			}
			/*
			 * User did not choose which columns to segment 
			 * */
			if(columnsSegArg.size() == 0) {
				JOptionPane.showMessageDialog(null,"Escolha pelo menos uma coluna para segmentar","Error", JOptionPane.ERROR_MESSAGE, null);
				return;
			}
			
			try {
				ArrayList<String> fields = new ArrayList<String>();
				ArrayList<String> types   = new ArrayList<String>();
				fields.add("tid");
				fields.add("sid");
				fields.add("start_gid");
				fields.add("final_gid");
				
				types.add("integer");
				types.add("integer");
				types.add("integer");
				types.add("integer");
				control.createTable(newTableName, fields, types);

			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			button.setEnabled(false);
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					try {
						//DBConfig.banco = "taxicab2";
						control.seg(oldTableName, newTableName, columnsSegArg);
						button.setEnabled(true);
						
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (DBConnectionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (AddColumnException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (GetTableColumnsException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
			}).start();
		}
		if(e.getSource() == tablesDataBase) {
			ArrayList<String> options = new ArrayList<String>();
			try {
				options = control.getColumnsName(tablesDataBase.getSelectedItem().toString());
				
				DefaultTableModel model = new DefaultTableModel();
				model.addColumn(tablesDataBase.getSelectedItem().toString());
				for(int i = 0; i < options.size(); i++)
					model.addRow(new Object[]{options.get(i)});
					
				columnsTable.setModel(model);
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			//System.out.println(options);
		}
		if(e.getSource() == addColumn) {
			int index = columnsTable.getSelectedRow();
			
			if(columnsTable.getRowCount() == 0) {
				JOptionPane.showMessageDialog(null,"Não há colunas para serem adicionadas","Error", JOptionPane.ERROR_MESSAGE, null);
				return;
			}
			
			if(index == -1) {
				JOptionPane.showMessageDialog(null,"Selecione uma coluna","Error", JOptionPane.ERROR_MESSAGE, null);
				return;
			}
			String column = (String) columnsTable.getValueAt(index, 0);
			DefaultTableModel model = (DefaultTableModel) columnsTable.getModel();
			model.removeRow(index);
			model = (DefaultTableModel) columnsSeg.getModel();
			model.addRow(new Object[]{column});
		}
		if(e.getSource() == deleteColumn) {
			int index = columnsSeg.getSelectedRow();
			
			if(columnsSeg.getRowCount() == 0) {
				JOptionPane.showMessageDialog(null,"Não há colunas para serem removidas","Error", JOptionPane.ERROR_MESSAGE, null);
				return;
			}
			
			if(index == -1) {
				JOptionPane.showMessageDialog(null,"Selecione uma coluna","Error", JOptionPane.ERROR_MESSAGE, null);
				return;
			}
			String column = (String) columnsSeg.getValueAt(index, 0);
			DefaultTableModel model = (DefaultTableModel) columnsSeg.getModel();
			model.removeRow(index);
			model = (DefaultTableModel) columnsTable.getModel();
			model.addRow(new Object[]{column});
		}
	}
	@Override
	public void update(Observable o, Object arg) {
		
		Integer i = (Integer) arg;
		progressBar.setValue(i.intValue());
		repaint();
	}
	@Override
	public void run() {
		//for(;;) {
		//	repaint();
		//	this.control.persistencia2.addObserver(this);
			//System.out.println(Persistencia.completed);
		//}
		
	}
	public void addListener(){
		
	}
}
