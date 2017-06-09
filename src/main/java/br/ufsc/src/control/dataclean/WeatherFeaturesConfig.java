package br.ufsc.src.control.dataclean;

public class WeatherFeaturesConfig {

	public WeatherFeaturesConfig() {
		clear();
	}
	public boolean isHasTemperature() {
		return hasTemperature;
	}
	public void setHasTemperature(boolean hasTemperature) {
		if(hasTemperature) setNumFeatures(getNumFeatures() + 1);
		else setNumFeatures(getNumFeatures() - 1);
		this.hasTemperature = hasTemperature;
	}
	public boolean isHasCond() {
		return hasCond;
	}
	public void setHasCond(boolean hasCond) {
		if(hasCond) setNumFeatures(getNumFeatures() + 1);
		else setNumFeatures(getNumFeatures() - 1);
		this.hasCond = hasCond;
	}
	public boolean isHasHum() {
		return hasHum;
	}
	public void setHasHum(boolean hasHum) {
		if(hasHum) setNumFeatures(getNumFeatures() + 1);
		else setNumFeatures(getNumFeatures() - 1);
		this.hasHum = hasHum;
	}
	public boolean isHasRain() {
		return hasRain;
	}
	public void setHasRain(boolean hasRain) {
		if(hasRain) setNumFeatures(getNumFeatures() + 1);
		else setNumFeatures(getNumFeatures() - 1);
		this.hasRain = hasRain;
	}
	public boolean isHasHail() {
		return hasHail;
	}
	public void setHasHail(boolean hasHail) {
		if(hasHail) setNumFeatures(getNumFeatures() + 1);
		else setNumFeatures(getNumFeatures() - 1);
		this.hasHail = hasHail;
	}
	public boolean isHasThunder() {
		return hasThunder;
	}
	public void setHasThunder(boolean hasThunder) {
		if(hasThunder) setNumFeatures(getNumFeatures() + 1);
		else setNumFeatures(getNumFeatures() - 1);
		this.hasThunder = hasThunder;
	}
	public boolean isHasTornado() {
		return hasTornado;
	}
	public void setHasTornado(boolean hasTornado) {
		if(hasTornado) setNumFeatures(getNumFeatures() + 1);
		else setNumFeatures(getNumFeatures() - 1);
		this.hasTornado = hasTornado;
	}
	public void setTable(String table) {
		this.table = table;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public void clear() {
		hasCond        = false;
		hasHail        = false;
		hasRain        = false;
		hasThunder     = false;
		hasTemperature = false;
		hasTornado     = false;
		hasHum         = false;
		setNumFeatures(0);
	}
	public String getTable() {
		return table;
	}

	public int getNumFeatures() {
		return numFeatures;
	}
	public void setNumFeatures(int numFeatures) {
		this.numFeatures = numFeatures;
	}

	public boolean isWriteOriginal() {
		return writeOriginal;
	}
	public void setWriteOriginal(boolean writeOriginal) {
		this.writeOriginal = writeOriginal;
	}

	public String getSegTable() {
		return segTable;
	}
	public void setSegTable(String segTable) {
		this.segTable = segTable;
	}

	private boolean hasTemperature;
	private boolean hasCond;
	private boolean hasHum;
	private boolean hasRain;
	private boolean hasHail;
	private boolean hasThunder;
	private boolean hasTornado;
	private String table;
	private String segTable;
	private String city;
	private boolean writeOriginal = false;
	private int numFeatures = 0;
}
