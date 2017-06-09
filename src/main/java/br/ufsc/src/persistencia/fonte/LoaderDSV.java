package br.ufsc.src.persistencia.fonte;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.sql.SQLException;
import java.util.Scanner;

import br.ufsc.src.control.Utils;
import br.ufsc.src.persistencia.Persistencia;
import br.ufsc.src.persistencia.conexao.DBConnectionProvider;
import br.ufsc.src.persistencia.exception.AddBatchException;
import br.ufsc.src.persistencia.exception.CreateStatementException;
import br.ufsc.src.persistencia.exception.DBConnectionException;
import br.ufsc.src.persistencia.exception.ExecuteBatchException;
import br.ufsc.src.persistencia.exception.FileNFoundException;
import br.ufsc.src.persistencia.exception.GetSequenceException;
import br.ufsc.src.persistencia.exception.TimeStampException;

public class LoaderDSV implements ILoader {

	public void loadFile(File file, TrajetoriaBruta tb, int folder_id) throws DBConnectionException, CreateStatementException, GetSequenceException, TimeStampException, AddBatchException, FileNFoundException, ExecuteBatchException {
		
		DBConnectionProvider DB_CONN = DBConnectionProvider.getInstance();
		Scanner scanner;
		
		int posDate = -1;
		int posTime = -1;
		int posLon = -1;
		int posLat = -1;
		int colPos = -1;
		Object[][] tableData = tb.getTableData();

		for (int i = 0; i <= tableData.length - 1; i++) {
			String colName = (String) tableData[i][0];
			String cs = (String) tableData[i][1];
			try {
				colPos = Integer.parseInt(cs);
			} catch (NumberFormatException e) {
				colPos = -1;
			}
			if (colName.equalsIgnoreCase("date"))
				posDate = colPos;
			else if (colName.equalsIgnoreCase("time"))
				posTime = colPos;
			else if (colName.equalsIgnoreCase("lat"))
				posLat = colPos;
			else if (colName.equalsIgnoreCase("lon"))
				posLon = colPos;
		}

		String date = "";
		String time = "";
		String lon = "";
		String lat = "";
		String timestamp = "";
		String sql = "";
		Persistencia.abraConexao();
		try {
			DB_CONN.createStatement();
		} catch (SQLException e1) {
			throw new CreateStatementException(e1.getMessage());
		}
		int seq = 0;
		int cont = 0;
		if (tb.isTID())
			seq = Persistencia.getSequence(tb.getTabelaBanco(), "tid");
		try {
			scanner = new Scanner(new FileReader(file.getAbsolutePath()));
			for (int i = 0; i < tb.getNroLinhasIgnorar(); i++) { //ignore initial lines
				if (scanner.hasNextLine())
					scanner.nextLine();
			}
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				String[] linha = line.split(tb.getSeparador());
				if (posDate >= 0)
					date = linha[posDate - 1];
				if (posTime >= 0)
					time = linha[posTime - 1];

				boolean tstamp = (tb.getFormatoData().equalsIgnoreCase("")
						&& tb.getFormatoHorario().equalsIgnoreCase(""));
				timestamp = Utils.getTimeStamp(date, time, tb.getFormatoData(), tb.getFormatoHorario(), tstamp);

				sql = "insert into " + tb.getTabelaBanco() + " (";
				String sql1 = ") values (";

				if (posLon >= 0 && posLat >= 0) {
					lon = linha[posLon - 1];
					lat = linha[posLat - 1];
					sql += "geom,";
					sql1 += "ST_SetSRID(ST_MakePoint(" + lon + "," + lat + ")," + tb.getSridAtual() + "),";
				}
				sql += "timestamp,";
				sql1 += "'" + timestamp + "',";

				for (int i = 0; i <= tableData.length - 1; i++) {
					String aux = (String) tableData[i][0];
					if (!aux.equalsIgnoreCase("geom") && !aux.equalsIgnoreCase("timestamp")) {

						String cs = (String) tableData[i][1];
						int crs = -1;
						try {
							crs = Integer.parseInt(cs);
						} catch (NumberFormatException e) {
							crs = -1;
						}
						if (aux.equalsIgnoreCase("time") && crs == -1)
							continue;
						else
							sql += (String) tableData[i][0] + ",";
						if (crs != -1) {
							if (aux.equalsIgnoreCase("time")) {
								sql1 += "'" + linha[crs - 1] + "',";
							} else if (aux.equalsIgnoreCase("date")) {
								if (linha[crs - 1].indexOf('T') != -1)
									sql1 += "'" + linha[crs - 1].split("T")[0] + "',";
								else
									sql1 += "'" + linha[crs - 1] + "',";
							} else
								sql1 += "'" + linha[crs - 1] + "',";
						}

					}
				}
				if (tb.isMetaData()) {
					sql += "path,folder_id,";
					sql1 += "'" + file.getAbsolutePath() + "'," + folder_id + ",";
				}
				if (tb.isTID()) {
					sql += "tid,";
					sql1 += seq + ",";
				}

				sql = sql.trim().substring(0, sql.length() - 1);
				sql1 = sql1.trim().substring(0, sql1.length() - 1);
				sql += sql1 + ")";
				try {
					DB_CONN.addBatch(sql);
					cont++;
				} catch (SQLException e) {
					throw new AddBatchException(e.getMessage());
				}
				if(cont == 200000){
					try {
						DB_CONN.executeBatch();
					} catch (SQLException e) {
						throw new ExecuteBatchException(e.getMessage());
					}
					cont = 0;
				}

			}
		} catch (FileNotFoundException e) {
			throw new FileNFoundException(e.getMessage());
		}
		try {
			DB_CONN.executeBatch();
			DB_CONN.closeStatement();
		} catch (SQLException e) {
			throw new ExecuteBatchException(e.getMessage());
		}
		Persistencia.fechaConexao(); 

	}

}