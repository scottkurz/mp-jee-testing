package org.aguibert.liberty;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collection;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;

import org.aguibert.testcontainers.framework.MicroProfileApplication;
import org.aguibert.testcontainers.framework.jupiter.MicroProfileTest;
import org.aguibert.testcontainers.framework.jupiter.RestClient;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@MicroProfileTest
public class BasicJAXRSServiceTest {

    @Container
    public static MicroProfileApplication myService = new MicroProfileApplication("my-service", "myservice");

    @RestClient
    public static PersonService personSvc;

    @Test
    public void testCreatePerson() {
        Long createId = personSvc.createPerson("Hank", 42);
        assertNotNull(createId);
    }

    @Test
    public void testMinSizeName() {
        Long minSizeNameId = personSvc.createPerson("Ha", 42);
        assertEquals(new Person("Ha", 42, minSizeNameId),
                     personSvc.getPerson(minSizeNameId));
    }

    @Test
    public void testMinAge() {
        Long minAgeId = personSvc.createPerson("Newborn", 0);
        assertEquals(new Person("Newborn", 0, minAgeId),
                     personSvc.getPerson(minAgeId));
    }

    @Test
    public void testGetPerson() {
        Long bobId = personSvc.createPerson("Bob", 24);
        Person bob = personSvc.getPerson(bobId);
        assertEquals("Bob", bob.name);
        assertEquals(24, bob.age);
        assertNotNull(bob.id);
    }

    @Test
    public void testGetAllPeople() {
        Long person1Id = personSvc.createPerson("Person1", 1);
        Long person2Id = personSvc.createPerson("Person2", 2);

        Person expected1 = new Person("Person1", 1, person1Id);
        Person expected2 = new Person("Person2", 2, person2Id);

        Collection<Person> allPeople = personSvc.getAllPeople();
        assertTrue("Expected at least 2 people to be registered, but there were only: " + allPeople,
                   allPeople.size() >= 2);
        assertTrue("Did not find person " + expected1 + " in all people: " + allPeople,
                   allPeople.contains(expected1));
        assertTrue("Did not find person " + expected2 + " in all people: " + allPeople,
                   allPeople.contains(expected2));
    }

    @Test
    public void testUpdateAge() {
        Long personId = personSvc.createPerson("newAgePerson", 1);

        Person originalPerson = personSvc.getPerson(personId);
        assertEquals("newAgePerson", originalPerson.name);
        assertEquals(1, originalPerson.age);
        assertEquals(personId, Long.valueOf(originalPerson.id));

        personSvc.updatePerson(personId, new Person(originalPerson.name, 2, originalPerson.id));
        Person updatedPerson = personSvc.getPerson(personId);
        assertEquals("newAgePerson", updatedPerson.name);
        assertEquals(2, updatedPerson.age);
        assertEquals(personId, Long.valueOf(updatedPerson.id));
    }

    @Test
    public void testGetUnknownPerson() {
        assertThrows(NotFoundException.class, () -> personSvc.getPerson(-1L));
    }

    @Test
    public void testCreateBadPersonNullName() {
        assertThrows(BadRequestException.class, () -> personSvc.createPerson(null, 5));
    }

    @Test
    public void testCreateBadPersonNegativeAge() {
        assertThrows(BadRequestException.class, () -> personSvc.createPerson("NegativeAgePersoN", -1));
    }

    @Test
    public void testCreateBadPersonNameTooLong() {
        assertThrows(BadRequestException.class, () -> personSvc.createPerson("NameTooLongPersonNameTooLongPersonNameTooLongPerson", 5));
    }

}