package com.gempukku.stccg.gamestate;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class IdDeserializerTest {

    @Test
    public void serializeTest() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        School school = new School(1, "St Magdalene's");
        Student mary = new Student(1, "Mary", school);
        Student bob = new Student(2, "Bob", school);
        Student[] students = new Student[] {mary, bob};

        // Write out
        String serialized = mapper.writeValueAsString(students);
        System.out.println("Serialized: " + serialized);
        // Read in
        Student[] deserialized = mapper.readValue(serialized, Student[].class);
    }

    @JsonIdentityInfo(scope=School.class, generator= ObjectIdGenerators.PropertyGenerator.class, property="id")
    private class School {
        private Integer id;
        private String name;

        public School() {
        }

        public School(Integer id, String name) {
            this.id = id;
            this.name = name;
        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }


    @JsonIdentityInfo(scope=Student.class, generator=ObjectIdGenerators.PropertyGenerator.class, property="id")
    private class Student {
        private Integer id;
        private String name;
        private School school;

        public Student() {
        }

        public Student(Integer id, String name, School school) {
            this.id = id;
            this.name = name;
            this.school = school;
        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public School getSchool() {
            return school;
        }

        public void setSchool(School school) {
            this.school = school;
        }
    }


}