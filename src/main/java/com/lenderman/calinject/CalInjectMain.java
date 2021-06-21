package com.lenderman.calinject;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.lenderman.calinject.misc.Constants;
import com.lenderman.calinject.misc.ProcessingUtils;
import com.lenderman.calinject.prefs.CalConfiguration;
import com.lenderman.calinject.prefs.CalPrefs;
import com.lenderman.calinject.watcher.CalFileWatcher;

public class CalInjectMain
{
    // Class logger
    private static Logger log = LoggerFactory.getLogger(CalInjectMain.class);

    // Main entry point
    public static void main(String[] args) throws Exception
    {
        log.info("Starting...");

        // Load configuration
        CalConfiguration config = CalPrefs
                .loadConfiguration(args.length >= 1 ? args[0]
                        : Constants.DEFAULT_CONFIG_FILE_NAME);

        // Configure Watcher Paths
        Path watchInputDir = Paths
                .get(config.getWatchPathBase() + Constants.INPUT_PATH_SUFFIX);
        Path watchProcessingDir = Paths.get(
                config.getWatchPathBase() + Constants.PROCESSING_PATH_SUFFIX);
        Path watchOutputDir = Paths
                .get(config.getWatchPathBase() + Constants.OUTPUT_PATH_SUFFIX);

        // Create watcher directories if not present
        watchInputDir.toFile().mkdirs();
        watchProcessingDir.toFile().mkdirs();
        watchOutputDir.toFile().mkdirs();

        // Process any files that were already present before we started this
        // program
        Files.walk(watchInputDir).filter(Files::isRegularFile).forEach(path -> {
            try
            {
                ProcessingUtils.processNewInputFile(config, path,
                        watchProcessingDir, watchOutputDir);
            }
            catch (Exception e)
            {
                log.error("Error processing file {} on startup",
                        path.getFileName(), e);
            }
        });

        // Start the watch loop
        CalFileWatcher.startWatcher(config, watchInputDir, watchProcessingDir,
                watchOutputDir);

        // We should never return from the watch loop, if we do, log an error
        log.error("Watcher was interrupted.  Exiting program!");
    }
}
