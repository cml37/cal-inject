package com.lenderman.calinject.db;

import java.io.File;
import java.util.Arrays;
import java.util.Calendar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.lenderman.calinject.prefs.CalConfiguration;
import com.linuxense.javadbf.DBFWriter;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.DtStart;

public class DatabaseInjector
{
    // Class Logger
    private static Logger log = LoggerFactory.getLogger(DatabaseInjector.class);

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

        // MONTH
        databaseRow[0] = calendar.get(Calendar.MONTH) + 1;

        // FIXED
        databaseRow[1] = Boolean.TRUE;

        // DAY
        databaseRow[2] = calendar.get(Calendar.DAY_OF_MONTH);

        // WEEK
        databaseRow[3] = calendar.get(Calendar.WEEK_OF_YEAR) - 1;

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
        log.debug("End Calendar Row Contents:");

        return databaseRow;
    }

    public static void injectCalFileToDatabase(CalConfiguration configuration,
            net.fortuna.ical4j.model.Calendar calData) throws Exception
    {
        DBFWriter writer = new DBFWriter(
                new File(configuration.getDatabaseFileName()));
        calData.getComponents().forEach(component -> writer
                .addRecord(converCalComponentToDatabaseRow(component)));
        log.debug("Record written to database");
        writer.close();
    }
}