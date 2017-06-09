package br.ufsc.src.control;

import java.io.File;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import br.ufsc.src.control.entities.Point;
import br.ufsc.src.control.entities.TPoint;
import br.ufsc.src.persistencia.exception.TimeStampException;

public class Utils {
	
	public static String getTimeStamp(String date, String time, String dateFormat, String timeFormat, boolean usaTimeStamp) throws TimeStampException {
		date = date.replace("\"", "");
		time = time.replace("\"", "");
		String dt = "";
		if(usaTimeStamp){ 
			if(date.length() == 10)
				date = date+"000000";
			long newdate = Long.parseLong(date)/1000; //Considering miliseconds
			java.sql.Timestamp timeStampDate = new Timestamp(newdate);
			dt = timeStampDate.toString();
		}else if(dateFormat.indexOf('T') != -1){ //dateFormat with timezone
			dateFormat = dateFormat.replace("T", "'T'");
			DateFormat df = new SimpleDateFormat(dateFormat);
			Date result;
			date = date.replace("Z", "");
			try {
				result = df.parse(date);
			    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			    sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
			    dt = sdf.format(result);
			}catch (Exception e) {
				throw new TimeStampException(e.getMessage());
			}

		}else if(dateFormat.length() > 0){ //dateFormat to dateFormat
				timeFormat = " "+timeFormat;
			try {
				DateFormat formatter;
				formatter = new SimpleDateFormat(dateFormat+""+timeFormat);
				Date newdate = (Date) formatter.parse(date + " " + time);
				java.sql.Timestamp timeStampDate = new Timestamp(newdate.getTime());
				dt = timeStampDate.toString();
				} catch (ParseException e) {
					throw new TimeStampException(e.getMessage());
				}
		}
		return dt;
	}
	
	public static String getDurationBreakdown(long millis)
    {
        if(millis < 0)
        {
            throw new IllegalArgumentException("Duration must be greater than zero!");
        }

        long days = TimeUnit.MILLISECONDS.toDays(millis);
        millis -= TimeUnit.DAYS.toMillis(days);
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);
        millis -= TimeUnit.SECONDS.toMillis(seconds);
        long milliseconds = TimeUnit.MILLISECONDS.toMillis(millis);

        StringBuilder sb = new StringBuilder(64);
        if(days != 0){
        	sb.append(days);
        	sb.append(" Days ");
        }if(hours != 0){
        	sb.append(hours);
        	sb.append(" Hours ");
        }if(minutes != 0){
        	sb.append(minutes);
        	sb.append(" Minutes ");
        }if(seconds != 0){
        	sb.append(seconds);
        	sb.append(" Seconds ");
        }
       	sb.append(milliseconds);
        sb.append(" MilliSeconds");

        return(sb.toString());
    }
	
	public static String getFileExtension(File file) {
		String fileName = file.getName();
		if (fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
			return fileName.substring(fileName.lastIndexOf(".") + 1);
		else
			return "";
	}
	
	public static boolean isNumeric(String s) {  
	    return s.matches("[-+]?\\d*\\.?\\d+");  
	} 
	
	public static boolean isStringEmpty(String st){
		return st.trim().equalsIgnoreCase("");
	}
	
	public static double euclidean(Point p1,Point p2){
		double distX = Math.abs(p1.getX()-p2.getX());
		double distXSquare = distX*distX;
		double distY = Math.abs(p1.getY()-p2.getY());
		double distYSquare = distY*distY;
		return Math.sqrt(distXSquare+distYSquare);
	}

	public static TPoint mean(List<TPoint> pointsToFilter) {
		TPoint p = new TPoint(pointsToFilter.get(0).getX(), pointsToFilter.get(0).getY());
		p.setGid(pointsToFilter.get(0).getGid());
		double x = 0;
		double y = 0;		
		for (TPoint tPoint : pointsToFilter) {
			x += tPoint.getX();
			y += tPoint.getY();
		}
		p.setX(x/pointsToFilter.size());
		p.setY(y/pointsToFilter.size());
		return p;
	}

	public static TPoint median(List<TPoint> pointsToFilter) {
		List<Double> x = new ArrayList();
		List<Double> y = new ArrayList();
		TPoint p = new TPoint(pointsToFilter.get(0).getX(), pointsToFilter.get(0).getY());
		p.setGid(pointsToFilter.get(0).getGid());
		
		for (TPoint point : pointsToFilter) {
			x.add(point.getX());
			y.add(point.getY());
		}
		Collections.sort(x);
		Collections.reverse(x);
		Collections.sort(y);
		Collections.reverse(y);
		
		double[] arrayx = new double[x.size()];
		double[] arrayy = new double[y.size()];
		
		for (int i = 0; i < arrayy.length; i++) {
			arrayx[i] = x.get(i);
			arrayy[i] = y.get(i);
		}
		double medianX = median(arrayx);
		double medianY = median(arrayy);
		
		p.setX(medianX);
		p.setY(medianY);
		
		return p;
	}
	
	public static double median(double[] m) {
	    int middle = m.length/2;
	    if (m.length%2 == 1) 
	        return m[middle];
	    else 
	        return (m[middle-1] + m[middle]) / 2.0;
	}
}