import java.util.Comparator;

public class ChromosomeComparator implements Comparator<Chromosome> {
    @Override
    public int compare(Chromosome c1, Chromosome c2) {
        return Integer.compare(c1.getValue(), c2.getValue());
    }
}