package com.lenderman.calinject.db;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.lenderman.calinject.prefs.CalConfiguration;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.RRule;
import nl.knaw.dans.common.dbflib.BooleanValue;
import nl.knaw.dans.common.dbflib.DbfLibException;
import nl.knaw.dans.common.dbflib.NumberValue;
import nl.knaw.dans.common.dbflib.Record;
import nl.knaw.dans.common.dbflib.StringValue;
import nl.knaw.dans.common.dbflib.Table;
import nl.knaw.dans.common.dbflib.Value;

public class DatabaseInjector
{
    // Class Logger
    private static Logger log = LoggerFactory.getLogger(DatabaseInjector.class);

    private static final int MAX_DESCRIPTION_LENGTH = 30;

    // **** Calendar for DOS Data file format ****
    // =================

    // Although the format for the events data file(s) are of the general .DBF
    // type, the fields for these must be precisely defined, in a particular
    // order,
    // as follows:
    //

    /*-
       Name   Type     Length Decimals Description
       ----- --------- ------ -------- -----------
       MONTH  Numeric   2     0        1=Jan, 12=Dec (0=every month if FIXED)
       FIXED  Logical   1              TRUE if same day every year
       DAY    Numeric   2     0        1 to 31
       WEEK   Numeric   1     0        1..6 => week number; 0 = last week
       DOW    Numeric   1     0        0=Sun, 6=Sat
       YEAR   Numeric   4     0        MinYear to MaxYear
       MODE   Character 1              Either blank, "U", or "F".
       DESC   Character 30             Descriptive text displayed
    */

    //
    // The fields are related as follows:
    //
    // Rule #1: If "FIXED" is TRUE, then "DAY" must be entered. "WEEK" and "DOW"
    // are ignored.
    // ^^ Implemented
    //
    // Rule #2: If "FIXED" is FALSE, then "DAY" should usually be zero (0).
    // "WEEK" and "DOW" must be entered. However, if day is within the range
    // 1-6, the otherwise resulting date is "incremented" by this much. See
    // "Election Day" in the sample EVENTS.DBF file for an example of this.
    // ^^ Not implemented, we are calculating recurrences manually, should
    // improve here
    //
    // Rule #3: If "FIXED" is TRUE, and "MONTH" is "0", then the event is
    // highlighted on the same day every month. A special case is setting "DAY"
    // to "31". Then the *last* day of each month is highlighted. (Modified)
    // ^^ Not implemented, we are calculating recurrences manually, should
    // improve here
    //
    // Rule #4: If "YEAR" is "0", then the rules for determining the date is
    // applied every year.
    // ^^ Not implemented, we are calculating recurrences manually, should
    // improve here
    //
    // Rule #5: If "YEAR" is "2", then the rules for determining the date is
    // applied on even years only. See "Election Day" in the sample EVENTS.DBF
    // file for an example of this. (New)
    // ^^ Not implemented, we are calculating recurrences manually, should
    // improve here
    //
    // Rule #6: If "YEAR" is "3", then the rules for determining the date is
    // applied on odd years only. (New)
    // ^^ Not implemented, we are calculating recurrences manually, should
    // improve here
    //
    // Rule #7: If "YEAR" falls within the range MinYear to MaxYear, and "MODE"
    // is either blank or contains the letter 'U', then this indicates a Unique
    // date.
    // ^^ Will implement as "blank"
    //
    // Rule #8: If "YEAR" falls within the range MinYear to MaxYear, and "MODE"
    // contains the letter 'F', then the rules for determining the date is
    // applied from this year Forward.
    // ^^ Not implemented in examples, so no plans to implement
    //
    // Study the sample EVENTS.DBF file to see how events are defined.
    //
    // Look at "New Year's Day". It's a FIXED event, always occurring on the
    // first (DAY=1) of January (MONTH=1).
    //
    // Look at "Thanksgiving Day". It is NOT a fixed event. It falls on the
    // fourth (WEEK=4) Thursday (DOW=4) of November (MONTH=11).
    //
    // "Memorial Day" is different from the other holidays. It falls on the
    // *last* (WEEK=0) Monday (DOW=1) of May (MONTH=5).
    //
    //

