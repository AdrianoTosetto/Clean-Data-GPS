package br.ufsc.src.persistencia.fonte;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.apache.commons.io.IOUtils;

import br.ufsc.src.lib.org.json.JSONArray;
import br.ufsc.src.lib.org.json.JSONObject;
import br.ufsc.src.persistencia.Persistencia;
import br.ufsc.src.persistencia.conexao.DBConnectionProvider;
import br.ufsc.src.persistencia.exception.AddBatchException;
import br.ufsc.src.persistencia.exception.CreateStatementException;
import br.ufsc.src.persistencia.exception.DBConnectionException;
import br.ufsc.src.persistencia.exception.ExecuteBatchException;
import br.ufsc.src.persistencia.exception.FileNFoundException;
import br.ufsc.src.persistencia.exception.GetSequenceException;

public class LoaderJSON implements ILoader {

	public void loadFile(File file, TrajetoriaBruta tb, int folder_id) throws FileNFoundException, DBConnectionException, CreateStatementException, GetSequenceException, AddBatchException, ExecuteBatchException {
		
		DBConnectionProvider DB_CONN = DBConnectionProvider.getInstance();
		Persistencia.abraConexao();
		try {
			DB_CONN.createStatement();
		} catch (SQLException e1) {
			throw new CreateStatementException(e1.getMessage());
		}
		int seq = 0;
		if (tb.isTID())
			seq = Persistencia.getSequence(tb.getTabelaBanco(), "tid");
		else
			seq = 1;
		
        if (file.exists()){
            InputStream is;
            String jsonTxt = null;
			try {
				is = new FileInputStream(file.getAbsolutePath());
				jsonTxt = IOUtils.toString(is);
			} catch (IOException e1) {
				throw new FileNFoundException(e1.getMessage());
			}
           
            JSONObject json = new JSONObject(jsonTxt);    
            JSONArray jsonArray = json.getJSONArray("locations");
            for (int i = 0; i < jsonArray.length(); i++) {
            	
            	String timestamp = formatTimestamp(jsonArray.getJSONObject(i).getString("timestampMs"));
				String latitude = formatCoordinate(Long.toString(jsonArray.getJSONObject(i).getLong("latitudeE7")));
				String longitude = formatCoordinate(Long.toString(jsonArray.getJSONObject(i).getLong("longitudeE7")));
				
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
				} catch (SQLException e) {
					throw new AddBatchException(e.getMessage());
				}
				
			}
            
            try {
    			DB_CONN.executeBatch();
    			DB_CONN.closeStatement();
    		} catch (SQLException e) {
    			throw new ExecuteBatchException(e.getMessage());
    		}

        }
        
		Persistencia.fechaConexao(); 

	}
	
	private static String formatTimestamp(String date){
		return new Timestamp(Long.parseLong(date)).toString();
	}
	
	private static String formatCoordinate(String coordinate){
		
		if(coordinate.indexOf('.') != -1)
			return coordinate;
		
		if(coordinate.indexOf('-') != -1){
			if(coordinate.length() == 10)
				coordinate = coordinate.substring(0, 3) + "." + coordinate.substring(4, coordinate.length());
			else
				coordinate = coordinate.substring(0, 2) + "." + coordinate.substring(3, coordinate.length());
		}else{
			if(coordinate.length() == 9)
				coordinate = coordinate.substring(0, 2) + "." + coordinate.substring(3, coordinate.length());
			else
				coordinate = coordinate.substring(0, 1) + "." + coordinate.substring(2, coordinate.length());
		}
			return coordinate;
	}

}
