package com.ordermgr.ofbiz.order;

import java.util.*;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.math.BigDecimal;

import org.apache.ofbiz.base.util.UtilMisc;
import org.apache.ofbiz.base.util.UtilDateTime;
import org.apache.ofbiz.service.LocalDispatcher;
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

    public static Map<String, Object> createOrder(DispatchContext dctx, Map<String, Object> context) {
        // Method implementation here
        Delegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        Locale locale = (Locale) context.get("locale");

        String customerId = (String) context.get("customerId");
        String productId = (String) context.get("productId");
        BigDecimal quantity = new BigDecimal(context.get("quantity").toString());
        String address1 = (String) context.get("address1");
        String address2 = (String) context.get("address2");
        String city = (String) context.get("city");
        String state = (String) context.get("state");
        String postalCode = (String) context.get("postalCode");
        String country = (String) context.get("country");
        String paymentMethodTypeId = (String) context.get("paymentMethodType");

        // Validate required fields
        if (UtilValidate.isEmpty(customerId) || UtilValidate.isEmpty(productId) || quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            return ServiceUtil.returnError("Missing or invalid required parameters.");
        }

        try {
            // Validate Customer
            GenericValue customer = EntityQuery.use(delegator).from("Party").where("partyId", customerId).queryOne();
            if (customer == null) {
                return ServiceUtil.returnError("Invalid customer ID: " + customerId);
            }

            // Validate Product
            GenericValue product = EntityQuery.use(delegator).from("Product").where("productId", productId).queryOne();
            if (product == null) {
                return ServiceUtil.returnError("Invalid product ID: " + productId);
            }

            // Generate Order ID
            String orderId = delegator.getNextSeqId("OrderHeader");
            Timestamp now = UtilDateTime.nowTimestamp();

            // Create Order Header
            GenericValue orderHeader = delegator.makeValue("OrderHeader",
                    UtilMisc.toMap("orderId", orderId, "orderTypeId", "SALES_ORDER", "orderDate", now,
                            "statusId", "ORDER_CREATED"));
            delegator.create(orderHeader);

            // Create Order Item
            GenericValue orderItem = delegator.makeValue("OrderItem",
                    UtilMisc.toMap("orderId", orderId, "orderItemSeqId", delegator.getNextSeqId("OrderItem"),
                            "productId", productId, "quantity", quantity,
                            "statusId", "ITEM_CREATED"));
            delegator.create(orderItem);

            // Create Order Role
            GenericValue orderRole = delegator.makeValue("OrderRole",
                    UtilMisc.toMap("orderId", orderId, "roleTypeId", "CUSTOMER", "partyId", customerId));
            delegator.create(orderRole);

            // Create a contact mech
            String contactMechId = delegator.getNextSeqId("ContactMech");
            GenericValue contactMech = delegator.makeValue("ContactMech",
                    UtilMisc.toMap("contactMechId", contactMechId,
                            "contactMechTypeId", "POSTAL_ADDRESS"));
            delegator.create(contactMech);

            // Create Order Contact Mechanism (Shipping Address)
            GenericValue orderContactMech = delegator.makeValue("OrderContactMech",
                    UtilMisc.toMap("orderId", orderId, "contactMechPurposeTypeId", "SHIPPING_LOCATION", "contactMechId", contactMechId));
            delegator.create(orderContactMech);

            // Create Postal Address
            GenericValue postalAddress = delegator.makeValue("PostalAddress",
                    UtilMisc.toMap("contactMechId", contactMechId,
                            "address1", address1, "address2", address2, "city", city,
                            "stateProvinceGeoId", state, "postalCode", postalCode, "countryGeoId", country));
            delegator.create(postalAddress);

            // Order Payment Preference
            String orderPaymentPreferenceId = delegator.getNextSeqId("OrderPaymentPreference");
            GenericValue paymentPreference = delegator.makeValue("OrderPaymentPreference",
                    UtilMisc.toMap("orderPaymentPreferenceId", orderPaymentPreferenceId,
                            "orderId", orderId, "paymentMethodTypeId", paymentMethodTypeId));
            delegator.create(paymentPreference);

            // Store orderId in response
            Map<String, Object> result = ServiceUtil.returnSuccess();
            result.put("orderId", orderId);
            return result;

        } catch (GenericEntityException e) {
            return ServiceUtil.returnError("Error creating order: " + e.getMessage());
        }
    }

    public static Map<String, Object> updateOrder(DispatchContext dctx, Map<String, ? extends Object> context) {
        Delegator delegator = dctx.getDelegator();

        // Extract input parameters
        String orderId = (String) context.get("orderId");
        String address1 = (String) context.get("address1");
        String address2 = (String) context.get("address2");
        String city = (String) context.get("city");
        String state = (String) context.get("state");
        String postalCode = (String) context.get("postalCode");
        String country = (String) context.get("country");

        // Validate required fields
        if (UtilValidate.isEmpty(orderId)) {
            return ServiceUtil.returnError("Missing required parameter: orderId.");
        }

        try {
            // Check if the order exists
            GenericValue orderHeader = EntityQuery.use(delegator)
                    .from("OrderHeader")
                    .where("orderId", orderId)
                    .queryOne();

            if (orderHeader == null) {
                return ServiceUtil.returnError("Order with ID " + orderId + " not found.");
            }

            // Fetch the associated contact mechanism (Shipping Address)
            GenericValue orderContactMech = EntityQuery.use(delegator)
                    .from("OrderContactMech")
                    .where("orderId", orderId, "contactMechPurposeTypeId", "SHIPPING_LOCATION")
                    .queryFirst();

            if (orderContactMech == null) {
                return ServiceUtil.returnError("No shipping address found for order ID " + orderId);
            }

            // Get the contactMechId to update the PostalAddress
            String contactMechId = orderContactMech.getString("contactMechId");

            GenericValue postalAddress = EntityQuery.use(delegator)
                    .from("PostalAddress")
                    .where("contactMechId", contactMechId)
                    .queryOne();

            if (postalAddress == null) {
                return ServiceUtil.returnError("No Postal Address found for contactMechId " + contactMechId);
            }

            // Update the shipping address
            postalAddress.set("address1", address1);
            postalAddress.set("address2", address2);
            postalAddress.set("city", city);
            postalAddress.set("stateProvinceGeoId", state);
            postalAddress.set("postalCode", postalCode);
            postalAddress.set("countryGeoId", country);
            postalAddress.store();

            Map<String, Object> result = ServiceUtil.returnSuccess();
            result.put("orderId", orderId);
            return result;
        } catch (GenericEntityException e) {
            return ServiceUtil.returnError("Error updating order: " + e.getMessage());
        }
    }
}