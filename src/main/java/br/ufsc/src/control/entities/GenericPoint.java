package br.ufsc.src.control.entities;

import java.sql.Timestamp;
import java.util.Date;
import java.util.ArrayList;


/**
 * 
 * @author Adriano Tosetto
 * Make software great again
 */

public class GenericPoint {
	protected ArrayList<Pair<String, Object>> attributes;
	private double y;
	private double x;
	
	private Date date;
	
	private Timestamp dateTS;
	
	public GenericPoint(ArrayList<Pair<String, Object>> attrs) {
		if(attrs == null)
			this.attributes = new ArrayList<Pair<String,Object>>();
		else
			this.attributes = attrs;
	}
	public GenericPoint() {
		this.attributes = new ArrayList<Pair<String,Object>>();
	}
	public GenericPoint(double x, double y) {
		this.x = x;
		this.y = y;
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
		throw new IllegalArgumentException("key does not exist");
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
	
	public void updateAttr(String key, Object newValue) {
		for(int i = 0; i < attributes.size(); i++) {
			if(attributes.get(i).getLeft().equals(key)) {
				attributes.set(i, new Pair<String, Object>(key, newValue));
				return;
			}
		}
		throw new IllegalArgumentException("key does not exist");
	}
	/*
	 * @override
	 * */
	public String _toString() {
		StringBuilder ret = new StringBuilder();
		for(int i = 0; i < attributes.size() - 1; i++) {
			ret.append(attributes.get(i).toString() + ";");
		}
		ret.append(attributes.get(attributes.size() - 1) + ";");
		return ret.toString();
	}
	public double getY() {
		return y;
	}
	public void setY(double y) {
		this.y = y;
	}
	public double getX() {
		return x;
	}
	public void setX(double x) {
		this.x = x;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date){
		this.date = date;
	}
	public Timestamp getDateTS() {
		return dateTS;
	}
	public void setDateTS(Timestamp dateTS) {
		this.dateTS = dateTS;
	}
}
