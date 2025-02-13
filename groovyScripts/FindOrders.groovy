import java.util.*
import java.sql.Timestamp
import org.apache.ofbiz.base.util.Debug;

module = 'FindOrders.groovy';

currentOrderId = request.getParameter("orderId");
Debug.logInfo("--------${currentOrderId}-----------", module);