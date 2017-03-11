import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import com.google.common.collect.Range;
import com.google.common.collect.TreeRangeSet;
import examples.MgfRead;
import examples.Peptide;
import mscanlib.ms.db.FastaRecord;
import mscanlib.ms.msms.MsMsQuery;
import examples.FastaRead;


public class MainClass
{


    public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {
        PrintStream out = new PrintStream(new FileOutputStream("output.txt"));
        System.setOut(out);
        checkArgs(args);
        long startG = System.currentTimeMillis();
        //----czytanie widm, tworzenie drzewo zakresu i mapy zakres-widma----
        long start = System.currentTimeMillis();
        MgfRead mgfReader = new MgfRead(args[1]);
        long stop = System.currentTimeMillis();
        System.out.println("czas czytania wykresów: "+(stop-start));
        // ---- mapa zakres-widma----
        HashMap<Range,List<MsMsQuery>> rangeMsMsQueryHashMap = mgfReader.getRangeMsMsQueryHashMap();
        TreeRangeSet treeRangeSet = mgfReader.getTreeRangeSet();
        //---- czytanie białek-----
        FastaRead fastareader = new FastaRead(args[0]);
        Vector<FastaRecord> fastaRecords = fastareader.getFastaRecords();
        //---- trawienie bazy danych
        PeptideHashMapCreator peptideHashMapCreator = new PeptideHashMapCreator(fastaRecords, rangeMsMsQueryHashMap, treeRangeSet);
        HashMap<MsMsQuery, HashSet<Peptide>> msMsQueryListHashMap = peptideHashMapCreator.getMsMsQueryListHashMap();
        ChooseMaxScored chooseMaxScored = new ChooseMaxScored(msMsQueryListHashMap);
        start = System.currentTimeMillis();
        chooseMaxScored.score();
        chooseMaxScored.getMaxScoredPeptide();
        stop = System.currentTimeMillis();
        long stopG = System.currentTimeMillis();
        System.out.println("czas wykoania oceny: "+(stop-start));
        System.out.println("czas wykoania całość: "+(stopG-startG));
        System.out.println("----------------------");

    }


    public static void checkArgs(String[] args){
        if(args == null || args.length<2){
            System.out.println("Nie podano argument�w");
            System.exit(0);
          
        }
}
}
