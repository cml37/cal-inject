package com.lenderman.calinject.db;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.lenderman.calinject.prefs.CalConfiguration;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.DtStart;
import nl.knaw.dans.common.dbflib.BooleanValue;
import nl.knaw.dans.common.dbflib.DateValue;
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

    // Data file format:
    // =================
    // Although the format for the events data file(s) are of the general .DBF
    // type, the fields for these must be precisely defined, in a particular
    // order,
    // as follows:
    //
    // Name Type Length Decimals Description
    // ----- --------- ------ -------- -----------
    // MONTH Numeric 2 0 1=Jan, 12=Dec (0=every month if FIXED)
    // FIXED Logical 1 TRUE if same day every year
    // DAY Numeric 2 0 1 to 31
    // WEEK Numeric 1 0 1..6 => week number; 0 = last week
    // DOW Numeric 1 0 0=Sun, 6=Sat
    // YEAR Numeric 4 0 MinYear to MaxYear
    // MODE Character 1 Either blank, "U", or "F".
    // DESC Character 30 Descriptive text displayed
    //
    // The fields are related as follows:
    //
    // If "FIXED" is TRUE, then "DAY" must be entered. "WEEK" and "DOW" are
    // ignored.
    //
    // If "FIXED" is FALSE, then "DAY" should usually be zero (0). "WEEK" and
    // "DOW" must be entered. However, if day is within the range 1-6, the
    // otherwise resulting date is "incremented" by this much. See "Election
    // Day" in the sample EVENTS.DBF file for an example of this.
    //
    // If "FIXED" is TRUE, and "MONTH" is "0", then the event is highlighted on
    // the same day every month. A special case is setting "DAY" to "31". Then
    // the *last* day of each month is highlighted. (Modified)
    //
    // If "YEAR" is "0", then the rules for determining the date is applied
    // every year.
    //
    // If "YEAR" is "2", then the rules for determining the date is applied on
    // even years only. See "Election Day" in the sample EVENTS.DBF file for an
    // example of this. (New)
    //
    // If "YEAR" is "3", then the rules for determining the date is applied on
    // odd years only. (New)
    //
    // If "YEAR" falls within the range MinYear to MaxYear, and "MODE" is either
    // blank or contains the letter 'U', then this indicates a Unique date.
    //
    // If "YEAR" falls within the range MinYear to MaxYear, and "MODE" contains
    // the letter 'F', then the rules for determining the date is applied from
    // this year Forward.
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
    private static Record converCalComponentToDatabaseRecord(
            CalendarComponent component)
    {

        Map<String, Value> databaseRow = new HashMap<String, Value>();

        DtStart dtstart = component.getProperties()
                .getProperty(Property.DTSTART);
        Description description = component.getProperties()
                .getProperty(Property.DESCRIPTION);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dtstart.getDate());

        // TODO incorporate logic from the comments into the below
        // MONTH
        databaseRow.put("MONTH",
                new NumberValue(calendar.get(Calendar.MONTH) + 1));

        // FIXED
        databaseRow.put("FIXED", new BooleanValue(Boolean.TRUE));

        // DAY
        databaseRow.put("DAY",
                new NumberValue(calendar.get(Calendar.DAY_OF_MONTH)));

        // WEEK
        databaseRow.put("WEEK",
                new NumberValue(calendar.get(Calendar.WEEK_OF_MONTH)));

        // Day of Week
        databaseRow.put("DOW",
                new NumberValue(calendar.get(Calendar.DAY_OF_WEEK) - 1));

        // Year
        databaseRow.put("YEAR", new NumberValue(0));

        // Mode
        databaseRow.put("MODE", new StringValue(""));

        // Description
        databaseRow.put("DESC", new StringValue(description.getValue()
                .substring(0, Math.min(description.getValue().length(), 30))));

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
            else if (v instanceof DateValue)
            {
                log.debug("{} => {}", k, record.getDateValue(k));
            }
        });
        log.debug("End Calendar Row Contents");

        return new Record(databaseRow);
    }

    public static void injectCalFileToDatabase(CalConfiguration configuration,
            net.fortuna.ical4j.model.Calendar calData) throws Exception
    {
        Table table = new Table(new File(configuration.getDatabaseFileName()));
        table.open();

        calData.getComponents().forEach(component -> {
            try
            {
                table.addRecord(converCalComponentToDatabaseRecord(component));
            }
            catch (IOException | DbfLibException e)
            {
                throw new RuntimeException(e);
            }
        });
        log.debug("Record written to database");

    }
}