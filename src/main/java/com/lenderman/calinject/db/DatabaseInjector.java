package com.lenderman.calinject.db;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Calendar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.lenderman.calinject.prefs.CalConfiguration;
import com.linuxense.javadbf.DBFReader;
import com.linuxense.javadbf.DBFWriter;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.DtStart;

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

    private static Object[] converCalComponentToDatabaseRow(
            CalendarComponent component)
    {
        Object[] databaseRow = new Object[8];

        DtStart dtstart = component.getProperties()
                .getProperty(Property.DTSTART);
        Description description = component.getProperties()
                .getProperty(Property.DESCRIPTION);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dtstart.getDate());

        // TODO incorporate logic from the comments into the below
        // MONTH
        databaseRow[0] = calendar.get(Calendar.MONTH) + 1;

        // FIXED
        databaseRow[1] = Boolean.TRUE;

        // DAY
        databaseRow[2] = calendar.get(Calendar.DAY_OF_MONTH);

        // WEEK
        databaseRow[3] = calendar.get(Calendar.WEEK_OF_MONTH);

        // Day of Week
        databaseRow[4] = calendar.get(Calendar.DAY_OF_WEEK) - 1;

        // Year
        databaseRow[5] = 0;

        // Mode
        databaseRow[6] = "";

        // Description
        databaseRow[7] = description.getValue();

        log.debug("Calendar Row Contents:");
        Arrays.asList(databaseRow).stream()
                .forEach(row -> log.debug("Calendar Row: {}", row));
        log.debug("End Calendar Row Contents");

        return databaseRow;
    }

    public static void injectCalFileToDatabase(CalConfiguration configuration,
            net.fortuna.ical4j.model.Calendar calData) throws Exception
    {
        DBFReader reader = new DBFReader(
                new FileInputStream(configuration.getDatabaseFileName()));
        Charset charset = reader.getCharset();
        log.debug("Database charset: {}", charset);
        reader.close();
        DBFWriter writer = new DBFWriter(
                new File(configuration.getDatabaseFileName()), charset);
        calData.getComponents().forEach(component -> writer
                .addRecord(converCalComponentToDatabaseRow(component)));
        log.debug("Record written to database");
        writer.close();
    }
}