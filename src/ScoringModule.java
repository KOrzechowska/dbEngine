import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import dbEngineModules.Configuration;
import dbEngineModules.ScoringClass;
import mscanlib.ms.msms.MsMsQuery;
import dbEngineModules.Peptide;
import gnu.trove.map.hash.THashMap;
import org.apache.log4j.Logger;


public class ScoringModule
{
    public static Logger logger = Logger.getRootLogger();

    /** mapa widmo - kandydaci */
    private THashMap<MsMsQuery,HashSet<Peptide>> mapOfMsMsQueryAndTheirPeptideCandidatesList;
    private Configuration configuration;

    /**
     *wypełnienie mapą z poprzednich kroków
     * @param mapOfMsMsQueryAndTheirPeptideCandidatesList - mapa widmo - peptydy kandydacki
     * @param configuration
     */
    public ScoringModule(THashMap<MsMsQuery,HashSet<Peptide>> mapOfMsMsQueryAndTheirPeptideCandidatesList,
                         Configuration configuration){
        this.mapOfMsMsQueryAndTheirPeptideCandidatesList = mapOfMsMsQueryAndTheirPeptideCandidatesList;
        this.configuration = configuration;

    }

    public void countScores(){
        ScoringClass scoringClass = new ScoringClass(configuration);

        Iterator<Map.Entry<MsMsQuery, HashSet<Peptide>>> it = mapOfMsMsQueryAndTheirPeptideCandidatesList.entrySet()
                                                                .iterator();
        while (it.hasNext()){
            Map.Entry<MsMsQuery, HashSet<Peptide>> query = it.next();
            // po kandydatach widma
            for (Peptide peptide : query.getValue()){
                peptide.setScore(scoringClass.getScore(query.getKey(),peptide));
            }
        }
    }

    /**
     * znalezienie peptydów o największej wartości oceny
     */
    public void getMaxScoredPeptides(){
        BufferedWriter writer = null;
        try {
             writer = new BufferedWriter(new FileWriter("/media/kasia/20E03D5DE03D3A7C/studia/proz/DBEngine/results.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Iterator<Map.Entry<MsMsQuery, HashSet<Peptide>>> it = mapOfMsMsQueryAndTheirPeptideCandidatesList.entrySet().iterator();
        while (it.hasNext()){
            Map.Entry<MsMsQuery, HashSet<Peptide>> query = it.next();
            String key =query.getKey().toString();
            // --- peptydy kandydackie
            HashSet<Peptide> peptideList = query.getValue();

            Peptide maxValueInMap2 = peptideList.stream().max(Comparator.comparing(Peptide::getScore)).get();
            try {
                writeLine(writer,"kandydatów: " + peptideList.size()+";" +" dla: "+";" +query.getKey().toString()+";"+query.getKey().getMass()+
                        ";"+maxValueInMap2.getSequence() +";" +maxValueInMap2.getProteinIds() +";"+
                        maxValueInMap2 +";"+maxValueInMap2.getMass());
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeLine(BufferedWriter writer,String line) throws IOException
    {
        writer.write(line);
        writer.newLine();
    }

    public static <T, E> Set<Peptide> getKeysByValue(Map<Peptide, Double> map, double value) {
        return map.entrySet()
                .stream()
                .filter(entry -> Objects.equals(entry.getValue(), value))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

}
