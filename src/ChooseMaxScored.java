import java.util.*;
import java.util.stream.Collectors;

import mscanlib.ms.msms.MsMsQuery;
import examples.Peptide;


public class ChooseMaxScored
{
    /** mapa widmo - kandydaci */
    private HashMap<MsMsQuery, List<Peptide>> msMsQueryListHashMap;

    /**
     *wypełnienie mapą z poprzednich kroków
     * @param msMsQueryListHashMap - mapa widmo - peptydy kandydacki
     */
    public ChooseMaxScored(HashMap<MsMsQuery, List<Peptide>> msMsQueryListHashMap){
        this.msMsQueryListHashMap = msMsQueryListHashMap;

    }

    /**
     * znalezienie peptydów o największej wartości oceny
     */
    public void getMaxScoredPeptide(){
        Iterator<Map.Entry<MsMsQuery, List<Peptide>>> it = msMsQueryListHashMap.entrySet().iterator();
        while (it.hasNext()){
            Map.Entry<MsMsQuery, List<Peptide>> query = it.next();
            String key =query.getKey().toString();
            // --- peptydy kandydackie
            List<Peptide> peptideList = query.getValue();

            Peptide maxValueInMap2 = peptideList.stream().max(Comparator.comparing(Peptide::getScore)).get();
            System.out.println("kandydatów: " + peptideList.size()+";" +" dla: "+";" + query.toString()+";"+query.getKey().getMass()+
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
