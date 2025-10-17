package com.aem.charger.core.services.impl;

import com.aem.charger.core.services.DynamicServiceGeneratorService;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

@Component(service = DynamicServiceGeneratorService.class, immediate = true)
public class DynamicServiceGeneratorServiceImpl implements DynamicServiceGeneratorService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public void generateService(String serviceName, String modelName, Map<String, String> fields, String outputDir) {
        String className = serviceName + "Impl";
        StringBuilder sb = new StringBuilder();

        // Package & imports
        sb.append("package com.aem.charger.core.services.impl;\n\n");
        sb.append("import com.adobe.cq.dam.cfm.ContentFragment;\n");
        sb.append("import com.adobe.cq.dam.cfm.FragmentTemplate;\n");
        sb.append("import com.aem.charger.core.models.").append(modelName).append(";\n");
        sb.append("import com.aem.charger.core.services.").append(serviceName).append(";\n");
        sb.append("import org.apache.sling.api.resource.*;\n");
        sb.append("import org.osgi.service.component.annotations.Component;\n");
        sb.append("import org.osgi.service.component.annotations.Reference;\n");
        sb.append("import javax.jcr.Node;\n");
        sb.append("import javax.jcr.Session;\n");
        sb.append("import java.util.*;\n");
        sb.append("import org.slf4j.Logger;\n");
        sb.append("import org.slf4j.LoggerFactory;\n\n");

        // Component annotation
        sb.append("@Component(service = ").append(serviceName).append(".class, immediate = true)\n");
        sb.append("public class ").append(className).append(" implements ").append(serviceName).append(" {\n\n");

        // Logger and references
        sb.append("    private final Logger log = LoggerFactory.getLogger(this.getClass());\n\n");
        sb.append("    @Reference\n");
        sb.append("    private ResourceResolverFactory resourceResolverFactory;\n\n");
        sb.append("    private ResourceResolver resourceResolver;\n");
        sb.append("    private Session session;\n\n");

        // init() method
        sb.append("    private void init() {\n");
        sb.append("        try {\n");
        sb.append("            Map<String, Object> serviceUserMap = new HashMap<>();\n");
        sb.append("            serviceUserMap.put(ResourceResolverFactory.SUBSERVICE, \"nirbhai\");\n");
        sb.append("            resourceResolver = resourceResolverFactory.getServiceResourceResolver(serviceUserMap);\n");
        sb.append("            session = resourceResolver.adaptTo(Session.class);\n");
        sb.append("        } catch (LoginException e) {\n");
        sb.append("            log.error(e.getMessage(), e);\n");
        sb.append("        }\n");
        sb.append("    }\n\n");

        // createCFS() method
        sb.append("    @Override\n");
        sb.append("    public void createCFS(List<").append(modelName).append("> users) {\n");
        sb.append("        init();\n");
        sb.append("        log.info(\"CFS creation starts !!\");\n\n");
        sb.append("        try {\n");
        sb.append("            Resource templateResource = resourceResolver.getResource(\"/conf/charger/settings/dam/cfm/models/").append(modelName.toLowerCase()).append("\");\n");
        sb.append("            FragmentTemplate fragmentTemplate = templateResource.adaptTo(FragmentTemplate.class);\n");
        sb.append("            Resource cfResource = resourceResolver.getResource(\"/content/dam/charger\");\n\n");
        sb.append("            int count = 1;\n");
        sb.append("            for (").append(modelName).append(" userObj : users) {\n");
        sb.append("                String cfName = \"cf\" + count;\n");
        sb.append("                ContentFragment cf = fragmentTemplate.createFragment(cfResource, cfName, cfName);\n");
        sb.append("                Node cfNode = resourceResolver.getResource(\"/content/dam/charger/\" + cf.getName() + \"/jcr:content/data/master\").adaptTo(Node.class);\n\n");

        // Loop through fields dynamically
        sb.append("                // Dynamically set properties\n");
        sb.append("                try {\n");
        sb.append("                    java.lang.reflect.Field[] classFields = userObj.getClass().getDeclaredFields();\n");
        sb.append("                    for(java.lang.reflect.Field field : classFields){\n");
        sb.append("                        field.setAccessible(true);\n");
        sb.append("                        Object value = field.get(userObj);\n");
        sb.append("                        if(value != null){\n");
        sb.append("                            cfNode.setProperty(field.getName(), value.toString());\n");
        sb.append("                        }\n");
        sb.append("                    }\n");
        sb.append("                    cfNode.getSession().save();\n");
        sb.append("                } catch(Exception e){\n");
        sb.append("                    log.error(\"Error setting CF properties\", e);\n");
        sb.append("                }\n\n");
        sb.append("                count++;\n");
        sb.append("            }\n");
        sb.append("        } catch(Exception e) {\n");
        sb.append("            log.error(\"Error creating CFs dynamically\", e);\n");
        sb.append("        }\n");
        sb.append("    }\n");

        sb.append("}\n");

        // Write to file
        try (FileWriter writer = new FileWriter(outputDir + "/" + className + ".java")) {
            writer.write(sb.toString());
            log.info("Dynamic service generated at: {}", outputDir + "/" + className + ".java");
        } catch (IOException e) {
            log.error("Error generating service", e);
        }
    }
}
