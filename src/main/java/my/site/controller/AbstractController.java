package my.site.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Common superclass of application controllers.
 * @author Radek Beran
 */
public abstract class AbstractController extends HttpServlet {

    protected static final String DEFAULT_PAGE = "index.jsp";
    protected static final String DEFAULT_CONTENT_TYPE = "text/html; charset=UTF-8";
    protected final Logger log;

    private static final long serialVersionUID = 1L;

    public AbstractController() {
        this.log = LoggerFactory.getLogger(getClass());
    }

    /**
     * @see HttpServlet#service(HttpServletRequest request, HttpServletResponse response)
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType(DEFAULT_CONTENT_TYPE);
        String page = resolveJspPage(request);
        request.getRequestDispatcher("/WEB-INF/jsp/" + page).forward(request, response);
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    protected String resolveJspPage(HttpServletRequest request) {
        String page = DEFAULT_PAGE;
        String requestUri = request.getRequestURI();
        if (requestUri != null && !requestUri.isEmpty()) {
            // There can be query parameters or ;jsessionid after the last slash, let's remove it
            int lastSlashIndex = requestUri.lastIndexOf("/");
            if (lastSlashIndex > 0) {
                requestUri = requestUri.substring(0, lastSlashIndex);
            }

            // Remove possible context path from the beginning
            if (request.getContextPath() != null && requestUri.startsWith(request.getContextPath())) {
                requestUri = requestUri.substring(request.getContextPath().length());
            }
            // Remove possible leading slash
            if (requestUri.startsWith("/")) {
                requestUri = requestUri.substring(1);
            }

            if (requestUri == null || requestUri.isEmpty()) {
                page = DEFAULT_PAGE;
            } else {
                page = requestUri + ".jsp";
            }
        }
        return page;
    }
}
