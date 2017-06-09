package br.ufsc.src.igu.panel;

import static javax.swing.GroupLayout.Alignment.BASELINE;
import static javax.swing.GroupLayout.Alignment.LEADING;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Dialog.ModalityType;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.AbstractButton;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import org.jdesktop.swingx.prompt.PromptSupport;

import br.ufsc.src.control.ServiceControl;
import br.ufsc.src.control.Utils;
import br.ufsc.src.persistencia.exception.AddBatchException;
import br.ufsc.src.persistencia.exception.CreateSequenceException;
import br.ufsc.src.persistencia.exception.CreateStatementException;
import br.ufsc.src.persistencia.exception.CreateTableException;
import br.ufsc.src.persistencia.exception.DBConnectionException;
import br.ufsc.src.persistencia.exception.ExecuteBatchException;
import br.ufsc.src.persistencia.exception.FileNFoundException;
import br.ufsc.src.persistencia.exception.GetSequenceException;
import br.ufsc.src.persistencia.exception.LoadDataFileException;
import br.ufsc.src.persistencia.exception.SyntaxException;
import br.ufsc.src.persistencia.exception.TimeStampException;
import br.ufsc.src.persistencia.exception.UpdateGeomException;
import br.ufsc.src.persistencia.fonte.Diretorio;
import br.ufsc.src.persistencia.fonte.TrajetoriaBruta;


public class LoadPanelDSV extends AbstractPanel {

	private static final long serialVersionUID = 1L;
	private JLabel directoryLabel, igLineLabel, delimiterLabel,
			formatDateLabel, formatTimeLabel, igFilesLabel, extLabel,
			igDirLabel, sridCurrentLabel, sridNewLabel, tableNameLabel,t1,t2,t3,t4,t5,t6,t7,t8;
	private JTextField directoryTf, igLineTf, delimiterTf, formatDateTf,
			igFilesTf, igDirTf, extTf, formatTimeTf, tableNameTf, sridCurrentlTf,
			sridNewTf, newColumnTf, positionTf, typeSizeTf ;
	private JButton directoryBtn, addColumnBtn;
	private JCheckBox addMetadata, igExt, tid, gid;
	private JTable table1;
	private JScrollPane table;
	private JComboBox typesCb;
	
	public LoadPanelDSV(ServiceControl controle) {
		super("Load DSV files", controle, new JButton("Load"));
		defineComponents();
		adjustComponents();
	}

