package br.ufsc.src.persistencia;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DecimalFormat;

import InterfacePersistencia.TrajectoriesInterface;
import br.ufsc.src.control.dataclean.FeaturesConfig;
import br.ufsc.src.control.dataclean.OptionsDistance;
import br.ufsc.src.control.dataclean.OptionsDuration;
import br.ufsc.src.control.dataclean.OptionsSpeed;
import br.ufsc.src.control.dataclean.TrajectoryFeatures;
import br.ufsc.src.control.datafeatures.WeatherFeatures;
import br.ufsc.src.control.entities.GenericPoint;
import br.ufsc.src.control.entities.GenericTrajectory;
import br.ufsc.src.persistencia.conexao.DBConfig;
import br.ufsc.src.persistencia.exception.AddColumnException;
import br.ufsc.src.persistencia.exception.DBConnectionException;
import br.ufsc.src.persistencia.exception.GetTableColumnsException;

public class Test {

	public static void main(String[] args) throws SQLException, DBConnectionException, GetTableColumnsException, AddColumnException {
		Persistencia p = new Persistencia("org.postgresql.Driver","jdbc:postgresql://localhost/","bovinos","postgres", "post123");
		
		final long startTime = System.nanoTime();
		TrajectoriesInterface ti = new TrajectoriesInterface(p);
		TrajectoryFeatures tf = new TrajectoryFeatures(p);
		
		FeaturesConfig config = new FeaturesConfig();
		config.hasDistance = false;
		config.hasDuration = true;
		config.hasSpeed  = false;
		
		config.argSeconds = true;
		
		/*DBConfig.banco   = "bovinos";
		DBConfig.senha   = "post123";
		DBConfig.usuario = "postgres";
		
		//tf.extracFeatures("taxicab", "taxicab_seg", config, 1, 14);
		
		tf.loadResultSet("taxicab_seg");
		System.out.println("eita");
		tf.doItAll("taxicab", "taxicab_seg", config, 4);
		tf.loadResultSet("taxicab_seg");
		tf.doItAll("taxicab", "taxicab_seg", config, 7);*/
		tf.doItAll("taxicab", "taxicab_seg", config, 4);
		
		final long duration = System.nanoTime() - startTime;
		final double seconds = ((double)duration / 1000000000);
		final double minutes = ((double)seconds / 60);
		final double hours   = ((double)minutes / 60);
		System.out.println("Time : " + seconds + " Seconds");
		System.out.println("Time : " + minutes + " Minutes");
		System.out.println("Time : " + hours + " Hours");
	}
}
