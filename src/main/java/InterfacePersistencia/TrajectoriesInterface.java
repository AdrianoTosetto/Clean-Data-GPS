package InterfacePersistencia;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import br.ufsc.src.control.entities.GenericPoint;
import br.ufsc.src.control.entities.GenericTrajectory;
import br.ufsc.src.persistencia.Persistencia;

public class TrajectoriesInterface {

	private Persistencia persistencia;
	
	public TrajectoriesInterface(Persistencia p){
		this.persistencia = p;
	}
	
	public GenericTrajectory readTrajectory(String table, int tid) throws SQLException {
		
		if(!persistencia.tableExists(table, "public"))
			throw new IllegalArgumentException("table does not exist");
		GenericTrajectory ret = new GenericTrajectory(tid);
		ResultSet rs = null;
		try {
			rs = persistencia.fetchRowsToResultSet("select distinct gid, time, st_x(geom) as x, st_y(geom) as y from " + table + " where tid = " + tid + " order by time");
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		while(rs.next()) {
			GenericPoint gp = new GenericPoint(rs.getDouble("x"), rs.getDouble("y"));
			gp.addNewAttr("time", rs.getString("time"));
			SimpleDateFormat dt = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
			try {
				gp.setDate(dt.parse(rs.getString("time")));
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			ret.addPoint(gp);
		}
		
		return ret;
	}
	public GenericTrajectory readTrajectory(String table, int startGid, int endGid) throws SQLException {
		if(!persistencia.tableExists(table, "public"))
			throw new IllegalArgumentException("table does not exist");
		GenericTrajectory ret = new GenericTrajectory(0);
		ResultSet rs = null;
		try {
			rs = persistencia.fetchRowsToResultSet("select distinct gid, time, st_x(geom) as x, st_y(geom) as y from " 
						+ table + " where gid between " + startGid +" and " + endGid + "order by time");
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		while(rs.next()) {
			GenericPoint gp = new GenericPoint(rs.getDouble("x"), rs.getDouble("y"));
			gp.setDateTS(Timestamp.valueOf(rs.getString("time")));
			ret.addPoint(gp);
		}
		
		return ret;
	}
}
