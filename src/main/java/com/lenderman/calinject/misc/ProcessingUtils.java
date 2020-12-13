package com.lenderman.calinject.misc;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.lenderman.calinject.db.DatabaseInjector;
import com.lenderman.calinject.prefs.CalConfiguration;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Calendar;

public class ProcessingUtils
{
    // Class Logger
    private static Logger log = LoggerFactory.getLogger(ProcessingUtils.class);

    public static void processNewInputFile(CalConfiguration config, Path child,
            Path watchProcessingDir, Path watchOutputDir) throws Exception
    {
        log.debug("Processing file {}", child.getFileName());
        Calendar calendar = new CalendarBuilder()
                .build(Files.newInputStream(child));
        log.debug("Moving file to processed: {}", child.getFileName());
        Path processingFile = Files.move(child,
                watchProcessingDir.resolve(child.getFileName()),
                StandardCopyOption.REPLACE_EXISTING);
        DatabaseInjector.injectCalFileToDatabase(config, calendar);
        log.debug("Moving file to done: {}", child.getFileName());
        Files.move(processingFile,
                watchOutputDir.resolve(processingFile.getFileName()),
                StandardCopyOption.REPLACE_EXISTING);
    }
}
