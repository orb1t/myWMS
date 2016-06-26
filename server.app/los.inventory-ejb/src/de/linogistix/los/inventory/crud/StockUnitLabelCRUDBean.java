/*
 * UserCRUDBean.java
 *
 * Created on 20.02.2007, 18:37:29
 *
 * Copyright (c) 2006/2007 LinogistiX GmbH. All rights reserved.
 *
 * <a href="http://www.linogistix.com/">browse for licence information</a>
 *
 */

package de.linogistix.los.inventory.crud;

import de.linogistix.los.crud.BusinessObjectCRUDBean;
import de.linogistix.los.inventory.model.StockUnitLabel;
import de.linogistix.los.inventory.service.StockUnitLabelService;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import org.mywms.service.BasicService;


/**
 * @author trautm
 *
 */
@Stateless
public class StockUnitLabelCRUDBean extends BusinessObjectCRUDBean<StockUnitLabel> implements StockUnitLabelCRUDRemote {

	@EJB 
	StockUnitLabelService service;
	
	@Override
	protected BasicService<StockUnitLabel> getBasicService() {
		
		return service;
	}
}
