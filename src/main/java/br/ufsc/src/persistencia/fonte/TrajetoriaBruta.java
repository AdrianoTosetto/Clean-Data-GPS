package br.ufsc.src.persistencia.fonte;

public class TrajetoriaBruta {

	private String separador, formatoData, formatoHorario, tabelaBanco;
	private int sridAtual, sridNovo, nroLinhasIgnorar;
	private boolean metaData, tid, gid;
	private Object[][] tableData;
	
	public String toString(){
		
		return "------- Trajetoria Bruta -------" 
				+"\n N�mero de linhas ig: "+nroLinhasIgnorar
				+"\n Separador: "+separador
				+"\n Formato data: "+formatoData
				+"\n Formato hora: "+formatoHorario
				+"\n Tabela Banco: "+tabelaBanco
				+"\n SRID atual: "+sridAtual
				+"\n SRID novo: "+sridNovo
				+"\n Usa metadata: "+metaData;
	}

	public TrajetoriaBruta(int nroLinhasIgnorar, String separador, String formatoData, String formatoHorario, String tabelaBanco,
			int sridAtual, int sridNovo, boolean metaData, Object[][] tableData, boolean gid, boolean tid) {
		this.nroLinhasIgnorar = nroLinhasIgnorar;
		this.separador = separador;
		this.formatoData = formatoData;
		this.setFormatoHorario(formatoHorario);
		this.metaData = metaData;
		this.tabelaBanco = tabelaBanco;
		this.sridAtual = sridAtual;
		this.sridNovo = sridNovo;
		this.tableData = tableData;
		this.tid = tid;
		this.gid = gid;
	}

	public boolean isMetaData() {
		return metaData;
	}

	public void setMetaData(boolean metadata) {
		this.metaData = metadata;
	}

	public int getNroLinhasIgnorar() {
		return nroLinhasIgnorar;
	}

	public void setNroLinhasIgnorar(int nroLinhasIgnorar) {
		this.nroLinhasIgnorar = nroLinhasIgnorar;
	}

	public String getFormatoData() {
		return formatoData;
	}

	public void setFormatoData(String formatoData) {
		this.formatoData = formatoData;
	}

	public String getTabelaBanco() {
		return tabelaBanco;
	}

	public void setTabelaBanco(String tabelaBanco) {
		this.tabelaBanco = tabelaBanco;
	}

	public int getSridAtual() {
		return sridAtual;
	}

	public void setSridAtual(int sridAtual) {
		this.sridAtual = sridAtual;
	}

	public int getSridNovo() {
		return sridNovo;
	}

	public void setSridNovo(int sridNovo) {
		this.sridNovo = sridNovo;
	}

	public String getFormatoHorario() {
		return formatoHorario;
	}

	public void setFormatoHorario(String formatoHorario) {
		this.formatoHorario = formatoHorario;
	}

	public String getSeparador() {
		return separador;
	}

	public void setSeparador(String separador) {
		this.separador = separador;
	}
	
	public void setGID(boolean gid){
		this.gid = gid;
	}

	public boolean isGID(){
		return this.gid;
	}
	
	public void setTID(boolean tid){
		this.tid = tid;
	}
	
	public boolean isTID(){
		return this.tid;
	}
	
	public void setTableData(Object[][] tableData){
		this.tableData = tableData;
	}
	
	public Object[][] getTableData(){
		return this.tableData;
	}
}