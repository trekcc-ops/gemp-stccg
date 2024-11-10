import org.junit.jupiter.api.Test;

import java.util.*;

public class NewLibraryTestyTest extends NewLibraryTest {

    @Test
    public void constructLibraryMap() {
        createLibrary();
        System.out.println(_newLibraryMap.size());

        Map<String, Integer> typeCount = new HashMap<>();
        for (CardData card : _newLibraryMap.values()) {
            typeCount.merge(card._type, 1, Integer::sum);
        }

        List<String> typeCounts = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : typeCount.entrySet())
            typeCounts.add(entry.getKey() + ": " + entry.getValue());
        Collections.sort(typeCounts);
        typeCounts.forEach(System.out::println);

        List<String> artifacts = new ArrayList<>();

        for (CardData card : _newLibraryMap.values())
            if (Objects.equals(card._type, "Artifact"))
                artifacts.add(card._title);
        Collections.sort(artifacts);
        artifacts.forEach(System.out::println);

        for (CardData card : _newLibraryMap.values())
            if (Objects.equals(card._title, "The Genesis Device"))
                System.out.println(card._rawGameText);

    }
}