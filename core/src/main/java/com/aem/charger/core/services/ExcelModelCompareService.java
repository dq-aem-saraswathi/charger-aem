package com.aem.charger.core.services;

import org.apache.sling.api.resource.ResourceResolver;
import java.io.InputStream;

public interface ExcelModelCompareService {
    /**
     * Compares Excel columns with the Content Fragment Model fields.
     * @param resolver current resource resolver
     * @param excelInputStream Excel file input stream
     * @param modelPath path of the CF model (e.g. /conf/charger/settings/dam/cfm/models/myModel)
     * @return "Matched" if all columns match model fields, else "Not Matched"
     */
    String compareExcelWithModel(ResourceResolver resolver, InputStream excelInputStream, String modelPath);
}
