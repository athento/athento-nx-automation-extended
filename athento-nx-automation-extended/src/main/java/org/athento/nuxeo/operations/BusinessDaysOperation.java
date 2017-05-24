package org.athento.nuxeo.operations;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.athento.nuxeo.operations.exception.AthentoException;
import org.athento.nuxeo.operations.security.AbstractAthentoOperation;
import org.athento.nuxeo.operations.utils.AthentoOperationsHelper;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.model.Property;
import org.nuxeo.ecm.core.api.model.impl.ListProperty;
import org.nuxeo.ecm.core.schema.types.ListType;
import org.nuxeo.ecm.core.schema.types.Type;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

@Operation(id = BusinessDaysOperation.ID, category = "Athento", label = "Calculate BD", description = "Calculate business days between two given dates")
public class BusinessDaysOperation extends AbstractAthentoOperation {

    public final static String ID = "Athento.BusinessDays";

    @Context
    protected OperationContext ctx;

    @Context
    protected CoreSession session;

    @Param(name = "name")
    protected String name;

    @Param(name = "startDate")
    protected Date startDate;

    @Param(name = "endDate")
    protected Date endDate;

    @Param(name = "year")
    protected String year;

    @Param(name = "region")
    protected String region;

    @OperationMethod
    public void run() throws AthentoException {
        int businessDays = calculateBusinessDays(startDate, endDate, year, region);
        ctx.put(name,businessDays);
    }

    // Dates are not inclusive
    private int calculateBusinessDays(Date startDate, Date endDate, String year, String region) {
        int businessDays = 0;

        if (startDate != null && endDate != null) {

            // Initializing calendars
            Calendar startCal = Calendar.getInstance();
            startCal.setTime(startDate);

            Calendar endCal = Calendar.getInstance();
            endCal.setTime(endDate);

            //In case that dates are swapped, we swap calendars too
            if (startCal.getTimeInMillis() > endCal.getTimeInMillis())
            {
                startCal.setTime(endDate);
                endCal.setTime(startDate);
            }
            List<Calendar> holidays = null;
            try{
                holidays = getHolidays( year, region);
            }catch(OperationException e){
                _log.error(e);
                holidays = new ArrayList<>();
            }

            while (startCal.getTimeInMillis() <= endCal.getTimeInMillis())
            {
                if (startCal.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY && startCal.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY)
                {
                    if(!isHoliday(startCal.getTime(), holidays)) {
                        businessDays++;
                    }
                }
                startCal.add(Calendar.DAY_OF_MONTH, 1);
            }
        }

        return businessDays;
    }

    private List<Calendar> getHolidays(String year, String region ) throws OperationException{
        List<Calendar> holidays = new ArrayList<Calendar>();

        if(!nullOrEmpty(year) && !nullOrEmpty(region)){
            try {
                String operationId = "Athento.Document.Query";
                String query = "SELECT * FROM Holidays_Cal WHERE holidayscr:Year='" + year + "' AND holidayscr:Region='"+ region +"' AND ecm:mixinType != 'HiddenInNavigation' AND ecm:isProxy = 0 AND ecm:isCheckedInVersion = 0 AND ecm:currentLifeCycleState != 'deleted'";
                Object input = null;
                Map<String, Object> params = new HashMap<>();
                params.put("query", query);
                Object retValue = AthentoOperationsHelper.runOperation(operationId, input, params, session);
                DocumentModelList queryRes = null;
                if (retValue instanceof DocumentModelList) {
                    queryRes = (DocumentModelList) retValue;
                    if(!queryRes.isEmpty()){
                        Property prop = queryRes.get(0).getProperty("Holidays");
                        Type type = prop.getType();
                        Serializable value = prop.getValue();
                        if(type.isListType()){
                            type = ((ListType) type).getFieldType();
                            Collection<Object> values;
                            if (type.isComplexType()) {
                                values = (Collection) ((ListProperty) prop).getChildren();
                            } else if (value instanceof Object[]) {
                                values = Arrays.asList((Object[]) value);
                            } else if (value instanceof List<?>) {
                                values = (List<Object>) value;
                            } else {
                                throw new AthentoException("Unknown value type: " + value.getClass().getName());
                            }

                            Calendar aux = null;
                            for (Object o: values) {
                                aux = (Calendar) o;
                                holidays.add(aux);
                            }
                        }
                    }
                } else {
                    _log.error("Unexpected return type [" + retValue.getClass()
                            + "] for operation: " + operationId);
                    holidays = new ArrayList<>();
                }
            } catch (OperationException e) {
                holidays = new ArrayList<>();
                _log.error("Business days Error--> " + e);
            }
        }

        return holidays;
    }

    private boolean isHoliday(Date date, List<Calendar> holidays){
        boolean isHoliday = false;

        if(holidays != null){
            for(Calendar holiday : holidays){
                if(isSameDay(date,holiday.getTime())){
                    isHoliday = true;
                    break;
                }
            }
        }

        return isHoliday;
    }

    private boolean isSameDay(Date d1, Date d2){
        SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
        return fmt.format(d1).equals(fmt.format(d2));
    }

    private boolean nullOrEmpty(String string){
        return string != null ? string.isEmpty() : true;
    }
    private static final Log _log = LogFactory
            .getLog(AthentoDocumentQueryOperation.class);

}