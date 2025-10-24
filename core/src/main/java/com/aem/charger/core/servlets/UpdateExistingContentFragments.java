package com.aem.charger.core.servlets;

import com.adobe.cq.dam.cfm.ContentElement;
import com.adobe.cq.dam.cfm.ContentFragment;
import com.adobe.cq.dam.cfm.ContentFragmentException;
import com.adobe.cq.dam.cfm.FragmentTemplate;
import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.api.servlets.ServletResolverConstants;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Component(
        service = Servlet.class,
        property = {
                Constants.SERVICE_DESCRIPTION + "=Update Existing Content Fragments from Excel",
                ServletResolverConstants.SLING_SERVLET_PATHS + "=/bin/updateCFs",
                ServletResolverConstants.SLING_SERVLET_METHODS + "=" + HttpConstants.METHOD_POST
        }
)
public class UpdateExistingContentFragments extends SlingAllMethodsServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateExistingContentFragments.class);
    @Override
    protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");

        LOGGER.info("====== 🧾 Starting Content Fragment Update Process ======");

        String parentPath = request.getParameter("parentPath");
        String titleColumn = request.getParameter("selectfield"); // CF title column from user
        String modelType = request.getParameter("modelType");

        if (StringUtils.isBlank(parentPath) || StringUtils.isBlank(titleColumn)) {
            LOGGER.error("❌ Missing parentPath or titleColumn parameter");
            response.getWriter().write("{\"error\":\"Missing parentPath or titleColumn parameter\"}");
            return;
        }

        ResourceResolver resolver = request.getResourceResolver();

        String mode = request.getParameter("mode");
        if ("update".equalsIgnoreCase(mode)) {
            List<Map<String, Object>> fragments = getExistingContentFragments(resolver, parentPath);
            //  response.getWriter().write(new Gson().toJson(fragments));
            LOGGER.info("existing content fragments : {}", fragments);
        }


        try {
            Part filePart = request.getPart("excel");
            if (filePart == null) {
                LOGGER.error("❌ Excel file not provided");
                response.getWriter().write("{\"error\":\"Excel file not provided\"}");
                return;
            }

        try (InputStream inputStream = filePart.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            LOGGER.info("📑 Sheet loaded: {} | Rows: {}", sheet.getSheetName(), sheet.getPhysicalNumberOfRows());

            List<String> headers = new ArrayList<>();
            List<List<String>> rowsData = new ArrayList<>();

            for (Row row : sheet) {
                List<String> rowData = new ArrayList<>();
                for (Cell cell : row) {
                    cell.setCellType(CellType.STRING);
                    rowData.add(cell.getStringCellValue().trim());
                }
                if (row.getRowNum() == 0) {
                    headers = rowData;
                    LOGGER.info("🧠 Header Row: {}", headers);
                } else {
                    rowsData.add(rowData);
                }
            }

            int titleIndex = headers.indexOf(titleColumn);
            if (titleIndex == -1) {
                LOGGER.warn("Selected title column '{}' not found. Using first column as fallback.", titleColumn);
                titleIndex = 0;
            }

            int updatedCount = 0;
            int skippedCount = 0;
            List<String> updatedFragments = new ArrayList<>();
            List<String> skippedFragments = new ArrayList<>();

            LOGGER.info("📊 Starting update for {} content fragments", rowsData.size());

            for (List<String> dataRow : rowsData) {
                if (dataRow.isEmpty()) continue;

                String cfTitleValue = dataRow.get(titleIndex);
                if (StringUtils.isEmpty(cfTitleValue)) {
                    cfTitleValue = "row_" + dataRow.hashCode();
                }

                String cfName = sanitizeForJcr(cfTitleValue);
                String cfPath = parentPath + "/" + cfName;

                LOGGER.info("------------------------------------------------------");
                LOGGER.info("🔍 Checking CF: {} (Excel row {})", cfPath, dataRow);

                Resource cfResource = resolver.getResource(cfPath);

                if (cfResource == null) {
                  //  LOGGER.warn("⚠️ CF does not exist at path: {}", cfPath);

                    skippedFragments.add(cfName);

                    createContentFragment(resolver, parentPath, headers, dataRow.toArray(new String[0]), modelType, cfName);
                    resolver.commit();
                    skippedCount++;
                    continue;
                }

                ContentFragment cf = cfResource.adaptTo(ContentFragment.class);
                Iterator<ContentElement> elements = cf.getElements();
                if (cf == null) {
                    LOGGER.warn("⚠️ Resource is not a valid CF: {}", cfPath);
                    skippedFragments.add(cfName);
                    skippedCount++;
                    continue;
                }

                boolean updated = true;

                while (elements.hasNext()) {
                    ContentElement element = elements.next();
                    String elementNormalized = element.getName().replaceAll("[ _]", "").toLowerCase();

                    for (int i = 0; i < headers.size(); i++) {
                        String headerNormalized = headers.get(i).replaceAll("[ _]", "").toLowerCase();
                        if (i < dataRow.size() && elementNormalized.equals(headerNormalized)) {
                            String oldValue = StringUtils.trimToEmpty(element.getContent());
                            String newValue = dataRow.get(i).trim();
                            if (!StringUtils.equalsIgnoreCase(oldValue, newValue)) {
                                element.setContent(newValue, element.getContentType());
                                updated = false;
                                LOGGER.info("✅ Updated '{}' in CF '{}': '{}' -> '{}'", element.getName(), cfName, oldValue, newValue);
                                LOGGER.info("boolean: {}",updated);
                            }
                            break;
                        }
                    }
                }

                if (!updated) {
                    updatedCount++;
                    updatedFragments.add(cfName);
                    resolver.commit();
                    LOGGER.info("💾 Changes committed for CF '{}'", cfName);
                } else {
                    skippedCount++;
                    skippedFragments.add(cfName);
                    LOGGER.info("⏭️ No changes detected for CF '{}'", cfName);
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("updatedCount", updatedCount);
            result.put("skippedCount", skippedCount);
            result.put("updatedFragments", updatedFragments);
            result.put("skippedFragments", skippedFragments);

            LOGGER.info("====== ✅ Update Summary ======");
            LOGGER.info("Updated CFs: {} | Skipped CFs: {}", updatedCount, skippedCount);
            LOGGER.info("================================");

            response.getWriter().write(new Gson().toJson(result));

        } catch (Exception e) {
            LOGGER.error("❌ Error processing Excel for CF update", e);
            response.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
        catch (Exception e){
            response.getWriter().write(new Gson().toJson(e.getMessage()));
        }
    }




/**
     * ✅ Utility method to fetch existing content fragments and their data
     */
    private List<Map<String, Object>> getExistingContentFragments(ResourceResolver resolver, String parentPath) {
        List<Map<String, Object>> fragmentsData = new ArrayList<>();

        Resource parent = resolver.getResource(parentPath);
        if (parent == null) {
            LOGGER.error("❌ Parent path not found: {}", parentPath);
            return fragmentsData;
        }

        for (Resource cfRes : parent.getChildren()) {
            ContentFragment cf = cfRes.adaptTo(ContentFragment.class);
            if (cf == null) continue;

            Map<String, Object> cfMap = new LinkedHashMap<>();
            cfMap.put("cfName", cfRes.getName());

            Map<String, String> fields = new LinkedHashMap<>();
            Iterator<ContentElement> iterator = cf.getElements();
            while (iterator.hasNext()) {
                ContentElement element = iterator.next();
                fields.put(element.getName(), element.getContent());
            }

            cfMap.put("fields", fields);
            fragmentsData.add(cfMap);
        }

        LOGGER.info("📦 Found {} content fragments under {}", fragmentsData.size(), parentPath);
        return fragmentsData;
    }

    /** Sanitize CF name for JCR path safety */
    private String sanitizeForJcr(String name) {
        if (name == null) return "";
        return name.trim().toLowerCase().replaceAll("[^a-z0-9\\-]", "-");
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

