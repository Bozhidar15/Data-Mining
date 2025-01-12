import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Instance {
    String classification; // Клас на инстанцията
    List<String> attributes; // Атрибути на инстанцията

    public Instance(String[] data) {
        this.classification = data[1]; // предполагаме, че класът е второ поле
        this.attributes = new ArrayList<>(Arrays.asList(data).subList(2, data.length));
    }
}
