package com.aem.charger.core.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.api.servlets.ServletResolverConstants;
import org.osgi.service.component.annotations.Component;
import com.google.gson.Gson;

/**
 * Returns child DAM folder/page paths for a PathBrowser-like use.
 */
@Component(service = { Servlet.class },
        property = {
                ServletResolverConstants.SLING_SERVLET_PATHS + "=/bin/getDamPaths",
                ServletResolverConstants.SLING_SERVLET_METHODS + "=GET"
        })
public class GetAssetsServlet extends SlingSafeMethodsServlet {

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");

        List<String> paths = new ArrayList<>();

        Resource damRoot = request.getResourceResolver().getResource("/content/dam");
        if (damRoot != null) {
            for (Resource child : damRoot.getChildren()) {
                paths.add(child.getPath());
            }
        }

        response.getWriter().write(new Gson().toJson(paths));
    }
}
