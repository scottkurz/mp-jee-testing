package org.aguibert.liberty;

import static org.junit.Assert.assertEquals;

import org.aguibert.testcontainers.framework.MicroProfileApplication;
import org.aguibert.testcontainers.framework.jupiter.MicroProfileTest;
import org.aguibert.testcontainers.framework.jupiter.RestClient;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@MicroProfileTest
public class PersonServiceTest {

    @Container
    public static MicroProfileApplication myService = new MicroProfileApplication("my-service", "myservice");

    @RestClient
    public static PersonService personSvc;

    @BeforeAll
    public static void helloAll() {
        System.out.println("HELLO BEFORE EACH");
    }

    @BeforeEach
    public void hello() {
        System.out.println("HELLO BEFORE EACH");
    }

    @Test
    public void testGetPerson() {
        Long bobId = personSvc.createPerson("Bob", 24);
        Person bob = personSvc.getPerson(bobId);
        assertEquals("Bob", bob.name);
        assertEquals(24, bob.age);
    }

}