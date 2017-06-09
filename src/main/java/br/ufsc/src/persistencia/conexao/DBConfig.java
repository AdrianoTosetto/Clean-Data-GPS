package br.ufsc.src.persistencia.conexao;

import java.util.logging.Logger;

public class DBConfig {

	public static  String driverPostgres;
	public static  String url;
	public static  String banco;
	public static  String usuario;
	public static  String senha;
	
	static final Logger LOGGER = Logger.getLogger(DBConnectionProvider.class.getName());
}