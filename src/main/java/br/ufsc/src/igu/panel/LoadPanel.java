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
import javax.swing.GroupLayout.Alignment;
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


public class LoadPanel extends AbstractPanel {

	private static final long serialVersionUID = 1L;
	private JLabel dirLabel, igFileLabel, extLabel, igDirLabel, sridLabel, sridNewLabel, tableLabel, t1,t2,t3,t4,t5,t6;
	private JTextField dirTf, igFileTf, igDirTf, extTf, tableDBTf, sridTf, sridNewTf ;
	private JButton dirBtn;
	private JCheckBox addMetadata, igExt, tid, gid;
	
	public LoadPanel(ServiceControl controle) {
		super("Load JSON/GPX/KML/WKT files", controle, new JButton("Load"));
		defineComponents();
		adjustComponents();
	}

	public void defineComponents() {
		dirLabel = new JLabel("Directory/File");
		sridLabel = new JLabel("Data SRID");
		sridNewLabel = new JLabel("  New SRID");
		tableLabel = new JLabel("Table name");
		igFileLabel = new JLabel("Ignore files");
		extLabel = new JLabel("Extensions");
		igDirLabel = new JLabel("Ign. folders");
		dirTf = new JTextField();
		tableDBTf = new JTextField();
		sridTf = new JTextField();
		sridNewTf = new JTextField();
		igFileTf = new JTextField();
		igDirTf = new JTextField();
		extTf = new JTextField();
		
		dirBtn = new JButton("Select");

		addMetadata = new JCheckBox("Add Metadata");
		igExt = new JCheckBox("Ignore extensions");
		tid = new JCheckBox("Generate TID");
		gid = new JCheckBox("Generate GID");
		
		extTf.setText("pdf,zip,txt,csv,tsv");
		igExt.setSelected(true);

		dirBtn.addActionListener(this);

		processButton.setBackground(Color.DARK_GRAY);
		dirBtn.setToolTipText("Click to select a directory/file");
		processButton.setToolTipText("Click to load");
		
		t1 = new JLabel("");
		t2 = new JLabel("");
		t3 = new JLabel("");
		t4 = new JLabel("");
		t5 = new JLabel("");
		t6 = new JLabel("");
	}

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
									.addComponent(dirLabel))
								.addGroup(layout.createParallelGroup(LEADING)
										.addComponent(dirTf))
								.addGroup(layout.createParallelGroup(LEADING)
										.addComponent(dirBtn))
						)
						.addGroup(layout.createSequentialGroup()
								.addGroup(layout.createSequentialGroup()
									.addGroup(layout.createParallelGroup(LEADING)
											.addComponent(tableLabel, 0, GroupLayout.DEFAULT_SIZE, 77))
									.addGroup(layout.createParallelGroup(LEADING)
											.addComponent(tableDBTf, 0, GroupLayout.DEFAULT_SIZE, 300)))
						)
						.addGroup(layout.createSequentialGroup()
								.addGroup(layout.createSequentialGroup()
									.addGroup(layout.createParallelGroup(LEADING)
											.addComponent(sridLabel, 0, GroupLayout.DEFAULT_SIZE, 77))
									.addGroup(layout.createParallelGroup(LEADING)
											.addComponent(sridTf, 0, GroupLayout.DEFAULT_SIZE, 70))
									.addGroup(layout.createParallelGroup(LEADING)
											.addComponent(sridNewLabel))
									.addGroup(layout.createParallelGroup(LEADING)
											.addComponent(sridNewTf, 0, GroupLayout.DEFAULT_SIZE, 70)))
						)
						.addGroup(layout.createSequentialGroup()
								.addGroup(layout.createParallelGroup(LEADING)
										.addComponent(igDirLabel, 0, GroupLayout.DEFAULT_SIZE, 77))
								.addGroup(layout.createParallelGroup(LEADING)
										.addComponent(igDirTf, 0, GroupLayout.DEFAULT_SIZE, 200))
								.addGroup(layout.createParallelGroup(LEADING)
										.addComponent(igFileLabel))
								.addGroup(layout.createParallelGroup(LEADING)
										.addComponent(igFileTf, 0, GroupLayout.DEFAULT_SIZE, 200))	
						)
						.addGroup(layout.createSequentialGroup()
								.addGroup(layout.createParallelGroup(LEADING)
										.addComponent(extLabel, 0, GroupLayout.DEFAULT_SIZE, 77))
								.addGroup(layout.createParallelGroup(LEADING)
										.addComponent(extTf, 0, GroupLayout.DEFAULT_SIZE, 200))
								.addGroup(layout.createParallelGroup(LEADING)
										.addComponent(igExt))
						)
						.addGroup(layout.createSequentialGroup()
									.addGroup(layout.createParallelGroup(LEADING)
											.addComponent(t3, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
									.addGroup(layout.createParallelGroup(LEADING)
											.addComponent(addMetadata))
									.addGroup(layout.createParallelGroup(LEADING)
											.addComponent(t1, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
									.addGroup(layout.createParallelGroup(LEADING)
											.addComponent(gid))
									.addGroup(layout.createParallelGroup(LEADING)
											.addComponent(t2, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
									.addGroup(layout.createParallelGroup(LEADING)
											.addComponent(tid))
									.addGroup(layout.createParallelGroup(LEADING)
											.addComponent(t6, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
									
						)
						.addGroup(layout.createSequentialGroup()
								.addGroup(layout.createParallelGroup(LEADING)
										.addComponent(t4, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
								.addGroup(layout.createParallelGroup(LEADING)
										.addComponent(t5, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
								.addGroup(layout.createParallelGroup(LEADING)
										.addComponent(processButton)
								))
				)
		);

		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(BASELINE)
						.addComponent(dirLabel)
						.addComponent(dirTf)
						.addComponent(dirBtn))
				.addGroup(layout.createParallelGroup(BASELINE)
						.addComponent(tableLabel)
						.addComponent(tableDBTf))
				.addGroup(layout.createParallelGroup(BASELINE)
						.addComponent(sridLabel)
						.addComponent(sridTf)
						.addComponent(sridNewLabel)
						.addComponent(sridNewTf))
				.addGroup(layout.createParallelGroup(BASELINE)
						.addComponent(igDirLabel)
						.addComponent(igDirTf)
						.addComponent(igFileLabel)
						.addComponent(igFileTf))
				.addGroup(layout.createParallelGroup(BASELINE)
						.addComponent(extLabel)
						.addComponent(extTf)
						.addComponent(igExt))
				.addGroup(layout.createParallelGroup(BASELINE)
						.addComponent(t3)
						.addComponent(addMetadata)
						.addComponent(t1)
						.addComponent(gid)
						.addComponent(t2)
						.addComponent(tid)
						.addComponent(t6))
				.addGroup(layout.createParallelGroup(BASELINE)
						.addComponent(t4)
						.addComponent(t5)
						.addComponent(processButton))
				);	
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == dirBtn) {
			JFileChooser fc = new JFileChooser();
			fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			fc.setAcceptAllFileFilterUsed(true);
			int returnVal = fc.showOpenDialog(null);
			if (returnVal == JFileChooser.APPROVE_OPTION)
				dirTf.setText(fc.getSelectedFile().getAbsolutePath());
		}else if (e.getSource() == processButton) {
			if (!control.testConnection())
				JOptionPane.showMessageDialog(null, "PAUUUU",
						"DB connection", JOptionPane.ERROR_MESSAGE);
			else {	
				if (checkInputs()) {
					String inLocal = dirTf.getText();
					String inTabela = tableDBTf.getText();
					boolean inMetadata = addMetadata.isSelected();
					boolean inGID = gid.isSelected();
					boolean inTID = tid.isSelected();
					int inSRIDAtual, inSRIDNovo = 0;
					try{
						inSRIDAtual = Integer.parseInt(sridTf.getText());
					}catch(NumberFormatException ex){
						JOptionPane.showMessageDialog(null,
								"SRID not a number, SRID should be a number",
								"Load File", JOptionPane.ERROR_MESSAGE);
						sridTf.requestFocus(true);
						return;
					}
					if(sridNewTf.getText().length() != 0){
						try{
							inSRIDNovo = Integer.parseInt(sridNewTf.getText());
						}catch(NumberFormatException ex){
							JOptionPane.showMessageDialog(null,
									"SRID not a number, SRID should be a number",
									"Load File", JOptionPane.ERROR_MESSAGE);
							sridNewTf.requestFocus(true);
							return;
						}
					}else
						inSRIDNovo = inSRIDAtual;
					
					String inExt = (extTf.getText().length() > 0 ) ? extTf.getText() : null;
					boolean inIgExt = igExt.isSelected();
					String inIgDir = (igDirTf.getText().length() > 0) ? igDirTf.getText() : null;
					String inIgArq = (igFileTf.getText().length() > 0) ? igFileTf.getText() : null;
					
					Object [][] tableColumns        = new Object[][]{
						{"tid",  "", EnumTypes.NUMERIC.toString(),""}
						, {"lat",  "", EnumTypes.NUMERIC.toString(),""}
						, {"lon",  "", EnumTypes.NUMERIC.toString(), ""}
						, {"timestamp", "", EnumTypes.TIMESTAMP.toString(), ""}
						, {"geom", "", EnumTypes.POINT.toString(), ""} 
					};

					TrajetoriaBruta tb = new TrajetoriaBruta(0, null, null, null, inTabela, inSRIDAtual, inSRIDNovo, inMetadata, tableColumns, inGID, inTID);
					Diretorio dir = directoryDefinitions(inLocal, inExt, inIgExt, inIgDir, inIgArq);
				
					try {
						control.createTable(tb); 
					} catch (SyntaxException e1){
						JOptionPane.showMessageDialog(null,e1.getMsg(),"Loading data", JOptionPane.ERROR_MESSAGE);
						tableDBTf.requestFocus(true);
						return;
					} catch (CreateTableException e1) {
						JOptionPane.showMessageDialog(null,"Error creating table: "+e1.getMsg(),"Loading data", JOptionPane.ERROR_MESSAGE);
						tableDBTf.requestFocus(true);
						return;
					} catch (DBConnectionException e1) {
						JOptionPane.showMessageDialog(null,"Error connecting to DB: "+e1.getMsg(),"Loading data", JOptionPane.ERROR_MESSAGE);
						tableDBTf.requestFocus(true);
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

	private Diretorio directoryDefinitions(String url, String inExt, boolean inIgExt,
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

	private boolean checkInputs() {
		if (dirTf.getText().length() == 0) {
			JOptionPane.showMessageDialog(null,
					"Directory or file to be load",
					"Load File", JOptionPane.ERROR_MESSAGE);
			dirTf.requestFocus(true);
			return false;
		}else if (tableDBTf.getText().length() == 0) {
			JOptionPane
					.showMessageDialog(
							null,
							"Input the table name to be create in the database",
							"Load File", JOptionPane.ERROR_MESSAGE);
			tableDBTf.requestFocus(true);
			return false;
		} else if (sridTf.getText().length() == 0) {
			JOptionPane.showMessageDialog(null,
					"You should tell the SRID to coordinates", "Load File",
					JOptionPane.ERROR_MESSAGE);
			sridTf.requestFocus(true);
			return false;
		}
		return true;
	}
}