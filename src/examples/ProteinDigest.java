package examples;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.google.common.collect.Range;
import com.google.common.collect.TreeRangeSet;
import mscanlib.MScanException;
import mscanlib.ms.db.FastaRecord;
import mscanlib.ms.mass.AminoAcidSequence;
import mscanlib.ms.mass.EnzymeMap;
import mscanlib.ms.mass.InSilicoDigest;
import mscanlib.ms.mass.InSilicoDigestConfig;
import mscanlib.ms.mass.MassTools;
import mscanlib.ms.msms.MsMsQuery;

import static java.lang.Math.abs;

/**
 * Program demonstrujacy trawienie proteolityczne bialka<br>
 *
 * @author trubel
 */
public class ProteinDigest
{
    private HashSet<AminoAcidSequence> sequencesSet;


    /**
	 * Konstruktor
	 * @param fastaRecord wiersz z bazy danych - czyli białko
	 * @param rangeListHashMap - mapa zakres - widmo
	 * @param msMsQueryListHashMap - globalna (wynikowa) mapa widmo - peptydy
	 * @param treeRangeSet
	 */
	public ProteinDigest(FastaRecord fastaRecord, HashMap<Range, List<MsMsQuery>> rangeListHashMap,
						 HashMap<MsMsQuery, List<Peptide>> msMsQueryListHashMap, TreeRangeSet treeRangeSet)
	{

		/*
		 * Inicjalizacja map
		 */
		try
		{
			MassTools.initMaps();
		}
		catch (MScanException mse)
		{
			System.out.println("Error while initalizing maps");
		}
		
		/*
		 * Utworzenie konfiguracji trawienia protolitycznego 
		 */
		InSilicoDigestConfig digestConfig=new InSilicoDigestConfig();				//obiekt reprezentujacy kofiguracje trawienia bialek
		
		digestConfig.setEnzyme(EnzymeMap.getEnzyme("Trypsin"));						//wybor enzymu
		digestConfig.mLengthRange=new int[]{6,Integer.MAX_VALUE};					//zakres dlugosci peptydow
		digestConfig.mMzRange=new double[]{300.0,2000.0};							//zakres wartosci m/z peptydow
		  
	    
		/*
		 * Utworzenie i wypisanie listy sekwencji peptydow (obiektow klasy AminoAcidSequence)
		 * 
		 * Dodatkowe przykladzy uzycia obiektow AminoAcidSequence sa w pliki AASequence
		 */
	    sequencesSet=InSilicoDigest.digestSequence(fastaRecord.getSequence(),digestConfig);
    	
	    
	    // -- zbiór peptydów z białka ---
    	for (AminoAcidSequence sequence: sequencesSet){
			// -- jeśli masa peptydu mieści się w zakresie
			if(treeRangeSet.contains((float)sequence.getMonoMass())){
				Range range = treeRangeSet.rangeContaining((float)sequence.getMonoMass()); {
					// lista widm dla danego zakresu
					List<MsMsQuery> msMsQueryList = rangeListHashMap.get(range);
					if (msMsQueryList != null) { // jeśli zakres ma widma
						for (MsMsQuery msMsQuery : msMsQueryList) {
							// -- jeśli peptyd jest w tolerancji widma to jest kandyadtem
							if (abs(msMsQuery.getMass() - sequence.getMonoMass()) < 5*msMsQuery.getMass()/1000000) {

								// czy widmo ma już kandydatów
								if (!msMsQueryListHashMap.containsKey(msMsQuery)) { // nie ma
									// tworzymy peptyd - sekwencję i białko do którego nalezy
									Peptide peptide = new Peptide(sequence, fastaRecord.getId());
									// wyliczamy score dla peptyd - widmo
									ScoringClass scoringClass = new ScoringClass(msMsQuery,peptide);
									peptide.setScore(scoringClass.getScore());
									List<Peptide> peptideList = new ArrayList<>();
									// tworzymy listę kandydatów bo te widmo jeszcze nie ma
									peptideList.add(peptide);
									// dodajemy do ogólnej listy
									msMsQueryListHashMap.put(msMsQuery, peptideList);
								} else { // ma
									// tworzymy peptyd - sekwencję i białko do którego nalezy
									Peptide peptide = new Peptide(sequence, fastaRecord.getId());
									// możemy mieć już taki peptyd czyli znamy dla niego score
									if (doWeKnowThatPeptideScore(msMsQueryListHashMap.get(msMsQuery), peptide.getSequence()))
										addPeptideId(msMsQueryListHashMap.get(msMsQuery), peptide);
									else { // nie było takie peptydu - liczymy mu score
										// wyliczamy score dla peptyd - widmo
										ScoringClass scoringClass = new ScoringClass(msMsQuery, peptide);
										peptide.setScore(scoringClass.getScore());
										msMsQueryListHashMap.get(msMsQuery).add(peptide);
									}
								}
							}
						}
					}
				}
			}

    		
    	}
	}

	/**
	 * sprawdzenie czy dany peptyd był już oceniony
	 * @param list - lista paptydów kandydackich
	 * @param aminoAcidSequence - sekwencja danego peptydu
     * @return true - znamy wynik oceny, false - nie znamy
     */
	public boolean doWeKnowThatPeptideScore(final List<Peptide> list, final AminoAcidSequence aminoAcidSequence){
		return list.stream().map(Peptide::getSequence).filter(aminoAcidSequence::equals).findFirst().isPresent();
	}

	/**
	 * dany peptyd jest nam znany, czyli dopisujemy mu tylko id białka do jakiego należy
	 * @param list - lista peptydów kandydackich
	 * @param peptideNew - nowy peptyd
     */
	public void addPeptideId(final List<Peptide> list,
											Peptide peptideNew){
		list.stream().filter(o -> o.getSequence().equals(peptideNew.getSequence())).forEach(
				o -> {
					o.getProteinIds().add(peptideNew.getProteinId());
				}
		);
	}
	
	public HashSet<AminoAcidSequence> getSequencesSet(){
	    return sequencesSet;
	}

}
