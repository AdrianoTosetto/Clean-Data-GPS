package br.ufsc.src.persistencia;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Observer;
import java.util.Set;

import br.ufsc.src.control.dataclean.ConfigTraj;
import br.ufsc.src.control.entities.TPoint;
import br.ufsc.src.control.entities.Trajectory;
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

public interface InterfacePersistencia {
	
	public boolean testaConexao() throws DBConnectionException;
	
	public boolean carregaArquivo(Diretorio dir, TrajetoriaBruta tj) throws TimeStampException, LoadDataFileException, GetSequenceException, CreateSequenceException, UpdateGeomException, CreateStatementException, AddBatchException, FileNFoundException, ExecuteBatchException, DBConnectionException;
	
	public void createTable(String tableName, Object[][] tableData, boolean isGID, boolean isTID, boolean isMetaData) throws SyntaxException, CreateTableException, DBConnectionException;

	public ArrayList<String> getTableColumns(String tableName) throws DBConnectionException, GetTableColumnsException;
	
	public void addColumn(String tableName, String ColumnName, String columnType) throws DBConnectionException, AddColumnException;
	
	public void dropColumn(String tableName, String ColumnName);

	public void createIndex(String tableName, String columnName, String indexType) throws DBConnectionException, SQLException;

	public Set<Integer> fetchTIDS(String columnName, String tableNameOrigin) throws DBConnectionException, SQLException;
	
	public void createSequence(String tableName, String id) throws CreateSequenceException, DBConnectionException;
	
	public int getSeq (String tableName, String id) throws GetSequenceException, DBConnectionException, CreateStatementException, SQLException;

	public void moveDataFromColumnToColumn(String columnName, String string, String tableName) throws DBConnectionException, SQLException;

	public void dropIndex(String tableNameOrigin, String columnName) throws SQLException, DBConnectionException;

	public Trajectory fetchTrajectory(Integer tid, ConfigTraj configTrajBroke, String columnTID) throws DBConnectionException, SQLException;

	public void updateTID(List<String> querys) throws SQLException, DBConnectionException;
	
	public void deletePointWhere(String tableName, String columnName, String operator, double condition) throws SQLException, DBConnectionException;

	public void deleteByGids(List<Integer> gids, String tableNameOrigin) throws DBConnectionException, SQLException;

	public void exportTable(String path, String table) throws DBConnectionException, SQLException;

	public void updateGIDs(List<TPoint> pointsToUpdate, ConfigTraj configTraj) throws DBConnectionException, AddBatchException, ExecuteBatchException;
	
	public boolean createTableMoveTrajNearPoint(String sql, String columnName, String tidColumn);

	public void createTableFromAnother(String tableNameOrigin, String newTableName) throws DBConnectionException, SQLException;
	
	/*--------------------------------------------------------------------------------------------------------------------------*/
	
	public void insertMultRow(String table, ArrayList<String> columns, ArrayList<ArrayList<String>> values, boolean extendQuery) throws SQLException;
	
	public int getNumDiffenteColumnValues(String table, String column) throws DBConnectionException, SQLException;
	
	public boolean tableExists(String tableName, String schema);
	public void createTableAsAnother(String tableSource, String tableDest) throws SQLException, DBConnectionException;
	public void createTableAsAnother1(String tableSource, String tableDest) throws SQLException, DBConnectionException;
	public void addNewColumn(String tableName, String columnName, String columType);
	public void seg(String oldTable, String newTable, ArrayList<String> columnsSeg) throws SQLException, DBConnectionException, AddColumnException, GetTableColumnsException;
}