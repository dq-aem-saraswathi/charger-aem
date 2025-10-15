package com.aem.charger.core.models;

import java.util.Calendar;
import java.util.Date;

/**
 *
 */
public class TunnelDetails {
	String TUPDATEID;




	//  String tunnelId;
	Date lastUpdatedDate;

	Calendar lastUpdatedDateCal;
  String operatingMode;
  String operatingSystem;
  String ventillation;
  String laneCoveInflows;
  String scottsCreekInflows;
  String tunksParkInflows;
  String quakersHatBayInflows;
  String shellyBeachInflows;
  String laneCoveOverflows;
  String scottsCreekOverflows;
  String tunksParkOverflows;
  String quakersHatBayOverflows;
  String shellyBeachOverflows;
  String laneCoveTunnelVenting;
  String scottsCreekTunnelVenting;
  String tunksParkTunnelVenting;
  String quakersHatBayTunnelVenting;
  String shellyBeachTunnelVenting;
  int laneCoveTotal;
  int scottsCreekTotal;
  int tunksParkTotal;
  int quakersHatBayTotal;
  int shellyBeachTotal;
  String lastUpdatedDateTimeStr;
	//
	public String getLastUpdatedDateTimeStr() {
		return lastUpdatedDateTimeStr;
	}
	//
	public void setLastUpdatedDateTimeStr(String lastUpdatedDateTimeStr) {
		this.lastUpdatedDateTimeStr = lastUpdatedDateTimeStr;
	}
//
  public String getTunnelId() {
     return TUPDATEID;
  }

  public void setTunnelId(String tunnelId) {
     this.TUPDATEID = tunnelId;
  }

  public Date getLastUpdatedDate() {
     return lastUpdatedDate;
  }

  public void setLastUpdatedDate(Date lastUpdatedDate) {
     this.lastUpdatedDate = lastUpdatedDate;
  }

  public String getOperatingMode() {
     return operatingMode;
  }

  public void setOperatingMode(String operatingMode) {
     this.operatingMode = operatingMode;
  }

  public String getOperatingSystem() {
     return operatingSystem;
  }

  public void setOperatingSystem(String operatingSystem) {
     this.operatingSystem = operatingSystem;
  }

  public String getVentillation() {
     return ventillation;
  }

  public void setVentillation(String ventillation) {
     this.ventillation = ventillation;
  }

  public String getLaneCoveInflows() {
     return laneCoveInflows;
  }

  public void setLaneCoveInflows(String laneCoveInflows) {
     this.laneCoveInflows = laneCoveInflows;
  }

  public String getScottsCreekInflows() {
     return scottsCreekInflows;
  }

  public void setScottsCreekInflows(String scottsCreekInflows) {
     this.scottsCreekInflows = scottsCreekInflows;
  }

  public String getTunksParkInflows() {
     return tunksParkInflows;
  }

  public void setTunksParkInflows(String tunksParkInflows) {
     this.tunksParkInflows = tunksParkInflows;
  }

  public String getQuakersHatBayInflows() {
     return quakersHatBayInflows;
  }

  public void setQuakersHatBayInflows(String quakersHatBayInflows) {
     this.quakersHatBayInflows = quakersHatBayInflows;
  }

  public String getShellyBeachInflows() {
     return shellyBeachInflows;
  }

  public void setShellyBeachInflows(String shellyBeachInflows) {
     this.shellyBeachInflows = shellyBeachInflows;
  }

  public String getLaneCoveOverflows() {
     return laneCoveOverflows;
  }

  public void setLaneCoveOverflows(String laneCoveOverflows) {
     this.laneCoveOverflows = laneCoveOverflows;
  }

  public String getScottsCreekOverflows() {
     return scottsCreekOverflows;
  }

  public void setScottsCreekOverflows(String scottsCreekOverflows) {
     this.scottsCreekOverflows = scottsCreekOverflows;
  }

  public String getTunksParkOverflows() {
     return tunksParkOverflows;
  }

  public void setTunksParkOverflows(String tunksParkOverflows) {
     this.tunksParkOverflows = tunksParkOverflows;
  }

  public String getQuakersHatBayOverflows() {
     return quakersHatBayOverflows;
  }

  public void setQuakersHatBayOverflows(String quakersHatBayOverflows) {
     this.quakersHatBayOverflows = quakersHatBayOverflows;
  }

  public String getShellyBeachOverflows() {
     return shellyBeachOverflows;
  }

  public void setShellyBeachOverflows(String shellyBeachOverflows) {
     this.shellyBeachOverflows = shellyBeachOverflows;
  }

  public String getLaneCoveTunnelVenting() {
     return laneCoveTunnelVenting;
  }

  public void setLaneCoveTunnelVenting(String laneCoveTunnelVenting) {
     this.laneCoveTunnelVenting = laneCoveTunnelVenting;
  }

  public String getScottsCreekTunnelVenting() {
     return scottsCreekTunnelVenting;
  }

  public void setScottsCreekTunnelVenting(String scottsCreekTunnelVenting) {
     this.scottsCreekTunnelVenting = scottsCreekTunnelVenting;
  }

  public String getTunksParkTunnelVenting() {
     return tunksParkTunnelVenting;
  }

  public void setTunksParkTunnelVenting(String tunksParkTunnelVenting) {
     this.tunksParkTunnelVenting = tunksParkTunnelVenting;
  }

  public String getQuakersHatBayTunnelVenting() {
     return quakersHatBayTunnelVenting;
  }

  public void setQuakersHatBayTunnelVenting(String quakersHatBayTunnelVenting) {
     this.quakersHatBayTunnelVenting = quakersHatBayTunnelVenting;
  }

  public String getShellyBeachTunnelVenting() {
     return shellyBeachTunnelVenting;
  }

  public void setShellyBeachTunnelVenting(String shellyBeachTunnelVenting) {
     this.shellyBeachTunnelVenting = shellyBeachTunnelVenting;
  }

  public int getLaneCoveTotal() {
     return laneCoveTotal;
  }

	public int getScottsCreekTotal() {
		return scottsCreekTotal;
	}

	public void setScottsCreekTotal(int scottsCreekTotal) {
		this.scottsCreekTotal = scottsCreekTotal;
	}

	public int getTunksParkTotal() {
		return tunksParkTotal;
	}

	public void setTunksParkTotal(int tunksParkTotal) {
		this.tunksParkTotal = tunksParkTotal;
	}

	public int getQuakersHatBayTotal() {
		return quakersHatBayTotal;
	}

	public void setQuakersHatBayTotal(int quakersHatBayTotal) {
		this.quakersHatBayTotal = quakersHatBayTotal;
	}

	public int getShellyBeachTotal() {
		return shellyBeachTotal;
	}

	public void setShellyBeachTotal(int shellyBeachTotal) {
		this.shellyBeachTotal = shellyBeachTotal;
	}

	public void setLaneCoveTotal(int laneCoveTotal) {
     this.laneCoveTotal = laneCoveTotal;
  }
  public Calendar getLastUpdatedDateCal() {
     return lastUpdatedDateCal;
  }

  public void setLastUpdatedDateCal(Calendar lastUpdatedDateCal) {
     this.lastUpdatedDateCal = lastUpdatedDateCal;
  }
}