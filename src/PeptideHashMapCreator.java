import com.google.common.collect.Range;
import com.google.common.collect.TreeRangeSet;
import examples.Peptide;
import examples.ProteinDigest;
import mscanlib.ms.db.FastaRecord;
import mscanlib.ms.mass.AminoAcidSequence;
import mscanlib.ms.msms.MsMsQuery;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

/**
 * Created by kasia on 17.12.16.
 */
public class PeptideHashMapCreator {

    private HashMap<MsMsQuery, List<Peptide>> msMsQueryListHashMap;

    public PeptideHashMapCreator(Vector<FastaRecord> fastaRecords, HashMap<Range, List<MsMsQuery>> rangeListHashMap, TreeRangeSet treeRangeSet){

        msMsQueryListHashMap = new HashMap<>();
        long start = System.currentTimeMillis();
        // dla każdego białka
        for (FastaRecord fastaRecord :fastaRecords){

            // dzieli białko, porównuje z zakresami i
            // dopisuje do mapy widmo - kandydaci jeśli sie mieści w tolerancji
            ProteinDigest proteinDigest = new ProteinDigest(fastaRecord, rangeListHashMap, msMsQueryListHashMap, treeRangeSet);
        }
        long stop = System.currentTimeMillis();
        System.out.println("Czas wykoania ciachania białek: "+ (stop-start));
    }

    public HashMap<MsMsQuery, List<Peptide>> getMsMsQueryListHashMap() {
        return msMsQueryListHashMap;
    }
}
