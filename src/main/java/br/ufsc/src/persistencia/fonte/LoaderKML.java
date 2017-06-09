package br.ufsc.src.persistencia.fonte;

import java.io.File;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import br.ufsc.src.lib.de.micromata.opengis.kml.v_2_2_0.Kml;
import br.ufsc.src.lib.de.micromata.opengis.kml.v_2_2_0.Placemark;
import br.ufsc.src.lib.de.micromata.opengis.kml.v_2_2_0.gx.Track;
import br.ufsc.src.persistencia.Persistencia;
import br.ufsc.src.persistencia.conexao.DBConnectionProvider;
import br.ufsc.src.persistencia.exception.AddBatchException;
import br.ufsc.src.persistencia.exception.CreateStatementException;
import br.ufsc.src.persistencia.exception.DBConnectionException;
import br.ufsc.src.persistencia.exception.ExecuteBatchException;
import br.ufsc.src.persistencia.exception.GetSequenceException;

public class LoaderKML implements ILoader {

	public void loadFile(File file, TrajetoriaBruta tb, int folder_id) throws DBConnectionException, CreateStatementException, GetSequenceException, AddBatchException, ExecuteBatchException {

		final Kml kml = Kml.unmarshal(file);
		final Placemark placemark = (Placemark) kml.getFeature();
		Track track = (Track) placemark.getGeometry();
		List<String> coord = track.getCoord();
		List<String> ts = track.getWhen();
		Iterator<String> it1 = coord.iterator();
		Iterator<String> it2 = ts.iterator();
		
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
		while (it1.hasNext() && it2.hasNext()) {
			String[] coordinates = it1.next().split(" ");
			String longitude = coordinates[0];
			String latitude = coordinates[1];
			String timestamp = formatTimestamp(it2.next());
			
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
		Persistencia.fechaConexao(); 
		
	}

	private static String formatTimestamp(String ts) {
		String[] timeStamp = ts.split("T");
		return timeStamp[0] + " " + timeStamp[1].split("Z")[0];
	}

}
