package br.ufsc.src.control.datafeatures;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Observable;
import java.util.Scanner;

import br.ufsc.src.control.dataclean.WeatherFeaturesConfig;
import br.ufsc.src.control.http.HTTPRequest;
import br.ufsc.src.lib.org.json.JSONArray;
import br.ufsc.src.lib.org.json.JSONObject;
import br.ufsc.src.persistencia.Persistencia;
import br.ufsc.src.persistencia.exception.AddColumnException;
import br.ufsc.src.persistencia.exception.DBConnectionException;

public class WeatherFeatures extends Observable{
	
	Persistencia p;
	String conds[]         = new String[24];
	double temperatures[]  = new double[24];
	double hum []          = new double[24];
  	int rain[]             = new int[24];
  	int tornado[]          = new int[24];
  	int thunder[]          = new int[24];
  	int hail[]             = new int[24];
	
	public WeatherFeatures(Persistencia p) {
		this.p = p;
	}
	
	@SuppressWarnings("deprecation")
	public int numDays(String table) throws SQLException {
		
		ResultSet rs = null;
		
		try {
			rs = p.fetchRowsToResultSet("select time from " + table + " order by time");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		int day = -1;
		int month = -1;
		int year = -1;
		int currentDay= -1;
		int currentMonth = -1;
		int currentYear = -1;
		int ndays = 0;
		boolean update = true;
		
		while(rs.next()) {
			Timestamp t = rs.getTimestamp("time");
			if(update) {
				day = t.getDay();
				month = t.getMonth();
				year  = t.getYear();
				update = false;
			}
			currentDay   = t.getDay();
			currentMonth = t.getMonth();
			currentYear  = t.getYear();
			
			if(day != currentDay || month != currentMonth || year != currentYear) {
				ndays++;
				update = true;
			}
			
		}
		
		return ndays;
	}
	@SuppressWarnings("deprecation")
	private boolean dayChanged(Timestamp t1, Timestamp t2) {
		
		return t1.getDay() != t2.getDay() || t1.getMonth() != t2.getMonth() || t1.getYear() != t2.getYear(); 
	}
	@SuppressWarnings({ "unused", "deprecation" })
	private DayPeriod getDayPeriod(Timestamp t) {
		
		int currentHours = t.getHours();
		if(currentHours  >= 0  && currentHours < 6) return DayPeriod.EARLY_MORNING;
		if(currentHours  >= 6  && currentHours < 12) return DayPeriod.MORNING;
		if(currentHours  >= 12 && currentHours < 18) return DayPeriod.AFTERNOON;
		if(currentHours  >= 18 && currentHours < 24) return DayPeriod.EVENING;
		
		
		return DayPeriod.AFTERNOON;
	}
	@SuppressWarnings("unused")
	private DayPeriod getDayPeriod(int currentHours) {
		if(currentHours  >= 0  && currentHours < 6) return DayPeriod.EARLY_MORNING;
		if(currentHours  >= 6  && currentHours < 12) return DayPeriod.MORNING;
		if(currentHours  >= 12 && currentHours < 18) return DayPeriod.AFTERNOON;
		if(currentHours  >= 18 && currentHours < 24) return DayPeriod.EVENING;
		
		
		throw new IllegalArgumentException();
	}
	private void addColumns(WeatherFeaturesConfig config) throws DBConnectionException {
		
		if(config.isHasCond()) p.addNewColumn(config.getTable(), "cond", "varchar");
		if(config.isHasHail()) p.addNewColumn(config.getTable(), "hail", "integer");
		if(config.isHasHum())  p.addNewColumn(config.getTable(), "hum", "real");
		if(config.isHasRain()) p.addNewColumn(config.getTable(), "rain", "integer");
		if(config.isHasTemperature()) p.addNewColumn(config.getTable(), "temperature", "real");
		if(config.isHasThunder()) p.addNewColumn(config.getTable(), "thunder", "integer");
		if(config.isHasTornado()) p.addNewColumn(config.getTable(), "tornado", "integer");
	}
	private void deleteColumn(WeatherFeaturesConfig config) {
		if(config.isHasCond()) p.addNewColumn(config.getTable(), "cond", "");
		if(config.isHasHail()) p.addNewColumn(config.getTable(), "hail", "integer");
		if(config.isHasHum())  p.addNewColumn(config.getTable(), "hail", "real");
		if(config.isHasRain()) p.addNewColumn(config.getTable(), "rain", "integer");
		if(config.isHasTemperature()) p.addNewColumn(config.getTable(), "temperature", "real");
		if(config.isHasThunder()) p.addNewColumn(config.getTable(), "thunder", "integer");
		if(config.isHasTornado()) p.addNewColumn(config.getTable(), "tornado", "integer");
	}
	private String assembleURLRequest(WeatherFeaturesConfig config, Timestamp t) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(t.getTime());
		int day   = cal.get(Calendar.DAY_OF_MONTH);
		int month = t.getMonth() + 1;
		int year  = t.getYear() + 1900;
		return "http://api.wunderground.com/api/b9e4e0bf0765fc87/history_" +
				year +""+ (month < 10 ? "0" + month: month) +""+ (day < 10 ? "0" + day: day)+
				"/q/CA/" +"san_Francisco"+ ".json";
	}
	private int countDays(ResultSet rs) throws SQLException {
		rs.next();
		Timestamp t1 = rs.getTimestamp("time");
		rs.last();
		Timestamp t2 = rs.getTimestamp("time");
		rs.first();
		
		return (int) Math.ceil((double)(t2.getTime() - t1.getTime()) / (1000*60*60*24));
	}
	@SuppressWarnings("deprecation")
	public void extractFeatures(WeatherFeaturesConfig config) throws SQLException {
		try {
			addColumns(config);
		} catch (DBConnectionException e) {
			e.printStackTrace();
		}
		ResultSet rs = p.fetchRowsToResultSet("select time, gid from " + config.getTable() + " order by time");

		int totalDays = countDays(rs);
		int  days      = 1;
		Timestamp previous = null; /* first day of the points set */
		Timestamp current  = null; /* current day of the points set */
		int currentGid = 0;
		int hour = -1;
		boolean first = true;
		ArrayList<Integer> gids = new ArrayList<Integer>();
		rs.previous();
		while(rs.next()) {
			if(first) {
				setChanged();
				notifyObservers(days  + " of  "+totalDays+" done\n"+"Requesting info for a new day...\n");
				previous = rs.getTimestamp("time");
				String URLRequest = assembleURLRequest(config, previous);
				request(URLRequest, config);
			}
			current = rs.getTimestamp("time");
			System.out.println(current);
			currentGid = rs.getInt("gid");
			first = false;
			int currentHour = rs.getTimestamp("time").getHours();
			gids.add(rs.getInt("gid"));
			if(hour == -1) {
				hour = currentHour;
				continue;
			}
			if(currentHour != hour || rs.isLast()) {
				if(!rs.isLast()) gids.remove(gids.size() - 1);
				p.execute(assembleSQL(config, gids, hour));
				System.out.println(gids);
				gids = new ArrayList<Integer>();
				gids.add(currentGid);
				hour = currentHour;
			}
			
			/* new request */
			
			if(dayChanged(previous, current)) {
				first = true; /* Update the first day of the set. Set changed */
				rs.previous(); /* force to point to the first day */
				days++;
				setChanged();
				notifyObservers(Integer.toString((days*100)/totalDays));
				notifyObservers("Carregando dados para o dia " + current.getDay() + "/" + current.getMonth() + "/" + (current.getYear()) + 1900);
			}
		}
		p.closePersistentConnection(); /* finished writing */
	}
	private String assembleSQL(WeatherFeaturesConfig config, ArrayList<Integer> gids, int currentHour) {
		int numFeatures = config.getNumFeatures();
		StringBuilder sql = new StringBuilder("update " + config.getTable() + " set");
		if(config.isHasCond()) {
			numFeatures--;
			if(numFeatures > 0)
				sql.append(" cond = '" + conds[currentHour] + "', ");
			else
				sql.append(" cond = '" + conds[currentHour] + "'");
		}
		if(config.isHasHum()) {
			numFeatures--;
			if(numFeatures > 0)
				sql.append(" hum = '" + hum[currentHour] + "', ");
			else
				sql.append(" hum = '" + hum[currentHour] +"'");
		}
		if(config.isHasRain()) {
			numFeatures--;
			if(numFeatures > 0)
				sql.append(" rain = '" + rain[currentHour] + "', ");
			else
				sql.append(" rain = '" + rain[currentHour] + "'");
		}
		if(config.isHasThunder()) {
			numFeatures--;
			if(numFeatures > 0)
				sql.append(" thunder = '" + thunder[currentHour] + "', ");
			else
				sql.append(" thunder = '" + thunder[currentHour] + "'");
		}
		if(config.isHasHail()) {
			numFeatures--;
			if(numFeatures > 0)
				sql.append(" hail = '" + hail[currentHour] + "', ");
			else
				sql.append(" hail = '" + hail[currentHour] + "'");
		}
		if(config.isHasTornado()) {
			numFeatures--;
			if(numFeatures > 0)
				sql.append(" tornado = '" + tornado[currentHour] + "', ");
			else
				sql.append(" tornado = '" + tornado[currentHour]+"'");
		}
		if(config.isHasTemperature()) {
			numFeatures--;
			if(numFeatures > 0)
				sql.append(" temperature = '" + temperatures[currentHour] + "', ");
			else
				sql.append(" temperature = '" + temperatures[currentHour] + "'");
		}
		sql.append(" where ");
		for(int i = 0; i < gids.size(); i++) {
			if(i == 0)
				sql.append("gid = " + gids.get(i));
			else 
				sql.append(" or gid = " + gids.get(i));
		}
		return sql.toString();
	}
	@SuppressWarnings("unused")
	private String readFile(String pathname) throws IOException {

	    File file = new File(pathname);
	    StringBuilder fileContents = new StringBuilder((int)file.length());
	    Scanner scanner = new Scanner(file);
	    String lineSeparator = System.getProperty("line.separator");

	    try {
	        while(scanner.hasNextLine()) {
	            fileContents.append(scanner.nextLine() + lineSeparator);
	        }
	        return fileContents.toString();
	    } finally {
	        scanner.close();
	    }
	}
	public void request(String url, WeatherFeaturesConfig config) {
		String json = null;
		try {
			HTTPRequest http = new HTTPRequest();
			http.setURLRequest(url);
			json = http.sendGet(); //readFile("C:\\Users\\Usuário\\Desktop\\Clean-Data-GPS-master\\Clean-Data-GPS-master\\src\\main\\java\\br\\ufsc\\src\\control\\datafeatures\\san_Francisco.txt");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		JSONObject obj = new JSONObject(json);
		
		JSONArray arr = obj.getJSONObject("history").getJSONArray("observations");
		System.out.println(url);
		
		for(int i = 0; i < arr.length(); i++) {
			
			JSONObject obj1 = arr.getJSONObject(i);
			int currentHour = Integer.parseInt(obj1.getJSONObject("utcdate").get("hour").toString());
			
			if(config.isHasCond())        conds[currentHour]        = obj1.getString("conds");
			if(config.isHasHum())         hum[currentHour]          = obj1.getDouble("hum");
			if(config.isHasRain())        rain[currentHour]         = obj1.getInt("rain");
			if(config.isHasThunder())     thunder[currentHour]      = obj1.getInt("thunder");
			if(config.isHasHail())		  hail[currentHour]         = obj1.getInt("hail");
			if(config.isHasTornado())     tornado[currentHour]      = obj1.getInt("tornado");
			if(config.isHasTemperature()) temperatures[currentHour] = obj1.getDouble("tempm");
			
		}
		
	}
}
