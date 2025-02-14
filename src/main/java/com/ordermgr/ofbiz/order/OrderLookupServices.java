package com.ordermgr.ofbiz.order;

import java.util.*;

import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.base.util.UtilValidate;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.GenericEntityException;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.entity.condition.EntityCondition;
import org.apache.ofbiz.entity.condition.EntityConditionList;
import org.apache.ofbiz.entity.condition.EntityExpr;
import org.apache.ofbiz.entity.condition.EntityOperator;
import org.apache.ofbiz.service.DispatchContext;
import org.apache.ofbiz.service.ServiceUtil;
import org.apache.ofbiz.entity.util.EntityQuery;

public class OrderLookupServices {
    public static final String module = OrderLookupServices.class.getName();

    public static Map<String, Object> findOrders(DispatchContext dctx, Map<String, ? extends Object> context) {
        Delegator delegator = dctx.getDelegator();
        List<EntityCondition> conditions = new LinkedList<>();

        // Retrieve parameters from the context
        String orderId = (String) context.get("orderId");
        String statusId = (String) context.get("statusId");
        String orderDate = (String) context.get("orderDate");
        String firstName = (String) context.get("firstName");
        String lastName = (String) context.get("lastName");
        String paymentStatusId = (String) context.get("paymentStatusId");
        String address1 = (String) context.get("address1");
        String address2 = (String) context.get("address2");
        String city = (String) context.get("city");
        String postalCode = (String) context.get("postalCode");
        String stateProvinceGeoId = (String) context.get("stateProvinceGeoId");
        String countryGeoId = (String) context.get("countryGeoId");

        // Add conditions based on the input parameters
        if (UtilValidate.isNotEmpty(orderId)) {
            conditions.add(EntityCondition.makeCondition("orderId", EntityOperator.EQUALS, orderId));
        }
        if (UtilValidate.isNotEmpty(statusId)) {
            conditions.add(EntityCondition.makeCondition("statusId", EntityOperator.EQUALS, statusId));
        }
        if (UtilValidate.isNotEmpty(orderDate)) {
            conditions.add(EntityCondition.makeCondition("orderDate", EntityOperator.EQUALS, orderDate));
        }
        if (UtilValidate.isNotEmpty(firstName)) {
            conditions.add(EntityCondition.makeCondition("firstName", EntityOperator.LIKE, "%" + firstName + "%"));
        }
        if (UtilValidate.isNotEmpty(lastName)) {
            conditions.add(EntityCondition.makeCondition("lastName", EntityOperator.LIKE, "%" + lastName + "%"));
        }
        if (UtilValidate.isNotEmpty(paymentStatusId)) {
            conditions.add(EntityCondition.makeCondition("paymentStatusId", EntityOperator.EQUALS, paymentStatusId));
        }
        if (UtilValidate.isNotEmpty(address1)) {
            conditions.add(EntityCondition.makeCondition("address1", EntityOperator.LIKE, "%" + address1 + "%"));
        }
        if (UtilValidate.isNotEmpty(address2)) {
            conditions.add(EntityCondition.makeCondition("address2", EntityOperator.LIKE, "%" + address2 + "%"));
        }
        if (UtilValidate.isNotEmpty(city)) {
            conditions.add(EntityCondition.makeCondition("city", EntityOperator.LIKE, "%" + city + "%"));
        }
        if (UtilValidate.isNotEmpty(postalCode)) {
            conditions.add(EntityCondition.makeCondition("postalCode", EntityOperator.EQUALS, postalCode));
        }
        if (UtilValidate.isNotEmpty(stateProvinceGeoId)) {
            conditions.add(EntityCondition.makeCondition("stateProvinceGeoId", EntityOperator.EQUALS, stateProvinceGeoId));
        }
        if (UtilValidate.isNotEmpty(countryGeoId)) {
            conditions.add(EntityCondition.makeCondition("countryGeoId", EntityOperator.EQUALS, countryGeoId));
        }

        List<GenericValue> orderList = new ArrayList<>();
        try {
            Debug.logInfo("Conditions for FindOrderView query: " + orderList, module);
            orderList = EntityQuery.use(delegator).from("FindOrderView").where(EntityCondition.makeCondition(conditions, EntityOperator.AND)).queryList();
            Debug.logInfo("Conditions for FindOrderView query: " + orderList, module);
        } catch (GenericEntityException e) {
            Debug.logError(e, "Error finding orders", module);
            return ServiceUtil.returnError("Error retrieving orders: " + e.getMessage());
        }

        // Create result map
        Map<String, Object> result = ServiceUtil.returnSuccess();
        result.put("orderList", orderList);
        return result;
    }
}