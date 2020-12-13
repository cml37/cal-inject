package com.lenderman.calinject.watcher;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.lenderman.calinject.misc.ProcessingUtils;
import com.lenderman.calinject.prefs.CalConfiguration;

public class CalFileWatcher
{
    // Class Logger
    private static Logger log = LoggerFactory.getLogger(CalFileWatcher.class);

    public static void startWatcher(CalConfiguration config, Path watchInputDir,
            Path watchProcessingDir, Path watchOutputDir) throws Exception
    {
        WatchService watcher = FileSystems.getDefault().newWatchService();

        WatchKey key = watchInputDir.register(watcher,
                StandardWatchEventKinds.ENTRY_CREATE);

        log.info("Starting watcher loop");
        for (;;)
        {
            // wait for key to be signaled
            try
            {
                key = watcher.take();
            }
            catch (InterruptedException x)
            {
                return;
            }

            for (WatchEvent<?> event : key.pollEvents())
            {
                WatchEvent.Kind<?> kind = event.kind();

                if (kind == StandardWatchEventKinds.OVERFLOW)
                {
                    continue;
                }

                WatchEvent<Path> ev = (WatchEvent<Path>) event;
                Path filename = ev.context();

                try
                {
                    ProcessingUtils.processNewInputFile(config,
                            watchInputDir.resolve(filename), watchProcessingDir,
                            watchOutputDir);
                }
                catch (Exception x)
                {
                    log.error("Exception in processing file {}:",
                            filename.getFileName(), x);
                    continue;
                }
            }

            boolean valid = key.reset();
            if (!valid)
            {
                break;
            }
        }
    }
}