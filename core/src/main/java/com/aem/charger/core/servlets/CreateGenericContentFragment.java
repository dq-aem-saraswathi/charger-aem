package com.aem.charger.core.servlets;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import javax.jcr.Node;
import javax.servlet.Servlet;
import javax.servlet.http.Part;

import com.adobe.cq.dam.cfm.FragmentTemplate;
import com.adobe.cq.dam.cfm.ContentElement;
import com.adobe.cq.dam.cfm.ContentFragment;
import com.adobe.cq.dam.cfm.ContentFragmentException;
import com.aem.charger.core.services.ExcelModelCompareService;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.ServletResolverConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.stream.JsonWriter;

@Component(
        service = Servlet.class,
        property = {
                Constants.SERVICE_DESCRIPTION + "=" + CreateGenericContentFragment.SERVICE_DESCRIPTION,
                ServletResolverConstants.SLING_SERVLET_PATHS + "=" + "/bin/content/createContentFragment",
                ServletResolverConstants.SLING_SERVLET_METHODS + "=" + HttpConstants.METHOD_POST
        }
)
public class CreateGenericContentFragment extends SlingAllMethodsServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateGenericContentFragment.class);
    private static final long serialVersionUID = 1L;
    protected static final String SERVICE_DESCRIPTION = "Create Generic Content Fragment from Excel";
    private static final String PARENT_PATH = "parentPath";
    private static final String MODEL_TYPE = "modelType";
    private static final String CSV = "excel";

    @Reference
    private ExcelModelCompareService excelModelCompareService;

    @Override
    protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        LOGGER.debug("Received POST request to create content fragment.");
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.setHeader("Cache-Control", "nocache");
        response.setCharacterEncoding("utf-8");

        JsonWriter jsout = null;
        ResourceResolver resolver = request.getResourceResolver();

        try {
            jsout = new JsonWriter(response.getWriter());
            jsout.beginObject();

            String parentPath = request.getParameter(PARENT_PATH);
            String modelType = request.getParameter(MODEL_TYPE);
            String titleColumn = request.getParameter("selectfield"); // user-selected column for CF title

            LOGGER.info("Parent Path: {}", parentPath);
            LOGGER.info("Model Type: {}", modelType);
            LOGGER.info("Selected CF title column: {}", titleColumn);

            if (StringUtils.isEmpty(parentPath) || StringUtils.isEmpty(modelType) || StringUtils.isEmpty(titleColumn)) {
                jsout.name("status").value("Parent path, model type, or title column missing");
                return; // finally will close the JSON object exactly once
            }

            Part filePart = request.getPart(CSV);
            if (filePart == null) {
                jsout.name("status").value("Excel file not provided");
                return;
            }

            // Compare Excel with CF model
            InputStream compareStream = filePart.getInputStream();
            String modelPath = "/conf/charger/settings/dam/cfm/models/" + modelType;
            String compareResult = excelModelCompareService.compareExcelWithModel(resolver, compareStream, modelPath);

            if (!"Matched".equals(compareResult)) {
                LOGGER.warn("Excel columns do not match the selected model: {}", modelType);
                jsout.name("status").value("Model mismatched for uploaded file");
                return;
            }

            // Reset input stream and read workbook
            InputStream fileContent = request.getPart(CSV).getInputStream();
            try (Workbook workbook = new XSSFWorkbook(fileContent)) {
                Sheet sheet = workbook.getSheetAt(0);

                List<String> headerList = new ArrayList<>();
                List<List<String>> rowsData = new ArrayList<>();

                for (Row row : sheet) {
                    List<String> rowData = new ArrayList<>();
                    for (Cell cell : row) {
                        cell.setCellType(CellType.STRING);
                        rowData.add(cell.getStringCellValue());
                    }
                    if (row.getRowNum() == 0) {
                        headerList = rowData;
                    } else {
                        rowsData.add(rowData);
                    }
                }

                int titleIndex = headerList.indexOf(titleColumn);
                if (titleIndex == -1) {
                    LOGGER.warn("Selected title column not found in Excel headers. Using first column as fallback.");
                    titleIndex = 0;
                }

                int createdCount = 0;
                List<String> existingCFs = new ArrayList<>();
                List<String> newCFs = new ArrayList<>();

                // Step 1: Identify existing CFs (based on sanitized name)
                for (List<String> dataRow : rowsData) {
                    if (dataRow.isEmpty()) continue;

                    String cfTitleValue = dataRow.get(titleIndex);
                    if (StringUtils.isEmpty(cfTitleValue)) continue;

                    // clean numeric formatting like "326.0" -> "326"
                    cfTitleValue = cfTitleValue.replaceAll("\\.0$", "");
                    String cfName = sanitizeForJcr(cfTitleValue);

                    String cfPath = parentPath + "/" + cfName;
                    Resource existingResource = resolver.getResource(cfPath);

                    if (existingResource != null) {
                        existingCFs.add(cfName);
                    } else {
                        newCFs.add(cfName);
                    }
                }

                // Step 2: If all CFs already exist, report and stop
                if (newCFs.isEmpty()) {
                    String msg = "⚠️ All content fragments already exist for this Excel file.";
                    LOGGER.info(msg);
                    jsout.name("status").value(msg);
                    return;
                }

                // Step 3: Create only new CFs
                for (List<String> dataRow : rowsData) {
                    if (dataRow.isEmpty()) continue;

                    String cfTitleValue = dataRow.get(titleIndex);
                    if (StringUtils.isEmpty(cfTitleValue)) continue;

                    cfTitleValue = cfTitleValue.replaceAll("\\.0$", "");
                    String cfName = sanitizeForJcr(cfTitleValue);

                    if (existingCFs.contains(cfName)) {
                        LOGGER.info("Skipping existing CF: {}", cfName);
                        continue;
                    }

                    createContentFragment(resolver, parentPath, headerList, dataRow.toArray(new String[0]), modelType, cfName);
                    createdCount++;
                }

                // Step 4: Return status
                if (!existingCFs.isEmpty()) {
                    String msg = String.format(
                            "✅ Created %d new CF(s). ⚠️ Skipped %d existing CF(s).",
                            createdCount, existingCFs.size()
                    );
                    LOGGER.info(msg);
                    jsout.name("status").value(msg);
                } else {
                    jsout.name("status").value("✅ Created total of " + createdCount + " Content Fragments");
                }
            } catch (Exception e) {
                LOGGER.error("Error reading Excel workbook", e);
                jsout.name("status").value("Failed to read Excel file: " + e.getMessage());
                return;
            }

        } catch (Exception e) {
            LOGGER.error("Exception occurred while creating content fragments", e);
            // Try to write an error status if possible
            try {
                if (jsout != null) {
                    jsout.name("status").value("Failed to create Content Fragments: " + e.getMessage());
                } else {
                    response.getWriter().write("{\"status\":\"Failed to create Content Fragments: " + e.getMessage() + "\"}");
                }
            } catch (IOException ioEx) {
                LOGGER.error("Failed to send error response", ioEx);
            }
        } finally {
            // End JSON object exactly once and close writer
            try {
                if (jsout != null) {
                    jsout.endObject();
                    jsout.close();
                }
            } catch (Exception e) {
                LOGGER.error("Failed to properly close JsonWriter", e);
            }

            // Commit changes if any
            try {
                if (resolver != null && resolver.hasChanges()) {
                    resolver.commit();
                }
            } catch (Exception e) {
                LOGGER.error("Failed to commit changes to repository", e);
            }
        }
    }

    private String sanitizeForJcr(String name) {
        return name.replaceAll("[^a-zA-Z0-9_-]", "_");
    }

    public void createContentFragment(ResourceResolver resolver, String parentPath,
                                      List<String> headerList, String[] data, String modelType, String cfName) {

        LOGGER.info("Creating content fragment: {}", cfName);

        try {
            Resource templateResource = resolver.getResource("/conf/charger/settings/dam/cfm/models/" + modelType);
            if (templateResource == null) {
                LOGGER.error("Template not found: {}", modelType);
                return;
            }

            FragmentTemplate fragmentTemplate = templateResource.adaptTo(FragmentTemplate.class);
            if (fragmentTemplate == null) {
                LOGGER.error("Failed to adapt template to FragmentTemplate: {}", modelType);
                return;
            }

            Resource existingCF = resolver.getResource(parentPath + "/" + cfName);
            ContentFragment cf;

            if (existingCF == null) {
                Resource parentResource = resolver.getResource(parentPath);
                if (parentResource == null) {
                    LOGGER.error("Parent path does not exist: {}", parentPath);
                    return;
                }
                cf = fragmentTemplate.createFragment(parentResource, cfName, cfName);
                LOGGER.info("Created new content fragment: {}", cfName);
            } else {
                cf = existingCF.adaptTo(ContentFragment.class);
                LOGGER.info("Using existing content fragment: {}", cfName);
            }

            if (cf != null) {
                Iterator<ContentElement> elements = cf.getElements();
                while (elements.hasNext()) {
                    ContentElement element = elements.next();
                    String elementNormalized = element.getName().replaceAll("[ _]", "").toLowerCase();

                    for (int i = 0; i < headerList.size(); i++) {
                        String headerNormalized = headerList.get(i).replaceAll("[ _]", "").toLowerCase();
                        if (i < data.length && elementNormalized.equals(headerNormalized)) {
                            element.setContent(data[i], element.getContentType());
                            LOGGER.debug("Set content for element {} = {}", element.getName(), data[i]);
                            break;
                        }
                    }
                }

            }

        } catch (ContentFragmentException e) {
            LOGGER.error("Error creating/updating content fragment", e);
        } catch (Exception e) {
            LOGGER.error("Unexpected error in createContentFragment", e);
        }
    }
}
