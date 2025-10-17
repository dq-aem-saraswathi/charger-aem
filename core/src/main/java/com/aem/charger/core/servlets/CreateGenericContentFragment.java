package com.aem.charger.core.servlets;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import javax.jcr.Node;
import javax.servlet.Servlet;
import javax.servlet.http.Part;

import com.adobe.cq.dam.cfm.FragmentTemplate;
import com.aem.charger.core.services.ContentFragmentModelService;
import com.aem.charger.core.services.ExcelModelCompareService;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.ServletResolverConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.cq.dam.cfm.ContentElement;
import com.adobe.cq.dam.cfm.ContentFragment;
import com.adobe.cq.dam.cfm.ContentFragmentException;
import com.google.gson.stream.JsonWriter;
import org.osgi.service.component.annotations.Reference;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

@Component(
        service = Servlet.class,
        property = {
                Constants.SERVICE_DESCRIPTION + "=" + CreateGenericContentFragment.SERVICE_DESCRIPTION,
                ServletResolverConstants.SLING_SERVLET_PATHS + "=" + "/bin/content/createContentFragment",
                ServletResolverConstants.SLING_SERVLET_METHODS + "=" + HttpConstants.METHOD_POST,
                ServletResolverConstants.SLING_SERVLET_METHODS + "=" + HttpConstants.METHOD_GET
        }
)
public class CreateGenericContentFragment extends SlingAllMethodsServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateGenericContentFragment.class);
    private static final long serialVersionUID = 1L;
    protected static final String SERVICE_DESCRIPTION = "Create Generic Content Fragment from Excel";
    protected static final String CF_TEMPLATE = "/conf/charger/settings/dam/cfm/models/";
    private static final String PARENT_PATH = "parentPath";
    private static final String MODEL_TYPE = "modelType";
    private static final String CSV = "excel";
    @Reference
  private  ContentFragmentModelService contentFragmentModelService;
    @Reference
    private ExcelModelCompareService excelModelCompareService;

    @Override
    protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        LOGGER.debug("Received POST request to create content fragment.");
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.setHeader("Cache-Control", "nocache");
        response.setCharacterEncoding("utf-8");

        JsonWriter jsout = new JsonWriter(response.getWriter());
        jsout.beginObject();

        String parentPath = request.getParameter(PARENT_PATH);
        String modelType = request.getParameter(MODEL_TYPE);

        LOGGER.info("Parent Path: {}", parentPath);
        LOGGER.info("Model Type: {}", modelType);

        ResourceResolver resolver = request.getResourceResolver();

        try {
            if (StringUtils.isNotEmpty(parentPath) && StringUtils.isNotEmpty(modelType)) {

                Part filePart = request.getPart(CSV);
                InputStream fileContent = filePart.getInputStream();

                // ✅ Compare Excel columns with the model fields first
                String modelPath = "/conf/charger/settings/dam/cfm/models/" + modelType;
                String compareResult = excelModelCompareService.compareExcelWithModel(resolver, fileContent, modelPath);

                if ("Matched".equals(compareResult)) {
                    // Reset InputStream because it has been read in compareExcelWithModel
                    fileContent = request.getPart(CSV).getInputStream();

                    Workbook workbook = new XSSFWorkbook(fileContent);
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

                    int createdCount = 0;
                    for (List<String> dataRow : rowsData) {
                        if (dataRow.size() == 0 || StringUtils.isEmpty(dataRow.get(0))) continue;

                        String cfName = sanitizeForJcr(dataRow.get(0));
                        createContentFragment(resolver, parentPath, headerList,
                                dataRow.toArray(new String[0]), modelType, cfName);
                        createdCount++;
                    }

                    LOGGER.info("Total content fragments created: {}", createdCount);
                    jsout.name("status").value("Success: Created total of " + createdCount + " Content Fragments");

                } else {
                    LOGGER.warn("Excel columns do not match the selected model: {}", modelType);
                    jsout.name("status").value("Model mismatched for uploaded file");
                }
            }
        } catch (Exception e) {
            LOGGER.error("Exception occurred while processing POST request.", e);
            jsout.name("status").value("Failed to create Content Fragments: " + e.getMessage());
        } finally {
            if (resolver != null && resolver.hasChanges()) {
                resolver.commit();
            }
            jsout.endObject();
        }
    }

