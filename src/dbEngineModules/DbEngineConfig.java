package dbEngineModules;

import mscanlib.common.MScanException;
import mscanlib.io.MScanParameters;
import mscanlib.io.MScanXMLConfigFileReader;
import mscanlib.ms.mass.InSilicoDigestConfig;
import mscanlib.ms.mass.MassTools;
import mscanlib.ms.msms.dbengines.DbEngineSearchConfig;

public class DbEngineConfig
{
	private DbEngineSearchConfig config;

	public DbEngineConfig()
	{
		/*
		 * Inicjalizacja map
		 * 
		 */
		try
		{
			MassTools.initMaps();
		}
		catch (MScanException mse)
		{
			System.out.println("Error while initalizing maps");
			System.exit(-1);
		}
		
		/*
		 * Odczyt pliku konfiguracyjnego
		 */
		MScanXMLConfigFileReader reader=null;
		try
		{
			reader=new MScanXMLConfigFileReader("mscandb.xml");
			reader.parse();
		}
		catch (MScanException mse)
		{
			System.out.println("Error while reading file");
			System.exit(-1);
		}
		
		/*
		 * Pobranie mapy parametrow zapisanych w pliku
		 */
		MScanParameters params=reader.getParameters();
		//System.out.println(params);
		
		/*
		 * Utworzenie obiektu konfiguracji na podstawie paramtrow
		 */
		config=new DbEngineSearchConfig();
		config.setParameters(params);
		//System.out.println("Konfiguracja:  "+config);
		
		
		/*
		 * Pobieranie informacji z obiektu konfiguracji
		 */
		//pobranie sciezku pliku z baza danych bialek
		String dbFile=config.getDB(0).getDbFilename();
		System.out.println("Database: " + dbFile);
		
		//pobranie tolerancji masy jonow macierzystych i jednostki w jakiej jest wyrazana
		double	parentTol=config.getParentMMD();
		int		parentTolUnit=config.getParentMMDUnit();
		System.out.println("\nParent tol: " + parentTol + " [" + (parentTolUnit== MassTools.MMD_UNIT_DA?"Da":"ppm") + "]");
		
		//pobranie tolerancji masy jonow potomnych i jednostki w jakiej jest wyrazana
		double	fragmentTol=config.getFragmentMMD();
		int		fragmentTolUnit=config.getFragmentMMDUnit();
		System.out.println("Fragment tol: " + fragmentTol + " [" + (fragmentTolUnit== MassTools.MMD_UNIT_DA?"Da":"ppm") + "]");
		
		//pobranie tolerancji masy jonow potomnych i jednostki w jakiej jest wyrazana
		InSilicoDigestConfig digestConfig=config.getDigestConfig();
		System.out.println("\nDigest config:\n" + digestConfig);

		System.out.println("mzRange:"+ digestConfig.getMzRange()[1]);
	}

	public DbEngineSearchConfig getConfig() {
		return config;
	}

	/**
	 * main
	 * 
	 * @param args
	 */
	public static void main(String args[])
	{
		new DbEngineConfig();
	}
}
