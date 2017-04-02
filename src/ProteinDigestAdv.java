import java.io.*;
import java.util.*;

import mscanlib.common.*;
import mscanlib.system.*;
import mscanlib.ms.db.*;
import mscanlib.ms.db.io.*;
import mscanlib.ms.mass.*;

/**
 * Program demonstrujacy trawienie proteolityczne bialek.<br>
 * 
 * Sekwencje bialek odczytywane sa z pliku podanego jako argument wywolania programu.<br>
 * 
 * W pliku wyjsciowycm zapisywane sa informacje o bialach i listy ich peptydow proteolitycznych.
 *
 * @author trubel
 */

public final class ProteinDigestAdv
{	
	/**
	* Parametry trawienia bialek
	*/
	InSilicoDigestConfig	mDigestConfig=null;
	
	/**
	 * Konstruktor
	 * 
	 * @param	filename	nazwa pliku
	 */
	public ProteinDigestAdv(String filename)
	{
		LinkedHashSet<Protein>	proteins=null;
		
		try
		{
			/*
			 * Inicjalizacja map potrzebnych do odczytu
			 */
			MassTools.initMaps();
			
			/*
			 * Utworzenie konfiguracji trawienia proteolitycznego 
			 */
			this.mDigestConfig=new InSilicoDigestConfig();
			this.mDigestConfig.setEnzyme(EnzymeMap.getEnzyme("Trypsin"));						//lista enzymow
			this.mDigestConfig.mMissedCleavages=2;												//liczba niedotrawek
			
			this.mDigestConfig.mLengthRange=new int[]{6,50};									//zakres dlugosci peptydow

			this.mDigestConfig.mMassRange=new double[]{0.0,Double.POSITIVE_INFINITY};			//zakres mas peptydow
			this.mDigestConfig.mMzRange=new double[]{300.0,1500.0};								//zakres m/z jonow peptydowych
			this.mDigestConfig.mCharges=new int[]{2,3};											//stopnie naladowania						

			this.mDigestConfig.setFixedPTMs(new PTM[]{PTMMap.getPTM("Carbamidomethyl (C)")});	//lista stalych modyfikacji (wszystkie C beda mialy te modyfikacje)
			this.mDigestConfig.setVariablePTMs(new PTM[] {	PTMMap.getPTM("Oxidation (M)"),		//lista zmiennych modyfikacji (powstana zarowno sekwencje z modyfikacjami na M S T, jak i bez)
															PTMMap.getPTM("Phospho (ST)")});	
			this.mDigestConfig.mMaxSingleVarPTM=2;												//maksymalna liczba modyfikacji zmiennych jednego rodzaju w sekwencji peptydu
			this.mDigestConfig.mMaxTotalVarPTMs=2;												//maksymalna liczba modyfikacji zmiennych wszystkich rodzajow w sekwencji peptydu
			this.mDigestConfig.mSingleVariantVarPTM=false;										//jezeli true to nie sa generowane kombinacje polozen tego samego zestawu modyfikacji zmiennych 

			
			this.mDigestConfig.mBxzSequences=InSilicoDigestConfig.BXZ_EXPAND;					//sekwencje z B, X i Z beda zamieniane na liste swoich wariantow (B: Asx => Asn (N) lub Asp (D), Z: Glx => Glu (E) lub Gln (Q), X: dowolny aminokwas)
			//this.mDigestConfig.mBxzSequences=InSilicoDigestConfig.BXZ_EXCLUDE;				//sekwencje z B, X i Z zostana pominiete
			//this.mDigestConfig.mBxzSequences=InSilicoDigestConfig.BXZ_KEEP;					//sekwencje z B, X i Z zostana niezmienione 

			this.mDigestConfig.mBxzSequencesMaxBZ=3;											//maksymalna liczba znakow B lub Z w sekwencji 
			this.mDigestConfig.mBxzSequencesMaxX=1;												//maksymalna liczba znakow X w sekwencji 
			this.mDigestConfig.mBxzSequencesWithOU=false;										//jezeli true, to znak X jest zaminiany na 22 aminokwasy (lacznie z O i U)  
		    
			if ((proteins=this.readProteins(filename))!=null)
				this.writeProteins(proteins,filename);
			
		}
		catch (MScanException mse)
		{
			System.out.println("ERROR: " + mse.toString());
		}
		catch (IOException ioe)
		{
			System.out.println("ERROR: " + ioe.toString());
		}
	}
	
