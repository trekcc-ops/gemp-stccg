import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class FullTextTest extends NewLibraryTest {

    public void fullTextTest() {
        createLibrary();
        int canBeParsed = 0;
        int cannotBeParsed = 0;

        List<String> includedTypes = new LinkedList<>();
        includedTypes.add("Event");
        includedTypes.add("Incident");
        includedTypes.add("Interrupt");

        for (CardData card : _newLibraryMap.values()) {
            if (includedTypes.contains(card._type) && Objects.equals(card._set, "TNG")) {
                if (card._gameText.canBeParsed()) {
                    canBeParsed++;
                } else {
                    System.out.println(card._gameText);
                    cannotBeParsed++;
                }
            }
        }
        System.out.println("Can be parsed: " + canBeParsed);
        System.out.println("Cannot be parsed: " + cannotBeParsed);
    }


}