//    protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
//        LOGGER.debug("Received POST request to create content fragment.");
//        request.setCharacterEncoding("UTF-8");
//        response.setContentType("application/json");
//        response.setHeader("Cache-Control", "nocache");
//        response.setCharacterEncoding("utf-8");
//
//        JsonWriter jsout = new JsonWriter(response.getWriter());
//        jsout.beginObject();
//
//        String parentPath = request.getParameter(PARENT_PATH);
//        String modelType = request.getParameter(MODEL_TYPE);
//
//        LOGGER.info("Parent Path: {}", parentPath);
//        LOGGER.info("Model Type: {}", modelType);
//
//        ResourceResolver resolver = request.getResourceResolver();
//        List<Map<String, String>> models= contentFragmentModelService.getAllModels(resolver);
//        LOGGER.info("models : {}",models);
//        try {
//            if (StringUtils.isNotEmpty(parentPath) && StringUtils.isNotEmpty(modelType)) {
//
//                Part filePart = request.getPart(CSV);
//                InputStream fileContent = filePart.getInputStream();
//                // --- Apache POI Excel parsing ---
//                Workbook workbook = new XSSFWorkbook(fileContent);
//                Sheet sheet = workbook.getSheetAt(0);
//
//                List<String> headerList = new ArrayList<>();
//                List<List<String>> rowsData = new ArrayList<>();
//
//                for (Row row : sheet) {
//                    List<String> rowData = new ArrayList<>();
//                    for (Cell cell : row) {
//                        cell.setCellType(CellType.STRING);
//                        rowData.add(cell.getStringCellValue());
//                    }
//                    if (row.getRowNum() == 0) {
//                        headerList = rowData; // first row is header
//                    } else {
//                        rowsData.add(rowData);
//                    }
//                }
//
//                int createdCount = 0;
//                for (List<String> dataRow : rowsData) {
//                    if (dataRow.size() == 0 || StringUtils.isEmpty(dataRow.get(0))) continue;
//
//                    String cfName = sanitizeForJcr(dataRow.get(0)); // sanitize for JCR node name
//                    createContentFragment(resolver, parentPath, headerList,
//                            dataRow.toArray(new String[0]), modelType, cfName);
//                    createdCount++;
//                }
//
//                LOGGER.info("Total content fragments created: {}", createdCount);
//                jsout.name("status").value("Success Created total of " + createdCount + " Content Fragments");
//            }
//        } catch (Exception e) {
//            LOGGER.error("Exception occurred while processing POST request.", e);
//            jsout.name("status").value("Failed to create Content Fragments");
//        } finally {
//            if (resolver != null && resolver.hasChanges()) {
//                resolver.commit();
//            }
//        }
//
//        jsout.endObject();
//    }

