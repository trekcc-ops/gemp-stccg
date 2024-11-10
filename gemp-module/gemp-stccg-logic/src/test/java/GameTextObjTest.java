public class GameTextObjTest extends NewLibraryTest {


    void multiSentences() {
        createLibrary();
        for (CardData card : _newLibraryMap.values()) {
            if (!card._gameText.canBeParsed())
                System.out.println(card._gameText);
        }
    }


}