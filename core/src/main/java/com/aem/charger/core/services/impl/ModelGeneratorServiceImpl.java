package com.aem.charger.core.services.impl;

import com.aem.charger.core.services.ModelGeneratorService;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

@Component(service = ModelGeneratorService.class, immediate = true)
public class ModelGeneratorServiceImpl implements ModelGeneratorService {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public void generateModel(String className, Map<String, String> fields, String outputDir) {
        String packageName = "com.aem.charger.core.models";
        StringBuilder builder = new StringBuilder();

        // Package
        builder.append("package ").append(packageName).append(";\n\n");

        // Class declaration
        builder.append("public class ").append(className).append(" {\n\n");

        // Fields
        for (Map.Entry<String, String> entry : fields.entrySet()) {
            String fieldName = entry.getKey();
            String fieldType = mapFieldType(entry.getValue());
            builder.append("    private ").append(fieldType).append(" ").append(fieldName).append(";\n");
        }
        builder.append("\n");

        // Getters and setters
        for (Map.Entry<String, String> entry : fields.entrySet()) {
            String fieldName = entry.getKey();
            String fieldType = mapFieldType(entry.getValue());
            String capName = capitalize(fieldName);

            // Getter
            builder.append("    public ").append(fieldType)
                    .append(" get").append(capName).append("() {\n")
                    .append("        return ").append(fieldName).append(";\n")
                    .append("    }\n\n");

            // Setter
            builder.append("    public void set").append(capName)
                    .append("(").append(fieldType).append(" ").append(fieldName).append(") {\n")
                    .append("        this.").append(fieldName).append(" = ").append(fieldName).append(";\n")
                    .append("    }\n\n");
        }

        // Close class
        builder.append("}\n");

        // Write file
        try {
            File dir = new File(outputDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            File file = new File(dir, className + ".java");
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(builder.toString());
            }

            log.info("✅ POJO model class generated: {}", file.getAbsolutePath());
        } catch (IOException e) {
            log.error("Error generating model class", e);
        }
    }

    private String mapFieldType(String metaType) {
        if (metaType == null) return "String";
        switch (metaType.toLowerCase()) {
            case "number":
            case "double":
            case "long":
                return "Double";
            case "boolean":
                return "Boolean";
            case "text":
            case "string":
            default:
                return "String";
        }
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
