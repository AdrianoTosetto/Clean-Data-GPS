package br.ufsc.src.control.dataclean;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Set;

import br.ufsc.src.control.Utils;
import br.ufsc.src.control.entities.TPoint;
import br.ufsc.src.control.entities.Trajectory;
import br.ufsc.src.persistencia.InterfacePersistencia;
import br.ufsc.src.persistencia.conexao.DBConfig;
import br.ufsc.src.persistencia.exception.CreateSequenceException;
import br.ufsc.src.persistencia.exception.CreateStatementException;
import br.ufsc.src.persistencia.exception.DBConnectionException;
import br.ufsc.src.persistencia.exception.GetSequenceException;

public class TrajBroke extends Observable{
	
	private ConfigTraj configTrajBroke;
	private InterfacePersistencia persistencia;
	
	public TrajBroke (InterfacePersistencia persistencia, ConfigTraj configTraj){
		this.configTrajBroke = configTraj;
		this.persistencia = persistencia;
	}

	public void splitByStatus(Set<Integer> tids) throws DBConnectionException, SQLException, CreateSequenceException, GetSequenceException, CreateStatementException {
		persistencia.createSequence(configTrajBroke.getTableNameOrigin(), "tid");
		String trajid = "status_tid";
		String gid = configTrajBroke.getColumnName("GID");
		
		for (Integer tid : tids) {
			Trajectory t = persistencia.fetchTrajectory(tid, configTrajBroke, configTrajBroke.getColumnName("TID"));
			int sequenceValue = persistencia.getSeq(configTrajBroke.getTableNameOrigin(), "tid");
			List<String> querys = new ArrayList<String>();
			for (int i = 0; i < t.getPoints().size(); i++) {
                TPoint p = t.getPoint(i);
                boolean diff = false;
                if (i != 0) {
                    diff = t.getPoint(i).getOccupation() != t.getPoint(i-1).getOccupation() ? true : false;
                }

                if (!diff) {
                    querys.add("update "+configTrajBroke.getTableNameOrigin()+" set "+trajid+"=" + sequenceValue + " where "+gid+"=" + p.getGid());
                } else {
                    sequenceValue = persistencia.getSeq(configTrajBroke.getTableNameOrigin(), "tid");
                    querys.add("update "+configTrajBroke.getTableNameOrigin()+" set "+trajid+"=" + sequenceValue + " where "+gid+"=" + p.getGid());
                }
            }
			persistencia.updateTID(querys);
		}
	}

	public void splitBySample(Set<Integer> tids) throws CreateSequenceException, DBConnectionException, SQLException, GetSequenceException, CreateStatementException {
		persistencia.createSequence(configTrajBroke.getTableNameOrigin(), "tid");
		String trajid = "sample_tid";
		String gid = configTrajBroke.getColumnName("GID");
		String columnTID = configTrajBroke.isStatus() ? "status_tid" : configTrajBroke.getColumnName("TID");

		double samplingGap = configTrajBroke.getSample();
		
	    for (Integer tid : tids) {
	    	Trajectory t = persistencia.fetchTrajectory(tid, configTrajBroke, columnTID);
	        int sequenceValue = persistencia.getSeq(configTrajBroke.getTableNameOrigin(), "tid");
	        List<String> querys = new ArrayList<String>();
	        for (int i = 0; i < t.getPoints().size(); i++) {
	        	TPoint p = t.getPoint(i);
	            long timeDiff = 0;
	            if (i != 0) 
	            	timeDiff = p.getTime() - t.getPoint(i - 1).getTime(); 
	            if (timeDiff/1000 <= samplingGap) {
	            	querys.add("update "+configTrajBroke.getTableNameOrigin()+" set "+trajid+"=" + sequenceValue + " where "+gid+"=" + p.getGid());
	            } else {
	            	sequenceValue = persistencia.getSeq(configTrajBroke.getTableNameOrigin(), "tid");
	            	querys.add("update "+configTrajBroke.getTableNameOrigin()+" set "+trajid+"=" + sequenceValue + " where "+gid+"=" + p.getGid());
	           }
	       }
	       persistencia.updateTID(querys);
	   }
	}

	public void splitByDistance(Set<Integer> tids) throws CreateSequenceException, DBConnectionException, SQLException, GetSequenceException, CreateStatementException {
		persistencia.createSequence(configTrajBroke.getTableNameOrigin(), "tid");
		String trajid = "distance_tid";
		String gid = configTrajBroke.getColumnName("GID");
		String columnTID = configTrajBroke.getColumnName("TID");
		if(configTrajBroke.getSample() != 0)
			columnTID = "sample_tid";
		else if(configTrajBroke.isStatus()) 
			columnTID = "status_tid";
		double distanceGap = configTrajBroke.getDistanceMax();
		
		for(Integer tid : tids){
			Trajectory t = persistencia.fetchTrajectory(tid, configTrajBroke, columnTID);
	        int sequenceValue = persistencia.getSeq(configTrajBroke.getTableNameOrigin(), "tid");
	        List<String> querys = new ArrayList<String>();
	        for (int i = 0; i < t.getPoints().size(); i++) {
	        	TPoint p = t.getPoint(i);
	        	double distance = 0;
	        	if(i != 0)
	        		distance = Utils.euclidean(p, t.getPoint(i - 1));
	        	if(distance <= distanceGap){
	        		querys.add("update "+configTrajBroke.getTableNameOrigin()+" set "+trajid+"=" + sequenceValue + " where "+gid+"=" + p.getGid());
	        	}else{
	        		sequenceValue = persistencia.getSeq(configTrajBroke.getTableNameOrigin(), "tid");
	            	querys.add("update "+configTrajBroke.getTableNameOrigin()+" set "+trajid+"=" + sequenceValue + " where "+gid+"=" + p.getGid());
	        	}
	        }
	        persistencia.updateTID(querys);
		}
	}
}