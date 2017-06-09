package br.ufsc.src.igu.panel;

import javax.swing.table.DefaultTableModel;

class MyTableModel extends DefaultTableModel
{
	private static final long serialVersionUID = 1L;
	
	private boolean editable;
	 
	public MyTableModel(Object[][] data, Object[] columnNames, boolean editable){
		super(data,columnNames);
		this.editable = editable;
	}
 
    public void setEditable(boolean editable) { 
    	this.editable = editable; 
    }
 
    @Override
    public boolean isCellEditable(int row, int col) {
    	if(row <= 5 && col != 1)
    		return false; 
    	else
    		return editable;
    }
}