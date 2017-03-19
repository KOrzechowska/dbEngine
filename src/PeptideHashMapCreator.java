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

    private InSilicoDigestConfig digestConfig;

    
    public PeptideHashMapCreator(Vector<FastaRecord> fastaRecords, HashMap<Range, List<MsMsQuery>> rangeListHashMap, TreeRangeSet treeRangeSet,
                                 Configuration configuration){

        msMsQueryListHashMap = new THashMap<>();
        long start = System.currentTimeMillis();
        this.digestConfig = configuration.getDigestConfig();
        // dla każdego białka
        for (FastaRecord fastaRecord :fastaRecords){

            // dzieli białko, porównuje z zakresami i
            // dopisuje do mapy widmo - kandydaci jeśli sie mieści w tolerancji
            ProteinDigest proteinDigest = new ProteinDigest(fastaRecord, rangeListHashMap, msMsQueryListHashMap,
                    treeRangeSet, digestConfig);
        }
        long stop = System.currentTimeMillis();
        logger.info("Czas wykoania ciachania białek: "+ (stop-start));
    }

    public THashMap<MsMsQuery, HashSet<Peptide>> getMsMsQueryListHashMap() {
        return msMsQueryListHashMap;
    }
}
