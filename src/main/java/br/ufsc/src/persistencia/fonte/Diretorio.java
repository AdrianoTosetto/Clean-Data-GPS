package br.ufsc.src.persistencia.fonte;

import java.util.ArrayList;

public class Diretorio {
	private String url;
	private boolean igExtension;
	private ArrayList<String>extension = new ArrayList<String>();
	private ArrayList<String>igFolder = new ArrayList<String>();
	private ArrayList<String>igFile = new ArrayList<String>();
	
	public String toString(){
		return "------- Diretorio -------"
				+"\n url: "+url
				+"\n Ignore extension: "+igExtension
				+"\n Extension: "+extension.toString()
				+"\n Ignore folders: "+igFolder.toString()
				+"\n Ignore files: "+igFile.toString();
	}
	
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public boolean isIgExtension() {
		return igExtension;
	}
	public void setIgExtension(boolean igExtension) {
		this.igExtension = igExtension;
	}
	public ArrayList<String> getExtension() {
		return extension;
	}
	public void setExtension(ArrayList<String> extension) {
		this.extension = extension;
	}
	public ArrayList<String> getIgFolder() {
		return igFolder;
	}
	public void setIgFolder(ArrayList<String> igFolder) {
		this.igFolder = igFolder;
	}
	public ArrayList<String> getIgFile() {
		return igFile;
	}
	public void setIgFile(ArrayList<String> igFile) {
		this.igFile = igFile;
	}
}