	/**
	 * Metoda zapisujaca informacje o bialkach i listy ich peptydow
	 *  
	 * @param proteins	zbior bialek
	 * @param filename	nazwa pliku
	 * 
	 * @throws IOException
	 */
	
	private void writeProteins(LinkedHashSet<Protein> proteins,String filename) throws IOException
	{
		BufferedWriter						writer=null;
		LinkedHashSet<AminoAcidSequence>	peptides=null;
		int									peptidesCount=0;
		
		long start=System.currentTimeMillis();
		
		System.out.println("Writing out file: " + MScanSystemTools.replaceExtension(filename,"fasta.out"));
		
		writer=new BufferedWriter(new FileWriter(MScanSystemTools.replaceExtension(filename,"fasta.out")));
	
		for (Protein protein: proteins)
		{	
			/*
			 * wyznaczenie listy peptydow proteolitycznych 
			 */
			peptides=InSilicoDigest.digestSequence(protein.getSequence(),this.mDigestConfig);
			
			peptidesCount+=peptides.size();
			
			/*
			 * zapis informacji o bialku
			 */
			this.calcProteinMass(protein);
			this.writeLine(writer,"Protein:" + protein.getId() + " " + protein.getName());// + " " + protein.getMass());
			
			/*
			 * zapis listy peptydow bialka
			 */
			for (AminoAcidSequence peptide : peptides)
				this.writeLine(writer,peptide.toString(AminoAcidSequence.FORMAT_MASCOT,this.mDigestConfig.mVariablePTMsMap));
		}
		writer.close();
		
		System.out.println(InSilicoDigest.SCOUNT);
		System.out.println(InSilicoDigest.RSCOUNT);
		System.out.println(InSilicoDigest.MOD_RSCOUNT);
		System.out.println("\nTotal numer of peptides: " + peptidesCount);
		System.out.println("Time: " + (System.currentTimeMillis()-start));
	}
	
	/**
	 * Metoda wyznaczajaca mase bialka
	 * 
	 * @param protein	bialko
	 */
	private void calcProteinMass(Protein protein)
	{
		MassCalc MT=null;
		
		MT=new MassCalc();
		
		MT.setAminoacidsSequence(protein.getSequence());
		protein.setMass(MT.getSequenceMass(MassCalc.AVERAGE));
	}
	
	/**
	 * Metoda zapisujaca linie do pliku tekstowego
	 * 
	 * @param writer	otwarty plik
	 * @param line		linia
	 * @throws IOException
	 */
	private void writeLine(BufferedWriter writer,String line) throws IOException
	{
		writer.write(line);
		writer.newLine();
	}
	
	/**
	 * Metoda odczytujaca plik w formacie FASTA 
	 * 
	 * @param filename	nazwa pliku
	 * 
	 * @return			zbior bialek
	 * 
	 * @throws MScanException
	 */
	private LinkedHashSet<Protein> readProteins(String filename) throws MScanException
	{
		LinkedHashSet<Protein>		proteins=null;
		FastaImportConfig			fastaConfig=null;
		FastaFileReader				fastaReader=null;
		Vector<FastaRecord>			fastaRecords=null;
		
		System.out.println("Reading FASTA file: " + filename);

		fastaConfig=new FastaImportConfig(DBTools.DB_SP);
		fastaReader=new FastaFileReader(filename,fastaConfig);
		
		if ((fastaRecords=fastaReader.readRecords())!=null)
		{
			proteins=new LinkedHashSet<Protein>(fastaRecords.size());
			for (int i=0;i<fastaRecords.size();i++)
				proteins.add(fastaRecords.get(i).getProtein());
		}
		return(proteins);
	}
	
	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args)
	{
		if (args!=null && args.length>0)
		{
			new ProteinDigestAdv(args[0]);
		}
		else
		{
			System.out.println("Missing input file.");
		}
	}
}