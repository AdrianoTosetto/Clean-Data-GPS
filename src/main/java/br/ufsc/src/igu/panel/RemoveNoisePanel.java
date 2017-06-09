package br.ufsc.src.igu.panel;

import static javax.swing.GroupLayout.Alignment.BASELINE;
import static javax.swing.GroupLayout.Alignment.LEADING;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.GroupLayout.Alignment;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import br.ufsc.src.control.ServiceControl;
import br.ufsc.src.control.Utils;
import br.ufsc.src.control.dataclean.ConfigTraj;
import br.ufsc.src.control.exception.BrokeTrajectoryException;
import br.ufsc.src.persistencia.exception.AddColumnException;
import br.ufsc.src.persistencia.exception.DBConnectionException;
import br.ufsc.src.persistencia.exception.GetTableColumnsException;

public class RemoveNoisePanel extends AbstractPanel{
	
	private static final long serialVersionUID = 1L;
	private JLabel tableLabel, speedLabel, minPointsLabel, distancePointsLabel, dbscanLabel, meanMedianFilterLabel, numWindowPointsLabel, bySpeedLabel, t1,t2,t3;
	private JTextField tableTF, speedTF, minPointsTF, distancePointsTF, numWindowPointsTF;
	private JButton tableBtn;
	private JTable table1;
	private JScrollPane table;
	private JRadioButton fromFirst, fromSecondLookingBackward, dbscanRB, meanFilterRB, medianFilterRB;
	private JCheckBox pastPointsJC, removeNeighborNoiseJC;
	private JSeparator sep1,sep2;

	public RemoveNoisePanel(ServiceControl controle) {
		
		super("Data Clean - Remove Noise", controle, new JButton("Start"));
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
		
		tableTF.setText("traj_ricardo3");
		
		fromFirst = new JRadioButton("From First Looking Forward");
		fromSecondLookingBackward = new JRadioButton("From Second Looking Backward");
		dbscanRB = new JRadioButton("DBSCAN");
		meanFilterRB = new JRadioButton("Mean Filter");
		medianFilterRB = new JRadioButton("Median Filter");
		ButtonGroup group = new ButtonGroup();
		group.add(fromFirst);
		group.add(fromSecondLookingBackward);
		group.add(dbscanRB);
		group.add(meanFilterRB);
		group.add(medianFilterRB);
		
		speedLabel = new JLabel("Ignore speed up in m/s");
		speedTF = new JTextField();
		
		dbscanLabel = new JLabel(" DBSCAN ");
		dbscanLabel.setFont(new Font(null,Font.BOLD, 12));
		minPointsLabel = new JLabel("Minimum Points");
		minPointsTF = new JTextField();
		minPointsTF.setToolTipText("Minimum points to DBSCAN");
		distancePointsLabel = new JLabel("Distance points");
		distancePointsTF = new JTextField();
		distancePointsTF.setToolTipText("Distance between points in meters");
		removeNeighborNoiseJC = new JCheckBox("Remove neighbor and noise");
		bySpeedLabel = new JLabel("By Speed");
		bySpeedLabel.setFont(new Font(null,Font.BOLD, 12));
		
		meanMedianFilterLabel = new JLabel(" Mean/Median Filter ");
		meanMedianFilterLabel.setFont(new Font(null,Font.BOLD, 12));
		numWindowPointsLabel = new JLabel("Number window points");
		numWindowPointsTF = new JTextField();
		pastPointsJC = new JCheckBox("Only past points");
		
		Object [] columnNames = new Object[]{ "Column", "Type description" };
        Object [][] data        = new Object[][]{};
        
        DefaultTableModel tab = new MyTableModel( data,columnNames, true );
        table1 = new JTable(tab);
        table = new JScrollPane(table1);
        table1.setRowHeight( 25 );
        setUpColumnComboBox(table1, table1.getColumnModel().getColumn(1));
        
        sep1 = new JSeparator(SwingConstants.HORIZONTAL);
		sep2 = new JSeparator(SwingConstants.HORIZONTAL);
		t1 = new JLabel("");
		t2 = new JLabel("");
		t3 = new JLabel("");
	}
	