	public void defineComponents() {
		directoryLabel = new JLabel("Directory/File");
		igLineLabel = new JLabel("Lines to ignore");
		delimiterLabel = new JLabel("Delimiter");
		formatDateLabel = new JLabel("Format date");
		formatTimeLabel = new JLabel("Format time");
		sridCurrentLabel = new JLabel("SRID current");
		sridNewLabel = new JLabel("SRID new");
		tableNameLabel = new JLabel("Table name");
		igFilesLabel = new JLabel("Ignore files");
		extLabel = new JLabel("Extensions");
		igDirLabel = new JLabel("Ignore directories");

		directoryTf = new JTextField();
		igLineTf = new JTextField();
		igLineTf.setSize(getMinimumSize());
		delimiterTf = new JTextField();
		delimiterTf.setSize(getMinimumSize());
		formatDateTf = new JTextField();
		formatTimeTf = new JTextField();
		tableNameTf = new JTextField();
		sridCurrentlTf = new JTextField();
		sridNewTf = new JTextField();
		igFilesTf = new JTextField();
		igDirTf = new JTextField();
		extTf = new JTextField();
		newColumnTf = new JTextField();
		positionTf = new JTextField();
		typeSizeTf = new JTextField();
		
		t1 = new JLabel("");
		t2 = new JLabel("");
		t3 = new JLabel("");
		t4 = new JLabel("");
		t5 = new JLabel("");
		t6 = new JLabel("");
		t7 = new JLabel("");
		t8 = new JLabel("");
		
		directoryBtn = new JButton("Select");
		addColumnBtn = new JButton("Add line");

		PromptSupport.setPrompt("Column name", newColumnTf);
		PromptSupport.setPrompt("Pos. in file", positionTf);
		PromptSupport.setPrompt("Size", typeSizeTf);
		typeSizeTf.setHorizontalAlignment(JLabel.CENTER);

		typesCb = new JComboBox<>(getTypes());
		typesCb.setRenderer(new MyComboBoxRenderer("TYPE"));
		typesCb.setSelectedIndex(-1);
		((JLabel)typesCb.getRenderer()).setHorizontalAlignment(JLabel.CENTER);
		addMetadata = new JCheckBox("Add Metadata");
		igExt = new JCheckBox("Ignore");
		tid = new JCheckBox("Generate TID");
		gid = new JCheckBox("Generate GID");
		
		
		
		Object [] columnNames = new Object[]{ "Column", "Pos. in file", "Type", "Size" };
        Object [][] data        = new Object[][]{ {"date",     "", EnumTypes.CHARACTERVARYING.toString(),""} //TODO testes
        											, {"time", "", EnumTypes.CHARACTERVARYING.toString(),""}
        											, {"lat",  "", EnumTypes.NUMERIC.toString(),""}
        											, {"lon",  "", EnumTypes.NUMERIC.toString(), ""}
        											, {"timestamp", "", EnumTypes.TIMESTAMP.toString(), ""}
        											, {"geom", "", EnumTypes.POINT.toString(), ""} 
        										};
        DefaultTableModel tab = new MyTableModel( data,columnNames, true );
        table1 = new JTable(tab);
        table = new JScrollPane(table1);
        table1.setRowHeight( 25 );
        setUpColumnComboBox(table1, table1.getColumnModel().getColumn(2));
		

		
		extTf.setText("pdf,zip,gpx,kml,wkt,json");
		igExt.setSelected(true);

		addColumnBtn.addActionListener(this);
		directoryBtn.addActionListener(this);

		processButton.setBackground(Color.DARK_GRAY);
		directoryBtn
				.setToolTipText("Click to select directory/file");
		processButton.setToolTipText("Click to load");
		
		igLineTf.setText("1");
		delimiterTf.setText(",");
		directoryTf.setText("/Users/rogerjames/Desktop/trucks_rev_pos_teste.txt");
		sridCurrentlTf.setText("2100");
		sridNewTf.setText("900913");
		formatDateTf.setText("yyyy-MM-dd HH:mm:ss");
	}

