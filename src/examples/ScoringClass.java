package examples;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import mscanlib.MScanException;
import mscanlib.ms.mass.AminoAcidSequence;
import mscanlib.ms.mass.MassTools;
import mscanlib.ms.msms.MsMsFragmentationTools;
import mscanlib.ms.msms.MsMsQuery;
import mscanlib.ms.msms.dbengines.DbEngineScoringConfig;
import mscanlib.ms.msms.dbengines.mascot.io.MascotMgfFileReader;
import mscanlib.ms.msms.dbengines.mscandb.MScanDbScoring;
import mscanlib.ms.msms.io.MsMsScanConfig;
import mscanlib.ms.msms.spectrum.MsMsSpectrum;
import mscanlib.ms.msms.spectrum.MsMsSpectrumProcessingConfig;

public class ScoringClass
{
    /** mapa peptyd wynik*/
    HashMap<Peptide, Double> scoresMap;
    double score;

    /**
     *
     * @param sequences - lista peptydów kandydackich
     * @param query - widmo
     */
    public ScoringClass(List<Peptide> sequences, MsMsQuery query)
    {
        /*
         * Inicjalizacja map aminokwasow i modyfikacji
         */
        try
        {
            MassTools.initMaps();
        }
        catch (MScanException mse)
        {
            System.out.println("Error while initalizing maps");
        }

        /*
         * Konfiguracja scoringu
         */
        DbEngineScoringConfig config=new DbEngineScoringConfig();
        config.setFragmentMMD(0.02);                                                            //tolerancja m/z pikow fragmentacyjnych
        config.setFragmentMMDUnit(MassTools.MMD_UNIT_DA);
        
        config.getProcessingConfig().setPeakDepthOptimization(MsMsSpectrumProcessingConfig.PEAK_DEPTH_FIXED);   //wybor 15 najwyzszych pikow z kazdego zakresu po 100 Da
        config.getProcessingConfig().setPeakDepth(15);
        config.getProcessingConfig().setWindow(100);

        config.getFragmentationConfig().addIonType(MsMsFragmentationTools.B_ION);               //uwzglednienie jonow fragmentacyjnych z serii B, Y i A
        config.getFragmentationConfig().addIonType(MsMsFragmentationTools.Y_ION);
        
        config.getFragmentationConfig().addCharge(1);                                           //uwzglednienie jonow fragmentacyjnych o stopniu naladowania +1 i +2                            
        config.getFragmentationConfig().addCharge(2);
        //System.out.println(config);
        
        
        /*
         * Odczyt pliku MGF, pobranie pierwszego widma, pretworzenie go i wznaczenie dopasowania do listy peptydow kandydackich
         */
        //MsMsQuery queries[]=null;                                                             //tablica zapytan do systemu identyfikacji (widm MS/MS wraz z dodatkowymi informacjami)                                         
        //if ((queries=this.readQueries("CA_A_1.mgf"))!=null && queries.length>0)
        {
            /*
             * Pobranie pierwszego zapytania
             */
            //MsMsQuery query=queries[0];
                
            /*
             * Wyswietlenie informacji o widmie
             */
           // System.out.println("\n" + query.getSpectrum());
                
            /*
             * Preprocessing widma na potrzeby wyznaczenia score
             */
            MsMsSpectrum procSpectrum=MScanDbScoring.processSpectrum(query.getSpectrum(), config.getProcessingConfig());
           // System.out.println("\n" + procSpectrum);
                
                
            /*
             * Wyznaczenie score dla sekwencji kandydackich
             */
            System.out.println("\nScoring candidate peptides: ");
            /*AminoAcidSequence sequences[]={   new AminoAcidSequence("EFNAETFTFHADICTLSEK"),           //lista sekwencji kandydackich
                                            new AminoAcidSequence("SEKEFNAETFTFHADICTL"),
                                            new AminoAcidSequence("EFNAEHADICTLSEKTFTF"),
                                            new AminoAcidSequence("HADICTLSEKEFNAETFTF")};
                */
            scoresMap = new HashMap<Peptide, Double>();
            for (Peptide sequence:sequences)
            {

                    double score=MScanDbScoring.computeScore(procSpectrum,sequence.getSequence(),query.getCharge(),config);   //miara dopasowania widm
                    scoresMap.put(sequence,score);

            }       
        }
    }
    
    public HashMap<Peptide, Double> getScores(){
        return scoresMap;
    }
    
    public ScoringClass(MsMsQuery query, Peptide peptide){
        /*
         * Inicjalizacja map aminokwasow i modyfikacji
         */
        try
        {
            MassTools.initMaps();
        }
        catch (MScanException mse)
        {
            System.out.println("Error while initalizing maps");
        }

        /*
         * Konfiguracja scoringu
         */
        DbEngineScoringConfig config=new DbEngineScoringConfig();
        config.setFragmentMMD(0.02);                                                            //tolerancja m/z pikow fragmentacyjnych
        config.setFragmentMMDUnit(MassTools.MMD_UNIT_DA);

        config.getProcessingConfig().setPeakDepthOptimization(MsMsSpectrumProcessingConfig.PEAK_DEPTH_FIXED);   //wybor 15 najwyzszych pikow z kazdego zakresu po 100 Da
        config.getProcessingConfig().setPeakDepth(15);
        config.getProcessingConfig().setWindow(100);

        config.getFragmentationConfig().addIonType(MsMsFragmentationTools.B_ION);               //uwzglednienie jonow fragmentacyjnych z serii B, Y i A
        config.getFragmentationConfig().addIonType(MsMsFragmentationTools.Y_ION);

        config.getFragmentationConfig().addCharge(1);                                           //uwzglednienie jonow fragmentacyjnych o stopniu naladowania +1 i +2
        config.getFragmentationConfig().addCharge(2);

        MsMsSpectrum procSpectrum=MScanDbScoring.processSpectrum(query.getSpectrum(), config.getProcessingConfig());
         score=MScanDbScoring.computeScore(procSpectrum,peptide.getSequence(),query.getCharge(),config);

    }

    public double getScore() {
        return score;
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
        MsMsQuery           queries[]=null;
        MascotMgfFileReader fileReader=null;
        MsMsScanConfig      scanConfig=null;
        ArrayList<String>   msg=null;
                
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

    public static void main(String[] args)
    {
        //new SpectrumScore(); 
    }
}
