package com.gempukku.stccg;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class JsonToXmlTest {

    @Test
    public void jsonToXmlTest() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        TestEvent testEvent = new TestEvent();
//        JsonNode eventNode = mapper.valueToTree(testEvent);

        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        xmlMapper.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
//        xmlMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        String xmlString = xmlMapper.writeValueAsString(testEvent);

        System.out.println(xmlString);

        String xmlString2 = xmlMapper.writeValueAsString(new EventList(testEvent, testEvent));
        System.out.println(xmlString2);
    }

    @JacksonXmlRootElement(localName = "gameState")
    public class EventList {

        @JacksonXmlProperty(localName = "cn", isAttribute = true)
        final int channelNumber = 0;

        private static final TestEvent[] DEFAULTCAPACITY_EMPTY_ELEMENTDATA = {};
        transient TestEvent[] elementData; // non-private to simplify nested class access
        private int size;
        private static final int DEFAULT_CAPACITY = 0;
        public static final int SOFT_MAX_ARRAY_LENGTH = Integer.MAX_VALUE - 8;
        protected transient int modCount = 0;

        EventList() {
            this.elementData = DEFAULTCAPACITY_EMPTY_ELEMENTDATA;
        }

        EventList(TestEvent... events) {
            this.elementData = DEFAULTCAPACITY_EMPTY_ELEMENTDATA;
            for (TestEvent event : events) {
                add(event);
            }
        }


/*        public String serialize() throws JsonProcessingException {
            XmlMapper xmlMapper = new XmlMapper();
            xmlMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
            xmlMapper.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
            return xmlMapper.writeValueAsString(list);
        } */

        private void add(TestEvent e, TestEvent[] elementData, int s) {
            if (s == elementData.length)
                elementData = grow();
            elementData[s] = e;
            size = s + 1;
        }

        private TestEvent[] grow(int minCapacity) {
            int oldCapacity = elementData.length;
            if (oldCapacity > 0 || elementData != DEFAULTCAPACITY_EMPTY_ELEMENTDATA) {
                int newCapacity = newLength(oldCapacity,
                        minCapacity - oldCapacity, /* minimum growth */
                        oldCapacity >> 1           /* preferred growth */);
                return elementData = Arrays.copyOf(elementData, newCapacity);
            } else {
                return elementData = new TestEvent[Math.max(DEFAULT_CAPACITY, minCapacity)];
            }
        }

        private TestEvent[] grow() {
            return grow(size + 1);
        }
        public boolean add(TestEvent e) {
            modCount++;
            add(e, elementData, size);
            return true;
        }

        private static int newLength(int oldLength, int minGrowth, int prefGrowth) {
            // preconditions not checked because of inlining
            // assert oldLength >= 0
            // assert minGrowth > 0

            int prefLength = oldLength + Math.max(minGrowth, prefGrowth); // might overflow
            if (0 < prefLength && prefLength <= SOFT_MAX_ARRAY_LENGTH) {
                return prefLength;
            } else {
                // put code cold in a separate method
                return hugeLength(oldLength, minGrowth);
            }
        }

        private static int hugeLength(int oldLength, int minGrowth) {
            int minLength = oldLength + minGrowth;
            if (minLength < 0) { // overflow
                throw new OutOfMemoryError(
                        "Required array length " + oldLength + " + " + minGrowth + " is too large");
            } else if (minLength <= SOFT_MAX_ARRAY_LENGTH) {
                return SOFT_MAX_ARRAY_LENGTH;
            } else {
                return minLength;
            }
        }

        @JsonInclude
        @JacksonXmlElementWrapper(localName = "doesn't matter", useWrapping = false)
        @JacksonXmlProperty(localName = "ge")
        public TestEvent[] getList() {
            return elementData;
        }

        @JsonInclude
        @JacksonXmlElementWrapper(localName = "clocks")
        @JacksonXmlProperty(localName = "clock")
        private List<UserClock> getClocks() {
            List<UserClock> result = new ArrayList<>();
            result.add(new UserClock("qwer", 7192));
            result.add(new UserClock("asdf", 7200));
            return result;
        }

        private static class UserClock {

            @JacksonXmlProperty(localName = "participantId", isAttribute = true)
            final String _participantId;
            @JacksonXmlText
            final int _secondsLeft;

            UserClock(String participantId, int secondsLeft) {
                _participantId = participantId;
                _secondsLeft = secondsLeft;
            }

        }

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