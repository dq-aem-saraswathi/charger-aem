package com.aem.charger.core.services.impl;

import com.adobe.cq.dam.cfm.ContentFragment;
import com.adobe.cq.dam.cfm.FragmentTemplate;
import com.aem.charger.core.models.TunnelDetails;
import com.aem.charger.core.services.UserCreationService;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Component(service = UserCreationService.class, immediate = true)
public class UserCreationServiceImpl implements UserCreationService {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Reference
	private ResourceResolverFactory resourceResolverFactory;

	private ResourceResolver resourceResolver;

	private Session session;

	/**
	 * This method initializes stuff for the UserManager API
	 */
	private void init() {

		log.info("Initializing stuff for creating users");

		try {
			// Creating a map for holding service user data
			Map<String, Object> serviceUserMap = new HashMap<>();
			serviceUserMap.put(ResourceResolverFactory.SUBSERVICE, "nirbhai");

			// Getting the ResourceResolver from the serviceUserMap
			resourceResolver =
					resourceResolverFactory.getServiceResourceResolver(serviceUserMap);

			//resourceResolver = resourceResolverFactory.getAdministrativeResourceResolver(null);

			// Creating the session object by adapting ResourceResolver
			session = resourceResolver.adaptTo(Session.class);
		} catch (LoginException e) {
			log.error(e.getMessage(), e);
		}
	}

