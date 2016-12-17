package examples;

import mscanlib.MScanException;
import mscanlib.ms.mass.AminoAcidSequence;
import mscanlib.ms.mass.MassTools;
import mscanlib.ms.mass.PTMMap;


/**
 * Przyklad wykorzystania obiektu reprezentujacego sekwencje aminokwasow 
 *  
 * @author trubel
  */
public class AASequence
{	
	/**
	 * Konstruktor
	 */
	public AASequence(String peptyd)
	{
		AminoAcidSequence	aaSequence=null;	//sekwencja peptydu
		int					charge=2;			//stopien naladowania jonu peptydowego
		
		/*
		 * Inicjalizacja map aminokwasow i modyfikacji
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
		 * Utworzenie obiektu reprezentujacego sekwencje peptydu i dodanie modyfikacji do sekwencji (fosforylacja T na pozycji 4) 
		 */
		aaSequence=new AminoAcidSequence(peptyd);
		
		/*
		 * Wypisanie wlasciwosci sekwencji
		 */
		System.out.println("Amino acid sequence: " + aaSequence.getSequence());
		System.out.println("Composition: " + aaSequence.getComposition());
		System.out.println("Mass: " + aaSequence.getMonoMass() + " Da");
		System.out.println("m/z (charge state +" + charge + "): " + MassTools.getMz(aaSequence.getMonoMass(),charge));
		System.out.println("Amino acid at position 4: " + aaSequence.getAminoAcid(3));
	}
	
	/**
	 * main
	 * 
	 * @param args
	 */
	public static void main(String[] args)
	{
		//new AASequence();
	}
}
