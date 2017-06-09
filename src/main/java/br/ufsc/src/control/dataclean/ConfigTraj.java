package br.ufsc.src.control.dataclean;

import br.ufsc.src.control.entities.TPoint;

public class ConfigTraj {
	private Object[][] tableData;
	private String tableNameOrigin;
	private String accuracy;
	private String speed;
	private int sample;
	private double distanceMax;
	private String columnTID;
	private int minPoints;
	private double distancePoints;
	private int numWindowPoints;
	private TPoint point;
	
	private boolean status;
	private boolean removeNoiseFromFirst;
	private boolean removeNoiseFromSecond;
	private boolean dbscan;
	private boolean meanFilter;
	private boolean medianFilter;
	private boolean pastPoints;
	private boolean removeNeighborNoise;
	
	public ConfigTraj(Object[][] tableData, String tableNameOrigin, int sample, double distanceMax, boolean status){
		this.tableData = tableData;
		this.tableNameOrigin = tableNameOrigin;
		this.sample = sample;
		this.status = status;
		this.distanceMax = distanceMax;
	}
	
	public ConfigTraj(Object[][] tableData, String tableNameOrigin){
		this.tableData = tableData;
		this.tableNameOrigin = tableNameOrigin;
	}
	
	public ConfigTraj(Object[][] tableData, String tableNameOrigin, TPoint point, double distanceBuffer){
		this.tableData = tableData;
		this.tableNameOrigin = tableNameOrigin;
		this.point = point;
		this.distanceMax = distanceBuffer;
	}

	public Object[][] getTableData() {
		return tableData;
	}

	public void setTableData(Object[][] tableData) {
		this.tableData = tableData;
	}

	public String getTableNameOrigin() {
		return tableNameOrigin;
	}

	public void setTableNameOrigin(String tableNameOrigin) {
		this.tableNameOrigin = tableNameOrigin;
	}

	public String getAccuracy() {
		return accuracy;
	}

	public void setAccuracy(String accuracy) {
		this.accuracy = accuracy;
	}

	public String getSpeed() {
		return speed;
	}

	public void setSpeed(String speed) {
		this.speed = speed;
	}

	public double getSample() {
		return sample;
	}

	public void setSample(int sample) {
		this.sample = sample;
	}

	public boolean isStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}
	
	public double getDistanceMax() {
		return distanceMax;
	}

	public void setDistanceMax(double distanceMax) {
		this.distanceMax = distanceMax;
	}
	
	public String getColumnName(String column){
		for (int i = 0; i <= tableData.length - 1; i++) {
			String colName = (String)tableData[i][0];
			String colKind = tableData[i].length > 1 ? (String)tableData[i][1] : null;
			if(colKind != null && colKind.equalsIgnoreCase(column))
				return colName;
		}
		return "";
	}
	
	public String getColumnTID() {
		return columnTID;
	}

	public void setColumnTID(String columnTID) {
		this.columnTID = columnTID;
	}

	public boolean isRemoveNoiseFromFirst() {
		return removeNoiseFromFirst;
	}

	public void setRemoveNoiseFromFirst(boolean removeNoiseFromFirst) {
		this.removeNoiseFromFirst = removeNoiseFromFirst;
	}

	public boolean isRemoveNoiseFromSecond() {
		return removeNoiseFromSecond;
	}

	public void setRemoveNoiseFromSecond(boolean removeNoiseFromSecond) {
		this.removeNoiseFromSecond = removeNoiseFromSecond;
	}

	public boolean isDbscan() {
		return dbscan;
	}

	public void setDbscan(boolean dbscan) {
		this.dbscan = dbscan;
	}

	public boolean isMeanFilter() {
		return meanFilter;
	}

	public void setMeanFilter(boolean meanFilter) {
		this.meanFilter = meanFilter;
	}

	public boolean isMedianFilter() {
		return medianFilter;
	}

	public void setMedianFilter(boolean medianFilter) {
		this.medianFilter = medianFilter;
	}

	public int getMinPoints() {
		return minPoints;
	}

	public void setMinPoints(int minPoints) {
		this.minPoints = minPoints;
	}

	public double getDistancePoints() {
		return distancePoints;
	}

	public void setDistancePoints(double distancePoints) {
		this.distancePoints = distancePoints;
	}

	public int getNumWindowPoints() {
		return numWindowPoints;
	}

	public void setNumWindowPoints(int numWindowPoints) {
		this.numWindowPoints = numWindowPoints;
	}

	public boolean isPastPoints() {
		return pastPoints;
	}

	public void setPastPoints(boolean pastPoints) {
		this.pastPoints = pastPoints;
	}

	public boolean isRemoveNeighborNoise() {
		return removeNeighborNoise;
	}

	public void setRemoveNeighborNoise(boolean removeNeighborNoise) {
		this.removeNeighborNoise = removeNeighborNoise;
	}

	public TPoint getPoint() {
		return point;
	}

	public void setPoint(TPoint point) {
		this.point = point;
	}
	
	
}