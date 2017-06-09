package br.ufsc.src.control.dataclean;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import br.ufsc.src.control.Utils;
import br.ufsc.src.control.entities.TPoint;
import br.ufsc.src.control.entities.Trajectory;
import br.ufsc.src.persistencia.InterfacePersistencia;
import br.ufsc.src.persistencia.exception.AddBatchException;
import br.ufsc.src.persistencia.exception.DBConnectionException;
import br.ufsc.src.persistencia.exception.ExecuteBatchException;

public class RemoveNoise {

	private ConfigTraj configTraj;
	private InterfacePersistencia persistencia;
	
	public RemoveNoise (InterfacePersistencia persistencia, ConfigTraj configTraj){
		this.configTraj = configTraj;
		this.persistencia = persistencia;
	}

	public void findRemoveNoise(Set<Integer> tids) throws DBConnectionException, SQLException, AddBatchException, ExecuteBatchException {
		for (Integer tid : tids) {
			Trajectory traj = persistencia.fetchTrajectory(tid, configTraj, configTraj.getColumnName("TID"));
			if(configTraj.isRemoveNoiseFromFirst()){
				double speed = Double.parseDouble(configTraj.getSpeed());
				removeFromFirst(traj, speed);
			}else if(configTraj.isRemoveNoiseFromSecond()){
				double speed = Double.parseDouble(configTraj.getSpeed());
				removeFromSecond(traj, speed);
			}else if(configTraj.isDbscan()){
					dbscan(traj,configTraj.isRemoveNeighborNoise());
			}else if(configTraj.isMeanFilter() || configTraj.isMedianFilter()){
				if(configTraj.isPastPoints())
					meanMedianFilterOnlyPastPoints(traj);
				else
					meanMedianFilter(traj);
			}
		}
	}
	
	private void meanMedianFilter(Trajectory traj) throws DBConnectionException, AddBatchException, ExecuteBatchException{
		int numWindowPoints = configTraj.getNumWindowPoints();
		if(traj.length() < numWindowPoints)
			return;
		List<TPoint> pointsToUpdate = new ArrayList<TPoint>();
	    for (int i = 0; i < traj.length(); i++) {
	    	TPoint p = traj.getPoint(i);
	    	List<TPoint> pointsToFilter = new ArrayList<TPoint>();
	    	pointsToFilter.add(p);
	    	for(int z = 1; z <= numWindowPoints/2; z++){
	    		if(traj.hasNext(i+z))
	    			pointsToFilter.add(traj.getPoint(i+z));
	    		if(traj.hasPrevious(i-z) && pointsToFilter.size() != numWindowPoints)
	    			pointsToFilter.add(traj.getPoint(i-z));
	    	}
	    	if(pointsToFilter.size() == numWindowPoints){
	    		if(configTraj.isMeanFilter())
	    			pointsToUpdate.add(Utils.mean(pointsToFilter));
	    		else if(configTraj.isMedianFilter())
	    			pointsToUpdate.add(Utils.median(pointsToFilter));
	    	}
	    }
	    persistencia.updateGIDs(pointsToUpdate, configTraj);
	}
	
	private void meanMedianFilterOnlyPastPoints(Trajectory traj) throws DBConnectionException, AddBatchException, ExecuteBatchException{
		int numWindowPoints = configTraj.getNumWindowPoints();
		if(traj.length() < numWindowPoints)
			return;
		List<TPoint> pointsToUpdate = new ArrayList<TPoint>();
	    for (int i = 0; i < traj.length(); i++) {
	    	TPoint p = traj.getPoint(i);
	    	List<TPoint> pointsToFilter = new ArrayList<TPoint>();
	    	pointsToFilter.add(p);
	    	for(int z = 1; z < numWindowPoints; z++){
	    		if(traj.hasPrevious(i-z) || i-z == 0)
	    			pointsToFilter.add(traj.getPoint(i-z));
	    	}
	    	if(pointsToFilter.size() == numWindowPoints){
	    		if(configTraj.isMeanFilter())
	    			pointsToUpdate.add(Utils.mean(pointsToFilter));
	    		else if(configTraj.isMedianFilter())
	    			pointsToUpdate.add(Utils.median(pointsToFilter));
	    	}	 	    
	    }
	    persistencia.updateGIDs(pointsToUpdate, configTraj);
	}
	
