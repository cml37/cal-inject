package com.lenderman.calinject.prefs;

public final class CalConfiguration
{
    private String databaseFileName;
    private String watchPathBase;

    public String getDatabaseFileName()
    {
        return databaseFileName;
    }

    public void setDatabaseFileName(String databaseFileName)
    {
        this.databaseFileName = databaseFileName;
    }

    public String getWatchPathBase()
    {
        return watchPathBase;
    }

    public void setWatchPathBase(String watchPathBase)
    {
        this.watchPathBase = watchPathBase;
    }
}