package com.lenderman.calinject.prefs;

public final class CalConfiguration
{
    private String databaseFileName;
    private String watchPathBase;
    private int recurringEventNumberOfYears;

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

    public int getRecurringEventNumberOfYears()
    {
        return recurringEventNumberOfYears;
    }

    public void setRecurringEventNumberOfYears(int recurringEventNumberOfYears)
    {
        this.recurringEventNumberOfYears = recurringEventNumberOfYears;
    }
}