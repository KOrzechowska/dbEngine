package examples;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import mscanlib.*;
import mscanlib.ms.mass.*;
import mscanlib.ms.msms.*;
import mscanlib.ms.msms.dbengines.mascot.io.*;
import mscanlib.ms.msms.io.*;
import mscanlib.ms.msms.spectrum.*;


/**
 * Program odczytuje plik MGF i wypisuje informacje o zapytaniach do systemu bazodanowej identyfikacji<br>
 * 
 * Argumentam programu jest nazwa pliku wejsciowego 
 *
 * @author trubel
 */
public class MgfRead
{
    MsMsQuery  queries[]=null;     //tablica zapytan do systemu identyfikacji (widm MS/MS wraz z dodatkowymi informacjami)
	RangeSet<Float> rangeSet = TreeRangeSet.create();
	HashMap<Range,List<MsMsQuery>> rangeMsMsQueryHashMap;
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
			System.out.println("Reading file: " + filename);
			if ((queries=this.readQueries(filename))!=null)
			{
				
				/*
				 * Wypisanie informacji z pliku MGF
				 * tworzenie drzewa zakresów
				 */
				for (int i=0;i<50;i++)
				{
					if (queries[i]!=null)
					{
						float min = (float) (queries[i].getMass()-0.1);
						float max = (float) (queries[i].getMass()+0.1);
						rangeSet.add(Range.open(min,max));

						/*
						 * wypisanie informacji o jonie prekursorowym 
						 */
						/*System.out.println("\nQuery: " + queries[i].getNr());
						System.out.println("Precursor ion experimental mass: " + queries[i].getMass());
						System.out.println("Precursor ion charge state: " + queries[i].getCharge());				
						System.out.println("Precursor ion experimental m/z: " + queries[i].getMz());
						System.out.println("Precursor ion RT: " + queries[i].getRt());	
						System.out.println("Precursor ion peak intensity: " + queries[i].getPrecursorIntensity());
					*/
						/*
						 * wypisanie informacji o widmie MS/MS 
						 */
						MsMsSpectrum spectrum=queries[i].getSpectrum();
						//if (spectrum!=null)
						//	System.out.println(spectrum);
					}

				}
				System.out.println(rangeSet.toString()+ rangeSet.rangeContaining((float)queries[3].getMass()));
				rangeMsMsQueryHashMap = new HashMap<>();
				// przypisanie widm do zakresów
				for (int i=0;i<50;i++) {
					if (queries[i] != null) {
						// jeszcze zakres nie ma widma
						if(!rangeMsMsQueryHashMap.containsKey(rangeSet.rangeContaining((float)queries[i].getMass())))
						{
							List<MsMsQuery> msMsQueryList = new ArrayList<>();
							msMsQueryList.add(queries[i]);
							rangeMsMsQueryHashMap.put(rangeSet.rangeContaining((float)queries[i].getMass()),msMsQueryList);
						}// jeśli już jest taki klucz, to dodajemy mu kolejne widmo
						else{
							rangeMsMsQueryHashMap.get(rangeSet.rangeContaining((float)queries[i].getMass())).add(queries[i]);
						}
						//rangeMsMsQueryHashMap.put(rangeSet.rangeContaining((float)queries[i].getMass()),queries[i]);
					}
				}

				for (Range name: rangeMsMsQueryHashMap.keySet()){

					String key =name.toString();
					String value = rangeMsMsQueryHashMap.get(name).toString();
					System.out.println("klucz "+key + " " + value);


				}

			}
		}else {
		    throw new Exception("Z�a �cie�ka dla mgf");
		}
		}
		catch (MScanException mse)
		{
			System.out.println("ERROR: " + mse.toString());
		} catch (Exception e)
        {
            
            e.printStackTrace();
        }

	}
	
	public MsMsQuery[] getMsMs(){
	    return queries;
	}

	public HashMap<Range, List<MsMsQuery>> getRangeMsMsQueryHashMap() {
		return rangeMsMsQueryHashMap;
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
			System.out.println("Reading queries...");
			fileReader=new MascotMgfFileReader(filename,scanConfig);
			fileReader.readFile();
			
			/*
			 * pobranie zapytan do systemu bazodanowego
			 */
			queries=fileReader.getQueries();
			
			System.out.println(queries.length + " queries.");
			
			/*
			 * pobranie komunikatow bledow i ostrzezen 
			 */
			if ((msg=fileReader.getErrors())!=null && msg.size()>0)
				for (int i=0;i<msg.size();i++)
					System.out.println(msg.get(i));
			if ((msg=fileReader.getWarnings())!=null && msg.size()>0)
				for (int i=0;i<msg.size();i++)
					System.out.println(msg.get(i));
		}
		catch (MScanException mse)
		{
			System.out.println(mse.toString());
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
