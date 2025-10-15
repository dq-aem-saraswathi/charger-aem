package com.aem.charger.core.services;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

@Component(service = ReadContentFragment.class, immediate = true)
public class ReadContentFragment {
    Map<String, String> fieldsformodel = new LinkedHashMap<>();

private final Logger log = LoggerFactory.getLogger(this.getClass());
    public  Map<String, String> readCFModel(String modelPath,ResourceResolver resourceResolver) {
        // modelPath example: /conf/charger/settings/dam/cfm/models/mymodel/jcr:content/model/elements
        Resource modelResource = resourceResolver.getResource(modelPath);

        if (modelResource == null) {
            log.error("CF model not found at path: {}", modelPath);

        }


        // Iterate over all fields
        Iterator<Resource> fields = modelResource.listChildren();
        while (fields.hasNext()) {
            Resource field = fields.next();
            ValueMap props = field.getValueMap();

            String fieldName = field.getName();
            log.info("fields : {}",fieldName);
           String path= field.getPath();
            String fieldType = props.get("metaType","");
            String name=props.get("name","");
            boolean required = props.get("required", false);
            fieldsformodel.put(name, fieldType);
            log.info("name of the field : {}",name);
            log.info("Field: {}, Type: {}, Required: {}", name, fieldType, required);
        }

          return fieldsformodel;
    }
}
