package br.ufsc.src.control;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Set;

import br.ufsc.src.control.dataclean.ConfigTraj;
import br.ufsc.src.control.dataclean.RemoveNoise;
import br.ufsc.src.control.dataclean.TrajBroke;
import br.ufsc.src.control.exception.BrokeTrajectoryException;
import br.ufsc.src.persistencia.InterfacePersistencia;
import br.ufsc.src.persistencia.Persistencia;
import br.ufsc.src.persistencia.conexao.DBConfig;
import br.ufsc.src.persistencia.conexao.DBConnectionProvider;
import br.ufsc.src.persistencia.exception.AddBatchException;
import br.ufsc.src.persistencia.exception.AddColumnException;
import br.ufsc.src.persistencia.exception.CreateSequenceException;
import br.ufsc.src.persistencia.exception.CreateStatementException;
import br.ufsc.src.persistencia.exception.CreateTableException;
import br.ufsc.src.persistencia.exception.DBConnectionException;
import br.ufsc.src.persistencia.exception.ExecuteBatchException;
import br.ufsc.src.persistencia.exception.FileNFoundException;
import br.ufsc.src.persistencia.exception.GetSequenceException;
import br.ufsc.src.persistencia.exception.GetTableColumnsException;
import br.ufsc.src.persistencia.exception.LoadDataFileException;
import br.ufsc.src.persistencia.exception.SyntaxException;
import br.ufsc.src.persistencia.exception.TimeStampException;
import br.ufsc.src.persistencia.exception.UpdateGeomException;
import br.ufsc.src.persistencia.fonte.Diretorio;
import br.ufsc.src.persistencia.fonte.TrajetoriaBruta;

public class ServiceControl {
	
	public InterfacePersistencia persistencia;
	public Persistencia persistencia2;
	
	public ServiceControl(InterfacePersistencia persistencia) throws SQLException{
		this.persistencia = persistencia;
		this.persistencia = new Persistencia("org.postgresql.Driver","jdbc:postgresql://localhost/","taxicab","postgres", "postgres");
		this.persistencia2 = new Persistencia("org.postgresql.Driver","jdbc:postgresql://localhost/",DBConfig.banco,"postgres", "postgres");
		
		//DBConnectionProvider.getInstance().test();
		//JFrame j = new JFrame();
		//j.setSize(500,500);
		//j.setVisible(true);
	}
	
	public boolean testarBanco(String drive, String url, String usuario, String senha, String banco) throws SQLException{
		this.criaConexao(drive, url, usuario, senha, banco);
		try {
			return this.persistencia.testaConexao();
		} catch (DBConnectionException e) {
			return false;
		}
	}
	
	public void criaConexao(String drive, String url, String usuario, String senha, String banco) throws SQLException{
		this.persistencia = new Persistencia(drive, url, banco, usuario, senha);
	}
	
	public boolean testConnection(){
		try {
			return persistencia.testaConexao();
		} catch (DBConnectionException e) {
			return false;
		}
	}

	public void loadData(TrajetoriaBruta tb, Diretorio dir) throws TimeStampException, LoadDataFileException, GetSequenceException, CreateSequenceException, UpdateGeomException, CreateStatementException, AddBatchException, FileNFoundException, ExecuteBatchException, DBConnectionException {
		persistencia.carregaArquivo(dir, tb);
	}
	
	public void createTable(TrajetoriaBruta tb) throws SyntaxException, CreateTableException, DBConnectionException{
		persistencia.createTable(tb.getTabelaBanco(), tb.getTableData(), tb.isGID(), tb.isTID(), tb.isMetaData());
	}
	
	public ArrayList<String> getTableColumns(String tableName) throws DBConnectionException, GetTableColumnsException{
			return persistencia.getTableColumns(tableName);
	}

