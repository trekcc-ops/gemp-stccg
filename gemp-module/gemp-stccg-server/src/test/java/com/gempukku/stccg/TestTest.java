package com.gempukku.stccg;

import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.formats.FormatLibrary;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.util.Date;

public class TestTest {

    @Test
    public void TestyTestTest() {
        float floatVal = 5;
        checkType("blork");
        checkType(3);
        checkType(floatVal);
        checkType(System.currentTimeMillis());
        checkType(new Timestamp(new Date().getTime()));
        checkType(null);
    }

    private void checkType(Object object) {
        if (object instanceof String)
            System.out.println("string");
        if (object instanceof Integer)
            System.out.println("integer");
        if (object instanceof Long)
            System.out.println("long");
        if (object instanceof Float)
            System.out.println("float");
        if (object instanceof Timestamp)
            System.out.println("timestamp");
        if (object == null)
            System.out.println("null");
    }
}
