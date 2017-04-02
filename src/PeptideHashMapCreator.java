import com.google.common.collect.Range;
import com.google.common.collect.TreeRangeSet;

import examples.Configuration;
import examples.Peptide;
import examples.ProteinDigest;
import gnu.trove.map.hash.THashMap;
import mscanlib.ms.db.FastaRecord;
import mscanlib.ms.mass.AminoAcidSequence;
import mscanlib.ms.mass.InSilicoDigestConfig;
import mscanlib.ms.msms.MsMsQuery;
import mscanlib.ms.msms.dbengines.DbEngineScoringConfig;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;


public class PeptideHashMapCreator {

    public static Logger logger = Logger.getRootLogger();

    private THashMap<MsMsQuery, HashSet<Peptide>> msMsQueryListHashMap;
    private Vector<FastaRecord> fastaRecords;
    private HashMap<Range, List<MsMsQuery>> rangeMsMsQueryHashMap;
    private TreeRangeSet treeRangeSet;

    private InSilicoDigestConfig digestConfig;

    
    public PeptideHashMapCreator(Vector<FastaRecord> fastaRecords, HashMap<Range, List<MsMsQuery>> rangeMsMsQueryHashMap,
                                 TreeRangeSet treeRangeSet,
                                 Configuration configuration){

        msMsQueryListHashMap = new THashMap<>();
        this.fastaRecords = fastaRecords;
        this.rangeMsMsQueryHashMap = rangeMsMsQueryHashMap;
        this.treeRangeSet = treeRangeSet;
        this.digestConfig = configuration.getDigestConfig();
    }

    public THashMap<MsMsQuery, HashSet<Peptide>> createMapRangeAndCandidateList(){
        for (FastaRecord fastaRecord :fastaRecords)
        {

            // dzieli białko, porównuje z zakresami i
            // dopisuje do mapy widmo - kandydaci jeśli sie mieści w tolerancji
            ProteinDigest proteinDigest = new ProteinDigest(fastaRecord, rangeMsMsQueryHashMap, msMsQueryListHashMap,
                    treeRangeSet, digestConfig);
            proteinDigest.createPeptidesAndAddThenToTheCandidateList();
        }
        return msMsQueryListHashMap;
    }

    public THashMap<MsMsQuery, HashSet<Peptide>> getMsMsQueryListHashMap() {
        return msMsQueryListHashMap;
    }
}
