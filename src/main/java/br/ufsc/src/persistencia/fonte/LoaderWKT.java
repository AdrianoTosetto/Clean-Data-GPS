package br.ufsc.src.persistencia.fonte;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.sql.SQLException;
import java.util.Scanner;

import br.ufsc.src.persistencia.Persistencia;
import br.ufsc.src.persistencia.conexao.DBConnectionProvider;
import br.ufsc.src.persistencia.exception.AddBatchException;
import br.ufsc.src.persistencia.exception.CreateStatementException;
import br.ufsc.src.persistencia.exception.DBConnectionException;
import br.ufsc.src.persistencia.exception.ExecuteBatchException;
import br.ufsc.src.persistencia.exception.FileNFoundException;
import br.ufsc.src.persistencia.exception.GetSequenceException;

public class LoaderWKT implements ILoader {

	public void loadFile(File file, TrajetoriaBruta tb, int folder_id) throws FileNFoundException, DBConnectionException, CreateStatementException, AddBatchException, ExecuteBatchException, GetSequenceException {
		DBConnectionProvider DB_CONN = DBConnectionProvider.getInstance();
		Scanner scanner;
		Persistencia.abraConexao();
		try {
			DB_CONN.createStatement();
		} catch (SQLException e1) {
			throw new CreateStatementException(e1.getMessage());
		}
		
		int seq = 0;
		int cont = 0;
		if (tb.isTID())
			seq = Persistencia.getSequence(tb.getTabelaBanco(), "tid");

		try {
			scanner = new Scanner(new FileReader(file.getAbsolutePath()));
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				String[] linha = line.split(";");
				if(!tb.isTID())
					seq = Integer.parseInt(linha[0]);
				String timestamp = linha[1];
				String latitude = getLatitude(linha[2]);
				String longitude = getLongitude(linha[2]);
				
				String sql = "insert into " + tb.getTabelaBanco() + " ( tid, lat, lon, timestamp, geom ";
				String sql1 = ") values (";
				sql1 += seq+","+latitude+","+longitude+",'"+timestamp+"',";
				sql1 += "ST_SetSRID(ST_MakePoint(" + longitude + "," + latitude + ")," + tb.getSridAtual() + ")";
				if (tb.isMetaData()) {
					sql += ", path, folder_id";
					sql1 += ",'" + file.getAbsolutePath() + "'," + folder_id;
				}
				sql += sql1 +");";
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
		} catch (FileNotFoundException e) {
			throw new FileNFoundException(e.getMessage());
		}
		try {
			DB_CONN.executeBatch();
			DB_CONN.closeStatement();
		} catch (SQLException e) {
			throw new ExecuteBatchException(e.getMessage());
		}
		Persistencia.fechaConexao(); 
		
	}
	
	private static String getLatitude(String point){
		//lat lon
		return getCoordinates(point)[0];
	}
	
	private static String getLongitude(String point){
		return getCoordinates(point)[1];
	}
	
	private static String[] getCoordinates(String point){
		int start = point.indexOf('(');
		int end = point.indexOf(')');
		String coord = point.substring(start+1, end);
		String[] coordinates = coord.split(" ");
		return coordinates;
	}

}