package com.aem.charger.core.services;

import java.util.Map;

public interface DynamicServiceGeneratorService {
    void generateService(String serviceName, String modelName, Map<String, String> fields, String outputDir);
}
