package com.aem.charger.core.servlets;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.service.component.annotations.Component;
import javax.servlet.Servlet;
import java.io.IOException;

@Component(service = { Servlet.class }, property = {
        "sling.servlet.methods=GET",
        "sling.servlet.paths=/bin/simpleServletcharger"
})
public class ServletCheck extends SlingAllMethodsServlet {

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws IOException {
        response.setContentType("text/plain");
        response.getWriter().write("Servlet working under /bin/simpleServletcheck");
    }
}
