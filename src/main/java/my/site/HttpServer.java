package my.site;

import my.site.controller.AbstractController;
import my.site.filter.StaticResourceCacheFilter;
import org.apache.tomcat.InstanceManager;
import org.apache.tomcat.SimpleInstanceManager;
import org.eclipse.jetty.annotations.ServletContainerInitializersStarter;
import org.eclipse.jetty.apache.jsp.JettyJasperInitializer;
import org.eclipse.jetty.jsp.JettyJspServlet;
import org.eclipse.jetty.plus.annotation.ContainerInitializer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.jetty.servlet.*;
import org.eclipse.jetty.webapp.WebAppContext;
import org.reflections.Reflections;
import org.slf4j.bridge.SLF4JBridgeHandler;

import javax.servlet.DispatcherType;
import javax.servlet.annotation.WebServlet;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

/**
 * Main application - HTTP server using embedded Jetty container.
 * Based on https://github.com/jetty-project/embedded-jetty-jsp.
 *
 * @author Radek Beran
 */
public class HttpServer {

    /**
     * Root of web application resources. That folder has to be just somewhere in classpath.
     */
	private static final String WEB_APP_ROOT = "webapp";

    private static final int DEFAULT_PORT = 80;

    private static final String APP_NAME = "jetty-template";

    private static final int SESSION_DURATION_MIN = 30;

    private final Server server;

    public static void main(String[] args) throws Exception {
        // add SLF4JBridgeHandler to j.u.l's root logger, should be done once during
        // the initialization phase of your application
        SLF4JBridgeHandler.install();

        HttpServer httpServer = new HttpServer();
        httpServer.start(getPort());
        httpServer.waitForInterrupt();
    }

    public HttpServer() {
        this.server = new Server();
    }

    public void start(int port) throws Exception {
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(port);
        server.addConnector(connector);

        // Set JSP to use Standard JavaC always
        System.setProperty("org.apache.jasper.compiler.disablejsr199", "false");

        URI baseUri = getWebRootResourceUri();
        WebAppContext webAppContext = getWebAppContext(baseUri, getContextTempDir());

        server.setHandler(webAppContext);

        // Start Server
        server.start();
        // server.dump(); can be logged as server state
    }

    /**
     * Cause server to keep running until it receives an interrupt.
     * <p>Interrupt Signal, or SIGINT (Unix Signal), is typically seen as a result of a kill -TERM {pid} or Ctrl+C
     * @throws InterruptedException if interrupted
     */
    public void waitForInterrupt() throws InterruptedException {
        if (server != null) {
            server.join();
        }
    }

    private URI getWebRootResourceUri() throws FileNotFoundException, URISyntaxException {
        URL webRootUrl = this.getClass().getResource("/" + WEB_APP_ROOT);
        if (webRootUrl == null) {
            throw new FileNotFoundException("Unable to find resource /" + WEB_APP_ROOT);
        }
        return webRootUrl.toURI();
    }

    private static int getPort() {
        int port = DEFAULT_PORT;
        String portStr = System.getenv("PORT");
        if (portStr != null && !portStr.isEmpty()) {
            port = Integer.valueOf(portStr).intValue();
        }
        return port;
    }

    /**
     * Establish temporary directory for the servlet context (used by JSP compilation).
     */
    private File getContextTempDir() throws IOException {
        File tempDir = new File(System.getProperty("java.io.tmpdir"));
        File scratchDir = new File(tempDir, APP_NAME + "-context-tmp");

        if (!scratchDir.exists()) {
            if (!scratchDir.mkdirs()) {
                throw new IOException("Unable to create scratch directory: " + scratchDir);
            }
        }
        return scratchDir;
    }

    /**
     * Setup the basic application "context" for this application at "/"
     * This is also known as the handler tree (in jetty speak).
     */
    private WebAppContext getWebAppContext(URI baseUri, File contextTempDir) {
        WebAppContext context = new WebAppContext();
        context.setContextPath("/");
        context.setAttribute("javax.servlet.context.tempdir", contextTempDir);
        // To resolve JSP JSTL tags in JAR files:
        context.setAttribute("org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern",
                ".*/[^/]*servlet-api-[^/]*\\.jar$|.*/javax.servlet.jsp.jstl-.*\\.jar$|.*/.*taglibs.*\\.jar$");
        context.setResourceBase(baseUri.toASCIIString());
        context.setAttribute("org.eclipse.jetty.containerInitializers", jspInitializers());
        context.setAttribute(InstanceManager.class.getName(), new SimpleInstanceManager());
        context.addBean(new ServletContainerInitializersStarter(context), true);
        context.setClassLoader(getUrlClassLoader());

        context.addServlet(jspServletHolder(), "*.jsp");

        configureApplication(context);
        return context;
    }

