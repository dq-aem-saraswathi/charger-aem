package com.aem.charger.core.servlets;

import com.adobe.cq.dam.cfm.ContentElement;
import com.adobe.cq.dam.cfm.ContentFragment;
import com.adobe.cq.dam.cfm.ContentFragmentException;
import com.adobe.cq.dam.cfm.FragmentTemplate;
import com.aem.charger.core.services.ExcelModelCompareService;
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
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
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
    @Reference
    private ExcelModelCompareService excelModelCompareService;
    @Override
    protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        LOGGER.info("====== 🧾 Starting Content Fragment Update Process ======");

        String parentPath = request.getParameter("parentPath");
        String titleColumn = request.getParameter("selectfield");
        String modelType = request.getParameter("modelType");
        String modelPath = "/conf/charger/settings/dam/cfm/models/" + modelType;

        if (StringUtils.isBlank(parentPath) || StringUtils.isBlank(titleColumn)) {
            LOGGER.error("❌ Missing parentPath or titleColumn parameter");
            response.getWriter().write("{\"error\":\"Missing parentPath or titleColumn parameter\"}");
            return;
        }

        ResourceResolver resolver = request.getResourceResolver();

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
                LOGGER.info("📑 Loaded sheet: {} | Rows: {}", sheet.getSheetName(), sheet.getPhysicalNumberOfRows());

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
                InputStream compareStream = filePart.getInputStream();
                String compareResult = excelModelCompareService.compareExcelWithModel(resolver, compareStream, modelPath);
                if (!"Matched".equals(compareResult)) {
                    LOGGER.warn("Excel columns do not match the selected model: {}", modelType);
                //    jsout.name("status").value("Model mismatched for uploaded file");
                    return;
                }
                int titleIndex = headers.indexOf(titleColumn);
                if (titleIndex == -1) {
                    LOGGER.warn("⚠️ Title column '{}' not found — using first column.", titleColumn);
                    titleIndex = 0;
                }

                int updatedCount = 0, skippedCount = 0;
                List<String> updatedFragments = new ArrayList<>();
                List<String> skippedFragments = new ArrayList<>();
                List<String> createdFragments = new ArrayList<>();
                for (List<String> dataRow : rowsData) {
                    if (dataRow.isEmpty()) continue;

                    String cfTitleValue = safeCellValue(dataRow.get(titleIndex));

                    // Convert numeric-like "326.0" → "326"
                    if (cfTitleValue.matches("\\d+(\\.0+)?")) {
                        cfTitleValue = cfTitleValue.split("\\.")[0];
                    }

                    // Handle date-like titles gracefully
                    if (looksLikeDate(cfTitleValue)) {
                        cfTitleValue = formatDateForName(cfTitleValue);
                    }

                    if (StringUtils.isEmpty(cfTitleValue)) {
                        cfTitleValue = "row_" + dataRow.hashCode();
                    }

                    String cfName = sanitizeForJcr(cfTitleValue);
                    String cfPath = parentPath + "/" + cfName;

                    LOGGER.info("------------------------------------------------------");
                    LOGGER.info("🔍 Checking CF: {}", cfPath);

                    Resource cfResource = resolver.getResource(cfPath);

                    if (cfResource == null) {
                        LOGGER.info("⚠️ CF not found — will create: {}", cfName);
                        createContentFragment(resolver, parentPath, headers, dataRow.toArray(new String[0]), modelType, cfName);
                        resolver.commit();
                        // skippedCount++;
                        createdFragments.add(cfName);
                        continue;
                    }

                    ContentFragment cf = cfResource.adaptTo(ContentFragment.class);
                    if (cf == null) {
                        LOGGER.warn("⚠️ Not a valid CF resource: {}", cfPath);
                        skippedCount++;
                        skippedFragments.add(cfName);
                        continue;
                    }

                    boolean updated = false;

                    Iterator<ContentElement> elements = cf.getElements();
                    while (elements.hasNext()) {
                        ContentElement element = elements.next();
                        String elementNormalized = element.getName().replaceAll("[ _]", "").toLowerCase();

                        for (int i = 0; i < headers.size(); i++) {
                            String headerNormalized = headers.get(i).replaceAll("[ _]", "").toLowerCase();
                            if (i < dataRow.size() && elementNormalized.equals(headerNormalized)) {
                                String oldValue = StringUtils.trimToEmpty(element.getContent());
                                String newValue = safeCellValue(dataRow.get(i));

                                if (!StringUtils.equalsIgnoreCase(oldValue, newValue)) {
                                    element.setContent(newValue, element.getContentType());
                                    updated = true;
                                    LOGGER.info("✅ Updated '{}' → '{}'", element.getName(), newValue);
                                }
                                break;
                            }
                        }
                    }

                    if (updated) {
                        resolver.commit();
                        updatedCount++;
                        updatedFragments.add(cfName);
                        LOGGER.info("💾 Changes committed for {}", cfName);
                    } else {
                        skippedCount++;
                        skippedFragments.add(cfName);
                        LOGGER.info("⏭️ No changes for {}", cfName);
                    }
                }

                Map<String, Object> result = new HashMap<>();
                result.put("updatedCount", updatedCount);
                result.put("skippedCount", skippedCount);
                result.put("updatedFragments", updatedFragments);
                result.put("skippedFragments", skippedFragments);
                result.put("createFragments",createdFragments);
                LOGGER.info("====== ✅ Update Summary ======");
                LOGGER.info("Updated: {} | Skipped: {}", updatedCount, skippedCount);
                response.getWriter().write(new Gson().toJson(result));

            } catch (Exception e) {
                LOGGER.error("❌ Error updating CFs", e);
                response.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
            }

        } catch (Exception e) {
            response.getWriter().write(new Gson().toJson(e.getMessage()));
        }
    }

    /** ✅ Ensure non-null, trimmed string value */
    private String safeCellValue(String value) {
        return StringUtils.defaultString(value, "").trim();
    }

    /** ✅ Detect likely date values */
    private boolean looksLikeDate(String val) {
        return val.matches(".*\\d{2}/[A-Za-z]{3}/\\d{2}.*") || val.matches(".*\\d{2}:\\d{2}:\\d{2}.*");
    }

    /** ✅ Case-insensitive date parser for CF titles */
    private String formatDateForName(String value) {
        try {
            DateTimeFormatter inputFmt = new DateTimeFormatterBuilder()
                    .parseCaseInsensitive()
                    .appendPattern("dd/MMM/yy hh:mm:ss.SSSSSSSSS a")
                    .toFormatter(Locale.ENGLISH);

            LocalDateTime dt = LocalDateTime.parse(value.trim(), inputFmt);

            return dt.format(DateTimeFormatter.ofPattern("dd-MMM-yy-hh-mm-ss-a", Locale.ENGLISH)).toLowerCase();
        } catch (Exception e) {
            LOGGER.warn("Could not parse date '{}': {}", value, e.getMessage());
            return value.trim().toLowerCase().replaceAll("[^a-z0-9\\-]", "-");
        }
    }

    /** ✅ Sanitize CF name safely */
    private String sanitizeForJcr(String name) {
        if (name == null) return "";
        return name.trim().toLowerCase().replaceAll("[^a-z0-9\\-]", "-");
    }

    /** ✅ Create CF from template if missing */
    public void createContentFragment(ResourceResolver resolver, String parentPath,
                                      List<String> headerList, String[] data, String modelType, String cfName) {
        LOGGER.info("Creating CF: {}", cfName);
        try {
            Resource templateRes = resolver.getResource("/conf/charger/settings/dam/cfm/models/" + modelType);
            if (templateRes == null) {
                LOGGER.error("Template not found: {}", modelType);
                return;
            }

            FragmentTemplate template = templateRes.adaptTo(FragmentTemplate.class);
            if (template == null) {
                LOGGER.error("Failed to adapt template: {}", modelType);
                return;
            }

            Resource parentRes = resolver.getResource(parentPath);
            if (parentRes == null) {
                LOGGER.error("Parent path missing: {}", parentPath);
                return;
            }

            ContentFragment cf = template.createFragment(parentRes, cfName, cfName);

            if (cf != null) {
                Iterator<ContentElement> elements = cf.getElements();
                while (elements.hasNext()) {
                    ContentElement element = elements.next();
                    String elementNormalized = element.getName().replaceAll("[ _]", "").toLowerCase();

                    for (int i = 0; i < headerList.size(); i++) {
                        String headerNormalized = headerList.get(i).replaceAll("[ _]", "").toLowerCase();
                        if (i < data.length && elementNormalized.equals(headerNormalized)) {
                            element.setContent(safeCellValue(data[i]), element.getContentType());
                            LOGGER.debug("Set element {} = {}", element.getName(), data[i]);
                            break;
                        }
                    }
                }
            }

        } catch (Exception e) {
            LOGGER.error("Error creating/updating CF", e);
        }
    }
}

