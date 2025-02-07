package com.gempukku.stccg;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class SubstringTest {

    @Test
    public void substringTest() {
        List<String> strings = new ArrayList<>();
        strings.add("SERVER/game/35");
        strings.add("SERVER/gameHistory/35");

        for (String string : strings) {
            String afterServer = string.substring("SERVER/".length());
            String handlerType = afterServer.substring(0, afterServer.indexOf("/"));
            String remainingString = afterServer.substring(handlerType.length());
            System.out.println(handlerType);
            System.out.println(remainingString);
            System.out.println();
        }
    }

    private String getPath(String string) {
        String afterServer = string.substring("SERVER/".length());
        String result = afterServer.substring(0, afterServer.indexOf("/"));
        return result;
    }

}