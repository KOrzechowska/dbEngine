package examples;

import mscanlib.MScanException;
import mscanlib.ms.mass.*;
import mscanlib.ms.msms.MsMsFragmentationTools;
import mscanlib.ms.msms.dbengines.DbEngineScoringConfig;
import mscanlib.ms.msms.spectrum.MsMsSpectrumProcessingConfig;
import org.apache.log4j.Logger;

public class Configuration {

    private static Logger logger = Logger.getRootLogger();

    private InSilicoDigestConfig digestConfig;

    private DbEngineScoringConfig config;

    public Configuration(){

        // --------- Konfiguracja Protein Digest---------------------------------------
        /*
		 * Inicjalizacja map
		 */
        try
        {
            MassTools.initMaps();
        }
        catch (MScanException mse)
        {
            logger.error("Error while initalizing maps");
        }

		/*
		 * Utworzenie konfiguracji trawienia protolitycznego
		 */
        digestConfig=new InSilicoDigestConfig();				//obiekt reprezentujacy kofiguracje trawienia bialek

        digestConfig.setEnzyme(EnzymeMap.getEnzyme("Trypsin"));						//wybor enzymu
        digestConfig.mLengthRange=new int[]{6,Integer.MAX_VALUE};					//zakres dlugosci peptydow
        digestConfig.mMzRange=new double[]{300.0,2000.0};							//zakres wartosci m/z peptydow

        //lista stalych modyfikacji (wszystkie C beda mialy te modyfikacje)
        digestConfig.setFixedPTMs(new PTM[]{PTMMap.getPTM("Methylthio (C)")});

        //lista zmiennych modyfikacji (na K, M, S i T moga, ale nie musza pojawic sie modyfikacje)
        digestConfig.setVariablePTMs(new PTM[]{PTMMap.getPTM("Acetyl (K)"),PTMMap.getPTM("Oxidation (M)"),PTMMap.getPTM("Phospho (ST)")});

        //maksymalna liczba modyfikacji zmiennych jednego rodzaju w sekwencji peptydu
        digestConfig.mMaxSingleVarPTM=2;

        //maksymalna liczba modyfikacji zmiennych wszystkich rodzajow w sekwencji peptydu
        digestConfig.mMaxTotalVarPTMs=2;



        // ------------------------------------------------------------------------------
        // ------------- Konfiguracja ScoringClass --------------------------------------
        /*
         * Inicjalizacja map aminokwasow i modyfikacji
         */
        try
        {
            MassTools.initMaps();
        }
        catch (MScanException mse)
        {
            logger.error("Error while initalizing maps");
        }

        /*
         * Konfiguracja scoringu
         */
        config=new DbEngineScoringConfig();
        config.setFragmentMMD(0.02);                                                            //tolerancja m/z pikow fragmentacyjnych
        config.setFragmentMMDUnit(MassTools.MMD_UNIT_DA);

        config.getProcessingConfig().setPeakDepthOptimization(MsMsSpectrumProcessingConfig.PEAK_DEPTH_FIXED);   //wybor 15 najwyzszych pikow z kazdego zakresu po 100 Da
        config.getProcessingConfig().setPeakDepth(15);
        config.getProcessingConfig().setWindow(100);

        config.getFragmentationConfig().addIonType(MsMsFragmentationTools.B_ION);               //uwzglednienie jonow fragmentacyjnych z serii B, Y i A
        config.getFragmentationConfig().addIonType(MsMsFragmentationTools.Y_ION);

        config.getFragmentationConfig().addCharge(1);                                           //uwzglednienie jonow fragmentacyjnych o stopniu naladowania +1 i +2
        config.getFragmentationConfig().addCharge(2);
    }

    public InSilicoDigestConfig getDigestConfig() {
        return digestConfig;
    }

    public DbEngineScoringConfig getConfig() {
        return config;
    }
}