	public void adjustComponents() {
		GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);

		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(LEADING)
						.addGroup(layout.createSequentialGroup()
							.addGroup(layout.createParallelGroup(LEADING)
									.addComponent(directoryLabel, 0, GroupLayout.DEFAULT_SIZE, 100))
							.addGroup(layout.createParallelGroup(LEADING)
								.addComponent(directoryTf))
							.addGroup(layout.createParallelGroup(LEADING)
								.addComponent(directoryBtn)))
						.addGroup(layout.createSequentialGroup()
								.addGroup(layout.createParallelGroup(LEADING)
									.addComponent(formatDateLabel, 0, GroupLayout.DEFAULT_SIZE, 100))
								.addGroup(layout.createParallelGroup(LEADING)
									.addComponent(formatDateTf, 0, GroupLayout.DEFAULT_SIZE, 170))
								.addGroup(layout.createParallelGroup(LEADING)
										.addComponent(t2, 0, GroupLayout.DEFAULT_SIZE, 25))
								.addGroup(layout.createParallelGroup(LEADING)
									.addComponent(formatTimeLabel, 0, GroupLayout.DEFAULT_SIZE, 70))
								.addGroup(layout.createParallelGroup(LEADING)
									.addComponent(formatTimeTf, 0, GroupLayout.DEFAULT_SIZE, 80))
								.addGroup(layout.createParallelGroup(LEADING)
										.addComponent(delimiterLabel, 0, GroupLayout.DEFAULT_SIZE, 85))
									.addGroup(layout.createParallelGroup(LEADING)
										.addComponent(delimiterTf, 0, GroupLayout.DEFAULT_SIZE, 40)))		
						.addGroup(layout.createSequentialGroup()
								.addGroup(layout.createParallelGroup(LEADING)
									.addComponent(sridCurrentLabel, 0, GroupLayout.DEFAULT_SIZE, 100))
								.addGroup(layout.createParallelGroup(LEADING)
									.addComponent(sridCurrentlTf, 0, GroupLayout.DEFAULT_SIZE, 70))
								.addGroup(layout.createParallelGroup(LEADING)
										.addComponent(t3, 0, GroupLayout.DEFAULT_SIZE, 125))
								.addGroup(layout.createParallelGroup(LEADING)
									.addComponent(sridNewLabel, 0, GroupLayout.DEFAULT_SIZE, 70))
								.addGroup(layout.createParallelGroup(LEADING)
									.addComponent(sridNewTf, 0, GroupLayout.DEFAULT_SIZE, 70))
								.addGroup(layout.createParallelGroup(LEADING)
										.addComponent(t4, 0, GroupLayout.DEFAULT_SIZE, 5))
								.addGroup(layout.createParallelGroup(LEADING)
									.addComponent(igLineLabel))
								.addGroup(layout.createParallelGroup(LEADING)
									.addComponent(igLineTf, 0, GroupLayout.DEFAULT_SIZE, 40)))
						.addGroup(layout.createSequentialGroup()
								.addGroup(layout.createParallelGroup(LEADING)
									.addComponent(igDirLabel))
								.addGroup(layout.createParallelGroup(LEADING)
									.addComponent(igDirTf, 0, GroupLayout.DEFAULT_SIZE, 200))
								.addGroup(layout.createParallelGroup(LEADING)
									.addComponent(igFilesLabel, 0, GroupLayout.DEFAULT_SIZE, 70))
								.addGroup(layout.createParallelGroup(LEADING)
									.addComponent(igFilesTf, 0, GroupLayout.DEFAULT_SIZE, 200)))
						.addGroup(layout.createSequentialGroup()
								.addGroup(layout.createParallelGroup(LEADING)
									.addComponent(extLabel, 0, GroupLayout.DEFAULT_SIZE, 100))
								.addGroup(layout.createParallelGroup(LEADING)
									.addComponent(extTf, 0, GroupLayout.DEFAULT_SIZE, 200))
								.addGroup(layout.createParallelGroup(LEADING)
									.addComponent(igExt)))
						.addGroup(layout.createSequentialGroup()
								.addGroup(layout.createParallelGroup(LEADING)
									.addComponent(tableNameLabel, 0, GroupLayout.DEFAULT_SIZE, 100))
								.addGroup(layout.createParallelGroup(LEADING)
									.addComponent(tableNameTf, 0, GroupLayout.DEFAULT_SIZE, 300)))
						.addGroup(layout.createSequentialGroup()
								.addGroup(layout.createParallelGroup(LEADING)
										.addComponent(t5, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
								.addGroup(layout.createParallelGroup(LEADING)
									.addComponent(addMetadata))
								.addGroup(layout.createParallelGroup(LEADING)
										.addComponent(t6, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
								.addGroup(layout.createParallelGroup(LEADING)
									.addComponent(gid))
								.addGroup(layout.createParallelGroup(LEADING)
										.addComponent(t7, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
								.addGroup(layout.createParallelGroup(LEADING)
									.addComponent(tid))
								.addGroup(layout.createParallelGroup(LEADING)
										.addComponent(t8, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
						.addComponent(table)
						.addGroup(layout.createSequentialGroup()
								.addGroup(layout.createParallelGroup(LEADING)
									.addComponent(newColumnTf))
								.addGroup(layout.createParallelGroup(LEADING)
									.addComponent(positionTf, 0, GroupLayout.DEFAULT_SIZE, 70))
								.addGroup(layout.createParallelGroup(LEADING)
									.addComponent(typesCb, 0, GroupLayout.DEFAULT_SIZE, 200))
								.addGroup(layout.createParallelGroup(LEADING)
										.addComponent(typeSizeTf, 0, GroupLayout.DEFAULT_SIZE, 40))
								.addGroup(layout.createParallelGroup(LEADING)
										.addComponent(addColumnBtn)))
						.addGroup(layout.createSequentialGroup()
								.addGroup(layout.createParallelGroup(LEADING)
										.addComponent(t1, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
								.addGroup(layout.createParallelGroup(LEADING)
										.addComponent(processButton)))
				));

		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(BASELINE)
						.addComponent(directoryLabel)
						.addComponent(directoryTf)
						.addComponent(directoryBtn))
				.addGroup(layout.createParallelGroup(BASELINE)
						.addComponent(formatDateLabel)
						.addComponent(formatDateTf)
						.addComponent(t2)
						.addComponent(formatTimeLabel)
						.addComponent(formatTimeTf)
						.addComponent(delimiterLabel)
						.addComponent(delimiterTf))
				.addGroup(layout.createParallelGroup(BASELINE)
						.addComponent(sridCurrentLabel)
						.addComponent(sridCurrentlTf)
						.addComponent(t3)
						.addComponent(sridNewLabel)
						.addComponent(sridNewTf)
						.addComponent(t4)
						.addComponent(igLineLabel)
						.addComponent(igLineTf))
				.addGroup(layout.createParallelGroup(BASELINE)
						.addComponent(igDirLabel)
						.addComponent(igDirTf)
						.addComponent(igFilesLabel)
						.addComponent(igFilesTf))
				.addGroup(layout.createParallelGroup(BASELINE)
						.addComponent(extLabel)
						.addComponent(extTf)
						.addComponent(igExt))
				.addGroup(layout.createParallelGroup(BASELINE)
						.addComponent(tableNameLabel)
						.addComponent(tableNameTf))
				.addGroup(layout.createParallelGroup(BASELINE)
						.addComponent(t5)
						.addComponent(addMetadata)
						.addComponent(t6)
						.addComponent(gid)
						.addComponent(t7)
						.addComponent(tid)
						.addComponent(t8))
				.addGroup(layout.createParallelGroup(BASELINE)
						.addComponent(table))
				.addGroup(layout.createParallelGroup(BASELINE)
						.addComponent(newColumnTf)
						.addComponent(positionTf)
						.addComponent(typesCb)
						.addComponent(typeSizeTf)
						.addComponent(addColumnBtn))
				.addGroup(layout.createParallelGroup(BASELINE)
						.addComponent(t1)
						.addComponent(processButton))
				);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == directoryBtn) {
			JFileChooser fc = new JFileChooser();
			fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			fc.setAcceptAllFileFilterUsed(true);
			int returnVal = fc.showOpenDialog(null);
			if (returnVal == JFileChooser.APPROVE_OPTION)
				directoryTf.setText(fc.getSelectedFile().getAbsolutePath());
		}else if(e.getSource() == addColumnBtn){
			if(newColumnTf.getText().length() == 0){
				JOptionPane.showMessageDialog(null,
						"Column name is empty",
						"Load file", JOptionPane.ERROR_MESSAGE);
				newColumnTf.requestFocus(true);
				return;
			}
			int inColPos, inTypeSize;
			try{
				inColPos = Integer.parseInt(positionTf.getText()); 
			}catch(NumberFormatException ex){
				JOptionPane.showMessageDialog(null,
						"Not a number",
						"Load file", JOptionPane.ERROR_MESSAGE);
				positionTf.requestFocus(true);
				return;
			}
			
			if((String)typesCb.getSelectedItem() == null){
				JOptionPane.showMessageDialog(null,
						"Choose a type",
						"Load file", JOptionPane.ERROR_MESSAGE);
				typesCb.requestFocus(true);
				return;
			}

			try{
				if(typeSizeTf.getText().length() > 0){
					inTypeSize = Integer.parseInt(typeSizeTf.getText());
					if(!(inTypeSize > 0) ){
						JOptionPane.showMessageDialog(null,
								"Number should be greater than 0",
								"Load file", JOptionPane.ERROR_MESSAGE);
						typeSizeTf.requestFocus(true);
						return;
					}
				}
				
			}catch(NumberFormatException ex){
				JOptionPane.showMessageDialog(null,
						"Not a number",
						"Load file", JOptionPane.ERROR_MESSAGE);
				typeSizeTf.requestFocus(true);
				return;
			}
			
				
			DefaultTableModel model = (DefaultTableModel) table1.getModel();
			model.addRow(new Object[]{newColumnTf.getText()
										,positionTf.getText()
										,(String)typesCb.getSelectedItem()
										,typeSizeTf.getText()});
			
			DefaultComboBoxModel model1 = new DefaultComboBoxModel( getTypes() );
			typesCb.setModel( model1 );
			typesCb.setRenderer(new MyComboBoxRenderer("TYPE"));
			typesCb.setSelectedIndex(-1);
			((JLabel)typesCb.getRenderer()).setHorizontalAlignment(JLabel.CENTER);
			newColumnTf.setText("");
			positionTf.setText("");
			typeSizeTf.setText("");
		
		}else if (e.getSource() == processButton) {
			if (!control.testConnection())
				JOptionPane.showMessageDialog(null, "Error to connect to database",
						"DB connection", JOptionPane.ERROR_MESSAGE);
			else {	
				if (verificaEntradas()) {
					String inDir = directoryTf.getText();
					int inNlineig = Integer.parseInt(igLineTf.getText());
					String inDelimiter = delimiterTf.getText();
					String inFormatDate = formatDateTf.getText();
					String inFormatTime = formatTimeTf.getText();
					String inTable = tableNameTf.getText();
					boolean inMetadata = addMetadata.isSelected();
					boolean inGID = gid.isSelected();
					boolean inTID = tid.isSelected();
					Object[][] inTableData = getTableData(table1);
					int inSRIDCurrent, inSRIDNovo = 0;
					try{
						inSRIDCurrent = Integer.parseInt(sridCurrentlTf.getText());
					}catch(NumberFormatException ex){
						JOptionPane.showMessageDialog(null,
								"SRID not a number, SRID should be only numbers",
								"Load file", JOptionPane.ERROR_MESSAGE);
						sridCurrentlTf.requestFocus(true);
						return;
					}
					if(sridNewTf.getText().length() != 0){
						try{
							inSRIDNovo = Integer.parseInt(sridNewTf.getText());
						}catch(NumberFormatException ex){
							JOptionPane.showMessageDialog(null,
									"SRID not a number, SRID should be only numbers",
									"Load file", JOptionPane.ERROR_MESSAGE);
							sridNewTf.requestFocus(true);
							return;
						}
					}else
						inSRIDNovo = inSRIDCurrent;
					
					String inExt = (extTf.getText().length() > 0 ) ? extTf.getText() : null;
					boolean inIgExt = igExt.isSelected();
					String inIgDir = (igDirTf.getText().length() > 0) ? igDirTf.getText() : null;
					String inIgFil = (igFilesTf.getText().length() > 0) ? igFilesTf.getText() : null;

					TrajetoriaBruta tb = new TrajetoriaBruta(inNlineig, inDelimiter, inFormatDate, inFormatTime, inTable, inSRIDCurrent, inSRIDNovo, inMetadata, inTableData, inGID, inTID);
					Diretorio dir = definicoesDiretorio(inDir, inExt, inIgExt, inIgDir, inIgFil);
					
					try {
						control.createTable(tb);
					} catch (SyntaxException e1){
						JOptionPane.showMessageDialog(null,e1.getMsg(),"Loading data", JOptionPane.ERROR_MESSAGE);
						tableNameTf.requestFocus(true);
						return;
					} catch (CreateTableException e1) {
						JOptionPane.showMessageDialog(null,"Error creating table: "+e1.getMsg(),"Loading data", JOptionPane.ERROR_MESSAGE);
						tableNameTf.requestFocus(true);
						return;
					} catch (DBConnectionException e1) {
						JOptionPane.showMessageDialog(null,"Error connecting to DB: "+e1.getMsg(),"Loading data", JOptionPane.ERROR_MESSAGE);
						tableNameTf.requestFocus(true);
						return;
					}
					
					
					try {
						long startTime = System.currentTimeMillis();
						control.loadData(tb, dir);   
						long endTime   = System.currentTimeMillis();
						long totalTime = endTime - startTime;
						JOptionPane.showMessageDialog(null, "Data loadaded \n"+Utils.getDurationBreakdown(totalTime),
								"Loading data",
								JOptionPane.INFORMATION_MESSAGE);
						clearWindow();
					} catch (TimeStampException e1) {
						JOptionPane.showMessageDialog(null, "Error converting timestamp: "+e1.getMsg(),
								"Loading data", JOptionPane.ERROR_MESSAGE);
					} catch (LoadDataFileException e1) {
						JOptionPane.showMessageDialog(null, "Error loading file: "+e1.getMsg(),
								"Loading data", JOptionPane.ERROR_MESSAGE);
					} catch (GetSequenceException e1) {
						JOptionPane.showMessageDialog(null, "Error getting sequence: "+e1.getMsg(),
								"Loading data", JOptionPane.ERROR_MESSAGE);
					} catch (CreateSequenceException e1) {
						JOptionPane.showMessageDialog(null, "Error creating sequence: "+e1.getMsg(),
								"Loading data", JOptionPane.ERROR_MESSAGE);
					} catch (UpdateGeomException e1) {
						JOptionPane.showMessageDialog(null, "Error updating geom: "+e1.getMsg(),
								"Loading data", JOptionPane.ERROR_MESSAGE);
					} catch (CreateStatementException e1) {
						JOptionPane.showMessageDialog(null, "Error creating statement: "+e1.getMsg(),
								"Loading data", JOptionPane.ERROR_MESSAGE);
					} catch (AddBatchException e1) {
						JOptionPane.showMessageDialog(null, "Error adding batch: "+e1.getMsg(),
								"Loading data", JOptionPane.ERROR_MESSAGE);
					} catch (FileNFoundException e1) {
						JOptionPane.showMessageDialog(null, "Error finding file: "+e1.getMsg(),
								"Loading data", JOptionPane.ERROR_MESSAGE);
					} catch (ExecuteBatchException e1) {
						JOptionPane.showMessageDialog(null, "Error executing batch: "+e1.getMsg(),
								"Loading data", JOptionPane.ERROR_MESSAGE);
					} catch (DBConnectionException e1) {
						JOptionPane.showMessageDialog(null,"Error connecting to DB: "+e1.getMsg(),"Loading data", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		}
	}

	private Diretorio definicoesDiretorio(String url, String inExt, boolean inIgExt,
			String inIgDir, String inIgArq) {
		Diretorio dir = new Diretorio();
		String igExt[] = (inExt == null) ? new String[0] : inExt.split(",");
		String igDir[] = (inIgDir == null) ? new String[0] : inIgDir.split(",");
		String igArq[] = (inIgArq == null) ? new String[0] : inIgArq.split(",");
		dir.setUrl(url);
		dir.setExtension(new ArrayList<String>(Arrays.asList(igExt)));
		dir.setIgFolder(new ArrayList<String>(Arrays.asList(igDir)));
		dir.setIgFile(new ArrayList<String>(Arrays.asList(igArq)));
		dir.setIgExtension(inIgExt);
		return dir;
	}

	private boolean verificaEntradas() {
		if (directoryTf.getText().length() == 0) {
			JOptionPane.showMessageDialog(null,
					"Enter a directory or file to be load",
					"Load file", JOptionPane.ERROR_MESSAGE);
			directoryTf.requestFocus(true);
			return false;
		} else if (igLineTf.getText().length() == 0) {
			JOptionPane
					.showMessageDialog(
							null,
							"Enter a number of lines to be ignore in the file head",
							"Carregar documento", JOptionPane.ERROR_MESSAGE);
			igLineTf.requestFocus(true);
			return false;
		} else if (delimiterTf.getText().length() == 0) {
			JOptionPane
					.showMessageDialog(
							null,
							"Enter a delimiter character that split the columns",
							"Load file", JOptionPane.ERROR_MESSAGE);
			delimiterTf.requestFocus(true);
			return false;
		} else if (formatDateTf.getText().length() == 0 && formatTimeTf.getText().length() != 0) {
			JOptionPane.showMessageDialog(null,
					"Enter with the format of time (hour and minutes)",
					"Load file", JOptionPane.ERROR_MESSAGE);
			formatDateTf.requestFocus(true);
			return false;
		}else if (tableNameTf.getText().length() == 0) {
			JOptionPane
					.showMessageDialog(
							null,
							"Enter the table name where to put the data",
							"Load file", JOptionPane.ERROR_MESSAGE);
			tableNameTf.requestFocus(true);
			return false;
		} else if (sridCurrentlTf.getText().length() == 0) {
			JOptionPane.showMessageDialog(null,
					"Enter the current SRID of coordinates", "Load file",
					JOptionPane.ERROR_MESSAGE);
			sridCurrentlTf.requestFocus(true);
			return false;
		}
		return true;
	}
	
	 public void setUpColumnComboBox(JTable table, TableColumn column) {
		//Set up the editor for the sport cells.
		JComboBox comboBox = new JComboBox(getTypes());
		column.setCellEditor(new DefaultCellEditor(comboBox));
		//Set up tool tips for the cells.
		DefaultTableCellRenderer renderer =
		new DefaultTableCellRenderer();
		renderer.setToolTipText("Click for combo box");
		column.setCellRenderer(renderer);
	}
	 
	public String[] getTypes(){
		String[] types = new String[] {
				EnumTypes.VARCHAR.toString()
				,EnumTypes.INTEGER.toString() 			 	
			 	,EnumTypes.SMALLINT.toString()
			 	,EnumTypes.SERIAL.toString()
			 	,EnumTypes.DECIMAL.toString()
			 	,EnumTypes.NUMERIC.toString()
			 	,EnumTypes.REAL.toString()
			 	,EnumTypes.CHARACTERVARYING.toString()
			 	,EnumTypes.TIMESTAMP.toString()
			 	,EnumTypes.POINT.toString()
		};
		return types;
	}
	
	public Object[][] getTableData (JTable table) {
	    DefaultTableModel dtm = (DefaultTableModel) table.getModel();
	    int nRow = dtm.getRowCount(), nCol = dtm.getColumnCount();
	    Object[][] tableData = new Object[nRow][nCol];
	    for (int i = 0 ; i < nRow ; i++)
	        for (int j = 0 ; j < nCol ; j++)
	            tableData[i][j] = dtm.getValueAt(i,j);
	    return tableData;
	}

	 class MyComboBoxRenderer extends JLabel implements ListCellRenderer
	    {
	        private String _title;

	        public MyComboBoxRenderer(String title)
	        {
	            _title = title;
	        }

	     
	        public Component getListCellRendererComponent(JList list, Object value,
	                int index, boolean isSelected, boolean hasFocus)
	        {
	            if (index == -1 && value == null) setText(_title);
	            else setText(value.toString());
	            return this;
	        }

	    }
	 
}