package br.ufsc.src.control.http;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class HTTPRequest {

	private final String USER_AGENT = "Mozilla/5.0";
	private static final String API_KEY = "b9e4e0bf0765fc87";
	
	private String URLRequest = null;

	public void setURLRequest(final String s) {
		URLRequest = s;
	}
	public String sendGet() throws Exception {
		System.out.println(URLRequest);
		String url = URLRequest;
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		// optional default is GET
		con.setRequestMethod("GET");

		//add request header
		con.setRequestProperty("User-Agent", USER_AGENT);

		int responseCode = con.getResponseCode();

		if(responseCode != 200) 
			throw new Exception("Failed to request");
		
		BufferedReader in = new BufferedReader(
		        new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
		return response.toString();
	}

}