package examples;
import mscanlib.ms.mass.AminoAcidSequence;

import java.util.ArrayList;
import java.util.List;


public class Peptide
{
    private AminoAcidSequence sequence;
    private List<String> proteinIds;
    
    public Peptide(AminoAcidSequence sequence, String proteinId)
    {
        this.sequence = sequence;
        proteinIds = new ArrayList<>();
        proteinIds.add(proteinId);
    }

    public float getMass(){
        return (float)sequence.getMonoMass();
    }
    /**
     * @return the sequence
     */
    public AminoAcidSequence getSequence()
    {
        return sequence;
    }

    /**
     * @param sequence the sequence to set
     */
    public void setSequence(AminoAcidSequence sequence)
    {
        this.sequence = sequence;
    }

    /**
     * @return the proteinId
     */
    public String getProteinId()
    {
        return proteinIds.get(0);
    }

    /**
     * @param proteinId the proteinId to set
     */
    public void setProteinId(String proteinId)
    {
        proteinIds.add(proteinId);
    }

    public List<String> getProteinIds() {
        return proteinIds;
    }

    @Override
    public String toString() {
        return "Peptide{" +
                "sequence=" + sequence +
                ", proteinId='" + proteinIds + '\'' +
                '}';
    }
}
