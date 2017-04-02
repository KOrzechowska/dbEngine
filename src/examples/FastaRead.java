package examples;

import java.io.File;
import java.util.Vector;

import mscanlib.common.MScanException;
import mscanlib.ms.db.DBTools;
import mscanlib.ms.db.FastaRecord;
import mscanlib.ms.db.io.FastaFileReader;
import mscanlib.ms.db.io.FastaImportConfig;
import mscanlib.ms.mass.MassTools;
import org.apache.log4j.Logger;

/**
 * Program odczytuje plik FASTA i wypisuje informacje o jego rekordach<br>
 * 
 * Argumentam programu jest nazwa pliku wejsciowego 
 *
 * @author trubel
 */
public class FastaRead
{
	public static Logger logger = Logger.getRootLogger();

    FastaImportConfig   fastaConfig=null;       //konfigurcja odczytu
    FastaFileReader     fastaReader=null;       //obiekt odczytujacy pliki FASTA
    Vector<FastaRecord> fastaRecords=null;      //wektor rekordow FASTA
	/**
	 * Konstruktor
	 * 
	 * @param filename	nazwa pliku
	 */
	public FastaRead(String filename)
	{

		try
		{
			/*
			 * Inicjalizacja map
			 */
			MassTools.initMaps();
					
			/*
			 *	konfiguracja odczytu 
			 */
			fastaConfig=new FastaImportConfig(DBTools.DB_SP);
			
			/*
			 *	odczyt pliku FASTA 
			 */
			File fastFile = new File(filename);
			if(!fastFile.isDirectory() && fastFile.toString().endsWith(".fasta")){
				logger.info("Reading FASTA file: " + filename + "...");
			
			fastaReader=new FastaFileReader(filename,fastaConfig);
			fastaRecords=fastaReader.readRecords();
			
			logger.info(" (" + fastaRecords.size() + " records)");

		}else{
		    throw new Exception("Z�a �cie�ka dla fasta file");
		}
		}
		catch (MScanException mse)
		{
			logger.error(mse);
		} catch (Exception e)
        {
            e.printStackTrace();
        }
	}
	
	public Vector<FastaRecord> getFastaRecords(){
	    return fastaRecords;
	}

	/**
	 * main
	 * 
	 * @param args nazwa pliku
	 */
	public static void main(String[] args)
	{
		if (args!=null && args.length>0)
		{
			new FastaRead(args[0]);
		}
	}
}
