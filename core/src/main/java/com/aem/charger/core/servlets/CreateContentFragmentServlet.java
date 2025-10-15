package com.aem.charger.core.servlets;

import com.adobe.cq.dam.cfm.ContentElement;
import com.adobe.cq.dam.cfm.ContentFragment;
import com.adobe.cq.dam.cfm.ContentFragmentException;
import com.adobe.cq.dam.cfm.ContentFragmentManager;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.api.servlets.ServletResolverConstants;
import org.json.JSONObject;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import java.io.IOException;
import java.util.Iterator;

@Component(
        service = Servlet.class,
        property = {
                ServletResolverConstants.SLING_SERVLET_PATHS + "=/bin/createCF",
                ServletResolverConstants.SLING_SERVLET_METHODS + "=POST"
        }
)
public class CreateContentFragmentServlet extends SlingAllMethodsServlet {

    private static final Logger log = LoggerFactory.getLogger(CreateContentFragmentServlet.class);

    @Reference
    private ContentFragmentManager cfManager;

    @Override
    protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {

        response.setContentType("application/json");
        ResourceResolver   resourceResolver=request.getResourceResolver();
        try {
            // 1️⃣ Parameters
            String modelPath = request.getParameter("modelPath"); // e.g., /content/dam/models/myModel
            String folderPath = request.getParameter("folderPath"); // e.g., /content/dam/test
            String cfName = request.getParameter("cfName"); // e.g., myCF1
log.error("modelPath :{}",modelPath);
log.error("folderPath : {}",folderPath);
log.error("cfName : {}",cfName);
            if (modelPath == null || folderPath == null || cfName == null) {
                response.setStatus(400);
                response.getWriter().write("{\"error\":\"Missing parameters: modelPath, folderPath, cfName\"}");
                return;
            }

            // 2️⃣ Read JSON body
            String jsonData = request.getReader().lines().reduce("", (acc, line) -> acc + line);
            JSONObject json = new JSONObject(jsonData);
             log.error("json : {}",json);

            // 3️⃣ Resolve paths
            Resource parent = request.getResourceResolver().getResource(folderPath);
            Resource templateNode = request.getResourceResolver().getResource(modelPath);
            if (templateNode == null) {
                // handle error
            }
            Resource model = templateNode.getChild("jcr:content"); // This is the actual CFM model
            if (model == null) {
                log.error("CF model jcr:content node not found at {}", modelPath);
                response.setStatus(400);
                response.getWriter().write("{\"error\":\"CF model jcr:content not found at: " + modelPath + "\"}");
                return;
            }
log.error("parent : {}",parent);
log.error("model : {}",model);
            Resource modelResource = resourceResolver.getResource(modelPath);
            if (modelResource != null && "cq:Template".equals(modelResource.getResourceType())) {
                // If user passed the template node, fix it
                modelResource = resourceResolver.getResource(modelPath + "/jcr:content/model");
            }

            if (modelResource == null) {
                throw new IllegalArgumentException("Invalid model path: " + modelPath);
            }

            Resource folderResource = resourceResolver.getResource(folderPath);
            ContentFragmentManager cfManager = resourceResolver.adaptTo(ContentFragmentManager.class);

            ContentFragment cf = cfManager.create(folderResource, modelResource,cfName,null);

            if (model == null) {
                response.setStatus(400);
                response.getWriter().write("{\"error\":\"CF model jcr:content node not found under: " + modelPath + "\"}");
                return;
            }

            log.error("parent : {}",parent);
            log.error("model :{}",model);
            if (parent == null) {
                response.setStatus(400);
                response.getWriter().write("{\"error\":\"Invalid folderPath: " + folderPath + "\"}");
                return;
            }
            if (model == null) {
                response.setStatus(400);
                response.getWriter().write("{\"error\":\"Invalid modelPath: " + modelPath + "\"}");
                return;
            }

            // 4️⃣ Create Content Fragment
           log.error("cf :{}",cf);
            // 5️⃣ Populate fields from JSON
            Iterator<String> keys = json.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                String value = json.get(key).toString();
                 log.error("value : {}",value);
              log.error("key : {}",key);
                ContentElement element = cf.getElement(key);
                if (element != null) {
                    element.setContent(value, "text/plain");
                }
            }

            // 6️⃣ Commit changes
            request.getResourceResolver().commit();

            // 7️⃣ Respond success
            String cfPath = parent.getPath() + "/" + cfName;
            log.error("cfPath : {}",cfPath);
            response.getWriter().write("{\"success\":true,\"path\":\"" + cfPath + "\"}");

        } catch (ContentFragmentException e) {
            log.error("CF creation failed", e);
            response.setStatus(500);
            response.getWriter().write("{\"error\":\"CF creation failed: " + e.getMessage() + "\"}");
        } catch (Exception e) {
            log.error("Unexpected error", e);
            response.setStatus(500);
            response.getWriter().write("{\"error\":\"Unexpected error: " + e.getMessage() + "\"}");
        }
    }
}
