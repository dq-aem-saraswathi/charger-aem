package com.aem.charger.core.servlets;

import com.aem.charger.core.services.ModelGeneratorService;
import com.aem.charger.core.services.ReadContentFragment;
import com.aem.charger.core.services.impl.ModelGeneratorServiceImpl;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.apache.sling.api.servlets.ServletResolverConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Map;


@Component(
        service = Servlet.class,
        property = {
                ServletResolverConstants.SLING_SERVLET_PATHS + "=/bin/readCF",
                ServletResolverConstants.SLING_SERVLET_METHODS + "=" + HttpConstants.METHOD_POST
        }
)
public class ReadCfModelServlet extends SlingAllMethodsServlet{
    @Reference
    private ReadContentFragment readCFService;
    @Reference
    private ModelGeneratorService modelGeneratorService;
    private static final Logger log = LoggerFactory.getLogger(ReadCfModelServlet.class);

@Override
    protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws ServletException, IOException {

        String modelPath = request.getParameter("modelPath");
        log.info("modelPath :{}",modelPath);
        if (modelPath == null || modelPath.isEmpty()) {

            response.setStatus(400);
            response.getWriter().write("Please provide 'modelPath' as query parameter");
            return;
        }
    ResourceResolver resourceResolver=request.getResourceResolver();
        // Call the service to read CF model
    Map<String,String> fields= readCFService.readCFModel(modelPath,resourceResolver);

    //create model
    String dir="/Users/koratlasaraswathi/Documents/dq-projects/aembuilder/generated-projects/charger/core/src/main/java/com/aem/charger/core/models";
    modelGeneratorService.generateModel("MyModel",fields,dir);

        response.setContentType("text/plain");
        response.getWriter().write("CF model read successfully. Check logs for details.");
    }
}
