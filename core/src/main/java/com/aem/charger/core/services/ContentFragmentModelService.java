package com.aem.charger.core.services;

import org.apache.sling.api.resource.ResourceResolver;

import java.util.List;
import java.util.Map;

public interface ContentFragmentModelService {
    List<Map<String, String>> getAllModels(ResourceResolver resolver);
}
