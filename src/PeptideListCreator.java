import java.util.*;

import com.google.common.collect.Range;
import examples.Peptide;
import examples.ProteinDigest;
import mscanlib.ms.db.FastaRecord;
import mscanlib.ms.mass.AminoAcidSequence;
import mscanlib.ms.msms.MsMsQuery;


public class PeptideListCreator
{

    private List<Peptide> sequencesFromProteinList;
    private HashMap<Range, List<MsMsQuery>> rangeListHashMap;
    private HashMap<MsMsQuery,List<Peptide>> msMsQueryListHashMap;
    /**
     * Stworzenie listy peptyd�w poci�tych
     * @param fastaRecords
     */
    public PeptideListCreator(Vector<FastaRecord> fastaRecords){

        sequencesFromProteinList = new ArrayList<>();
        System.out.print("Tworzenie listy peptydów ....");
     // po ka�dym rekordzie z bazy - czyli dla ka�dego bia�ka
        for (FastaRecord fastaRecord: fastaRecords){
            // dzieli bia�ko
            ProteinDigest proteinDigest = new ProteinDigest(fastaRecord, rangeListHashMap, msMsQueryListHashMap);
            //hashSet z poci�tego bia�ka
            HashSet<AminoAcidSequence> aaSequenceHashSet = proteinDigest.getSequencesSet();
            addToAASequenceList(aaSequenceHashSet, fastaRecord.getId());
        }
        sortSequenceList();

    }
    
    /**
     * dodanie peptyd�w z bia�ka do listy
     * @param aaSequenceHashSet
     */
   private void addToAASequenceList(HashSet<AminoAcidSequence> aaSequenceHashSet, String id){
        for (AminoAcidSequence sequence: aaSequenceHashSet){
           // if(sequencesFromProteinList.contains(sequence))
            sequencesFromProteinList.add(new Peptide(sequence, id));
            //System.out.println(sequence.getSequence() + "\t" + sequence.getMonoMass());
        }
    }

    private void sortSequenceList(){
        System.out.print("Sortowanie ...");
        sequencesFromProteinList.sort(Comparator.comparing( Peptide::getMass));

        //for (Peptide peptide : sequencesFromProteinList)
          //  System.out.println(peptide.getSequence() + "\t" + peptide.getSequence().getMonoMass() + "\t" + peptide.getProteinId());
    }
    public List<Peptide> getSequencesFromProteinList(){
        return sequencesFromProteinList;
    }
    
    //public List<AminoAcidSequence> getAaSequenceList(){
    //    return aaSequenceList;
    //}
    

    
    public List<Peptide> getCandidatePeptideFromDBList(Float masFromExperiment, double tolerance){
        List<Peptide> candidatePeptideFromDBList = new ArrayList<>();
        //tolerancja liczona jako =/- delta mas
        double minMass = masFromExperiment - tolerance*masFromExperiment/1000000;
        double maxMass = masFromExperiment + tolerance*masFromExperiment/1000000;
       
        // wersja z wiadomo�ci� z jakiego to bia�ka
        for (Peptide sequence : sequencesFromProteinList){
            /*Float mass = (float) sequence.getSequence().getMonoMass();
            if(mass >maxMass) break;
            Float sigmaMass = (masFromExperiment - mass)/mass;
            if(sigmaMass <= 5){
                candidatePeptideFromDBList.add(sequence);
            }*/
        	double mass = sequence.getSequence().getMonoMass();

        	/*double deltaMassSpectrum = masFromExperiment-mass;
            double sigmaMassSpectrum = (deltaMassSpectrum/masFromExperiment)*1000000;            
			//tolerancja liczona jako +/- od sigmy masy
            double sigmaMassMin = sigmaMassSpectrum - tolerance;
            double sigmaMassMax = sigmaMassSpectrum + tolerance; 

            double deltaMassPeptide = mass-masFromExperiment;
            double sigmaMassPeptide = (deltaMassPeptide/mass)*1000000;          
                        
           if(sigmaMassPeptide>sigmaMassMin && sigmaMassPeptide<sigmaMassMax){
                candidatePeptideFromDBList.add(sequence);
            }*/
        	if(mass>minMass && mass<maxMass){
                candidatePeptideFromDBList.add(sequence);
            }

        }
        //for (Peptide sequence : candidatePeptideFromDBList){
            //System.out.println("Jestem z :... "+ sequence.getProteinId());
            //System.out.println("Wybrany :... "+ sequence.getSequence().getSequence() + "\t" + sequence.getSequence().getMonoMass());
        //}
        return candidatePeptideFromDBList;
    }
}