	@Override
	public void createCFS(List<TunnelDetails> users) {
		init();
		log.info("CFS creation starts !!");

		try {
			log.info("helloooooooooooooo");
			Resource templateResource = resourceResolver
					.getResource("/conf/charger/settings/dam/cfm/models/tunneldetails");
			if (templateResource == null) {
				log.error("Template resource is null! Path does not exist or no read access: /conf/charger/settings/dam/cfm/models/tunneldetails");
				return; // exit gracefully
			}
			log.info("Template resource found: {}", templateResource.getPath());

			log.info("templateResource :{}",templateResource);
			FragmentTemplate fragmentTemplate = templateResource.adaptTo(FragmentTemplate.class);
			log.info("fragmentTemplate : {}",fragmentTemplate);
			Resource cfResource = resourceResolver.getResource("/content/dam/charger");
			log.info("cfResource : {}",cfResource);
			// templateResource.adaptTo(FragmentTemplate.class).createFragment(cfResource,
			// "test4", "testTitle");
			// templateResource.adaptTo(Node.class).getSession().save();

			int count = 1;
			// Loop for all the users
			for (TunnelDetails userObj : users) {
				String cfName = "cf" + count;
				// Getting user details
				log.info("cfName : {}",cfName);
           String tunnelId = userObj.getTunnelId();
				log.info("tunnelId :{}",tunnelId);
           String operatingMode = userObj.getOperatingMode();
				log.info("operatingMode : {}",operatingMode);
           String operatingSystem = userObj.getOperatingSystem();
				log.info("operatingSystem : {}",operatingSystem);
           String ventillation = userObj.getVentillation();
				log.info("ventillation : {}",ventillation);

				String laneCoveInflows = userObj.getLaneCoveInflows();
				log.info("laneCoveInflows : {}",laneCoveInflows);

				String scottsCreekInflows = userObj.getScottsCreekInflows();
				log.info("scottsCreekInflows : {}",scottsCreekInflows);

				String tunksParkInflows = userObj.getTunksParkInflows();
				log.info("tunksParkInflows : {}",tunksParkInflows);

				String quakersHatBayInflows = userObj.getQuakersHatBayInflows();
				log.info("quakersHatBayInflows : {}",quakersHatBayInflows);

				String shellyBeachInflows = userObj.getShellyBeachInflows();
				log.info("shellyBeachInflows : {}",shellyBeachInflows);

				String laneCoveOverflows = userObj.getLaneCoveOverflows();
				log.info("laneCoveOverflows : {}",laneCoveOverflows);

				String scottsCreekOverflows = userObj.getScottsCreekOverflows();
				log.info("scottsCreekOverflows : {}",scottsCreekOverflows);

				String tunksParkOverflows = userObj.getTunksParkOverflows();
				log.info("tunksParkOverflows : {}",tunksParkOverflows);

				String quakersHatBayOverflows = userObj.getQuakersHatBayOverflows();
				log.info("quakersHatBayOverflows : {}",quakersHatBayOverflows);

				String shellyBeachOverflows = userObj.getShellyBeachOverflows();
				log.info("shellyBeachOverflows : {}",shellyBeachOverflows);

				String laneCoveTunnelVenting = userObj.getLaneCoveTunnelVenting();
				log.info("laneCoveTunnelVenting : {}",laneCoveTunnelVenting);

				String scottsCreekTunnelVenting = userObj.getScottsCreekTunnelVenting();
				log.info("scottsCreekTunnelVenting : {}",scottsCreekTunnelVenting);

				String tunksParkTunnelVenting = userObj.getTunksParkTunnelVenting();
				log.info("tunksParkTunnelVenting : {}",tunksParkTunnelVenting);

				String quakersHatBayTunnelVenting = userObj.getQuakersHatBayTunnelVenting();
				log.info("quakersHatBayTunnelVenting : {}",quakersHatBayTunnelVenting);

				String shellyBeachTunnelVenting = userObj.getShellyBeachTunnelVenting();
				log.info("shellyBeachTunnelVenting : {}",shellyBeachTunnelVenting);

				int laneCoveTotal = userObj.getLaneCoveTotal();
				log.info("laneCoveTotal : {}",laneCoveTotal);

				int scottsCreekTotal = userObj.getScottsCreekTotal();
				log.info("scottsCreekTotal : {}",scottsCreekTotal);

				int tunksParkTotal = userObj.getTunksParkTotal();
				log.info("tunksParkTotal : {}",tunksParkTotal);

				int quakersHatBayTotal = userObj.getQuakersHatBayTotal();
				log.info("quakersHatBayTotal : {}",quakersHatBayTotal);

				int shellyBeachTotal = userObj.getShellyBeachTotal();

				log.info("shellyBeachTotal : {}",shellyBeachTotal);


//
				Date lastUpdatedDate = userObj.getLastUpdatedDate();
				String timeStr = userObj.getLastUpdatedDateTimeStr();
           timeStr = timeStr.split(" ")[1];
         timeStr = timeStr.split(":")[0] + "-" + timeStr.split(":")[1];
           log.debug("cf name is " + timeStr);

           Calendar calLastUpdated =  Calendar.getInstance(TimeZone.getTimeZone("Australia/Sydney")); //Calendar.getInstance();
           if (null != lastUpdatedDate)
              calLastUpdated.setTime(lastUpdatedDate);
           log.debug("cfResource is : " + cfResource.getPath());
//
				String folderPath = getCfNode(lastUpdatedDate).split("#")[0];
				String nodeName = timeStr;// getCfNode(lastUpdatedDate).split("#")[1];
				// nodeName = nodeName.replace(":", "-");


				if(folderPath!=null){
					log.debug("folderPath is : " + folderPath);
				}
				else{
					log.debug("folder path is not existing ......");
				}
				log.debug("node is : " + nodeName);
				cfResource = resourceResolver.getResource(folderPath);
				/*
				 * Node cfResourceNode = cfResource.adaptTo(Node.class); int duplicateCount = 1;
				 * if (cfResourceNode.hasNode(nodeName)) { nodeName = nodeName + "-" +
				 * duplicateCount; duplicateCount++; }
				 */
				ContentFragment cf = fragmentTemplate.createFragment(cfResource, nodeName, nodeName);
				templateResource.adaptTo(Node.class).getSession().save();
				log.debug("done 1");

				log.debug("created the cf : " + cf.getName());
				Node cfNode = resourceResolver.getResource(folderPath + "/" + cf.getName() + "/jcr:content/data/master")
						.adaptTo(Node.class);

				log.debug("created the cf in the path :: " + cfNode.getPath());

				if (null != lastUpdatedDate) {
					cfNode.setProperty("lastUpdatedDate", calLastUpdated);
					log.debug("setting the cal direclty : "+ userObj.getLastUpdatedDateCal());
					 cfNode.setProperty("lastUpdatedDate", userObj.getLastUpdatedDateCal());
				}

           if (null != operatingMode) {
              cfNode.setProperty("operatingMode", operatingMode);
           }
           if (null != operatingSystem) {
              cfNode.setProperty("operatingStatus", operatingSystem);
           }
           if (null != ventillation) {
              cfNode.setProperty("ventilation", ventillation);
           }
           if (null != laneCoveInflows) {
              cfNode.setProperty("laneCoveInflows", laneCoveInflows);
           }
           if (null != scottsCreekInflows) {
              cfNode.setProperty("scottsCreekInflows", scottsCreekInflows);
           }

				/*
				 * String tunnelId; Date lastUpdatedDate; String operatingMode; String
				 * operatingSystem; String ventillation; String laneCoveInflows; String
				 * scottsCreekInflows; String tunksParkInflows; String quakersHatBayInflows;
				 * String shellyBeachInflows; String laneCoveOverflows; String
				 * scottsCreekOverflows; String tunksParkOverflows; String
				 * quakersHatBayOverflows; String shellyBeachOverflows; String
				 * laneCoveTunnelVenting; String scottsCreekTunnelVenting; String
				 * tunksParkTunnelVenting; String quakersHatBayTunnelVenting; String
				 * shellyBeachTunnelVenting; int laneCoveTotal; int scottsCreekTotal; int
				 * tunksParkTotal; int quakersHatBayTotal; int shellyBeachTotal;
				 */

           if (null != tunksParkInflows) {
              cfNode.setProperty("tunksParkInflows", tunksParkInflows);
           }
           if (null != quakersHatBayInflows) {
              cfNode.setProperty("quakersHatBayInflows", quakersHatBayInflows);
           }
           if (null != shellyBeachInflows) {
              cfNode.setProperty("shellyBeachInflows", shellyBeachInflows);
           }
           if (null != laneCoveOverflows) {
              cfNode.setProperty("laneCoveOverflows", laneCoveOverflows);
           }
           if (null != scottsCreekOverflows) {
              cfNode.setProperty("scottsCreekOverflows", scottsCreekOverflows);
           }
           if (null != tunksParkOverflows) {
              cfNode.setProperty("tunksParkOverflows", tunksParkOverflows);
           }
           if (null != quakersHatBayOverflows) {
              cfNode.setProperty("quakersHatBayOverflows", quakersHatBayOverflows);
           }
           if (null != shellyBeachOverflows) {
              cfNode.setProperty("shellyBeachOverflows", shellyBeachOverflows);
           }
           if (null != laneCoveTunnelVenting) {
              cfNode.setProperty("laneCoveTunnelVenting", laneCoveTunnelVenting);
           }
           if (null != scottsCreekTunnelVenting) {
              cfNode.setProperty("scottsCreekTunnelVenting", scottsCreekTunnelVenting);
           }
           if (null != tunksParkTunnelVenting) {
              cfNode.setProperty("tunksParkTunnelVenting", tunksParkTunnelVenting);
           }
           if (null != quakersHatBayTunnelVenting) {
              cfNode.setProperty("quakersHatBayTunnelVenting", quakersHatBayTunnelVenting);
           }
           if (null != shellyBeachTunnelVenting) {
              cfNode.setProperty("shellyBeachTunnelVenting", shellyBeachTunnelVenting);
           }
           if (laneCoveTotal != 0) {
              cfNode.setProperty("laneCoveTotal", laneCoveTotal);
           }
           if (scottsCreekTotal != 0) {
              cfNode.setProperty("scottsCreekTotal", scottsCreekTotal);
           }
           if (tunksParkTotal != 0) {
              cfNode.setProperty("tunksParkTotal", tunksParkTotal);
           }
           if (quakersHatBayTotal != 0) {
              cfNode.setProperty("quakersHatBayTotal", quakersHatBayTotal);
           }
           if (shellyBeachTotal != 0) {
              cfNode.setProperty("shellyBeachTotal", shellyBeachTotal);
           }

				log.debug("created the cf completed");
				cfNode.getSession().save();
				count++;
			}

		} catch (Exception e) {

			log.info("Error creating CF:",e.getMessage());
		}

	}

