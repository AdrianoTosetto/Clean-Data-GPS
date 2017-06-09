package br.ufsc.src.igu.panel;

import static javax.swing.GroupLayout.Alignment.BASELINE;
import static javax.swing.GroupLayout.Alignment.LEADING;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.DefaultCellEditor;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import br.ufsc.src.control.ServiceControl;
import br.ufsc.src.control.Utils;
import br.ufsc.src.control.dataclean.ConfigTraj;
import br.ufsc.src.control.entities.TPoint;
import br.ufsc.src.control.exception.BrokeTrajectoryException;
import br.ufsc.src.persistencia.exception.AddColumnException;
import br.ufsc.src.persistencia.exception.DBConnectionException;
import br.ufsc.src.persistencia.exception.GetTableColumnsException;

public class TrajNearPointPanel extends AbstractPanel{
	
	private static final long serialVersionUID = 1L;
	private JLabel tableLabel, latitudeLabel, distanceLabel, longitudeLabel;
	private JTextField tableTF, latitudeTF, distanceTF, longitudeTF;
	private JButton tableBtn;
	private JTable table1;
	private JScrollPane table;

	public TrajNearPointPanel(ServiceControl controle) {
		
		super("Data Clean - Trajectories Near a Point", controle, new JButton("Start"));
		defineComponents();
		adjustComponents();
	}

	@Override
	public void defineComponents() {
		
		processButton.setBackground(Color.DARK_GRAY);
		
		tableLabel = new JLabel("Table name");
		tableTF = new JTextField();
		tableBtn = new JButton("Find");
		tableBtn.addActionListener(this);
		
		latitudeLabel = new JLabel("Latitude");
		latitudeTF = new JTextField();
		latitudeTF.setToolTipText("Set latitude in ref 4326");
		
		longitudeLabel = new JLabel("Longitude");
		longitudeTF = new JTextField();
		longitudeTF.setToolTipText("Set longitude in ref 4326");
		
		distanceLabel = new JLabel("Distance in meters");
		distanceTF = new JTextField();
		distanceTF.setToolTipText("Set distance in meters to the buffer around the point");
		
		Object [] columnNames = new Object[]{ "Column", "Type description" };
        Object [][] data        = new Object[][]{};
        
        DefaultTableModel tab = new MyTableModel( data,columnNames, true );
        table1 = new JTable(tab);
        table = new JScrollPane(table1);
        table1.setRowHeight( 25 );
        setUpColumnComboBox(table1, table1.getColumnModel().getColumn(1));
        
	}
	
