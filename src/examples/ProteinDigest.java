package examples;

import com.google.common.collect.Range;
import com.google.common.collect.TreeRangeSet;
import gnu.trove.map.hash.THashMap;
import mscanlib.ms.db.FastaRecord;
import mscanlib.ms.mass.AminoAcidSequence;
import mscanlib.ms.mass.InSilicoDigest;
import mscanlib.ms.mass.InSilicoDigestConfig;
import mscanlib.ms.msms.MsMsQuery;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import static java.lang.Math.abs;

/**
 * Program demonstrujacy trawienie proteolityczne bialka<br>
 *
 * @author trubel
 */
public class ProteinDigest
{
	public static Logger logger = Logger.getRootLogger();
    private HashSet<AminoAcidSequence> sequencesSet;
	private double DELTA_VAALUE = (double)5/1000000;

	private FastaRecord fastaRecord;
	private HashMap<Range, List<MsMsQuery>> rangeMsMsQueryHashMap;
	private THashMap<MsMsQuery, HashSet<Peptide>> msMsQueryListHashMap;
	private TreeRangeSet treeRangeSet;
	private InSilicoDigestConfig digestConfig;

    /**
	 * Konstruktor
	 * @param fastaRecord wiersz z bazy danych - czyli białko
	 * @param rangeMsMsQueryHashMap - mapa zakres - widmo
	 * @param msMsQueryListHashMap - globalna (wynikowa) mapa widmo - peptydy
	 * @param treeRangeSet
	 */
	public ProteinDigest(FastaRecord fastaRecord, HashMap<Range, List<MsMsQuery>> rangeMsMsQueryHashMap,
						 THashMap<MsMsQuery, HashSet<Peptide>> msMsQueryListHashMap, TreeRangeSet treeRangeSet,
						 InSilicoDigestConfig digestConfig)
	{

		this.fastaRecord = fastaRecord;
		this.rangeMsMsQueryHashMap = rangeMsMsQueryHashMap;
		this.msMsQueryListHashMap = msMsQueryListHashMap;
		this.treeRangeSet = treeRangeSet;
		this.digestConfig = digestConfig;


	}

	public void createPeptidesAndAddThenToTheCandidateList(){
		/*
		 * Utworzenie i wypisanie listy sekwencji peptydow (obiektow klasy AminoAcidSequence)
		 *
		 * Dodatkowe przyklady uzycia obiektow AminoAcidSequence sa w pliki AASequence
		 */
		try {
			sequencesSet = InSilicoDigest.digestSequence(fastaRecord.getSequence(), digestConfig);


			// -- zbiór peptydów z białka ---
			for (AminoAcidSequence sequence: sequencesSet){
				// -- jeśli masa peptydu mieści się w zakresie
				if(treeRangeSet.contains((float)sequence.getMonoMass())){
					Range range = treeRangeSet.rangeContaining((float)sequence.getMonoMass()); {
						// lista widm dla danego zakresu
						List<MsMsQuery> msMsQueryList = rangeMsMsQueryHashMap.get(range);
						if (msMsQueryList != null) { // jeśli zakres ma widma
							for (MsMsQuery msMsQuery : msMsQueryList) {
								addPeptideCandidate(msMsQuery, sequence, fastaRecord, msMsQueryListHashMap);
							}
						}
					}
				}
			}

		}catch (OutOfMemoryError e){
			logger.error("Za duży HashSet "+fastaRecord.getId()+"  "+fastaRecord.getSequence()+" ");
		}
	}

	/**
	 * dodanie peptydu do listy kandydatów jesli miesci sie w zakresie
	 * @param msMsQuery widmo rozpatrywane
	 * @param sequence peptyd
	 * @param fastaRecord białko
	 * @param msMsQueryListHashMap mapa widmo - kandydaci do którego sa dopisywani
     */
	public void addPeptideCandidate(MsMsQuery msMsQuery, AminoAcidSequence sequence, FastaRecord fastaRecord,
									THashMap<MsMsQuery, HashSet<Peptide>> msMsQueryListHashMap){
		// -- jeśli peptyd jest w tolerancji widma to jest kandyadtem
		if (abs(msMsQuery.getMass() - sequence.getMonoMass()) < msMsQuery.getMass()*DELTA_VAALUE) {

			// czy widmo ma już kandydatów
			if (msMsQueryListHashMap.containsKey(msMsQuery)) {
				addPeptideCandidateToList(msMsQuery, sequence, fastaRecord, msMsQueryListHashMap);
			} else {
				createPeptideCandidateList(msMsQuery, sequence, fastaRecord, msMsQueryListHashMap);
			}
		}
	}

	/**
	 * tworzenie dla widma listy peptydów kandydackich
	 * tworzymy peptyd i dodajemy go do utworzonej listy
	 * @param msMsQuery widmo
	 * @param sequence peptyd
	 * @param fastaRecord białko
	 * @param msMsQueryListHashMap mapa widmo - kandydaci do którego odpisywani są
     */
	public void createPeptideCandidateList(MsMsQuery msMsQuery, AminoAcidSequence sequence, FastaRecord fastaRecord,
										   THashMap<MsMsQuery,HashSet<Peptide>> msMsQueryListHashMap){
		// tworzymy peptyd - sekwencję i białko do którego nalezy
		Peptide peptide = new Peptide(sequence, fastaRecord.getId());
		HashSet<Peptide> peptideList = new HashSet<>();
		// tworzymy listę kandydatów bo te widmo jeszcze nie ma
		peptideList.add(peptide);
		// dodajemy do ogólnej listy
		msMsQueryListHashMap.put(msMsQuery, peptideList);
	}

	/**
	 * dodanie peptydu do listy kandydatów widma, albo dodajemy peptyd do listy
	 * albo dodajemy białko do peptydu jeśli ten juz jest w liscie
	 * @param msMsQuery widmo
	 * @param sequence peptyd
	 * @param fastaRecord białko
	 * @param msMsQueryListHashMap mapa widmo - lista kandydatów
     */
	public void addPeptideCandidateToList(MsMsQuery msMsQuery, AminoAcidSequence sequence, FastaRecord fastaRecord,
										  THashMap<MsMsQuery,HashSet<Peptide>> msMsQueryListHashMap){
		// tworzymy peptyd - sekwencję i białko do którego nalezy
		Peptide peptide = new Peptide(sequence, fastaRecord.getId());
		// możemy mieć już taki peptyd czyli znamy dla niego countScores
		if (isPeptideScored(msMsQueryListHashMap.get(msMsQuery), peptide.getSequence())) {
			addPeptideId(msMsQueryListHashMap.get(msMsQuery), peptide);
		}
		else { // nie było takie peptydu - liczymy mu countScores
			// wyliczamy countScores dla peptyd - widmo
			msMsQueryListHashMap.get(msMsQuery).add(peptide);
		}
	}

	/**
	 * sprawdzenie czy dany peptyd był już oceniony
	 * @param list - lista paptydów kandydackich
	 * @param aminoAcidSequence - sekwencja danego peptydu
     * @return true - znamy wynik oceny, false - nie znamy
     */
	public boolean isPeptideScored(final HashSet<Peptide> list, final AminoAcidSequence aminoAcidSequence){
		return list.stream().map(Peptide::getSequence).filter(aminoAcidSequence::equals).findFirst().isPresent();
	}

	/**
	 * dany peptyd jest nam znany, czyli dopisujemy mu tylko id białka do jakiego należy
	 * @param list - lista peptydów kandydackich
	 * @param peptideNew - nowy peptyd
     */
	public void addPeptideId(final HashSet<Peptide> list,
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