    private static Record createDatabaseRecord(Calendar calendar,
            Description description)
    {
        Map<String, Value> databaseRow = new HashMap<String, Value>();

        // All events are fixed in our implementation
        databaseRow.put("FIXED", new BooleanValue(Boolean.TRUE));

        // DAY OF WEEK (always zero since we used fixed dates)
        databaseRow.put("DOW", new NumberValue(0));

        // WEEK (always zero since we used fixed dates)
        databaseRow.put("WEEK", new NumberValue(0));

        // MONTH
        databaseRow.put("MONTH",
                new NumberValue(calendar.get(Calendar.MONTH) + 1));

        // DAY
        databaseRow.put("DAY",
                new NumberValue(calendar.get(Calendar.DAY_OF_MONTH)));

        // Year
        databaseRow.put("YEAR", new NumberValue(calendar.get(Calendar.YEAR)));

        // Mode - Unique Date
        databaseRow.put("MODE", new StringValue(""));

        // Description
        databaseRow.put("DESC",
                new StringValue(description == null ? ""
                        : description.getValue().substring(0,
                                Math.min(description.getValue().length(),
                                        MAX_DESCRIPTION_LENGTH))));

        Record record = new Record(databaseRow);

        log.debug("Calendar Row Contents:");
        databaseRow.forEach((k, v) -> {
            if (v instanceof StringValue)
            {
                log.debug("{} => {}", k, record.getStringValue(k));
            }
            else if (v instanceof NumberValue)
            {
                log.debug("{} => {}", k, record.getNumberValue(k));
            }
            else if (v instanceof BooleanValue)
            {
                log.debug("{} => {}", k, record.getBooleanValue(k));
            }
        });
        log.debug("End Calendar Row Contents");

        return new Record(databaseRow);
    }

    private static List<Record> converCalComponentToDatabaseRecord(
            CalendarComponent component, int recurringEventNumberOfYears)
    {
        List<Record> recordList = new ArrayList<Record>();

        DtStart dtstart = component.getProperties()
                .getProperty(Property.DTSTART);

        if (dtstart != null)
        {
            RRule rrule = component.getProperties().getProperty(Property.RRULE);
            Recur recurrence = null;
            if (rrule != null)
            {
                recurrence = rrule.getRecur();
            }

            Description description = component.getProperties()
                    .getProperty(Property.DESCRIPTION);

            Calendar startDate = Calendar.getInstance();
            startDate.setTime(dtstart.getDate());

            // Rule #1 & Rule #7 Implementation
            if (recurrence == null)
            {
                recordList.add(createDatabaseRecord(startDate, description));
            }
            // We are taking the easy way out and not using the recurrence
            // concept
            // in the DBF format specified.
            // Instead, we are calculating dates for a period of years as
            // defined by
            // the configuration file.
            else
            {
                // Get all dates starting with the start date as specified by
                // recurringEventNumberOfYears years
                Calendar endRecurrence = Calendar.getInstance();
                endRecurrence.setTime(dtstart.getDate());
                endRecurrence.add(Calendar.YEAR, recurringEventNumberOfYears);

                Calendar tempCalendar = Calendar.getInstance();

                recurrence
                        .getDates(new Date(startDate), new Date(endRecurrence),
                                net.fortuna.ical4j.model.parameter.Value.DATE)
                        .forEach(date -> {
                            tempCalendar.setTime(date);
                            recordList.add(createDatabaseRecord(tempCalendar,
                                    description));
                        });
            }
        }
        return recordList;
    }

    public static void injectCalFileToDatabase(CalConfiguration configuration,
            net.fortuna.ical4j.model.Calendar calData) throws Exception
    {
        Table table = new Table(new File(configuration.getDatabaseFileName()));
        table.open();

        calData.getComponents().forEach(component -> {
            converCalComponentToDatabaseRecord(component,
                    configuration.getRecurringEventNumberOfYears())
                            .forEach(record -> {
                                try
                                {
                                    table.addRecord(record);
                                    log.debug("Record written to database");
                                }
                                catch (IOException | DbfLibException e)
                                {
                                    throw new RuntimeException(e);
                                }
                            });
        });
        table.close();
    }
}