package br.ufsc.src.persistencia.fonte;

import java.io.File;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

import br.ufsc.src.lib.net.divbyzero.gpx.GPX;
import br.ufsc.src.lib.net.divbyzero.gpx.Track;
import br.ufsc.src.lib.net.divbyzero.gpx.TrackSegment;
import br.ufsc.src.lib.net.divbyzero.gpx.Waypoint;
import br.ufsc.src.lib.net.divbyzero.gpx.parser.JDOM;
import br.ufsc.src.lib.net.divbyzero.gpx.parser.ParsingException;
import br.ufsc.src.persistencia.Persistencia;
import br.ufsc.src.persistencia.conexao.DBConnectionProvider;
import br.ufsc.src.persistencia.exception.AddBatchException;
import br.ufsc.src.persistencia.exception.CreateStatementException;
import br.ufsc.src.persistencia.exception.DBConnectionException;
import br.ufsc.src.persistencia.exception.ExecuteBatchException;
import br.ufsc.src.persistencia.exception.FileNFoundException;
import br.ufsc.src.persistencia.exception.GetSequenceException;

public class LoaderGPX implements ILoader {

	public void loadFile(File file, TrajetoriaBruta tb, int folder_id) throws GetSequenceException, DBConnectionException, CreateStatementException, FileNFoundException, AddBatchException, ExecuteBatchException {
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
		
		JDOM jdom = new JDOM();
		try {
			GPX dom = jdom.parse(file.getAbsoluteFile());
			ArrayList<Track> tracks  = dom.getTracks();
			for (Track track : tracks) {
				ArrayList<TrackSegment> ts = track.getSegments();
				for (TrackSegment trackSegment : ts) {
					if (!tb.isTID())
						seq += 1;
					ArrayList<Waypoint> wp = trackSegment.getWaypoints();
					for (Waypoint waypoint : wp) {
						Long date = waypoint.getTime().getTime();
						String timestamp = new Timestamp(date).toString();
						double latitude = waypoint.getCoordinate().getLatitude();
						double longitude = waypoint.getCoordinate().getLongitude();
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
				}
			}
		} catch (ParsingException e) {
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
}