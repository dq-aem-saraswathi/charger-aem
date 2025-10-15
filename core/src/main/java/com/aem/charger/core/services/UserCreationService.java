package com.aem.charger.core.services;


import com.aem.charger.core.models.TunnelDetails;

import java.util.List;

/**
 */
public interface UserCreationService {

	/**
	 * This method takes the users in the form of a list and create them one by one
	 * in AEM instance
	 * 
	 * @param users
	 */

	void createCFS(List<TunnelDetails> users);

}
