package com.lenderman.calinject.prefs;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.yaml.snakeyaml.Yaml;

public class CalPrefs
{
    public static CalConfiguration loadConfiguration(String fileName)
            throws Exception
    {
        Yaml yaml = new Yaml();
        InputStream in = Files.newInputStream(Paths.get(fileName));
        {
            return yaml.loadAs(in, CalConfiguration.class);
        }
    }
}
