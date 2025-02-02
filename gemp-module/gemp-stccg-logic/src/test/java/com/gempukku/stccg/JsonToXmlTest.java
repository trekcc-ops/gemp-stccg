package com.gempukku.stccg;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import org.junit.jupiter.api.Test;

public class JsonToXmlTest {

    @Test
    public void jsonToXmlTest() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        TestEvent testEvent = new TestEvent();
//        JsonNode eventNode = mapper.valueToTree(testEvent);

        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        xmlMapper.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
        String xmlString = xmlMapper.writeValueAsString(testEvent);

        System.out.println(xmlString);
    }


    @JacksonXmlRootElement(localName = "ge")
    public class TestEvent {

        @JacksonXmlProperty(localName = "allParticipantIds", isAttribute = true)
        public final String _allParticipantIds = "qwer,asdf";
        @JacksonXmlProperty(localName = "discardPublic", isAttribute = true)
        public final boolean discardPublic = false;

        @JacksonXmlProperty(localName = "participantId", isAttribute = true)
        public final String participantId = "qwer";

        @JacksonXmlProperty(localName = "timestamp", isAttribute = true)
        public final String timestamp = "2025-02-02 01:12:21.8257";

        @JacksonXmlProperty(localName = "type", isAttribute = true)
        public final String type = "P";
        @JacksonXmlProperty(localName = "notGiven", isAttribute = true)
        public String notGiven;
    }

}