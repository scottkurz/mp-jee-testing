/**
 *
 */
package org.aguibert.testcontainers.framework;

import java.util.ArrayList;
import java.util.List;

import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;

/**
 * @author aguibert
 *
 */
public class MicroProfileApplication extends GenericContainer<MicroProfileApplication> {

    static final Logger LOGGER = LoggerFactory.getLogger(MicroProfileApplication.class);

    private final String appContextRoot;
    private String baseURL;

    public MicroProfileApplication(final String dockerImageName) {
        this(dockerImageName, "/");
    }

    public MicroProfileApplication(final String dockerImageName, String appContextRoot) {
        super(dockerImageName);
        withExposedPorts(9080);
        withLogConsumer(new Slf4jLogConsumer(LOGGER));
        if (appContextRoot != null && !appContextRoot.startsWith("/"))
            appContextRoot = "/" + appContextRoot;
        this.appContextRoot = appContextRoot;
//        if (appContextRoot == null)
//            waitingFor(Wait.forLogMessage("^.*CWWKF0011I.*$", 1));
        waitingFor(Wait.forHttp(this.appContextRoot)); // TODO: can eventually default this to MP Health 2.0 readiness check
    }

    public <T> T createRestClient(Class<T> clazz, String applicationPath) {
        List<Class<?>> providers = new ArrayList<>();
        providers.add(JsonBProvider.class);
        String urlPath = getBaseURL();
        if (applicationPath != null)
            urlPath += applicationPath;
        return JAXRSClientFactory.create(urlPath, clazz, providers);
    }

    public <T> T createRestClient(Class<T> clazz) {
        return createRestClient(clazz, appContextRoot);
    }

    public String getBaseURL() throws IllegalStateException {
        if (baseURL != null)
            return baseURL;
        if (!this.isRunning())
            throw new IllegalStateException("Container must be running to determine hostname and port");
        baseURL = "http://" + this.getContainerIpAddress() + ':' + this.getFirstMappedPort();
        return baseURL;
    }

}