	private String getCfNode(Date dateCreated) throws PathNotFoundException, RepositoryException {
		String parentNodePath = "/content/dam/charger";
		log.info("parentNodePath : {}",parentNodePath);
		Calendar calCreated = Calendar.getInstance(TimeZone.getTimeZone("Australia/Sydney")); // Calendar.getInstance();
		log.info("calCreated :{}",calCreated);
		if (null != dateCreated)
			calCreated.setTime(dateCreated);
		LocalDate dt = LocalDateTime.ofInstant(calCreated.toInstant(), calCreated.getTimeZone().toZoneId())
				.toLocalDate();
		int year = calCreated.get(Calendar.YEAR);
		log.info("year {}",year);
		String month = new SimpleDateFormat("MMM").format(calCreated.getTime());
		String day = dt.getDayOfWeek().toString();
		String folderName = day + ", " + dt.getDayOfMonth() + " " + month + " " + year;
		String folderPath = parentNodePath + "/" + year + "/" + month + "/" + folderName;
		String yearNodePath = parentNodePath + "/" + year;
		String monthNodePath = parentNodePath + "/" + year + "/" + month;
		String nodeTitle = day;

		String yearString = Integer.toString(year);
		Node folderNode = null;
		Node yearNode = null;
		Node monthNode = null;
		Node parentNode = session.getNode(parentNodePath);
		if (!session.itemExists(yearNodePath)) {
			yearNode = parentNode.addNode(yearString, "sling:Folder");
			log.info("yearNode :{}",yearNode);
			session.save();
		} else {
			yearNode = session.getNode(yearNodePath);
			log.info("yearNode :{}",yearNode);
		}
		if (!session.itemExists(monthNodePath)) {
			monthNode = yearNode.addNode(month, "sling:Folder");
			log.info("monthNode :{}",monthNode);
			session.save();
		} else {
			monthNode = session.getNode(monthNodePath);
			log.info("monthNode :{}",monthNode);

		}
		if (!session.itemExists(folderPath)) {
			folderNode = monthNode.addNode(folderName, "sling:Folder");
			log.info("folderNode :{}",folderNode);

			session.save();
		} else {
			folderNode = session.getNode(folderPath);
			log.info("folderNode :{}",folderNode);

		}

		String dateStr = calCreated.getTime().toString();
		String time = dateStr.split(" ")[3];
		log.info("dateStr :{}",dateStr);
		return folderNode.getPath() + "#" + time;

	}
}