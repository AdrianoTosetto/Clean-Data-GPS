package Utils;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.SimpleFormatter;

import br.ufsc.src.persistencia.Persistencia;

/*
 * @autor Adriano Tosetto
 * @date 09/02/2017
 * 
 * Classe que implementa algumas features
 * */




public class Features {
	Persistencia p;
	public Features() throws SQLException {
		p = new Persistencia("org.postgresql.Driver","jdbc:postgresql://localhost/","taxicab","postgres", "postgres");
	}
	
	/*
	 * @param table string com o nome da tabela
	 * @param groupId inteiro com o id do grupo do seguimento
	 * 
	 * @return int tempo em segundos de duração de um segmento
	 * */
	
	
	public int segDuration(String table, int groupId) throws SQLException{ 
		
		ArrayList<String> first = p.fetchFirstRow("select time from taxicab2_group where group_id = "+groupId+" order by time");
		ArrayList<String> last  = p.fetchLastRow("select time from taxicab2_group where group_id = "+groupId+" order by time");
	    
	    String f = first.get(0);
	    String l = last.get(0);
	    int year1    = Integer.parseInt(f.substring(0,4));
	    int month1   = Integer.parseInt(f.substring(5,7));
	    int day1     = Integer.parseInt(f.substring(8,10));
	    int hours1   = Integer.parseInt(f.substring(11,13));
	    int minutes1 = Integer.parseInt(f.substring(14,16));
	    int seconds1 = Integer.parseInt(f.substring(17,19));
	    
	    int year2    = Integer.parseInt(l.substring(0,4));
	    int month2   = Integer.parseInt(l.substring(5,7));
	    int day2     = Integer.parseInt(l.substring(8,10));
	    int hours2   = Integer.parseInt(l.substring(11,13));
	    int minutes2 = Integer.parseInt(l.substring(14,16));
	    int seconds2 = Integer.parseInt(l.substring(17,19));
	    
	    return (int)(((new Date(year2, month2, day2, hours2, minutes2, seconds2)).getTime() - 
	    		(new Date(year1, month1, day1, hours1, minutes1, seconds1)).getTime())/1000);
	}
}