	@Override
	public void adjustComponents() {
		
		GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);

		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		layout.setHorizontalGroup(layout.createSequentialGroup()
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
					.addGroup(layout.createParallelGroup(LEADING)
							.addComponent(bySpeedLabel))
					.addGroup(layout.createSequentialGroup()
						.addGroup(layout.createParallelGroup(LEADING)
								.addComponent(speedLabel, 0, GroupLayout.DEFAULT_SIZE, 140))
						.addGroup(layout.createParallelGroup(LEADING)
								.addComponent(speedTF, 0, GroupLayout.DEFAULT_SIZE, 40))
						.addGroup(layout.createParallelGroup(LEADING)
								.addComponent(fromFirst)))
					.addGroup(layout.createSequentialGroup()
						.addGroup(layout.createParallelGroup(LEADING)
								.addComponent(t1, 0, GroupLayout.DEFAULT_SIZE, 185))
						.addGroup(layout.createParallelGroup(LEADING)
								.addComponent(fromSecondLookingBackward)))
					.addGroup(layout.createParallelGroup(LEADING)
							.addComponent(sep1))
					.addGroup(layout.createParallelGroup(LEADING)
							.addComponent(dbscanLabel))
					.addGroup(layout.createSequentialGroup()
						.addGroup(layout.createParallelGroup(LEADING)
								.addComponent(minPointsLabel, 0, GroupLayout.DEFAULT_SIZE, 140))
						.addGroup(layout.createParallelGroup(LEADING)
								.addComponent(minPointsTF, 0, GroupLayout.DEFAULT_SIZE, 40))
						.addGroup(layout.createParallelGroup(LEADING)
								.addComponent(dbscanRB)))
					.addGroup(layout.createSequentialGroup()
						.addGroup(layout.createParallelGroup(LEADING)
								.addComponent(distancePointsLabel, 0, GroupLayout.DEFAULT_SIZE, 140))
						.addGroup(layout.createParallelGroup(LEADING)
								.addComponent(distancePointsTF, 0, GroupLayout.DEFAULT_SIZE, 40)))
					.addGroup(layout.createParallelGroup(LEADING)
							.addComponent(removeNeighborNoiseJC))
					.addGroup(layout.createParallelGroup(LEADING)
							.addComponent(sep2))
					.addGroup(layout.createParallelGroup(LEADING)
							.addComponent(meanMedianFilterLabel))
					.addGroup(layout.createSequentialGroup()
						.addGroup(layout.createParallelGroup(LEADING)
								.addComponent(numWindowPointsLabel, 0, GroupLayout.DEFAULT_SIZE, 140))
						.addGroup(layout.createParallelGroup(LEADING)
								.addComponent(numWindowPointsTF, 0, GroupLayout.DEFAULT_SIZE, 40))
						.addGroup(layout.createParallelGroup(LEADING)
								.addComponent(meanFilterRB)))
					.addGroup(layout.createSequentialGroup()
						.addGroup(layout.createParallelGroup(LEADING)
								.addComponent(pastPointsJC))
						.addGroup(layout.createParallelGroup(LEADING)
								.addComponent(t2, 0, GroupLayout.DEFAULT_SIZE, 65))
						.addGroup(layout.createParallelGroup(LEADING)
								.addComponent(medianFilterRB)))
					.addGroup(layout.createSequentialGroup()
						.addGroup(layout.createParallelGroup(LEADING)
								.addComponent(t3, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
						.addGroup(layout.createParallelGroup(LEADING)
								.addComponent(processButton)))
					));

		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(BASELINE)
						.addComponent(tableLabel)
						.addComponent(tableTF)
						.addComponent(tableBtn))
				.addGroup(layout.createParallelGroup(BASELINE)
						.addComponent(table))
				.addGroup(layout.createParallelGroup(BASELINE)
						.addComponent(bySpeedLabel))
				.addGroup(layout.createParallelGroup(BASELINE)
						.addComponent(speedLabel)
						.addComponent(speedTF)
						.addComponent(fromFirst))
				.addGroup(layout.createParallelGroup(BASELINE)
							.addComponent(t1)
							.addComponent(fromSecondLookingBackward))
				.addGroup(layout.createParallelGroup(BASELINE)
							.addComponent(sep1))
				.addGroup(layout.createParallelGroup(BASELINE)
							.addComponent(dbscanLabel))
				.addGroup(layout.createParallelGroup(BASELINE)
							.addComponent(minPointsLabel)
							.addComponent(minPointsTF)
							.addComponent(dbscanRB))
				.addGroup(layout.createParallelGroup(BASELINE)
							.addComponent(distancePointsLabel)
							.addComponent(distancePointsTF))
				.addGroup(layout.createParallelGroup(BASELINE)
							.addComponent(removeNeighborNoiseJC))
				.addGroup(layout.createParallelGroup(BASELINE)
							.addComponent(sep2))
				.addGroup(layout.createParallelGroup(BASELINE)
							.addComponent(meanMedianFilterLabel))
				.addGroup(layout.createParallelGroup(BASELINE)
							.addComponent(numWindowPointsLabel)
							.addComponent(numWindowPointsTF)
							.addComponent(meanFilterRB))
				.addGroup(layout.createParallelGroup(BASELINE)
							.addComponent(pastPointsJC)
							.addComponent(t2)
							.addComponent(medianFilterRB))
				.addGroup(layout.createParallelGroup(BASELINE)
							.addComponent(t3)
							.addComponent(processButton))
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
					control.removeNoise(configTraj);   
					long endTime   = System.currentTimeMillis();
					long totalTime = endTime - startTime;
					JOptionPane.showMessageDialog(null, "Remove Noise \n"+Utils.getDurationBreakdown(totalTime),
							"Data Clean",
							JOptionPane.INFORMATION_MESSAGE);
				} catch (Exception e1) {
					JOptionPane.showMessageDialog(null, "DB connection error: "+e1.getMessage(),
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
		
		if(!(fromFirst.isSelected() || fromSecondLookingBackward.isSelected() || dbscanRB.isSelected() || meanFilterRB.isSelected() || medianFilterRB.isSelected())){
			JOptionPane.showMessageDialog(null,"A method should be selected","Data Clean", JOptionPane.ERROR_MESSAGE);
			return null;
		}
	
		if((fromFirst.isSelected() || fromSecondLookingBackward.isSelected()) && !Utils.isStringEmpty(speedTF.getText()) && !Utils.isNumeric(speedTF.getText())){
			JOptionPane.showMessageDialog(null,"Speed should be a number","Data Clean", JOptionPane.ERROR_MESSAGE);
			speedTF.setText("");
			speedTF.requestFocus(true);
			return null;
		}
		
		if((fromFirst.isSelected() || fromSecondLookingBackward.isSelected()) && Utils.isStringEmpty(speedTF.getText())){
			JOptionPane.showMessageDialog(null,"You should set a speed value","Data Clean", JOptionPane.ERROR_MESSAGE);
			speedTF.requestFocus(true);
			return null;
		}
		
		if(dbscanRB.isSelected() && (Utils.isStringEmpty(minPointsTF.getText()) || !Utils.isNumeric(minPointsTF.getText()))){
			JOptionPane.showMessageDialog(null,"You should set a NUMBER to minimum value of points","Data Clean", JOptionPane.ERROR_MESSAGE);
			minPointsTF.setText("");
			minPointsTF.requestFocus(true);
			return null;
		}
		
		if(dbscanRB.isSelected() && (Utils.isStringEmpty(distancePointsTF.getText()) || !Utils.isNumeric(distancePointsTF.getText()))){
			JOptionPane.showMessageDialog(null,"You should set a NUMBER to distance between points","Data Clean", JOptionPane.ERROR_MESSAGE);
			distancePointsTF.setText("");
			distancePointsTF.requestFocus(true);
			return null;
		}
		
		if((meanFilterRB.isSelected() || medianFilterRB.isSelected()) && (Utils.isStringEmpty(numWindowPointsTF.getText()) || !Utils.isNumeric(numWindowPointsTF.getText()) || Integer.parseInt(numWindowPointsTF.getText()) < 2)){
			JOptionPane.showMessageDialog(null,"You should set a NUMBER bigger than 1 to the window points","Data Clean", JOptionPane.ERROR_MESSAGE);
			numWindowPointsTF.setText("");
			numWindowPointsTF.requestFocus(true);
			return null;
		}
		
		String tableName = tableTF.getText();

		String speed = Utils.isNumeric(speedTF.getText()) ? speedTF.getText() : null;
		int minPoints = Utils.isNumeric(minPointsTF.getText()) ? Integer.parseInt(minPointsTF.getText()) : 0;
		double distPoints = Utils.isNumeric(distancePointsTF.getText()) ? Double.parseDouble(distancePointsTF.getText()) : 0;
		int numWindowPoints = Utils.isNumeric(numWindowPointsTF.getText()) ? Integer.parseInt(numWindowPointsTF.getText()) : 0;
		
		ConfigTraj configTraj= new ConfigTraj(tableData, tableName);
		configTraj.setRemoveNoiseFromFirst(fromFirst.isSelected());
		configTraj.setRemoveNoiseFromSecond(fromSecondLookingBackward.isSelected());
		configTraj.setDbscan(dbscanRB.isSelected());
		configTraj.setRemoveNeighborNoise(removeNeighborNoiseJC.isSelected());
		configTraj.setMeanFilter(meanFilterRB.isSelected());
		configTraj.setMedianFilter(medianFilterRB.isSelected());
		configTraj.setPastPoints(pastPointsJC.isSelected());
		
		configTraj.setNumWindowPoints(numWindowPoints);
		configTraj.setSpeed(speed);
		configTraj.setMinPoints(minPoints);
		configTraj.setDistancePoints(distPoints);
		
		return configTraj;
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