	public void brokeTraj(ConfigTraj configTrajBroke) throws DBConnectionException, AddColumnException, SQLException, BrokeTrajectoryException {
		TrajBroke trajBroke = new TrajBroke(persistencia, configTrajBroke);
		persistencia.addColumn(configTrajBroke.getTableNameOrigin(), "old_tid", "numeric");
		persistencia.createIndex(configTrajBroke.getTableNameOrigin(), configTrajBroke.getColumnName("gid"), "btree");
		persistencia.createIndex(configTrajBroke.getTableNameOrigin(), configTrajBroke.getColumnName("tid"), "btree");
		cleanColumns(configTrajBroke);
		Set<Integer> tids = null;
		String columnNewTID = configTrajBroke.getColumnName("TID");
		try{
			tids = persistencia.fetchTIDS(configTrajBroke.getColumnName("TID"), configTrajBroke.getTableNameOrigin());

			if(configTrajBroke.isStatus()){
				persistencia.addColumn(configTrajBroke.getTableNameOrigin(), "status_tid", "numeric");
				columnNewTID = "status_tid";
				trajBroke.splitByStatus(tids);
			}
			if(configTrajBroke.getSample() != 0){
				persistencia.addColumn(configTrajBroke.getTableNameOrigin(), "sample_tid", "numeric");
				if(configTrajBroke.isStatus())
					tids = persistencia.fetchTIDS("status_tid", configTrajBroke.getTableNameOrigin());
				columnNewTID = "sample_tid";
				trajBroke.splitBySample(tids);
			}
			if(configTrajBroke.getDistanceMax() != 0){
				persistencia.addColumn(configTrajBroke.getTableNameOrigin(), "distance_tid", "numeric");
				if(configTrajBroke.getSample() != 0)
					tids = persistencia.fetchTIDS("sample_tid", configTrajBroke.getTableNameOrigin());
				else if(configTrajBroke.isStatus())
					tids = persistencia.fetchTIDS("status_tid", configTrajBroke.getTableNameOrigin());
				columnNewTID = "distance_tid";
				trajBroke.splitByDistance(tids);
			}
		}catch(Exception e){
			persistencia.dropIndex(configTrajBroke.getTableNameOrigin(), configTrajBroke.getColumnName("gid"));
			persistencia.dropIndex(configTrajBroke.getTableNameOrigin(), configTrajBroke.getColumnName("tid"));
			persistencia.dropColumn(configTrajBroke.getTableNameOrigin(), "old_tid");
			if(configTrajBroke.isStatus())
				persistencia.dropColumn(configTrajBroke.getTableNameOrigin(), "status_tid");
			if(configTrajBroke.getSample() != 0)
				persistencia.dropColumn(configTrajBroke.getTableNameOrigin(), "sample_tid");
			if(configTrajBroke.getDistanceMax() != 0)
				persistencia.dropColumn(configTrajBroke.getTableNameOrigin(), "distance_tid");
			throw new BrokeTrajectoryException(e.getMessage());
		}
		persistencia.moveDataFromColumnToColumn(configTrajBroke.getColumnName("TID"),"old_tid", configTrajBroke.getTableNameOrigin());
		persistencia.moveDataFromColumnToColumn(columnNewTID, configTrajBroke.getColumnName("TID"), configTrajBroke.getTableNameOrigin());
		if(configTrajBroke.isStatus())
			persistencia.dropColumn(configTrajBroke.getTableNameOrigin(), "status_tid");
		if(configTrajBroke.getSample() != 0)
			persistencia.dropColumn(configTrajBroke.getTableNameOrigin(), "sample_tid");
		if(configTrajBroke.getDistanceMax() != 0)
			persistencia.dropColumn(configTrajBroke.getTableNameOrigin(), "distance_tid");
		persistencia.dropIndex(configTrajBroke.getTableNameOrigin(), configTrajBroke.getColumnName("gid"));
		persistencia.dropIndex(configTrajBroke.getTableNameOrigin(), configTrajBroke.getColumnName("tid"));
	}

	private void cleanColumns(ConfigTraj configTrajBroke) throws SQLException, DBConnectionException {
		double accuracy = 0.0;
		double speed = 0.0;
		if(configTrajBroke.getAccuracy() != null){
			accuracy = Double.parseDouble(configTrajBroke.getAccuracy());
			persistencia.deletePointWhere(configTrajBroke.getTableNameOrigin(), configTrajBroke.getColumnName("ACCURACY"), "<=", accuracy);
		}
		if(configTrajBroke.getSpeed() != null){
			speed = Double.parseDouble(configTrajBroke.getSpeed());
			persistencia.deletePointWhere(configTrajBroke.getTableNameOrigin(), configTrajBroke.getColumnName("SPEED"), ">=", speed);
		}	
	}

