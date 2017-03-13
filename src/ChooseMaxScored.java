import java.util.*;
import java.util.stream.Collectors;

import examples.Configuration;
import examples.ScoringClass;
import mscanlib.ms.msms.MsMsQuery;
import examples.Peptide;


public class ChooseMaxScored
{
    /** mapa widmo - kandydaci */
    private HashMap<MsMsQuery, HashSet<Peptide>> msMsQueryListHashMap;
    private Configuration configuration;

    /**
     *wypełnienie mapą z poprzednich kroków
     * @param msMsQueryListHashMap - mapa widmo - peptydy kandydacki
     * @param configuration
     */
    public ChooseMaxScored(HashMap<MsMsQuery, HashSet<Peptide>> msMsQueryListHashMap, Configuration configuration){
        this.msMsQueryListHashMap = msMsQueryListHashMap;
        this.configuration = configuration;

    }

    public void score(){
        // ustawienie konfiguracji scoringu
        ScoringClass scoringClass = new ScoringClass(configuration);

        Iterator<Map.Entry<MsMsQuery, HashSet<Peptide>>> it = msMsQueryListHashMap.entrySet().iterator();
        while (it.hasNext()){
            Map.Entry<MsMsQuery, HashSet<Peptide>> query = it.next();
            // po kandydatach widma
            for (Peptide peptide : query.getValue()){
                long start = System.currentTimeMillis();
                peptide.setScore(scoringClass.getScore(query.getKey(),peptide));
                long end = System.currentTimeMillis();
                System.out.println(end - start);

            }
        }
    }

    /**
     * znalezienie peptydów o największej wartości oceny
     */
    public void getMaxScoredPeptide(){
        Iterator<Map.Entry<MsMsQuery, HashSet<Peptide>>> it = msMsQueryListHashMap.entrySet().iterator();
        while (it.hasNext()){
            Map.Entry<MsMsQuery, HashSet<Peptide>> query = it.next();
            String key =query.getKey().toString();
            // --- peptydy kandydackie
            HashSet<Peptide> peptideList = query.getValue();

            Peptide maxValueInMap2 = peptideList.stream().max(Comparator.comparing(Peptide::getScore)).get();
            System.out.println("kandydatów: " + peptideList.size()+";" +" dla: "+";" +query.getKey().toString()+";"+query.getKey().getMass()+
                    ";"+maxValueInMap2.getSequence() +";" +maxValueInMap2.getProteinIds() +";"+
                    maxValueInMap2 +";"+maxValueInMap2.getMass());

        }
    }

    public static <T, E> Set<Peptide> getKeysByValue(Map<Peptide, Double> map, double value) {
        return map.entrySet()
                .stream()
                .filter(entry -> Objects.equals(entry.getValue(), value))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

}