	@Override
	public void adjustComponents() {
		
		GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);

		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		layout.setHorizontalGroup(layout
				.createSequentialGroup()
				.addGroup(layout.createParallelGroup(LEADING)
						.addGroup(layout.createSequentialGroup()
								.addGroup(layout.createParallelGroup(LEADING)
									.addComponent(tableLabel))
								.addGroup(layout.createParallelGroup(LEADING)
										.addComponent(tableTF))
								.addGroup(layout.createParallelGroup(LEADING)
										.addComponent(tableBtn))
						)
						.addComponent(table)
						.addGroup(layout.createSequentialGroup()
								.addGroup(layout.createParallelGroup(LEADING)
										.addComponent(longitudeLabel))
								.addGroup(layout.createParallelGroup(LEADING)
										.addComponent(longitudeTF))
								.addGroup(layout.createParallelGroup(LEADING)
										.addComponent(latitudeLabel))
								.addGroup(layout.createParallelGroup(LEADING)
										.addComponent(latitudeTF))
								.addGroup(layout.createParallelGroup(LEADING)
										.addComponent(distanceLabel))
								.addGroup(layout.createParallelGroup(LEADING)
										.addComponent(distanceTF))
								.addGroup(layout.createParallelGroup(LEADING)
										.addComponent(processButton))	
						)		
				)
		);

		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(BASELINE)
						.addComponent(tableLabel)
						.addComponent(tableTF)
						.addComponent(tableBtn))
				.addGroup(layout.createParallelGroup(BASELINE)
						.addComponent(table))
				.addGroup(layout.createParallelGroup(BASELINE)
						.addGroup(layout.createParallelGroup(Alignment.CENTER)
								.addComponent(longitudeLabel)
								.addComponent(longitudeTF)
								.addComponent(latitudeLabel)
								.addComponent(latitudeTF)
								.addComponent(distanceLabel)
								.addComponent(distanceTF)
								.addComponent(processButton)
						)
				)	
		);
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == tableBtn){
			try {
				if(tableTF.getText().length() != 0){
					ArrayList<String> columns = control.getTableColumns(tableTF.getText());
					DefaultTableModel model = (DefaultTableModel) table1.getModel();
					for (String col : columns) {
						String cl = isKind(col) ? col.toUpperCase() : null; 
						model.addRow(new Object[]{col,cl,null,null});
					}
				}
			} catch (DBConnectionException e1) {
				JOptionPane.showMessageDialog(null,"DB connection error: "+e1.getMsg(),"Data Clean", JOptionPane.ERROR_MESSAGE);
				return;
			} catch (GetTableColumnsException e1) {
				JOptionPane.showMessageDialog(null,"Error to get columns name: "+e1.getMsg(),"Data Clean", JOptionPane.ERROR_MESSAGE);
				tableTF.requestFocus(true);
				return;
			}
		}else if(e.getSource() == processButton){
			ConfigTraj configTraj = getDataFromWindow();
			if(configTraj != null){
				try {
					long startTime = System.currentTimeMillis();
					boolean movedTrajToNewTable = control.trajNearPoint(configTraj);   
					long endTime   = System.currentTimeMillis();
					long totalTime = endTime - startTime;
					if(movedTrajToNewTable)
						JOptionPane.showMessageDialog(null, "Created a new table and moved trajectories to "+configTraj.getTableNameOrigin()+"_trajsnearpoint \n"+Utils.getDurationBreakdown(totalTime), "Data Clean", JOptionPane.INFORMATION_MESSAGE);
					else
						JOptionPane.showMessageDialog(null, "No data founded \n"+Utils.getDurationBreakdown(totalTime), "Data Clean", JOptionPane.INFORMATION_MESSAGE);
				} catch (DBConnectionException e1) {
					JOptionPane.showMessageDialog(null, "DB connection error: "+e1.getMsg(),
							"Data Clean", JOptionPane.ERROR_MESSAGE);
				} catch (SQLException e1) {
					JOptionPane.showMessageDialog(null, "DB error: "+e1.getMessage(),
							"Data Clean", JOptionPane.ERROR_MESSAGE);
				} 
				clearWindow();
			}
		}
	}
	
	 private ConfigTraj getDataFromWindow() {
		Object[][] tableData = getTableData();
		
		if(Utils.isStringEmpty(tableTF.getText())){
			JOptionPane.showMessageDialog(null,"Table name is empty","Data Clean", JOptionPane.ERROR_MESSAGE);
			tableTF.requestFocus(true);
			return null;
		}
		if(tableData.length == 0){
			JOptionPane.showMessageDialog(null,"You should click Find to list the table's columns","Data Clean", JOptionPane.ERROR_MESSAGE);
			tableBtn.requestFocus(true);
			return null;
	 	}
		
		if(Utils.isStringEmpty(longitudeTF.getText()) || (!Utils.isStringEmpty(longitudeTF.getText()) && !Utils.isNumeric(longitudeTF.getText()))){
			JOptionPane.showMessageDialog(null,"Longitude should be a number [-180 - 180]","Data Clean", JOptionPane.ERROR_MESSAGE);
			longitudeTF.requestFocus(true);
			return null;
		}

		if(Utils.isStringEmpty(latitudeTF.getText()) || (!Utils.isStringEmpty(latitudeTF.getText()) && !Utils.isNumeric(latitudeTF.getText()))){
			JOptionPane.showMessageDialog(null,"Latitude should be a number [-90 - 90]","Data Clean", JOptionPane.ERROR_MESSAGE);
			latitudeTF.requestFocus(true);
			return null;
		}
		
		if(Utils.isStringEmpty(distanceTF.getText()) || (!Utils.isStringEmpty(distanceTF.getText()) && !Utils.isNumeric(distanceTF.getText()))){
			JOptionPane.showMessageDialog(null,"Distance should be a number","Data Clean", JOptionPane.ERROR_MESSAGE);
			distanceTF.requestFocus(true);
			return null;
		}
		
		String log = longitudeTF.getText().replace(",", ".");
		String lat = latitudeTF.getText().replace(",", ".");
		String dist = distanceTF.getText().replace(",", ".");
		
		TPoint point = new TPoint(Double.parseDouble(log), Double.parseDouble(lat));
		double distance = Double.parseDouble(dist);
		String tableName = tableTF.getText();

		ConfigTraj configTrajBroke = new ConfigTraj(tableData, tableName, point, distance);
		
		return configTrajBroke;
	}
	 
	private Object[][] getTableData () {
		DefaultTableModel dtm = (DefaultTableModel) table1.getModel();
		int nRow = dtm.getRowCount(), nCol = dtm.getColumnCount();
		Object[][] tableData = new Object[nRow][nCol];
		for (int i = 0 ; i < nRow ; i++)
			for (int j = 0 ; j < nCol ; j++)
				tableData[i][j] = dtm.getValueAt(i,j);
		return tableData;
	}

	public void setUpColumnComboBox(JTable table, TableColumn column) {
			//Set up the editor for the sport cells.
		 		
			JComboBox comboBox = new JComboBox(getKinds());
			column.setCellEditor(new DefaultCellEditor(comboBox));
			//Set up tool tips for the cells.
			DefaultTableCellRenderer renderer =
			new DefaultTableCellRenderer();
			renderer.setToolTipText("Click for combo box");
			column.setCellRenderer(renderer);
		}

	private String[] getKinds() {
		String[] kinds = new String[] {
				"GID"
				,"TID" 			 	
			 	,"TIMESTAMP"
			 	,"LAT"
			 	,"LON"
			 	,"GEOM"
			 	,"BOOLEAN STATUS"
			 	,"ACCURACY"
			 	,"SPEED"
			 	,""
		};
		return kinds;
	}
	
	private boolean isKind(String kind){
		String[] kinds = getKinds();
		for (String kd : kinds) {
			if(kd.equalsIgnoreCase(kind))
				return true;
		}
		return false;
	}
	
}