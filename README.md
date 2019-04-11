# mp-jee-testing

# How to run locally:

```
./gradlew test
```

# Proposed mockup:
```java
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.aguibert.testcontainers.framework.*;

@Testcontainers
@MicroProfileTest
public class PersonServiceTest {

    @Container
    public static MicroProfileApplication myService = new MicroProfileApplication("my-service", "myservice");

    @RestClient
    public static PersonService personSvc;

    @Test
    public void testGetPerson() {
        Long bobId = personSvc.createPerson("Bob", 24);
        Person bob = personSvc.getPerson(bobId);
        assertEquals("Bob", bob.name);
        assertEquals(24, bob.age);
    }

}
```
