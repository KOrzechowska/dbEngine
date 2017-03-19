import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import com.google.common.collect.Range;
import com.google.common.collect.TreeRangeSet;

import examples.Configuration;
import examples.MgfRead;
import examples.Peptide;
import mscanlib.ms.db.FastaRecord;
import mscanlib.ms.msms.MsMsQuery;
import examples.FastaRead;
import gnu.trove.map.hash.THashMap;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;


public class MainClass
{
    public static Logger logger = Logger.getRootLogger();

    public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {
        PrintStream out = new PrintStream(new FileOutputStream("output.txt"));
        System.setOut(out);
        BasicConfigurator.configure();


        checkArgs(args);
        Configuration configuration = new Configuration();
        long startG = System.currentTimeMillis();
        //----czytanie widm, tworzenie drzewo zakresu i mapy zakres-widma----
        long start = System.currentTimeMillis();
        MgfRead mgfReader = new MgfRead(args[1]);
        long stop = System.currentTimeMillis();
        logger.info("czas czytania wykresów: "+(stop-start));
        // ---- mapa zakres-widma----
        HashMap<Range,List<MsMsQuery>> rangeMsMsQueryHashMap = mgfReader.getMapOfRangeAndTheirMsMsQueriesList();
        TreeRangeSet treeRangeSet = mgfReader.getTreeRangeSet();
        //---- czytanie białek-----
        FastaRead fastareader = new FastaRead(args[0]);
        Vector<FastaRecord> fastaRecords = fastareader.getFastaRecords();
        //---- trawienie bazy danych
        PeptideHashMapCreator peptideHashMapCreator = new PeptideHashMapCreator(fastaRecords, rangeMsMsQueryHashMap,
                treeRangeSet, configuration);
        THashMap<MsMsQuery, HashSet<Peptide>> msMsQueryListHashMap = peptideHashMapCreator.getMsMsQueryListHashMap();
        ScoringModule scoringModule = new ScoringModule(msMsQueryListHashMap, configuration);
        start = System.currentTimeMillis();
        scoringModule.countScores();
        scoringModule.getMaxScoredPeptides();
        stop = System.currentTimeMillis();
        long stopG = System.currentTimeMillis();
        logger.info("czas wykoania oceny: "+(stop-start));
        logger.info("czas wykoania całość: "+(stopG-startG));
        logger.info("----------------------");

    }


    public static void checkArgs(String[] args){
        if(args == null || args.length<2){
            logger.error("Nie podano argument�w");
            System.exit(0);
          
        }
}
}
