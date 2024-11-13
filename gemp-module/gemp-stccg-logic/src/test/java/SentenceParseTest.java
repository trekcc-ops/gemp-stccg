import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;

public class SentenceParseTest extends NewLibraryTest {

    @Test
    public void sentenceTest() {
        createLibrary();
        int canBeParsed = 0;
        int cannotBeParsed = 0;

        List<String> includedTypes = new LinkedList<>();
        includedTypes.add("Event");
        includedTypes.add("Incident");
        includedTypes.add("Interrupt");

        for (CardData card : _newLibraryMap.values()) {
            if (includedTypes.contains(card._type)) {
//            if (includedTypes.contains(card._type) && Objects.equals(card._set, "TNG")) {
                for (Sentence sentence : card._gameText.getSentences()) {
                    if (sentence.canBeParsed()) {
                        canBeParsed++;
                    } else {
                        System.out.println(sentence);
                        cannotBeParsed++;
                    }
                }
            }
        }
        System.out.println("Can be parsed: " + canBeParsed);
        System.out.println("Cannot be parsed: " + cannotBeParsed);
    }


}