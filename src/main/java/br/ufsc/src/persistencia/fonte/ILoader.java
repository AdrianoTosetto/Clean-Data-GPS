package br.ufsc.src.persistencia.fonte;

import java.io.File;

import br.ufsc.src.persistencia.exception.AddBatchException;
import br.ufsc.src.persistencia.exception.CreateStatementException;
import br.ufsc.src.persistencia.exception.DBConnectionException;
import br.ufsc.src.persistencia.exception.ExecuteBatchException;
import br.ufsc.src.persistencia.exception.FileNFoundException;
import br.ufsc.src.persistencia.exception.GetSequenceException;
import br.ufsc.src.persistencia.exception.TimeStampException;

public interface ILoader{
	
	public void loadFile(File file, TrajetoriaBruta tb, int folder_id) throws DBConnectionException, CreateStatementException, GetSequenceException, TimeStampException, AddBatchException, FileNFoundException, ExecuteBatchException;

}
