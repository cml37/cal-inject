# cal-inject
Calendar injection utility to inject the contents of an ICS file into a DBF file to be used by [cal 1.04 for DOS|https://web.archive.org/web/20060506200416/http://members.aol.com/dosware/cal104.zip]

## Quick Start

### Build project

`mvn clean install assembly:single `

### Configure project

Edit `cal-inject-prefs.yml` to point to your `databaseFileName`
Edit `cal-inject-prefs.yml` to set a `watchPathBase`

### Run Project

java -jar target\calinject-1.0-SNAPSHOT-jar-with-dependencies.jar

copy `sample.ics` to the "input" directory under your `watchBasePath` (i.e., the default is `watched\input`)

From there, you can open the `databaseFileName` in a program such as DBF Viewer 2000 and see the added rows from the sample.ics file!

## Known issues

* log4j configuration needs updated to pick up the log4j.properties file
* better configuration to ease generation of executable JAR
* better error handling (i.e. should we re-attempt processing files stuck in `processing`?)