package com.aem.charger.core.services.impl;

import com.aem.charger.core.models.TunnelDetails;
import com.aem.charger.core.services.FileReaderService;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Implementation of FileReaderService to read Excel file and convert into TunnelDetails list
 */
@Component(service = FileReaderService.class, immediate = true)
public class FileReaderServiceImpl implements FileReaderService {

   private final Logger log = LoggerFactory.getLogger(this.getClass());
   private List<TunnelDetails> users;

   @Override
   public List<TunnelDetails> readExcel(String filePath) {

      log.info("Starting to read Excel file at path: {}", filePath);

      users = new LinkedList<>();

      try (Workbook workbook = WorkbookFactory.create(new File(filePath))) {

         int numberOfSheets = workbook.getNumberOfSheets();
         log.info("Workbook loaded successfully. Total sheets found: {}", numberOfSheets);

         for (int i = 0; i < numberOfSheets; i++) {
            Sheet sheet = workbook.getSheetAt(i);
            log.info("Processing sheet [{}]: {}", i + 1, sheet.getSheetName());

            Iterator<Row> rowIterator = sheet.iterator();
            int rowCount = 0;

            while (rowIterator.hasNext()) {
               Row row = rowIterator.next();
               log.debug("Reading row number: {}", rowCount);

               if (rowCount == 0) {
                  log.debug("Skipping header row");

          Iterator<Cell>cells= row.cellIterator();

          while(cells.hasNext()){
             log.info("cell details .....{}",cells.next().getStringCellValue());

          }
                  rowCount++;
                  continue;
               }

               TunnelDetails user = new TunnelDetails();
               log.debug("Created new TunnelDetails object for row {}", rowCount);

               // Tunnel ID
               String tunnelId = Optional.ofNullable(row.getCell(0))
                       .map(cell -> cell.toString().replace(".0", ""))
                       .orElse("");
               log.debug("Tunnel ID extracted: '{}'", tunnelId);

               if (tunnelId.isEmpty()) log.error("Tunnel ID is empty for row {}", rowCount);

               // Last Updated Date
               Date dateLastUpdated = null;
               Calendar dateLastUpdatedCal = null;
               String lastUpdatedDateStr = "";

               if (row.getCell(1) != null) {
                  log.debug("Processing Last Updated Date cell for row {}", rowCount);
                  switch (row.getCell(1).getCellType()) {
                     case NUMERIC:
                        log.info("Date cell type detected: NUMERIC");
                        dateLastUpdated = row.getCell(1).getDateCellValue();
                        dateLastUpdatedCal = Calendar.getInstance(TimeZone.getTimeZone("Australia/Sydney"));
                        dateLastUpdatedCal.setTime(dateLastUpdated);
                        lastUpdatedDateStr = new SimpleDateFormat("dd/MMM/yy HH:mm:ss").format(dateLastUpdated);
                        log.info("Parsed Last Updated Date (numeric): {}", lastUpdatedDateStr);
                        break;
                     case STRING:
                        String cellValue = row.getCell(1).toString().trim();
                        log.info("Date cell type detected: STRING, raw value: {}", cellValue);
                        if (!cellValue.isEmpty()) {
                           lastUpdatedDateStr = cellValue;
                           String[] parts = cellValue.split(" ");
                           if (parts.length > 1) {
                              try {
                                 String[] timeParts = parts[1].split(":");
                                 int hour = Integer.parseInt(timeParts[0]);
                                 if (cellValue.contains("PM") && hour != 12) hour += 12;
                                 if (cellValue.contains("AM") && hour == 12) hour = 0;
                                 String formatted = parts[0] + " " + String.format("%02d:%s:%s", hour, timeParts[1], timeParts[2]);
                                 SimpleDateFormat formatter = new SimpleDateFormat("dd/MMM/yy HH:mm:ss");
                                 formatter.setTimeZone(TimeZone.getTimeZone("Australia/Sydney"));
                                 dateLastUpdated = formatter.parse(formatted);
                                 dateLastUpdatedCal = Calendar.getInstance(TimeZone.getTimeZone("Australia/Sydney"));
                                 dateLastUpdatedCal.setTime(dateLastUpdated);
                                 log.info("Parsed Last Updated Date (string): {}", formatted);
                              } catch (Exception e) {
                                 log.error("Malformed lastUpdatedDate cell '{}' in row {}", cellValue, rowCount, e);
                              }
                           } else {
                              log.error("Malformed lastUpdatedDate cell: '{}' in row {}", cellValue, rowCount);
                           }
                        }
                        break;
                     default:
                        log.error("Unknown cell type for lastUpdatedDate in row {}", rowCount);
                        break;
                  }
               } else {
                  log.info("lastUpdatedDate cell is null for row {}", rowCount);
               }

               user.setLastUpdatedDate(dateLastUpdated);
               user.setLastUpdatedDateCal(dateLastUpdatedCal);
               user.setLastUpdatedDateTimeStr(lastUpdatedDateStr);
               log.debug("Last updated date fields set successfully");

               // Remaining string fields
               user.setOperatingMode(Optional.ofNullable(row.getCell(2)).map(Object::toString).orElse(""));
               user.setOperatingSystem(Optional.ofNullable(row.getCell(3)).map(Object::toString).orElse(""));
               user.setVentillation(Optional.ofNullable(row.getCell(4)).map(Object::toString).orElse(""));
               user.setLaneCoveInflows(Optional.ofNullable(row.getCell(5)).map(Object::toString).orElse(""));
               user.setScottsCreekInflows(Optional.ofNullable(row.getCell(6)).map(Object::toString).orElse(""));
               user.setTunksParkInflows(Optional.ofNullable(row.getCell(7)).map(Object::toString).orElse(""));
               user.setQuakersHatBayInflows(Optional.ofNullable(row.getCell(8)).map(Object::toString).orElse(""));
               user.setShellyBeachInflows(Optional.ofNullable(row.getCell(9)).map(Object::toString).orElse(""));
               user.setLaneCoveOverflows(Optional.ofNullable(row.getCell(10)).map(Object::toString).orElse(""));
               user.setScottsCreekOverflows(Optional.ofNullable(row.getCell(11)).map(Object::toString).orElse(""));
               user.setTunksParkOverflows(Optional.ofNullable(row.getCell(12)).map(Object::toString).orElse(""));
               user.setQuakersHatBayOverflows(Optional.ofNullable(row.getCell(13)).map(Object::toString).orElse(""));
               user.setShellyBeachOverflows(Optional.ofNullable(row.getCell(14)).map(Object::toString).orElse(""));
               user.setLaneCoveTunnelVenting(Optional.ofNullable(row.getCell(15)).map(Object::toString).orElse(""));
               user.setScottsCreekTunnelVenting(Optional.ofNullable(row.getCell(16)).map(Object::toString).orElse(""));
               user.setTunksParkTunnelVenting(Optional.ofNullable(row.getCell(17)).map(Object::toString).orElse(""));
               user.setQuakersHatBayTunnelVenting(Optional.ofNullable(row.getCell(18)).map(Object::toString).orElse(""));
               user.setShellyBeachTunnelVenting(Optional.ofNullable(row.getCell(19)).map(Object::toString).orElse(""));

               log.info("String fields populated for row {}", rowCount);

               // Remaining integer fields
               user.setLaneCoveTotal(parseIntCell(row.getCell(20)));
               user.setScottsCreekTotal(parseIntCell(row.getCell(21)));
               user.setTunksParkTotal(parseIntCell(row.getCell(22)));
               user.setQuakersHatBayTotal(parseIntCell(row.getCell(23)));
               user.setShellyBeachTotal(parseIntCell(row.getCell(24)));

             //  log.debug("Integer fields populated for row {}", rowCount);
               log.info("ab.............{}",Optional.ofNullable(row.getCell(2)).map(Object::toString).orElse(""));
               users.add(user);
              // log.info("Added TunnelDetails for row {} successfully", rowCount);

               rowCount++;
            }
         }

         log.info("File processing completed. Total sheets: {}, Total rows processed: {}", numberOfSheets, users.size());
log.info("users : {}",users);
      } catch (EncryptedDocumentException | IOException e) {
         log.error("Error reading Excel file: {}", e.getMessage(), e);
      }

      log.info("Returning {} TunnelDetails records", users.size());
      return users;
   }

   private int parseIntCell(org.apache.poi.ss.usermodel.Cell cell) {
      if (cell == null) return 0;
      try {
         String value = cell.toString().replace(".0", "");
         int parsed = Integer.parseInt(value);
         log.trace("Parsed integer cell: {}", parsed);
         return parsed;
      } catch (NumberFormatException e) {
         log.warn("Failed to parse integer cell: {}", cell.toString());
         return 0;
      }
   }
}
