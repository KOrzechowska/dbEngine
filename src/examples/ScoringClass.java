package examples;

import mscanlib.common.MScanException;
import mscanlib.ms.msms.MsMsQuery;
import mscanlib.ms.msms.dbengines.DbEngineScoringConfig;
import mscanlib.ms.msms.dbengines.mascot.io.MascotMgfFileReader;
import mscanlib.ms.msms.dbengines.mscandb.MScanDbScoring;
import mscanlib.ms.msms.io.MsMsScanConfig;
import mscanlib.ms.msms.spectrum.MsMsSpectrum;

import java.util.ArrayList;

public class ScoringClass
{
    double score;
    private DbEngineScoringConfig config;
    
    public ScoringClass(Configuration configuration){
        this.config = configuration.getConfig();
    }

    public double getScore(MsMsQuery query, Peptide peptide) {
        MsMsSpectrum procSpectrum=MScanDbScoring.processSpectrum(query.getSpectrum(), config.getProcessingConfig());
        score=MScanDbScoring.computeScore(procSpectrum,peptide.getSequence(),query.getCharge(),config);
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
