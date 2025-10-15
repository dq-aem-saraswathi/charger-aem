package com.aem.charger.core.services;

import org.apache.sling.api.resource.ResourceResolver;

import java.util.Map;

public interface ModelGeneratorService {
    void generateModel(String className, Map<String, String> fields, String outputDir);
}
