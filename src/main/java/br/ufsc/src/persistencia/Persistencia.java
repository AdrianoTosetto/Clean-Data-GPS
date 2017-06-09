package br.ufsc.src.persistencia;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.postgresql.util.PSQLException;

import br.ufsc.src.control.Utils;
import br.ufsc.src.control.dataclean.ConfigTraj;
import br.ufsc.src.control.entities.GenericPoint;
import br.ufsc.src.control.entities.GenericTrajectory;
import br.ufsc.src.control.entities.TPoint;
import br.ufsc.src.control.entities.Trajectory;
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
import br.ufsc.src.persistencia.fonte.ILoader;
import br.ufsc.src.persistencia.fonte.LoaderDSV;
import br.ufsc.src.persistencia.fonte.LoaderGPX;
import br.ufsc.src.persistencia.fonte.LoaderJSON;
import br.ufsc.src.persistencia.fonte.LoaderKML;
import br.ufsc.src.persistencia.fonte.LoaderWKT;
import br.ufsc.src.persistencia.fonte.TrajetoriaBruta;

public class Persistencia extends Observable implements InterfacePersistencia {

	protected static DBConnectionProvider DB_CONN;
	private int folder_id;
	private String path;
	public static int completed = 0;
	
	private Connection persistenConnection = null;
	
	public Persistencia(){}

	public Persistencia(String driverPostgres, String url, String banco, String usuario, String senha) throws SQLException{
		DBConfig.driverPostgres = driverPostgres;
		DBConfig.url = url;
		DBConfig.senha = senha;
		DBConfig.usuario = usuario;
		DBConfig.banco = banco;
		DB_CONN = DBConnectionProvider.getInstance();
		
		this.folder_id = 0;
		this.path = "";
	}

