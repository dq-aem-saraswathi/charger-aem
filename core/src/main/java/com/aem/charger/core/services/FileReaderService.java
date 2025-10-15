package com.aem.charger.core.services;


import com.aem.charger.core.models.TunnelDetails;

import java.util.List;

/**
 *
 */
public interface FileReaderService {

	/**
	 * This method reads the file uploaded by the user. The file should be an excel
	 * file (.xls or .xlsx)
	 */
	List<TunnelDetails> readExcel(String filePath);
}
