package br.ufsc.src.control.dataclean;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import InterfacePersistencia.TrajectoriesInterface;
import br.ufsc.src.control.entities.GenericPoint;
import br.ufsc.src.control.entities.GenericTrajectory;
import br.ufsc.src.persistencia.Persistencia;
import br.ufsc.src.persistencia.exception.AddColumnException;
import br.ufsc.src.persistencia.exception.DBConnectionException;


public class TrajectoryFeatures extends Observable{
	private int n = 0;
	private int totalSids = 0;
	private int totalTaskCompleted = 0; /* total task completed(%) */
	long startTime = 0; /* stores when the task begins */
	
	Persistencia persistencia;
	
	ArrayList<GenericTrajectory> genericTrajectories = null;
	
	
	private boolean taskEnded = false;
	
	private boolean persistantConnection = true;
	
	private ResultSet featuresResultSet;
	private int numberThreads = 1;
	private ThreadGroup threadGroup = new ThreadGroup("workers");
	
	public TrajectoryFeatures(Persistencia p) {
		this.persistencia = p;
	}
	/*
	 * 
	 * */
	public double distance(GenericPoint p1, GenericPoint p2, OptionsDistance opt) { 
		
		double R;
		
		switch (opt) {
		case KILOMETERS:
			R = 6371; // km
			break;
		
		case METERS:
			R = 6371000; //meters
		break;
		
		default:
			throw new IllegalArgumentException();
		}
		
		double dLat = Math.toRadians((p2.getY()-p1.getY()));
		double dLon = Math.toRadians(p2.getX()- p1.getX()); 
		double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
		        Math.cos(Math.toRadians(p1.getY())) * Math.cos(Math.toRadians(p2.getY())) * 
		        Math.sin(dLon/2) * Math.sin(dLon/2); 
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a)); 
	
		
		
		return R * c;
	}
	
	/*
	 * @param GenericTrajectory t
	 * @return double distância percorrida em kilometros
	 * */
	public double distance(GenericTrajectory gt, OptionsDistance opt) {
		
		int numPoints = gt.getPoints().size();
		ArrayList<GenericPoint> points = gt.getPoints();
		double totalDistance = 0;
		
		for(int i = 1; i < numPoints; i++) 
			totalDistance += distance(points.get(i - 1), points.get(i), opt);
		
		return totalDistance;
	}
	public double getSpeed(GenericTrajectory t, OptionsSpeed opt) {
		double time = 0;
		double distance = 1;
		
		switch (opt) {
		case METERS_PER_SECOND:
			time = duration(t, OptionsDuration.SECONDS);
			distance = distance(t, OptionsDistance.METERS);
			
			break;
		case KM_PER_HOUR:
			time = duration(t, OptionsDuration.HOURS);
			distance = distance(t, OptionsDistance.KILOMETERS);
			
			break;
		default:
			throw new IllegalArgumentException("");
		}
		return distance / time;
	}
	/*
	 * @param GenericTrajectory T
	 * @param Enum OPTIONS opt
	 * 
	 * @warning the method assumes that the points are ordered by time
	 * 
	 * @return duration of T
	 * */
	public double duration(GenericTrajectory  t, OptionsDuration opt) {
		
		Timestamp date1 = t.getPoints().get(0).getDateTS();
		Timestamp date2 = t.getPoints().get(t.getPoints().size() - 1).getDateTS();
		
		double diff = date2.getTime() - date1.getTime();
		
		switch(opt) {
			case SECONDS:
				return diff / 1000;
			case MINUTES:
				return diff / (1000 * 60);
			case HOURS:
				return diff / (1000 * 60 * 60);
			
			default:
				throw new IllegalArgumentException("");
		}
	}
	private void addColumns(String segTable, FeaturesConfig config) {
		if(config.hasDistance) {
			persistencia.addNewColumn(segTable, "distance" , "real");
		}
		if(config.hasDuration) {
			persistencia.addNewColumn(segTable, "duration" , "real");
		}
		if(config.hasSpeed)  {
			persistencia.addNewColumn(segTable, "speed" , "real");
		}
	}
	/*public void extractFeatures(String table, String segTable, FeaturesConfig config) throws SQLException {
		setChanged();
		notifyObservers("Adicionando novas colunas...\n");
		if(config.hasDistance) {
			persistencia.addNewColumn(segTable, "distance" , "real");
		}
		if(config.hasDuration) {
			persistencia.addNewColumn(segTable, "duration" , "real");
		}
		if(config.hasSpeed)  {
			persistencia.addNewColumn(segTable, "speed" , "real");
		}
		setChanged();
		notifyObservers("Carregando gids...\n");
		ResultSet rs = null;
		TrajectoriesInterface ti = new TrajectoriesInterface(persistencia);
		try {
			rs = persistencia.fetchRowsToResultSet("select distinct sid, start_gid, final_gid from " + segTable + " order by sid");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		setChanged();
		notifyObservers("Contando número de segmentos ...\n");
		int totalSids = 0;
		try {
			totalSids = persistencia.getNumDiffenteColumnValues(segTable, "sid");
		} catch (DBConnectionException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		setChanged();
		notifyObservers("Carregando segmentos em memória ...\n");
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int numTrajs = 0; //Numeros de segmentações
		while(rs.next()) {
			numTrajs++;
			int startGid = rs.getInt("start_gid");
			int endGid   = rs.getInt("final_gid");
			GenericTrajectory gt = ti.readTrajectory(table, startGid, endGid);
			if(config.hasDuration) {
				double duration = 0;
				if(startGid != endGid){
					if(config.argSeconds)  duration = duration(gt, OptionsDuration.SECONDS);
					if(config.argMinutes) duration = duration(gt, OptionsDuration.MINUTES);
					if(config.argHours) duration = duration(gt, OptionsDuration.HOURS);
				}
				
				persistencia.execute("Update " + segTable + " set duration = "+duration+" where start_gid = " + startGid + " and final_gid  = " + endGid, true);
			}
			if(config.hasDistance) {
				double distance = 0;
				
				if(startGid != endGid){
					if(config.argMeters) distance = distance(gt, OptionsDistance.METERS);
					if(config.argKM) distance = distance(gt, OptionsDistance.KILOMETERS);
				}
				
				persistencia.execute("Update " + segTable + " set distance = "+distance+" where start_gid = " + startGid + " and final_gid  = " + endGid, true);
			}
			if(config.hasSpeed) {
				double speed = 0;
				if(startGid != endGid) {
					if(config.argKMPerHour) speed = getSpeed(gt, OptionsSpeed.KM_PER_HOUR);
					if(config.argMetersPerSecond) speed = getSpeed(gt, OptionsSpeed.METERS_PER_SECOND);
				}
				persistencia.execute("Update " + segTable + " set speed = "+speed+" where start_gid = " + startGid + " and final_gid  = " + endGid, true);
			}

			setChanged();
			
			notifyObservers(numTrajs + " linhas escritas de um total de "+totalSids+"...\n");
			setChanged();
			notifyObservers(Integer.toString(((100*numTrajs/totalSids))));
		}
	}*/
	public void loadResultSet(String segTable) {
		try {
			featuresResultSet = persistencia.fetchRowsToResultSet("select distinct sid, start_gid, final_gid from " + segTable + " order by sid");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public void extractFeaturesParallel(String table, String segTable, FeaturesConfig config) throws SQLException {
		while(!taskEnded) {
			ResultSet aux = null;
			int startGid;
			int endGid;
			synchronized(this) {
				if(featuresResultSet.next()) {
					aux = featuresResultSet;
					startGid = aux.getInt("start_gid");
					endGid   = aux.getInt("final_gid");
				} else {
					taskEnded = true;
					totalTaskCompleted = 0;
					continue;
				}
			}
			extracFeatures(table, segTable, config, startGid, endGid);
		}
	}
	public void doItAll(String table, String segTable, FeaturesConfig config, int nThreads) {
		startTime = System.nanoTime();
		loadResultSet(segTable);
		setChanged();
		notifyObservers("Adicionando novas colunas...\n");
		if(config.isWriteOriginal())
			addColumns(table, config);
		else
			addColumns(segTable,config);
		
		setChanged();
		notifyObservers("Contando número de segmentos ...\n");
		try {
			totalSids = persistencia.getNumDiffenteColumnValues(segTable, "sid");
		} catch (DBConnectionException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Thread[] workers = new Thread[nThreads];
		
		for(int i = 0; i < workers.length; i++) {
			workers[i] = new Thread(threadGroup,new Runnable() {

				@Override
				public void run() {
					try {
						extractFeaturesParallel(table, segTable, config);
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			});
		}
		for(int i = 0; i < workers.length; i++) workers[i].start();
		
		
		for(int i = 0; i < workers.length; i++) {
			try {
				workers[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		persistencia.closePersistentConnection();
		
	}
	public void extracFeatures(String table, String segTable, FeaturesConfig config, int startGid, int endGid) throws SQLException {
		n++;
		TrajectoriesInterface ti = new TrajectoriesInterface(persistencia);
		GenericTrajectory gt = null;
		
		gt = ti.readTrajectory(table, startGid,endGid);
		
		if(config.hasDuration) {
			double duration = 0;
			if(startGid != endGid){
				if(config.argSeconds)  duration = duration(gt, OptionsDuration.SECONDS);
				if(config.argMinutes) duration = duration(gt, OptionsDuration.MINUTES);
				if(config.argHours) duration = duration(gt, OptionsDuration.HOURS);
			}
			
			if(config.isWriteOriginal())
				persistencia.execute("Update " + table + " set duration = "+duration+" where gid between " + startGid + " and " + endGid, persistantConnection);
			else
				persistencia.execute("Update " + segTable + " set duration = "+duration+" where start_gid = " + startGid + " and final_gid  = " + endGid, persistantConnection);
		}
		if(config.hasDistance) {
			double distance = 0;
			if(startGid != endGid){
				if(config.argMeters) distance = distance(gt, OptionsDistance.METERS);
				if(config.argKM) distance = distance(gt, OptionsDistance.KILOMETERS);
			}
			if(config.isWriteOriginal()) {
				persistencia.execute("Update " + table + " set distance = "+distance+" where gid between " + startGid + " and " + endGid, persistantConnection);
			} else 
				persistencia.execute("Update " + segTable + " set distance = "+distance+" where start_gid = " + startGid + " and final_gid  = " + endGid, persistantConnection);
		}
		if(config.hasSpeed) {
			double speed = 0;
			if(startGid != endGid) {
				if(config.argKMPerHour) speed = getSpeed(gt, OptionsSpeed.KM_PER_HOUR);
				if(config.argMetersPerSecond) speed = getSpeed(gt, OptionsSpeed.METERS_PER_SECOND);
			}
			if(config.isWriteOriginal()) {
				persistencia.execute("Update " + table + " set speed = "+speed+" where gid between " + startGid + " and " + endGid, persistantConnection);
			} else
				persistencia.execute("Update " + segTable + " set speed = "+speed+" where start_gid = " + startGid + " and final_gid  = " + endGid, persistantConnection);
		}
		
		/* 1% more of the task was completed*/
		
		if ( ((100*n)/totalSids) - totalTaskCompleted > 1) {
			setChanged();
			notifyObservers(n + " linhas escritas de um total de "+totalSids+"...\n");
			setChanged();
			notifyObservers(Integer.toString(((100*n/totalSids))));
			totalTaskCompleted = (100*n)/totalSids;
		}
		if(totalTaskCompleted == 100) {
			final long duration = System.nanoTime() - startTime;
			final double seconds = ((double)duration / 1000000000);
			final double minutes = ((double)seconds / 60);
			final double hours   = ((double)minutes / 60);
			setChanged();
			notifyObservers("Completed!\n" + 
			("Time : " + seconds + " Seconds\n") + 
			("Time : " + minutes + " Minutes\n") + 
			("Time : " + hours + " Hours"));
		}

	}
	public int getNumberThreads() {
		return numberThreads;
	}
	public void setNumberThreads(int numberThreads) {
		this.numberThreads = numberThreads;
	}
	public ThreadGroup getThreadGroup() {
		return threadGroup;
	}
	public void setThreadGroup(ThreadGroup threadGroup) {
		this.threadGroup = threadGroup;
	}
	
}
