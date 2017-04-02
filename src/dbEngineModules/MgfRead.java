package dbEngineModules;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.common.collect.Range;
import com.google.common.collect.TreeRangeSet;
import mscanlib.common.MScanException;
import mscanlib.ms.mass.*;
import mscanlib.ms.msms.*;
import mscanlib.ms.msms.dbengines.mascot.io.*;
import mscanlib.ms.msms.io.*;
import mscanlib.ms.msms.spectrum.*;
import org.apache.log4j.Logger;


/**
 * Program odczytuje plik MGF i wypisuje informacje o zapytaniach do systemu bazodanowej identyfikacji<br>
 * 
 * Argumentam programu jest nazwa pliku wejsciowego 
 *
 * @author trubel
 */
public class MgfRead
{
	public static Logger logger = Logger.getRootLogger();
    MsMsQuery  queries[]=null;     //tablica zapytan do systemu identyfikacji (widm MS/MS wraz z dodatkowymi informacjami)
	/**drzewo zakresów*/
	TreeRangeSet treeRangeSet = TreeRangeSet.create();
	/** mapa zakres - lista widm */
	HashMap<Range,List<MsMsQuery>> mapOfRangeAndTheirMsMsQueriesList;
	/**
	 * Konstruktor
	 * 
	 * @param filename nazwa pliku wejsciowego
	 */
	public MgfRead(String filename)
	{
		try
		{
			/*
			 * Inicjalizacja map potrzebnych do odczytu
			 */
			MassTools.initMaps();
			
			/*
			 * Odczyt pliku MGF
			 */
			File fastFile = new File(filename);
            if(!fastFile.isDirectory() && fastFile.toString().endsWith(".mgf")){
			logger.info("Reading file: " + filename);

				mapOfRangeAndTheirMsMsQueriesList = new HashMap<>();

			if ((queries=this.readQueries(filename))!=null)
			{
				
				/*
				 * Wypisanie informacji z pliku MGF
				 * tworzenie drzewa zakresów
				 */
				for (int i=0;i<queries.length;i++)
				{
					if (queries[i]!=null)
					{
						//tolerancja =/-detaMas
						float min = (float) (queries[i].getMass()-5*queries[i].getMass()/1000000);
						float max = (float) (queries[i].getMass()+5*queries[i].getMass()/1000000);
						treeRangeSet.add(Range.open(min,max));

						/*
						 * wypisanie informacji o widmie MS/MS 
						 */
						MsMsSpectrum spectrum=queries[i].getSpectrum();

						addMsMsQueryToRange(queries[i]);
					}

				}
				logger.info(treeRangeSet.rangeContaining((float)queries[3].getMass()));


			}
		}else {
		    throw new Exception("Wrong path to mgf");
		}
		}
		catch (MScanException mse)
		{
			logger.error("ERROR: " + mse.toString());
		} catch (Exception e)
        {
            
            e.printStackTrace();
        }

	}

	public void addMsMsQueryToRange(MsMsQuery query){
		// jeszcze zakres nie ma widma
		if(!mapOfRangeAndTheirMsMsQueriesList.containsKey(treeRangeSet.rangeContaining((float)query.getMass())))
		{
			List<MsMsQuery> msMsQueryList = new ArrayList<>();
			msMsQueryList.add(query);
			mapOfRangeAndTheirMsMsQueriesList.put(treeRangeSet.rangeContaining((float)query.getMass()),msMsQueryList);
		}// jeśli już jest taki klucz, to dodajemy mu kolejne widmo
		else{
			mapOfRangeAndTheirMsMsQueriesList.get(treeRangeSet.rangeContaining((float)query.getMass())).add(query);
		}
	}
	
	public MsMsQuery[] getMsMs(){
	    return queries;
	}

	public HashMap<Range, List<MsMsQuery>> getMapOfRangeAndTheirMsMsQueriesList() {
		return mapOfRangeAndTheirMsMsQueriesList;
	}

	public TreeRangeSet getTreeRangeSet() {
		return treeRangeSet;
	}

	/**
	 * Metoda odczytujaca tablica zapytan do systemu bazodanowego
	 * 
	 * @param filename nazwa pliku wejsiowego
	 * 
	 * @return tablica zapytan do systemu bazodanowego 
	 */
	private MsMsQuery[] readQueries(String filename)
	{
		MsMsQuery			queries[]=null;
		MascotMgfFileReader	fileReader=null;
		MsMsScanConfig		scanConfig=null;
		ArrayList<String>	msg=null;
				
		scanConfig=new MsMsScanConfig();
		
		try
		{
			/*
			 * odczyt pliku MGF
			 */
			logger.info("Reading queries...");
			fileReader=new MascotMgfFileReader(filename,scanConfig);
			fileReader.readFile();
			
			/*
			 * pobranie zapytan do systemu bazodanowego
			 */
			queries=fileReader.getQueries();
			
			logger.info(queries.length + " queries.");
			
			/*
			 * pobranie komunikatow bledow i ostrzezen 
			 */
			if ((msg=fileReader.getErrors())!=null && msg.size()>0)
				for (int i=0;i<msg.size();i++)
					logger.info(msg.get(i));
			if ((msg=fileReader.getWarnings())!=null && msg.size()>0)
				for (int i=0;i<msg.size();i++)
					logger.info(msg.get(i));
		}
		catch (MScanException mse)
		{
			logger.info(mse.toString());
			queries=null;
		}
		return(queries);
	}

	/**
	 * Main
	 */
	public static void main(String[] args)
	{
		if (args!=null && args.length>0)
			new MgfRead(args[0]); 
	}
}
