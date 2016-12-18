import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import com.google.common.collect.Range;
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
        //----czytanie widm, tworzenie drzewo zakresu i mapy zakres-widma----
        MgfRead mgfReader = new MgfRead(args[1]);
        // ---- mapa zakres-widma----
        HashMap<Range,List<MsMsQuery>> rangeMsMsQueryHashMap = mgfReader.getRangeMsMsQueryHashMap();
        //---- czytanie białek-----
        FastaRead fastareader = new FastaRead(args[0]);
        Vector<FastaRecord> fastaRecords = fastareader.getFastaRecords();
        PeptideHashMapCreator peptideHashMapCreator = new PeptideHashMapCreator(fastaRecords, rangeMsMsQueryHashMap);
        HashMap<MsMsQuery, List<Peptide>> msMsQueryListHashMap = peptideHashMapCreator.getMsMsQueryListHashMap();
        ScoringClass scoringClass = new ScoringClass(msMsQueryListHashMap);
        scoringClass.doScoring();
        System.out.println("----------------------");
        /*for (MsMsQuery name: msMsQueryListHashMap.keySet()){

            String key =name.toString();
            String value = msMsQueryListHashMap.get(name).toString();
            System.out.println("klucz "+key +" "+msMsQueryListHashMap.get(name).size()+ " " + value);


        }*/
        // przeczytaj baz� danych
       /* FastaRead fastareader = new FastaRead(args[0]);
        // rekordy z bazy danych
        Vector<FastaRecord> fastaRecords = fastareader.getFastaRecords();
        // stw�rz list� poci�tych peptyd�w
        PeptideListCreator peptideCreator = new PeptideListCreator(fastaRecords);
        MgfRead mgfReader = new MgfRead(args[1]);
        MsMsQuery[] queries = mgfReader.getMsMs();
        // we� wybrane peptydy - kandydackie
        ScoringClass scoreAll = new ScoringClass(peptideCreator, queries);
        //scoreAll.doScoring();
        scoreAll.doScoringFromDB();
        //List<AminoAcidSequence> candidatepeptideList = peptideCreator.getCandidatePeptideList((float) queries[0].getMass(), (float)0.1);
        //new SpectrumScore(candidatepeptideList, queries[0]);
        */
    }


    public static void checkArgs(String[] args){
        if(args == null || args.length<2){
            System.out.println("Nie podano argument�w");
            System.exit(0);
          
        }
}
}
