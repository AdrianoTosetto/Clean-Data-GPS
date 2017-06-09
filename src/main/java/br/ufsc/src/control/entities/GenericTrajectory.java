package br.ufsc.src.control.entities;

import java.sql.Date;
import java.util.ArrayList;

/**
 * 
 * @author Adriano Tosetto
 * Make software great again
 */

public class GenericTrajectory {
	protected ArrayList<Pair<String, Object>> attributes;
	private ArrayList<GenericPoint> points = new ArrayList<GenericPoint>();
	private  int tid;

	public GenericTrajectory(ArrayList<Pair<String, Object>> attrs) {
		if(attrs == null)
			this.attributes = new ArrayList<Pair<String,Object>>();
		else
			this.attributes = attrs;
	}
	public GenericTrajectory(int tid) {
		this.tid = tid;
		this.attributes = new ArrayList<Pair<String,Object>>();
	}
	/*
	 * @param String key, the column name from data base 
	 * */
	public Object getAttr(String key) {
		for(int i = 0; i < attributes.size(); i++) {
			if(attributes.get(i).getLeft().equals(key)) {
				return attributes.get(i).getRight();
			}
		}
		throw new IllegalArgumentException("Column does not exist");
	}
	/*
	 * @info this class was made to accept any data type, in order to
	 * make Trajectory most generic as possible  
	 *
	 * @param String key column name from data base
	 * @param Object value data from data base 
	 * */
	public void addNewAttr(String key, Object value) {
		this.attributes.add(new Pair<String, Object>(key, value));
	}
	public void addPoint(GenericPoint p) {
		this.points.add(p);
	}
	public ArrayList<GenericPoint> getPoints() {
		return this.points;
	}
	public int getTid() {
		return tid;
	}
	public void setTid(int tid) {
		this.tid = tid;
	}
}
