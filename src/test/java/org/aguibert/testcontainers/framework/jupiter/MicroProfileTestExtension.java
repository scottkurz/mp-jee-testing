/**
 *
 */
package org.aguibert.testcontainers.framework.jupiter;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;

import org.aguibert.testcontainers.framework.MicroProfileApplication;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;
import org.junit.platform.commons.support.AnnotationSupport;
import org.testcontainers.junit.jupiter.Container;

/**
 * @author aguibert
 */
public class MicroProfileTestExtension implements BeforeEachCallback, BeforeAllCallback, TestInstancePostProcessor {
    private static final Namespace NAMESPACE = Namespace.create(MicroProfileTestExtension.class);
    private static final String NAMESPACE_KEY = "mpExtensionKey";

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext context) throws Exception {
        System.out.println("@AGG post process");
        ExtensionContext.Store store = context.getStore(NAMESPACE);
        store.put(NAMESPACE_KEY, testInstance);
    }

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        System.out.println("@AGG beforeAll");
        Class<?> testClass = context.getRequiredTestClass();
        injectRestClients(testClass);
    }

    private static void injectRestClients(Class<?> clazz) throws Exception {
        System.out.println("@AGG injecting rest clients for " + clazz);
        List<Field> restClientFields = AnnotationSupport.findAnnotatedFields(clazz, RestClient.class);
        if (restClientFields.size() == 0)
            return;

        // There are 1 or more @RestClient fields. Match them with a server container
        MicroProfileApplication mpApp = null;
        List<Field> containerFields = AnnotationSupport.findAnnotatedFields(clazz, Container.class);
        for (Field f : containerFields) {
            System.out.println("   containerField=" + f);
            System.out.println("       is assignable1? " + MicroProfileApplication.class.isAssignableFrom(f.getType()));
            System.out.println("       is assignable2? " + f.getType().isAssignableFrom(MicroProfileApplication.class));
            System.out.println("       is static? " + Modifier.isStatic(f.getModifiers()));
            if (!MicroProfileApplication.class.isAssignableFrom(f.getType()))
                continue;
            // TODO: Handle non-static @Container fields
            if (!Modifier.isStatic(f.getModifiers()))
                continue;
            if (mpApp != null)
                throw new ExtensionConfigurationException("Multiple MicroProfileApplication instances found." +
                                                          " Cannot auto-configure @RestClient");
            mpApp = (MicroProfileApplication) f.get(null);
            System.out.println("        @AGG found MP App field: " + f);
        }

        if (mpApp == null)
            throw new ExtensionConfigurationException("No MicroProfileApplication instances found to connect @RestClient with");

        // At this point we have found exactly one MicroProfileApplication -- proceed with auto-configure
        if (!mpApp.isCreated() || !mpApp.isRunning())
            throw new ExtensionConfigurationException("Container " + mpApp.getDockerImageName() + " is not running yet. " +
                                                      "It should have been started by the @Testcontainers extension. " +
                                                      "TIP: Make sure that you list @TestContainers before @MicroProfileTest!");

        for (Field restClientField : restClientFields) {
            checkPublicStaticNonFinal(restClientField);
            Object restClient = mpApp.createRestClient(restClientField.getType());
            restClientField.set(null, restClient);
            System.out.println("@AGG injected rest client: " + restClient);
        }
    }

    private static void checkPublicStaticNonFinal(Field f) {
        if (!Modifier.isPublic(f.getModifiers()) ||
            !Modifier.isStatic(f.getModifiers()) ||
            Modifier.isFinal(f.getModifiers())) {
            throw new ExtensionConfigurationException("@RestClient annotated field must be public, static, and non-final: " + f.getName());
        }
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        System.out.println("@AGG before each");
    }

}