	private void dbscan(Trajectory traj, boolean neighborAndNoise ) throws DBConnectionException, SQLException{
		if(traj.length() < configTraj.getMinPoints())
			return;
        List<Integer> gidsToRemove = new ArrayList<Integer>();
        int minPoints = configTraj.getMinPoints();
        double maxDistance = configTraj.getDistancePoints();
        int i = 0;
        while (i < traj.length()) {
        	int nearPoints = 0;
        	TPoint p = traj.getPoint(i);
        	if(traj.hasNext(i)){
        		int numNoises = 0;
        		for(int cont = 1; i+cont < traj.length() && (numNoises < minPoints && nearPoints < minPoints); cont++){
        			TPoint pn = traj.getPoint(i+cont);
        			double distance = Utils.euclidean(p, pn);
        			if(distance <= maxDistance){
        				nearPoints++;
        				numNoises = 0;
        			}else
        				numNoises++;
        		}     	
        	}
        	if(nearPoints < minPoints && traj.hasPrevious(i)){
        		int numNoises = 0;
        		for(int cont = 1; i-cont >= 0 && (numNoises < minPoints && nearPoints < minPoints); cont++){
        			TPoint pn = traj.getPoint(i-cont);
        			double distance = Utils.euclidean(p, pn);
        			if(distance <= maxDistance){
        				nearPoints++;
        				numNoises = 0;
        			}else
        				numNoises++;
        		}
        	}
        	if(!neighborAndNoise && nearPoints == 0){
        		gidsToRemove.add(p.getGid());
        		traj.getPoints().remove(i);
        	}else if(neighborAndNoise && nearPoints < minPoints){
        		gidsToRemove.add(p.getGid());
        		traj.getPoints().remove(i);
        	}else
        		i++;
        }
        persistencia.deleteByGids(gidsToRemove, configTraj.getTableNameOrigin());
	}

	private void removeFromFirst(Trajectory traj, double speed) throws DBConnectionException, SQLException {
		Trajectory t = traj;
		while(hasExtremeNoiseTraj(t, speed))
			 t = removeFirstExtremeNoise(traj, speed);
	}
	
	private Trajectory removeFirstExtremeNoise(Trajectory T, double maxSpeed) throws DBConnectionException, SQLException {
        int i = 0;
        List<Integer> gidsToRemove = new ArrayList<Integer>();
        while (i < T.length() - 1) {
            TPoint p1 = T.getPoint(i);
            TPoint p2 = T.getPoint(i + 1);
            long timeDiff = (p2.getTime() - p1.getTime()) / 1000;
            double distance = Utils.euclidean(p1, p2);
            double speed = distance / (double) timeDiff;
            if (speed > maxSpeed) {
            	gidsToRemove.add(T.getPoint(i).getGid());
                T.getPoints().remove(i);
            } else {
                i++;
            }

        }
        persistencia.deleteByGids(gidsToRemove,configTraj.getTableNameOrigin());
        return T;
    }
	
	private void removeFromSecond(Trajectory traj, double speed) throws DBConnectionException, SQLException {
		Trajectory t = traj;
		while(hasExtremeNoiseTraj(t, speed))
			t = removeSecondExtremeNoise(traj, speed);
	}
	
	private Trajectory removeSecondExtremeNoise(Trajectory T, double maxSpeed) throws DBConnectionException, SQLException {
        int i = 0;
        List<Integer> gidsToRemove = new ArrayList<Integer>();
        while (i < T.length() - 1) {
            TPoint p1 = T.getPoint(i);
            TPoint p2 = T.getPoint(i + 1);
            long timeDiff = (p2.getTime() - p1.getTime()) / 1000;
            double distance = Utils.euclidean(p1, p2);
            double speed = distance / (double) timeDiff;
            if (speed > maxSpeed) {
                gidsToRemove.add(T.getPoint(i+1).getGid());
                T.getPoints().remove(i+1);
            } else {
                i++;
            }
        }
        persistencia.deleteByGids(gidsToRemove,configTraj.getTableNameOrigin());
        return T;
    }
	
	private boolean hasExtremeNoiseTraj(Trajectory T, double maxSpeed) {
		for (int i = 0; i < T.length() - 1; i++) {
			TPoint p1 = T.getPoint(i);
			TPoint p2 = T.getPoint(i + 1);
			long timeDiff = (p2.getTime() - p1.getTime()) / 1000;
			double distance = Utils.euclidean(p1, p2);
			double speed = distance / (double) timeDiff;;
			if (speed > maxSpeed) {
				return true;
	        }
	     }
	     return false;
	 }
}