	public void removeNoise(ConfigTraj configTraj) throws AddBatchException, ExecuteBatchException, DBConnectionException, SQLException {
		Set<Integer> tids = null;
		configTraj.setTableNameOrigin(createTableNoise(configTraj));
		RemoveNoise removeNoise = new RemoveNoise(persistencia, configTraj);
		try {
			tids = persistencia.fetchTIDS(configTraj.getColumnName("TID"), configTraj.getTableNameOrigin());
			removeNoise.findRemoveNoise(tids);
		} catch (DBConnectionException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private String createTableNoise(ConfigTraj configTraj) throws DBConnectionException, SQLException{
		String newTableName = configTraj.getTableNameOrigin()+"_removedNoise_";
		if(configTraj.isRemoveNoiseFromFirst()){
			newTableName += "fromfirst_"+configTraj.getSpeed().replace(".", "dot");
		}else if(configTraj.isRemoveNoiseFromSecond()){
			newTableName += "fromsecond_"+configTraj.getSpeed().replace(".", "dot");
		}else if(configTraj.isDbscan()){
			String dist =  configTraj.getDistancePoints()+"";
			dist = dist.replace(".", "dot");
			if(configTraj.isRemoveNeighborNoise())
				newTableName += "dbscan_neighbor_"+configTraj.getMinPoints()+"_"+dist;
			else
				newTableName += "dbscan_"+configTraj.getMinPoints()+"_"+dist;
		}else if(configTraj.isMeanFilter()){
			if(configTraj.isPastPoints())
				newTableName += "mean_pastpoints_"+configTraj.getNumWindowPoints();
			else
				newTableName += "mean_"+configTraj.getNumWindowPoints();
		}else if(configTraj.isMedianFilter()){
			if(configTraj.isPastPoints())
				newTableName += "median_pastpoints_"+configTraj.getNumWindowPoints();
			else
				newTableName += "median_"+configTraj.getNumWindowPoints();
		}
		persistencia.createTableFromAnother(configTraj.getTableNameOrigin(),newTableName);
		return newTableName;
	}
	

	public void exportTable(String path, String table) throws DBConnectionException, SQLException {
		persistencia.exportTable(path,table);
	}
	
	public boolean trajNearPoint(ConfigTraj configTraj) throws DBConnectionException, SQLException{
		String geomColumnName = configTraj.getColumnName("geom");
		String tidColumnName = configTraj.getColumnName("tid");
		
		persistencia.createIndex(configTraj.getTableNameOrigin(), tidColumnName, "btree");
		persistencia.createIndex(configTraj.getTableNameOrigin(), geomColumnName, "rtree");
		
		String sql = "select distinct tid from "+configTraj.getTableNameOrigin()+" where st_intersects(geom, ST_Buffer(ST_transform(st_setsrid(ST_Makepoint("+
				configTraj.getPoint().getX()+","+configTraj.getPoint().getY()+"),4326),900913),"+configTraj.getDistanceMax()+"))";
		boolean moved = persistencia.createTableMoveTrajNearPoint(sql,configTraj.getTableNameOrigin(), tidColumnName);
		
		persistencia.dropIndex(configTraj.getTableNameOrigin(), tidColumnName);
		persistencia.dropIndex(configTraj.getTableNameOrigin(), geomColumnName);
		
		return moved;
	}
	public void deleteColumn(String table, String column) {
		persistencia2.deleteColumn(table, column);
	}
	public boolean tableExists(String tableName) {
		return persistencia2.tableExists(tableName, "public");
	}
	public boolean tableExists(String tableName, String[] columns) throws SQLException {
		return persistencia2.tableExists(tableName, columns);
	}
	public void createTableAsAnother(String tableSource, String tableDest) throws SQLException, DBConnectionException {
		persistencia2.createTableAsAnother(tableSource, tableDest);
	}
	public void addColumn(String tableName, String ColumnName, String columnType) throws DBConnectionException, AddColumnException{
		persistencia.addColumn(tableName, ColumnName, columnType);
	}
	public void addNewColumn(String tableName, String ColumnName, String columnType) throws DBConnectionException, AddColumnException{
		persistencia2.addNewColumn(tableName, ColumnName, columnType);
	}
	public ArrayList<String> getColumnsName(String table) throws SQLException {
		return persistencia2.getColumnsName(table);
	}
	public ArrayList<String> getTableList() throws SQLException {
		return Persistencia.getTableList();
	}
	public void createTable(String tableName, ArrayList<String> fields, ArrayList<String> fieldsTypes) throws SQLException {
		persistencia2.createTable(tableName, fields, fieldsTypes);
	}
	public void seg(String oldTable, String newTable, ArrayList<String> columnsSeg) throws SQLException, DBConnectionException, AddColumnException, GetTableColumnsException {
		persistencia2.seg(oldTable, newTable, columnsSeg);
	}

}