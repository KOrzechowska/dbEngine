import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import mscanlib.ms.mass.AminoAcidSequence;
import mscanlib.ms.msms.MsMsQuery;
import examples.Peptide;
import examples.SpectrumScoreFromDB;


public class ScoringClass
{
    private HashMap<MsMsQuery, List<Peptide>> msMsQueryListHashMap;
    //co ka�da ocenianie wiedzie� potrzebuje
    PeptideListCreator peptideCreator;
    List<AminoAcidSequence> peptideList; // peptydy kandydackie
    private List<Peptide> sequencesFromProteinList; // peptydy kandydackie z info sk�d s�
    MsMsQuery[] queries; // widma eksperymentalne

    public ScoringClass(PeptideListCreator peptideCreator, MsMsQuery[] queries){
        this.peptideCreator = peptideCreator;
        //this.peptideList = peptideCreator.getAaSequenceList();
        this.sequencesFromProteinList = peptideCreator.getSequencesFromProteinList();
        this.queries = queries;
    }

    public ScoringClass(HashMap<MsMsQuery, List<Peptide>> msMsQueryListHashMap){
        this.msMsQueryListHashMap = msMsQueryListHashMap;

    }

    public void doScoring(){
        for (MsMsQuery query: msMsQueryListHashMap.keySet()){
            String key =query.toString();
            // --- peptydy kandydackie
            List<Peptide> peptideList = msMsQueryListHashMap.get(query);
            HashMap<AminoAcidSequence,Double> scoreHaspMap = new HashMap<>();
            HashMap<Peptide,Double> scorePeptideHaspMap = new HashMap<>();
            //System.out.println("klucz "+key + " " + peptideList.toString());
            if(peptideList.size()==0) continue;
            for (Peptide peptide : peptideList){
                if(!scoreHaspMap.containsKey(peptide.getSequence())){
                    SpectrumScoreFromDB spectrumScoreFromDB = new SpectrumScoreFromDB(query,peptide);
                    scoreHaspMap.put(peptide.getSequence(),spectrumScoreFromDB.getScore());
                    scorePeptideHaspMap.put(peptide,spectrumScoreFromDB.getScore());
                }
                else{
                    //System.out.println("już jest taki" + peptide.getSequence());
                    for(Peptide _peptide : scorePeptideHaspMap.keySet()){
                        if(_peptide.getSequence().equals(peptide.getSequence())){
                            _peptide.getProteinIds().add(peptide.getProteinId());
                        }
                    }
                    //peptide.getProteinIds().add(peptide.getProteinId());
                }

            }

            double maxValueInMap=(Collections.max(scorePeptideHaspMap.values()));  // This will return max value in the Hashmap
            for (Entry<Peptide, Double> entry : scorePeptideHaspMap.entrySet()) {  // Itrate through hashmap
                if (entry.getValue()==maxValueInMap) {
                    System.out.println("kandydat�w: " + peptideList.size()+";" +" dla: "+";" + query.toString()+";"+query.getMass()+
                            ";"+entry.getKey().getSequence() +";" +entry.getKey().getProteinIds() +";"+
                            entry.getValue() +";"+entry.getKey().getMass());     // Print the key with max value
                }
            }
            /*for(Peptide peptide : scorePeptideHaspMap.keySet()){

                System.out.println(peptide.getSequence()+"\t"+peptide.getProteinIds()+"\t"+scorePeptideHaspMap.get(peptide));
            }*/


        }
    }
    
    public void doScoringFromDB(){
        ///int i=0;
        for (int j=0; j<queries.length; j++){
            MsMsQuery query = queries[j];
            //System.out.println(query.getNr()+"  "+i); i++;
            List<Peptide> candidatePeptideList = peptideCreator.getCandidatePeptideFromDBList((float) query.getMass(), 5);
            //System.out.println(""+candidatePeptideList.size());
            if(candidatePeptideList.size()==0) continue;
            SpectrumScoreFromDB spectrumScorer = new SpectrumScoreFromDB(candidatePeptideList, query);
            HashMap<Peptide, Double> scores = spectrumScorer.getScores();
            //System.out.println("kandydatów: " + candidatePeptideList.size() +" dla: "+"\t" + query.toString()+"  "+query.getMass());
            double maxValueInMap=(Collections.max(scores.values()));  // This will return max value in the Hashmap
            for (Entry<Peptide, Double> entry : scores.entrySet()) {  // Itrate through hashmap
                if (entry.getValue()==maxValueInMap) {
                    System.out.println("kandydatów: " + candidatePeptideList.size()+";" +" dla: "+";" + query.toString()+";"+query.getMass()+
                            ";"+entry.getKey().getSequence() +";" +entry.getKey().getProteinId() +";"+
                            entry.getValue() +";"+entry.getKey().getMass());     // Print the key with max value
                }
            }
        }
    }
}
