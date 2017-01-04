package examples;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.google.common.collect.Range;
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
	 */
	public ProteinDigest(FastaRecord fastaRecord, HashMap<Range, List<MsMsQuery>> rangeListHashMap, HashMap<MsMsQuery, List<Peptide>> msMsQueryListHashMap)
	{
		/*
		 * Sekwencja bialka
		 */
		/*String 	peoteinSequence=	"MKWVTFISLLFLFSSAYSRGVFRRDAHKSEVAHRFKDLGEENFKALVLIAFAQYLQQCPF" +
									"EDHVKLVNEVTEFAKTCVADESAENCDKSLHTLFGDKLCTVATLRETYGEMADCCAKQEP" +
									"ERNECFLQHKDDNPNLPRLVRPEVDVMCTAFHDNEETFLKKYLYEIARRHPYFYAPELLF" +
									"FAKRYKAAFTECCQAADKAACLLPKLDELRDEGKASSAKQRLKCASLQKFGERAFKAWAV" +
									"ARLSQRFPKAEFAEVSKLVTDLTKVHTECCHGDLLECADDRADLAKYICENQDSISSKLK" +
									"ECCEKPLLEKSHCIAEVENDEMPADLPSLAADFVESKDVCKNYAEAKDVFLGMFLYEYAR" +
									"RHPDYSVVLLLRLAKTYETTLEKCCAAADPHECYAKVFDEFKPLVEEPQNLIKQNCELFE" +
									"QLGEYKFQNALLVRYTKKVPQVSTPTLVEVSRNLGKVGSKCCKHPEAKRMPCAEDYLSVV" +
									"LNQLCVLHEKTPVSDRVTKCCTESLVNRRPCFSALEVDETYVPKEFNAETFTFHADICTL" +
									"SEKERQIKKQTALVELVKHKPKATKEQLKAVMDDFAAFVEKCCKADDKETCFAEEGKKLV" +
									"AASQAALGL";*/
		
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
			//System.out.println(sequence.getMonoMass());
			//System.out.print(rangeListHashMap.toString());
			// -- jeśli masa peptydu mieści się w zakresie
			for(Range range : rangeListHashMap.keySet()){
				if(range.contains((float)sequence.getMonoMass())) {

					List<MsMsQuery> msMsQueryList = rangeListHashMap.get(range);
					if (msMsQueryList != null) { // jeśli zakres ma widma
						for (MsMsQuery msMsQuery : msMsQueryList) {
							// -- jeśli peptyd jest w tolerancji widma to jest kandyadtem
							if (abs(msMsQuery.getMass() - sequence.getMonoMass()) < 5*msMsQuery.getMass()/1000000) {
								//System.out.println("trafione");
								// czy widmo ma już kandydatów
								if (!msMsQueryListHashMap.containsKey(msMsQuery)) { // nie ma
									List<Peptide> peptideList = new ArrayList<>();
									peptideList.add(new Peptide(sequence, fastaRecord.getId()));
									msMsQueryListHashMap.put(msMsQuery, peptideList);
								} else { // ma
									msMsQueryListHashMap.get(msMsQuery).add(new Peptide(sequence, fastaRecord.getId()));
								}
							}
						}
					}
				}
			}
    		//System.out.println(sequence.getSequence() + "\t" + sequence.getMonoMass());
    		
    	}
    	
	}
	
	public HashSet<AminoAcidSequence> getSequencesSet(){
	    return sequencesSet;
	}
	
	/**
	 * main
	 * 
	 * @param args
	 */
	/*public static void main(String args[])
	{
		new ProteinDigest();
	}*/
}