	public void leiaTabela() throws DBConnectionException{
		abraConexao();
		try {
			ResultSet rs = DB_CONN.quickQuery("truck",null,null,"order by truckid, time");
			while (rs.next()) {
				int truck = rs.getInt("truckid");
				System.out.println(truck);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		fechaConexao();
	}

	public static void abraConexao() throws DBConnectionException{
		try {
			DBConnectionProvider.getInstance().open();
		} catch (SQLException e) {
			throw new DBConnectionException(e.getMessage());
		}
	}

	public static void fechaConexao() throws DBConnectionException{
		try {
			DBConnectionProvider.getInstance().close();
		} catch (SQLException e) {
			throw new DBConnectionException(e.getMessage());
		}
	}
	
	public boolean testaConexao() throws DBConnectionException{
		abraConexao();
		boolean test = DBConnectionProvider.isConnectionOpen();
		fechaConexao();
		return test;
	}
	
	public boolean carregaArquivo(Diretorio dir, TrajetoriaBruta tb) throws TimeStampException, LoadDataFileException, GetSequenceException, CreateSequenceException, UpdateGeomException, CreateStatementException, AddBatchException, FileNFoundException, ExecuteBatchException, DBConnectionException{
		try {
			folder_id = 0;
			path = "";
			if(tb.isTID())
				createSequence(tb.getTabelaBanco(), "tid");
			leiaCarregaDiretorios(dir.getUrl(), dir.getIgFile(), dir.getIgFolder(), dir.getExtension(), dir.isIgExtension(), tb);
			updateNewGeom(tb.getSridNovo(), tb.getSridAtual(), tb.getTabelaBanco());
		} catch (IOException e) {
			throw new LoadDataFileException(e.getMessage());
		}
		return true;
	}
	
	public void createTable(String tableName, Object[][] tableData, boolean isGID, boolean isTID, boolean isMetaData) throws SyntaxException, CreateTableException, DBConnectionException{
		String q = "CREATE TABLE "+tableName+" ( ";
		Object[][] td = tableData;
		String gid = "gid serial,";
		String tid = "tid serial,";
		String metadata = "path varchar(150), folder_id integer,";
		boolean t = false, gi = false;
		String q1 = "";
		for (Object[] objects : td) {
			String x = (String)objects[0];
			if(x.equalsIgnoreCase("gid"))
				gi = true;
			if(x.equalsIgnoreCase("tid"))
				t = true;
			String auxTime = (String)objects[1];
			if(x.equalsIgnoreCase("time") && auxTime.equalsIgnoreCase(""))
				continue;
			else
				q1 += objects[0]+" "+objects[2]+""+(!objects[3].equals("") ? "("+objects[3]+")," : ",");
		}
		if(isGID && !gi)
			q += gid;
		if(isTID && !t)
			q += tid;
		q += q1;

		if(isMetaData)
			q += metadata;
		q = q.trim().toLowerCase();
		q = q.substring(0, q.length()-1);
		q += ");";
		abraConexao();
		try {
			DB_CONN.execute(q);
		}catch(PSQLException e){
			throw new SyntaxException(e.getMessage());
		}catch (SQLException e) {
			throw new CreateTableException(e.getMessage());
		}
		fechaConexao();
		
	}
	
	public void leiaCarregaDiretorios(String dir, ArrayList<String> igFiles, ArrayList<String> igDir, ArrayList<String> ext, boolean aceitaExtensao, TrajetoriaBruta trajBruta) throws IOException, TimeStampException, GetSequenceException, CreateStatementException, AddBatchException, FileNFoundException, ExecuteBatchException, DBConnectionException {

		File folder = new File(dir);
		if (!folder.isFile()) {
			File[] listOfFiles = folder.listFiles();
			
			for (File file : listOfFiles) {
				if (file.isFile() && !igFiles.contains(file.getName().split("\\.")[0])) {
					if (file.getName().lastIndexOf(".") != 0 && (ext.isEmpty() || (ext.contains(Utils.getFileExtension(file)) == !aceitaExtensao))) {
						if(!folder.getParent().equalsIgnoreCase(path)){
							path = folder.getParent();
							folder_id++;
						}
						this.loadFile(file, trajBruta, folder_id);
					}	
				} else if (file.isDirectory() && !igDir.contains(file.getName()))	
					this.leiaCarregaDiretorios(file.getAbsolutePath(), igFiles, igDir, ext, aceitaExtensao, trajBruta);
			}
		} else if (!igFiles.contains(folder.getName().split("\\.")[0])) {
			if (folder.getName().lastIndexOf(".") != 0 && (ext.isEmpty() || (ext.contains(Utils.getFileExtension(folder)) == !aceitaExtensao))){
				if(!folder.getParent().equalsIgnoreCase(path)){
					path = folder.getParent();
					folder_id++;
				}
				this.loadFile(folder, trajBruta, folder_id);
			}
		}
	
	}
	
	private void loadFile(File file, TrajetoriaBruta tb, int folder_id) throws TimeStampException, GetSequenceException, CreateStatementException, AddBatchException, FileNFoundException, ExecuteBatchException, DBConnectionException{
		ILoader leitor = null;
		switch (Utils.getFileExtension(file).toLowerCase()) {
			case "kml":
				leitor = new LoaderKML();
				break;
			case "gpx":
				leitor = new LoaderGPX();
				break;
			case "wkt":
				leitor = new LoaderWKT();
				break;
			case "json":
				leitor = new LoaderJSON();
				break;
			default:
				leitor = new LoaderDSV();
		}
		leitor.loadFile(file, tb, folder_id);
	}

	private void updateNewGeom(int newSrid, int currentSrid, String tableName) throws UpdateGeomException, DBConnectionException{
		try {
			if(newSrid != currentSrid){
				abraConexao();
				DB_CONN.execute("update "+tableName+" set geom = ST_Transform(geom,"+newSrid+");");
				fechaConexao();
			}
		} catch (SQLException e) {
			throw new UpdateGeomException(e.getMessage());
		}
	}
	
	public void createSequence(String tableName, String id) throws CreateSequenceException, DBConnectionException{
		abraConexao();
		try {
			try{
				DB_CONN.execute("DROP SEQUENCE "+tableName+"_"+id+"_seq CASCADE;");
			}catch(SQLException e){
			}
			DB_CONN.execute("CREATE SEQUENCE "+tableName+"_"+id+"_seq START 1;");
		} catch (SQLException e) {
			throw new CreateSequenceException(e.getMessage());
		}
		fechaConexao();
	}
	
	public static int getSequence (String tableName, String id) throws GetSequenceException{
		try{
			return DB_CONN.getSequenceNextValue(tableName+"_"+id+"_seq");
		}catch(SQLException e){
			throw new GetSequenceException(e.getMessage());
		}
	}
	
	public int getSeq (String tableName, String id) throws GetSequenceException, DBConnectionException, CreateStatementException, SQLException{
		abraConexao();
		int seq = 0;
		try {
			DB_CONN.createStatement();
		} catch (SQLException e1) {
			throw new CreateStatementException(e1.getMessage());
		}
		try{
			 seq = DB_CONN.getSequenceNextValue(tableName+"_"+id+"_seq");
		}catch(SQLException e){
			throw new GetSequenceException(e.getMessage());
		}
		DB_CONN.closeStatement();
		fechaConexao();
		return seq;
	}

	@Override
	public ArrayList<String> getTableColumns(String tableName) throws DBConnectionException, GetTableColumnsException {
		String sql = "select column_name from information_schema.columns where table_name='"+tableName+"';";
		ArrayList<String> columns = new ArrayList<>();
		abraConexao();
			ResultSet rs;
			try {
				rs = DB_CONN.executeQuery(sql);
				while(rs.next()){
					String col = rs.getString(1);
					columns.add(col);
				}
			} catch (SQLException e) {
				throw new GetTableColumnsException(e.getMessage());
			}
		fechaConexao();
		return columns;
		
	}
	
	public void addColumn(String tableName, String ColumnName, String columnType) throws DBConnectionException, AddColumnException{
		String sql = "ALTER TABLE "+tableName+" ADD COLUMN "+ColumnName+" "+columnType+";";
		abraConexao();
		try {
			DB_CONN.execute(sql);
		} catch (SQLException e) {
			throw new AddColumnException(e.getMessage());
		}
		fechaConexao();
	}
	public void addNewColumn(String tableName, String columnName, String columType) {
		String sql = new StringBuilder("alter table " + tableName)
					.append(" add column ")
					.append(columnName)
					.append(" " + columType).toString();
		Connection c;
		try {
			c = DriverManager.getConnection(DBConfig.url + DBConfig.banco, DBConfig.usuario, DBConfig.senha);
			Statement st = c.createStatement();
			st.execute(sql);
			c.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void deleteColumn(String tableName, String columnName)  {
		try {
			Connection c = DriverManager.getConnection(DBConfig.url + DBConfig.banco, DBConfig.usuario, DBConfig.senha);
			Statement st = c.createStatement();
			st.execute("alter table " + tableName + " drop column " + columnName);
			c.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void createIndex(String tableName, String columnName, String indexType) throws DBConnectionException, SQLException {
		try{
			String s = "DROP INDEX "+tableName+"_"+columnName+"_idx"+";";
			abraConexao();
			DB_CONN.execute(s);
			fechaConexao();
		}catch(Exception e){
		}
		String sql = "CREATE INDEX "+tableName+"_"+columnName+"_idx"+" ON "+tableName+" USING "+indexType+"("+columnName+");";
		abraConexao();
		DB_CONN.execute(sql);
		fechaConexao();
		
	}

	public void dropColumn(String tableName, String ColumnName) {
		String sql = "ALTER TABLE "+tableName+" DROP COLUMN "+ColumnName+";";
		try {
			abraConexao();
			DB_CONN.execute(sql);
			fechaConexao();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (DBConnectionException e1) {
			e1.printStackTrace();
		}
	}

	public Set<Integer> fetchTIDS(String columnName, String tableNameOrigin) throws DBConnectionException, SQLException {

		String sql = "SELECT DISTINCT "+columnName+" from "+tableNameOrigin+";";
		abraConexao();
		ResultSet resultSet = DB_CONN.executeQuery(sql);
		Set<Integer> result = new HashSet<Integer>();
		while(resultSet.next()){
			Integer tid = resultSet.getInt(columnName);
			result.add(tid);
		}
		fechaConexao();
		return result;
		
	}

	public void moveDataFromColumnToColumn(String fromColumn, String toColumn, String tableName) throws DBConnectionException, SQLException {
		String sql = "UPDATE "+tableName+" SET "+toColumn+"="+fromColumn+";";
		abraConexao();
		DB_CONN.execute(sql);
		fechaConexao();
		
	}

	public void dropIndex(String tableNameOrigin, String columnName) throws SQLException, DBConnectionException {
		String sql = "DROP INDEX "+tableNameOrigin+"_"+columnName+"_idx"+";";
		abraConexao();
		DB_CONN.execute(sql);
		fechaConexao();
		
	}

	public Trajectory fetchTrajectory(Integer tid, ConfigTraj configTraj, String columnTID) throws DBConnectionException, SQLException {
	/*	String sql = "SELECT "+configTraj.getColumnName("GID")+" as gid,"+
					columnTID+" as tid,"+
					configTraj.getColumnName("TIMESTAMP")+" as timestamp,st_x("+
					configTraj.getColumnName("GEOM")+") as lon,st_y("+
					configTraj.getColumnName("GEOM")+") as lat";
				sql += configTraj.isStatus() ? ","+configTraj.getColumnName("BOOLEAN STATUS") : "";
				sql += " from "+ configTraj.getTableNameOrigin()+
				" where "+columnTID+"="+tid+" order by "+columnTID+","+configTraj.getColumnName("TIMESTAMP")+";";*/
		String sql = "SELECT gid, tid, timestamp,st_x(GEOM) as lon,st_y(geom) as lat";
			sql += configTraj.isStatus() ? ","+configTraj.getColumnName("BOOLEAN STATUS") : "";
			sql += " from "+ configTraj.getTableNameOrigin()+
			" where "+columnTID+"="+tid+" order by "+columnTID+",timestamp;";

		abraConexao();
		ResultSet resultSet = DB_CONN.executeQuery(sql);
		Trajectory result = new Trajectory(tid);
		while(resultSet.next()){
			Double x = resultSet.getDouble("lon");
			Double y = resultSet.getDouble("lat");
			Timestamp time = resultSet.getTimestamp("timestamp");
			int gid = resultSet.getInt("gid");
			int occupation = 0;
			if(configTraj.isStatus())
				occupation = resultSet.getInt(configTraj.getColumnName("BOOLEAN STATUS"));
			TPoint p= new TPoint(gid,x,y,time,occupation);
			result.addPoint(p);
		}
		fechaConexao();
		return result;
	}

	public void updateTID(List<String> querys) throws SQLException, DBConnectionException {
		abraConexao();
		DB_CONN.createStatement();
		for (String sql : querys) {
			DB_CONN.addBatch(sql);
		}
		DB_CONN.executeBatch();
		DB_CONN.closeStatement();
		fechaConexao();
	}
	
	public void deletePointWhere(String tableName, String columnName, String operator, double condition) throws SQLException, DBConnectionException{
		abraConexao();
		DB_CONN.execute("DELETE FROM "+tableName+" WHERE "+ columnName + operator + condition +";");
		fechaConexao();
	}

	public void deleteByGids(List<Integer> gids, String tableNameOrigin) throws DBConnectionException, SQLException {
		if(gids.size() == 0)
			return;
		String ids ="";
		for (Integer gid : gids) 
			ids += gid+",";

		ids = ids.substring(0, ids.length()-1);
		abraConexao();
		DB_CONN.execute("DELETE FROM "+tableNameOrigin+" WHERE gid in ("+ ids +");");
		fechaConexao();
	}
	
	public void exportTable(String path, String table) throws DBConnectionException, SQLException {
		String sql = "COPY "+table+" TO '"+path+"/"+table+".csv' DELIMITER ',' CSV HEADER;";
		abraConexao();
		DB_CONN.execute(sql);
		fechaConexao();
	}

	public void updateGIDs(List<TPoint> pointsToUpdate, ConfigTraj configTraj) throws DBConnectionException, AddBatchException, ExecuteBatchException {
		
		String colGeom = configTraj.getColumnName("geom");
		String colTID = configTraj.getColumnName("tid");
		String colGID = configTraj.getColumnName("gid");
		String colLon = configTraj.getColumnName("lon");
		String colLat = configTraj.getColumnName("lat");
		String tableName = configTraj.getTableNameOrigin();
		int srid= 0;
		abraConexao();
		ResultSet resultSet;
		try {
			resultSet = DB_CONN.executeQuery("select ST_SRID("+colGeom+") from "+tableName+" limit 1;");
			resultSet.next();
			srid = resultSet.getInt("st_srid");
			DB_CONN.createStatement();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		int cont = 0;
		
		for (TPoint tPoint : pointsToUpdate) {
			String sql = "update "+tableName+" set "+colGeom+" = "+"ST_SetSRID(ST_MakePoint(" + tPoint.getX() + "," + tPoint.getY() + ")," + srid + ")"+ " where "+colGID+" = "+tPoint.getGid()+";";
			try {
				DB_CONN.addBatch(sql);
				cont++;
			} catch (SQLException e) {
				throw new AddBatchException(e.getMessage());
			}
			if(cont == 200000){
				try {
					DB_CONN.executeBatch();
				} catch (SQLException e) {
					throw new ExecuteBatchException(e.getMessage());
				}
				cont = 0;
			}
		}
		try {
			DB_CONN.executeBatch();
			DB_CONN.closeStatement();
		} catch (SQLException e) {
			throw new ExecuteBatchException(e.getMessage());
		}
		fechaConexao();
	}
	
	public boolean createTableMoveTrajNearPoint(String sql, String tableName, String tidColumn){
		String hasResult = sql+" limit 1;";
		String sql1 = "create table "+tableName+"_trajsnearpoint as select * from "+tableName+" where "+tidColumn+" in ("+sql+");";
		boolean rt = false;
		try {
			abraConexao();
		} catch (DBConnectionException e) {
			return false;
		}
		ResultSet resultSet;
		try {
			resultSet = DB_CONN.executeQuery(hasResult);
			if(resultSet.next()){
				DB_CONN.execute(sql1);
				rt = true;
			}else
				rt = false;
		} catch (SQLException e1) {
		}
		try {
			fechaConexao();
		} catch (DBConnectionException e) {
		}
		return rt;
	}

	@Override
	public void createTableFromAnother(String tableNameOrigin, String newTableName) throws DBConnectionException, SQLException {
		String sql = "CREATE TABLE "+newTableName+" AS SELECT * FROM "+tableNameOrigin+";";
		abraConexao();
		DB_CONN.executeQuery(sql);
		fechaConexao();
	}
	
	/*----------------------------------------------------------------------------------------------------*/
	
	public void insertRow(String table, ArrayList<String> columns, ArrayList<String> values) throws DBConnectionException, SQLException {
		String sql = "insert into " + table + " (";
		int columnsSize, valuesSize;
		columnsSize = valuesSize = columns.size();
		for(int i = 0; i < columns.size(); i++)
			if(i == columnsSize - 1)
				sql += columns.get(i) + " ";
			else
				sql += columns.get(i) + ", ";
		
		sql += ") values (";
		for(int i = 0; i < valuesSize; i++)
			if(i == valuesSize - 1)
				sql += "'" + values.get(i) + "' ";
			else
				sql += "'" + values.get(i) + "', ";
		sql += ")";
		Connection c = DriverManager.getConnection(DBConfig.url + "taxicab2", DBConfig.usuario, DBConfig.senha);
		Statement st = c.createStatement();
		st.execute(sql);
		c.close();

	}
	
	public ArrayList<String> getDBList() throws SQLException{
		DB_CONN.open();
		Statement st = DB_CONN.getConn().createStatement();
		ResultSet rs;
		rs = st.executeQuery("SELECT datname FROM pg_database WHERE datistemplate = false;");
		ArrayList<String> DatabaseList = new ArrayList<String>();
		while(rs.next()){
			DatabaseList.add(rs.getString("datname"));
		}
		DB_CONN.close();
		return DatabaseList;
	}
	public static ArrayList<String> getTableList() throws SQLException{
		ArrayList<String> tableNames = new ArrayList<String>();
		DBConfig.senha  = "post123";
		Connection c = DriverManager.getConnection(DBConfig.url + DBConfig.banco, DBConfig.usuario, DBConfig.senha);
		Statement st = c.createStatement();
		ResultSet rs;
		rs = st.executeQuery("select * from information_schema.tables where table_schema = 'public' and table_type = 'BASE TABLE'");
		while(rs.next()){
			tableNames.add(rs.getString(3));
		}
		c.close();
		return tableNames;
	}
	public static ArrayList<String> getTableList(String dataBase) throws SQLException{
		ArrayList<String> tableNames = new ArrayList<String>();
		Connection c = DriverManager.getConnection(DBConfig.url + dataBase, DBConfig.usuario, DBConfig.senha);
		Statement st = c.createStatement();
		ResultSet rs;
		
		rs = st.executeQuery("select * from information_schema.tables where table_schema = 'public' and table_type = 'BASE TABLE'");
		while(rs.next()){
			tableNames.add(rs.getString(3));
		}
		c.close();
		return tableNames;
	}
	public ArrayList<String> getGeoDatabases(){
		ArrayList<String> geoDatabases = new ArrayList<String>();
		try {
			ArrayList<String> databases = getDBList();
			for(int i = 0; i < databases.size(); i++){
				String databaseName = databases.get(i);
				Connection c = DriverManager.getConnection(DBConfig.url + databaseName, DBConfig.usuario, DBConfig.senha);
				try{
					c.createStatement().executeQuery("Select * from geometry_columns");
					geoDatabases.add(databaseName);
				}catch (Exception e){}
			}
		} catch (SQLException e) {e.printStackTrace();}
		return geoDatabases;
	}
	public void generateView(String viewName,String database,String tableName,String[] fields, int limit) throws SQLException{
		String sql = "create view " + viewName + " as select ";
		for(int i = 0; i < fields.length; i++){
			if(i == fields.length - 1)
				sql = sql + fields[i];
			else
				sql = sql + fields[i] + ", ";
		}
		sql = sql + " from " + tableName + "limit " + limit;
		Connection c = DriverManager.getConnection(DBConfig.url + database, DBConfig.usuario, DBConfig.senha);
		Statement st = c.createStatement();
		st.execute(sql);
		c.close();
	}
	public void createTableGroupBy(ArrayList<String> selectFields, String function, String argFunction,
									String table, String groupByField, String orderByField){
		int it;
		int listSize = selectFields.size();
		String sql = "Select ";
		for(it = 0; it < listSize; it++) 
				sql += selectFields.get(it) + ", ";
		
		sql += function + "(" + argFunction + ") from " + table + " group by " + groupByField;
		if(orderByField != null)
			sql += " order by " + orderByField;
		System.out.println(sql);
	}
	/*
	 * Syntax: insert into <table> (field1, field2,..,fieldn) values(value1, value2,...,valuen),
	 * 																(value1, value2,...,valuen),
	 * 																			...
	 * 																(value1, value2,...,valuen)
	 * @param String table, table to insert
	 * @param ArrayList<String> columns, columns names
	 * @param  ArrayList<ArrayList<String>> values, values to be inserted. Each register is an ArrayList
	 * and values contains all these registers
	 * */
	public void insertMultRow(String table, ArrayList<String> columns, ArrayList<ArrayList<String>> values, boolean extendQuery) throws SQLException {

		Connection c = DriverManager.getConnection(DBConfig.url + DBConfig.banco, DBConfig.usuario, DBConfig.senha);
		Statement st = c.createStatement();
		StringBuilder sql = new StringBuilder("insert into " + table + " (");
		
		int columnsSize, valuesSize;
		columnsSize = columns.size();
		valuesSize = values.size();
		for(int i = 0; i < columns.size(); i++)
			if(i == columnsSize - 1)
				sql.append(columns.get(i) + " ");
			else
				sql.append(columns.get(i) + ", ");
			
			sql.append( ") values");
			//int t = sql.length();
		for(int i = 0; i < valuesSize; i++) {
			sql.append("(");
			ArrayList<String> aux = values.get(i);
			for(int j = 0; j < aux.size(); j++) {
				if(j == aux.size() - 1) {
					if(i == valuesSize - 1) 
						sql.append("'" + aux.get(j) + "'" + ")");
					else
						sql.append("'" + aux.get(j) + "'" + "),");
				} else
					sql.append("'" + aux.get(j) + "'" + ",");
			}
		}
		//System.out.println(sql.toString());
		st.execute(sql.toString());
		
		sql = null;
		c.close();
	}
	/*
	 * Syntax: insert into <table> (field1, field2,..,fieldn) values(value1, value2,...,valuen),
	 * 																(value1, value2,...,valuen),
	 * 																			...
	 * 																(value1, value2,...,valuen)
	 * @param String table, table to insert
	 * @param ArrayList<String> columns, columns names
	 * @param  ArrayList<ArrayList<String>> values, values to be inserted. Each register is an ArrayList
	 * and values contains all these registers
	 * 
	 * @return SQL string that was assembled by the method
	 * */
	private String assembleSQLInsertMultRow(String table,ArrayList<String> columns, ArrayList<ArrayList<String>> values){
		StringBuilder sql = new StringBuilder("insert into " + table + " (");
		int columnsSize, valuesSize;
		columnsSize = columns.size();
		valuesSize = values.size();
		for(int i = 0; i < columns.size(); i++)
			if(i == columnsSize - 1)
				sql.append(columns.get(i) + " ");
			else
				sql.append(columns.get(i) + ", ");
		
		sql.append( ") values");
		int t = sql.length();
		for(int i = 0; i < valuesSize; i++) {
			sql.append("(");
			ArrayList<String> aux = values.get(i);
			for(int j = 0; j < aux.size(); j++) {
				if(j == aux.size() - 1) {
					if(i == valuesSize - 1) 
						sql.append("'" + aux.get(j) + "'" + ")");
					else
						sql.append("'" + aux.get(j) + "'" + "),");
				}else
					sql.append("'" + aux.get(j) + "'" + ",");
			}
		}	
		return sql.toString();
	}
	public void insertNRows(String table, ArrayList<String> columns, ArrayList<ArrayList<String>> values, int n) throws SQLException{
		
	}
	public int getNumColumns(String table) throws SQLException {
	
		String sql = new StringBuilder("select count(*) from information_schema.columns")
				.append(" where table_name = '"+table+"';").toString();
		Connection c = DriverManager.getConnection(DBConfig.url + DBConfig.banco, DBConfig.usuario, DBConfig.senha);
		Statement st = c.createStatement();
		ResultSet rs = st.executeQuery(sql);
		rs.next();
		return Integer.parseInt(rs.getString(1));
		
	}
	public ArrayList<String> getColumnsName(String table) throws SQLException {
		ArrayList<String> names = new ArrayList<String>(5);
		String sql = "select column_name from information_schema.columns where table_name='"+table+"';";
		Connection c = DriverManager.getConnection(DBConfig.url + DBConfig.banco, DBConfig.usuario, DBConfig.senha);
		Statement st = c.createStatement();
		ResultSet rs = st.executeQuery(sql);
		while(rs.next()) {
			names.add(rs.getString(1));
		}
		return names;
	}
	
	public void seg(String oldTable, String newTable, ArrayList<String> columnsSeg) throws SQLException, DBConnectionException, AddColumnException, GetTableColumnsException{
		int sid = 0;
		ArrayList<String> columns = new ArrayList<String>(); //getColumnsName(oldTable);
		columns.add("tid");
		columns.add("sid");
		columns.add("start_gid");
		columns.add("final_gid");
		
		int totalTids = getNumDiffenteColumnValues(oldTable, "tid");
		int startGid = 1;
		int finalGid = 1;
		for(int tid = 1; tid <= totalTids; tid++) {
			ArrayList<ArrayList<String>> rows = new ArrayList<ArrayList<String>>();
			sid++;
			//String sql = "select distinct * from " + oldTable + " where tid = " + tid + " order by time";
			String sql = "select distinct * from " + oldTable + " where tid = " + tid + " order by gid";
			ResultSet rs = fetchRowsToResultSet(sql);
			ArrayList<String> currentValues = new ArrayList<String>();
						
			while(rs.next()) {
				
				if(rs.isFirst())
					for(int j = 0; j < columnsSeg.size(); j++)
						currentValues.add(rs.getString(columnsSeg.get(j)));
				
				finalGid     = rs.getInt("gid");
				boolean incrementSid = false;
				boolean isLastRow    = rs.isLast();
				for(int j = 0; j < columnsSeg.size(); j++) {
					String compareValue = currentValues.get(j);
					String currentCompareValue = rs.getString(columnsSeg.get(j));
					if(!compareValue.equals(currentCompareValue) || isLastRow){
						incrementSid = true;
						break;
					}
				}
				if(incrementSid) {
					ArrayList<String> aux = new ArrayList<String>();
					finalGid     = rs.getInt(2);
					aux.add(Integer.toString(tid));
					aux.add(Integer.toString(sid));
					aux.add(Integer.toString(startGid));
					if(isLastRow) {
						aux.add(Integer.toString(finalGid));
						startGid     = finalGid + 1;
					} else {
						aux.add(Integer.toString(finalGid - 1));
						startGid = finalGid;
					}
					
					rows.add(aux);
					
					currentValues = new ArrayList<String>();
					for(int j = 0; j < columnsSeg.size(); j++)
						currentValues.add(rs.getString(columnsSeg.get(j)));
					sid++;
				}
				incrementSid = false;
			}
			insertMultRow(newTable, columns, rows, true);
			setChanged();
			notifyObservers((tid*100)/(totalTids-1));
		}
	}
	
	/*
	 * Return how many different values a column assume in a table
	 * e.g: a call with getNumDiffenteColumnValues("taxicab2, "tid") will return how many different tids taxicab2 has
	 * @param String table name
	 * @param String column name
	 * 
	 * @return int number of different occurrences of a given column
	 * */
	public int getNumDiffenteColumnValues(String table, String column) throws DBConnectionException, SQLException {
		
		String sql = "select count(*) from (select distinct "+ column + " from " + table +") as whatever";
		//ResultSet s = DB_CONN.executeQuery(sql);
		Connection c = DriverManager.getConnection(DBConfig.url + DBConfig.banco, DBConfig.usuario, DBConfig.senha);
		Statement st = c.createStatement();
		ResultSet rs = st.executeQuery(sql);
		rs.next();
		int ocurrences = Integer.parseInt(rs.getString("count"));
		
		return ocurrences;
	}
	
	public ArrayList<ArrayList<String>> fetchRowsToArrayList(String query) throws DBConnectionException, SQLException {
		
		Connection c =  DriverManager.getConnection(DBConfig.url + DBConfig.banco, DBConfig.usuario, DBConfig.senha);
		Statement st = c.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		ResultSet rs = st.executeQuery(query);
		ResultSetMetaData rsmd = rs.getMetaData();
		int columns = rsmd.getColumnCount();
		ArrayList<ArrayList<String>> rows = new ArrayList<ArrayList<String>>();
		System.out.println(columns);
		while(rs.next()) {
			ArrayList<String> aux = new ArrayList<String>();
			for(int i = 1; i < columns + 1; i++) aux.add(rs.getString(i));	
			rows.add(aux);
			aux = null;
		}
		c.close();
		return rows;
	}
	
	public ResultSet fetchRowsToResultSet(String query) throws SQLException {
		Connection c =  DriverManager.getConnection(DBConfig.url + DBConfig.banco, DBConfig.usuario, DBConfig.senha);
		Statement st = c.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		ResultSet rs = st.executeQuery(query);
		c.close();
		return rs;
	}
	public ArrayList<String> fetchFirstRow(String query) throws SQLException {
		ArrayList<String> row;
		ResultSet rs = fetchRowsToResultSet(query);
		ResultSetMetaData rsmd = rs.getMetaData();
		int columnsCount = rsmd.getColumnCount();
		row = new ArrayList<String>(columnsCount);
		rs.next();
		for(int i = 1; i < columnsCount + 1; i++)
			row.add(rs.getString(i));
		
		return row;
	}
	public ArrayList<String> fetchLastRow(String query) throws SQLException {
		ResultSet rs = fetchRowsToResultSet(query);
		rs.afterLast();
		rs.previous();
		ResultSetMetaData rsmd = rs.getMetaData();
		int columnsCount = rsmd.getColumnCount();
		ArrayList<String> row = new ArrayList<String>(columnsCount);
		for(int i = 1; i < columnsCount + 1; i++)
			row.add(rs.getString(i));
		return row;
	}
	
	public boolean tableExists(String tableName, String schema) {
		try {
			String queryTest = new StringBuilder("SELECT EXISTS ( ")
					.append("SELECT * FROM information_schema.tables")
					.append(" WHERE table_schema = '"+schema+"' AND")
					.append(" table_name = '"+tableName+"');").toString();
			ResultSet rs = fetchRowsToResultSet(queryTest);
			rs.next();
			if(rs.getString("EXISTS").equals("t")) return true;
			return false;
			
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	public void createTableAsAnother(String tableSource, String tableDest) throws SQLException, DBConnectionException {
		if(tableExists(tableSource, "public")) {
			String queryCreator = new StringBuilder("create table "+tableDest+" as ")
											.append("(select * from "+tableSource+" limit 0 )").toString();
			Connection c =  DriverManager.getConnection(DBConfig.url + DBConfig.banco, DBConfig.usuario, DBConfig.senha);
			Statement st = c.createStatement();
			st.execute(queryCreator);
			c.close();
		}
	}
	public void createTableAsAnother1(String tableSource, String tableDest) throws SQLException, DBConnectionException {
		DBConfig.banco = "taxicab2";
		//DBConfig.url += DBConfig.banco;
		abraConexao();
		DB_CONN.execute("create table "+tableDest+" as (select * from "+tableSource+")");
		fechaConexao();
	}
	
	public void createTable(String tableName, ArrayList<String> fields, ArrayList<String> fieldsTypes) throws SQLException {
		if(tableExists(tableName, "public"))
			throw new IllegalArgumentException("tabela já existe");
		
		StringBuilder sql = new StringBuilder("create table " + tableName + "( ");
		for(int i = 0; i < fields.size(); i++) {
			if(i == fields.size() - 1)
				sql.append(fields.get(i) + " " + fieldsTypes.get(i));
			else
				sql.append(fields.get(i) + " " + fieldsTypes.get(i) + ",");
		}
		sql.append(")");
		Connection c =  DriverManager.getConnection(DBConfig.url + DBConfig.banco, DBConfig.usuario, DBConfig.senha);
		Statement st = c.createStatement();
		st.execute(sql.toString());
	}
	
	public ArrayList<ArrayList<Object>> fetchByColumn(String table, String column, Object value) throws SQLException {
		StringBuilder sql = new StringBuilder("select * from " + table + " where " + column  + " = " +value);
		System.out.println(sql.toString());
		ResultSet rs = fetchRowsToResultSet(sql.toString());
		ResultSetMetaData rsmd = rs.getMetaData();
		
		ArrayList<ArrayList<Object>> ret = new ArrayList<ArrayList<Object>>();
		
		int nColumns = rsmd.getColumnCount();
		
		while(rs.next()) {
			ArrayList<Object> row = new ArrayList<Object>();
			for(int i = 1; i <= nColumns; i++) row.add(rs.getString(i));
			ret.add(row);
		}
		
		return ret;
	}
	
	public ArrayList<ArrayList<Object>> fetchByColumn(String table,String[] fetchedColumns, String column, Object value) throws SQLException {
		StringBuilder sql = new StringBuilder("select ");
		for(int i = 0; i < fetchedColumns.length; i++) {
			if(i == fetchedColumns.length - 1)
				sql.append(fetchedColumns[i]);
			else
				sql.append(fetchedColumns[i] + ", ");
		}
		sql.append(" from " + table + " where " + column + " = " + value);
		System.out.println(sql.toString());
		ResultSet rs = fetchRowsToResultSet(sql.toString());
		ResultSetMetaData rsmd = rs.getMetaData();
		
		ArrayList<ArrayList<Object>> ret = new ArrayList<ArrayList<Object>>();
		
		int nColumns = rsmd.getColumnCount();
		
		while(rs.next()) {
			ArrayList<Object> row = new ArrayList<Object>();
			for(int i = 1; i <= nColumns; i++) row.add(rs.getString(i));
			ret.add(row);
		}
		
		return ret;
	}
	
	public void invertGid() throws SQLException, DBConnectionException {
		//Connection c =  DriverManager.getConnection(DBConfig.url + DBConfig.banco, DBConfig.usuario, DBConfig.senha);
		//Statement st = c.createStatement();
		//createTableAsAnother("taxicab2", "taxicab2_1");
		
		int totalTids = 537;
		ArrayList<String> tmp = new ArrayList<String>(5);
		ArrayList<String> columns = new ArrayList<String>(5);
		columns.add("tid");
		columns.add("gid");
		columns.add("geom");
		columns.add("time");
		columns.add("occupation");
		long gid = 1;
		int lowerBound = 1;
		int upperBound = 2;
		while(lowerBound < totalTids) {
			ArrayList<ArrayList<String>> rows = new ArrayList<ArrayList<String>>(1000000);
			ResultSet rs = fetchRowsToResultSet("select tid, geom, time, occupation from taxicab2 where tid between "+lowerBound+" and "+upperBound+" order by tid, time");
			System.out.println(lowerBound + " - " + upperBound);
			while(rs.next()) {
				tmp = new ArrayList<String>(5);
				tmp.add(rs.getString("tid"));
				tmp.add(Long.toString(gid));
				tmp.add(rs.getString("geom"));
				tmp.add(rs.getString("time"));
				tmp.add(rs.getString("occupation"));
				rows.add(tmp);
				gid++;
			}
			insertMultRow("taxicab2_1", columns, rows, true);
			lowerBound = upperBound + 1;
			upperBound += 1;
		}
	}
	public void part1() throws SQLException {
		int totalTids = 537;
		int lowerBound = 1;
		int upperBound = 10;
		long totalTime = 0L;
		long startTime = System.nanoTime();
		while(lowerBound < totalTids) {
			fetchRowsToResultSet("select tid, geom, time, occupation from taxicab2 where tid between "+lowerBound+" and "+upperBound+" order by tid, time");
			lowerBound = upperBound + 1;
			upperBound += 10;
		}
		totalTime = totalTime + (System.nanoTime() - startTime);
		final double seconds = ((double)totalTime / 1000000000);
		final double minutes = ((double)seconds / 60);
		final double hours   = ((double)minutes / 60);
		System.out.println("Time : " + seconds + " Seconds");
		System.out.println("Time : " + minutes + " Minutes");
		System.out.println("Time : " + hours + " Hours");
	}
	public void part2() throws SQLException {
		int totalTids = 537;
		ArrayList<String> tmp = new ArrayList<String>(5);
		long gid = 1;
		int lowerBound = 1;
		int upperBound = 10;
		long totalTime = 0L;
		while(lowerBound < totalTids) {
			ArrayList<ArrayList<String>> rows = new ArrayList<ArrayList<String>>(1000000);
			ResultSet rs = fetchRowsToResultSet("select tid, geom, time, occupation from taxicab2 where tid between "+lowerBound+" and "+upperBound+" order by tid, time");
			final long start = System.nanoTime();
			while(rs.next()) {
				tmp = new ArrayList<String>(5);
				tmp.add(rs.getString("tid"));
				tmp.add(Long.toString(gid));
				tmp.add(rs.getString("geom"));
				tmp.add(rs.getString("time"));
				tmp.add(rs.getString("occupation"));
				rows.add(tmp);
				gid++;
			}
			totalTime = totalTime + (System.nanoTime() - start);
			lowerBound = upperBound + 1;
			upperBound += 10;
		}
		final double seconds = ((double)totalTime / 1000000000);
		final double minutes = ((double)seconds / 60);
		final double hours   = ((double)minutes / 60);
		System.out.println("Time : " + seconds + " Seconds");
		System.out.println("Time : " + minutes + " Minutes");
		System.out.println("Time : " + hours + " Hours");
	}
	public void part3() throws SQLException {
		//Connection c =  DriverManager.getConnection(DBConfig.url + DBConfig.banco, DBConfig.usuario, DBConfig.senha);
		//Statement st = c.createStatement();
		//createTableAsAnother("taxicab2", "taxicab2_1");
		long totalTime = 0L;
		int totalTids = 537;
		ArrayList<String> tmp = new ArrayList<String>(5);
		ArrayList<String> columns = new ArrayList<String>(5);
		columns.add("tid");
		columns.add("gid");
		columns.add("geom");
		columns.add("time");
		columns.add("occupation");
		long gid = 1;
		int lowerBound = 1;
		int upperBound = 10;
		while(lowerBound < totalTids) {
			ArrayList<ArrayList<String>> rows = new ArrayList<ArrayList<String>>(1000000);
			ResultSet rs = fetchRowsToResultSet("select tid, geom, time, occupation from taxicab2 where tid between "+lowerBound+" and "+upperBound+" order by tid, time");
			//System.out.println(lowerBound + " - " + upperBound);
			while(rs.next()) {
				tmp = new ArrayList<String>(5);
				tmp.add(rs.getString("tid"));
				tmp.add(Long.toString(gid));
				tmp.add(rs.getString("geom"));
				tmp.add(rs.getString("time"));
				tmp.add(rs.getString("occupation"));
				rows.add(tmp);
				gid++;
			}
			long start = System.nanoTime();
			insertMultRow("taxicab2_1", columns, rows, true);
			totalTime = totalTime + (System.nanoTime() - start);
			lowerBound = upperBound + 1;
			upperBound += 10;
		}
		final double seconds = ((double)totalTime / 1000000000);
		final double minutes = ((double)seconds / 60);
		final double hours   = ((double)minutes / 60);
		System.out.println("Time : " + seconds + " Seconds");
		System.out.println("Time : " + minutes + " Minutes");
		System.out.println("Time : " + hours + " Hours");
	}
	
	/*
	 * @info return if <table> exists and if it has the columns passed by
	 * 
	 * @param String table to check if exists
	 * @param Strign[] columns to check
	 * */
	
	public boolean tableExists(String table, String[] columns) throws SQLException  {
		Connection c = null;
		DatabaseMetaData md = null;
		c = DriverManager.getConnection(DBConfig.url + DBConfig.banco, DBConfig.usuario, DBConfig.senha);
		md = c.getMetaData();
		
		for(int i = 0; i < columns.length; i++) {
			ResultSet rs = md.getColumns(null, null, table, columns[i]);
			if(!rs.next()) return false;
		}
		return true;

	}
	public void execute(String sql) throws SQLException {
		Connection c =  DriverManager.getConnection(DBConfig.url + DBConfig.banco, DBConfig.usuario, DBConfig.senha);
		Statement st = c.createStatement();
		st.execute(sql);
		c.close();
	}
	public void execute(String sql, boolean keepConnectionAlive) throws SQLException {
		if(persistenConnection == null)
			persistenConnection = DriverManager.getConnection(DBConfig.url + DBConfig.banco, DBConfig.usuario, DBConfig.senha);
			Statement st = persistenConnection.createStatement();
			st.execute(sql);
			
			if(!keepConnectionAlive) persistenConnection.close(); 
	}
	public void closePersistentConnection() {
		if(persistenConnection == null) return;
		try {
			persistenConnection.close();
		} catch (SQLException e) {
			System.out.println("Falha ao fechar conexão");
			e.printStackTrace();
		}
	}
}