    /**
     * Ensure the jsp engine is initialized correctly.
     */
    private List<ContainerInitializer> jspInitializers() {
        JettyJasperInitializer sci = new JettyJasperInitializer();
        ContainerInitializer initializer = new ContainerInitializer(sci, null);
        List<ContainerInitializer> initializers = new ArrayList<>();
        initializers.add(initializer);
        return initializers;
    }

    /**
     * Create JSP Servlet (must be named "jsp").
     */
    private ServletHolder jspServletHolder() {
        ServletHolder holderJsp = new ServletHolder("jsp", JettyJspServlet.class);
        holderJsp.setInitOrder(0);
        holderJsp.setInitParameter("logVerbosityLevel", "INFO");
        holderJsp.setInitParameter("fork", "false");
        holderJsp.setInitParameter("xpoweredBy", "false");
        holderJsp.setInitParameter("compilerTargetVM", "1.8");
        holderJsp.setInitParameter("compilerSourceVM", "1.8");
        holderJsp.setInitParameter("keepgenerated", "true");
        return holderJsp;
    }

    /**
     * Set classloader of context to be sane (needed for JSTL).
     * JSP requires a non-system classloader, this simply wraps the
     * embedded system classloader in a way that makes it suitable
     * for JSP to use.
     */
    private ClassLoader getUrlClassLoader() {
        return new URLClassLoader(new URL[0], this.getClass().getClassLoader());
    }

    /**
     * Application specific configuration.
     * @param context
     */
    private void configureApplication(WebAppContext context) {
        // Add application servlets
        registerApplicationServlets(context);

        // Add servlet filters
        registerApplicationFilters(context);

        context.setDisplayName(APP_NAME);
        context.setErrorHandler(createErrorHandler());
        context.getSessionHandler().getSessionManager().setMaxInactiveInterval(SESSION_DURATION_MIN * 60); // in seconds
    }

    private void registerApplicationFilters(WebAppContext context) {
        FilterHolder staticCacheFilter = new FilterHolder(StaticResourceCacheFilter.class);
        context.addFilter(staticCacheFilter, "/bootstrap/*,/javascripts/*,/styles/*", EnumSet.of(DispatcherType.REQUEST));
    }

    private void registerApplicationServlets(WebAppContext context) {
        // Our compiled servlets are not under WEB-INF/classes or WEB-INF/lib,
        // so we need to scan annotated servlets ourselves:
        Reflections reflections = new Reflections(AbstractController.class.getPackage().getName());
        Set<Class<?>> annotatedServlets = reflections.getTypesAnnotatedWith(WebServlet.class);
        registerAnnotatedServlets(context, annotatedServlets);

        // Lastly, the default servlet for root content (always needed, to satisfy servlet spec)
        // It is important that this is last.
        ServletHolder defaultJettyServlet = new ServletHolder("default", DefaultServlet.class);
        defaultJettyServlet.setInitParameter("acceptRanges", "true");
        defaultJettyServlet.setInitParameter("cacheControl", "max-age=604800,public");
        defaultJettyServlet.setInitOrder(1);
        context.addServlet(defaultJettyServlet, "/");
    }

    private void registerAnnotatedServlets(ServletContextHandler context, Collection<Class<?>> types) throws IllegalArgumentException, SecurityException {
        for (Class<?> type : types) {
            WebServlet servletAnnot = type.getAnnotation(WebServlet.class);
            if (servletAnnot != null) {
                String[] urlPatterns = null;
                if (servletAnnot.urlPatterns() != null && servletAnnot.urlPatterns().length > 0) {
                    urlPatterns = servletAnnot.urlPatterns();
                } else {
                    urlPatterns = servletAnnot.value();
                }
                if (urlPatterns != null && urlPatterns.length > 0) {
                    for (String pattern : urlPatterns) {
                        context.addServlet(type.getName(), pattern);
                    }
                }
            }
        }
    }

    private ErrorHandler createErrorHandler() {
        ErrorPageErrorHandler err = new ErrorPageErrorHandler();
        err.addErrorPage(404, "/WEB-INF/jsp/error_pages/missing.jsp");
        err.addErrorPage(401, "/WEB-INF/jsp/error_pages/unauthorized.jsp");
        err.addErrorPage(403, "/WEB-INF/jsp/error_pages/unauthorized.jsp");
        err.addErrorPage(RuntimeException.class, "/WEB-INF/jsp/error_pages/error.jsp");
        return err;
    }

}
