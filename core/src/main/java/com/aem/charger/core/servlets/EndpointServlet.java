package com.aem.charger.core.servlets;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.service.component.annotations.Component;
import javax.servlet.Servlet;
import java.io.IOException;

@Component(service = { Servlet.class }, property = {
        "sling.servlet.resourceTypes=com.aem.charger.core.endpoint",
        "sling.servlet.methods=GET",
        "sling.servlet.extensions=txt"
})
public class EndpointServlet extends SlingAllMethodsServlet {

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws IOException {
        response.setContentType("text/plain");
        response.getWriter().write("✅ Servlet executed successfully!");
    }
}