//    // --- Updated createContentFragment ---
//    public void createContentFragment(ResourceResolver resourceResolver, String parentPath,
//                                      List<String> headerList, String[] data, String modelType, String cfName) {
//        LOGGER.info("Creating content fragment for data: {}", Arrays.toString(data));
//
//        try {
//            // 1. Get template
//            Resource templateResource = resourceResolver.getResource("/conf/charger/settings/dam/cfm/models/" + modelType);
//            if (templateResource == null) {
//                LOGGER.error("Template not found: {}", modelType);
//                return;
//            }
//
//            FragmentTemplate fragmentTemplate = templateResource.adaptTo(FragmentTemplate.class);
//            if (fragmentTemplate == null) {
//                LOGGER.error("Failed to adapt template to FragmentTemplate: {}", modelType);
//                return;
//            }
//
//            // 2. Check if CF exists
//            Resource modelResource = resourceResolver.getResource(parentPath + "/" + cfName);
//            ContentFragment newFragment;
//
//            if (modelResource == null) {
//                // Create new CF
//                Resource parentResource = resourceResolver.getResource(parentPath);
//                if (parentResource == null) {
//                    LOGGER.error("Parent path does not exist: {}", parentPath);
//                    return;
//                }
//                newFragment = fragmentTemplate.createFragment(parentResource, cfName, cfName);
//                LOGGER.info("Created new content fragment: {}", cfName);
//            } else {
//                newFragment = modelResource.adaptTo(ContentFragment.class);
//                LOGGER.info("Using existing content fragment: {}", cfName);
//            }
//
//            // 3. Update CF elements
//            if (newFragment != null) {
//                Iterator<ContentElement> elements = newFragment.getElements();
//                while (elements.hasNext()) {
//                    ContentElement element = elements.next();
//                    for (int i = 0; i < headerList.size(); i++) {
//                        if (i < data.length && (StringUtils.equalsIgnoreCase(element.getName(), headerList.get(i))
//                                || StringUtils.containsAny(element.getName(), '[', ']'))) {
//                            element.setContent(data[i], element.getContentType());
//                            break;
//                        }
//                    }
//                }
//
//                // 4. Set custom JCR property (example: operatingMode)
//                String masterNodePath = parentPath + "/" + cfName + "/jcr:content/data/master";
//                Resource masterResource = resourceResolver.getResource(masterNodePath);
//                if (masterResource != null) {
//                    Node cfNode = masterResource.adaptTo(Node.class);
//                    if (cfNode != null) {
//                        cfNode.setProperty("operatingMode", "defaultMode");
//                    }
//                }
//            }
//
//        } catch (ContentFragmentException e) {
//            LOGGER.error("Exception occurred while creating/updating content fragment.", e);
//        } catch (Exception e) {
//            LOGGER.error("Unexpected exception in createContentFragment", e);
//        }
//    }
//
    // --- Sanitize CF name for JCR ---
    private String sanitizeForJcr(String name) {
        return name.replaceAll("[^a-zA-Z0-9_-]", "_");
    }

    public void createContentFragment(ResourceResolver resourceResolver, String parentPath,
                                      List<String> headerList, String[] data, String modelType, String cfName) {
        LOGGER.info("Creating content fragment for data: {}", Arrays.toString(data));

        try {
            // 1. Get template
            Resource templateResource = resourceResolver.getResource("/conf/charger/settings/dam/cfm/models/" + modelType);
            if (templateResource == null) {
                LOGGER.error("Template not found: {}", modelType);
                return;
            }

            FragmentTemplate fragmentTemplate = templateResource.adaptTo(FragmentTemplate.class);
            if (fragmentTemplate == null) {
                LOGGER.error("Failed to adapt template to FragmentTemplate: {}", modelType);
                return;
            }

            // 2. Check if CF exists
            Resource modelResource = resourceResolver.getResource(parentPath + "/" + cfName);
            ContentFragment newFragment;

            if (modelResource == null) {
                // Create new CF
                Resource parentResource = resourceResolver.getResource(parentPath);
                if (parentResource == null) {
                    LOGGER.error("Parent path does not exist: {}", parentPath);
                    return;
                }
                newFragment = fragmentTemplate.createFragment(parentResource, cfName, cfName);
                LOGGER.info("Created new content fragment: {}", cfName);
            } else {
                newFragment = modelResource.adaptTo(ContentFragment.class);
                LOGGER.info("Using existing content fragment: {}", cfName);
            }

            // 3. Update CF elements
            if (newFragment != null) {
                Iterator<ContentElement> elements = newFragment.getElements();
                while (elements.hasNext()) {
                    ContentElement element = elements.next();

                    String elementNameNormalized = element.getName()
                            .replaceAll("[ _]", "")
                            .toLowerCase();

                    for (int i = 0; i < headerList.size(); i++) {
                        String headerNameNormalized = headerList.get(i)
                                .replaceAll("[ _]", "")
                                .toLowerCase();

                        LOGGER.debug("Comparing normalized element '{}' with header '{}'",
                                elementNameNormalized, headerNameNormalized);

                        if (i < data.length && elementNameNormalized.equals(headerNameNormalized)) {
                            element.setContent(data[i], element.getContentType());
                            LOGGER.info("Set content for element '{}' = '{}'", element.getName(), data[i]);
                            break;
                        }
                    }
                }

                // 4. Optional: Set custom property
                String masterNodePath = parentPath + "/" + cfName + "/jcr:content/data/master";
                Resource masterResource = resourceResolver.getResource(masterNodePath);
                if (masterResource != null) {
                    Node cfNode = masterResource.adaptTo(Node.class);
                    if (cfNode != null) {
                        cfNode.setProperty("operatingMode", "defaultMode");
                    }
                }
            }

        } catch (ContentFragmentException e) {
            LOGGER.error("Exception occurred while creating/updating content fragment.", e);
        } catch (Exception e) {
            LOGGER.error("Unexpected exception in createContentFragment", e);
        }
    }

}



//    public void createContentFragment(ResourceResolver resourceResolver, String parentPath, List<String> headerList,
//                                      String[] data, String modelType) {
//        LOGGER.info("Creating content fragment for data: {}", Arrays.toString(data));
//
//        try {
//            Resource templateResource = resourceResolver
//                    .getResource("/conf/charger/settings/dam/cfm/models/mymodel");
//            Resource modelResource = resourceResolver.getResource(parentPath + "/" + data[0]);
//            ContentFragment newFragment = modelResource.adaptTo(ContentFragment.class);
//            FragmentTemplate fragmentTemplate = templateResource.adaptTo(FragmentTemplate.class);
//            Resource cfResource = resourceResolver.getResource("/content/dam/charger");
//            LOGGER.info("cfResource : {}",cfResource);
//
//            if (newFragment != null) {
//                Iterator<ContentElement> elements = newFragment.getElements();
//                while (elements.hasNext()) {
//                    ContentElement element = elements.next();
//                    for (int i = 0; i < headerList.size(); i++) {
//                        if (StringUtils.equalsIgnoreCase(element.getName(), headerList.get(i))
//                                || StringUtils.containsAny(element.getName(), '[', ']')) {
//                            element.setContent(data[i], element.getContentType());
//                            break;
//                        }
//                    }
//                }
//            }
//        } catch (ContentFragmentException e) {
//            LOGGER.error("Exception occurred while creating content fragment.", e);
//        }
//    }
