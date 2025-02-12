package com.gempukku.stccg;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class SubstringTest {

    @Test
    public void substringTest() {
        List<String> strings = new ArrayList<>();
        strings.add("SERVER/getGameState/35/admin");
        strings.add("SERVER/getGameState/35/thisPlayer");

        for (String string : strings) {
            String userType = string.substring(string.lastIndexOf("/")).replace("/","");
            String gameId = string.substring(string.indexOf("getGameState/")).replace("getGameState","").replace(userType,"").replace("/","");
            System.out.println(userType);
            System.out.println(gameId);
        }


/*        for (String string : strings) {
            String afterServer = string.substring("SERVER/".length());
            String handlerType = afterServer.substring(0, afterServer.indexOf("/"));
            String remainingString = afterServer.substring(handlerType.length());
            System.out.println(handlerType);
            System.out.println(remainingString);
            System.out.println();
        } */
    }

    private String getPath(String string) {
        String afterServer = string.substring("SERVER/".length());
        String result = afterServer.substring(0, afterServer.indexOf("/"));
        return result;
    }

}