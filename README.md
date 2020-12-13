# cal-inject
Calendar injection utility to inject the contents of an ICS file into a DBF file to be used by [cal 1.04 for DOS](https://web.archive.org/web/20060506200416/http://members.aol.com/dosware/cal104.zip)

## Quick Start

### Build project
* `git clone https://github.com/cml37/cal-inject.git`
* `cd cal-inject`
* `mvn clean install assembly:single `

### Configure project

* You can optionally configure the following configuration options:
  * Edit `cal-inject-prefs.yml` to point to your `databaseFileName`
  * Edit `cal-inject-prefs.yml` to set a `watchPathBase`

### Run Project

* Run `java -jar target\calinject-1.0-SNAPSHOT-jar-with-dependencies.jar`
* Copy `sample.ics` to the "input" directory under your `watchBasePath` (i.e., the default is `watched\input`)
* The file `sample.ics` should move to the "processing" directory under your `watchBasePath` and start to process
* The file `sample.ics` should move to the "output" directory under your `watchBasePath` 
* From there, you can open the `databaseFileName` in a program such as DBF Viewer 2000 and see the added rows from the sample.ics file!

## Known issues and enhancements
* On first edit of the `EVENTS.DBF` file included from the cal 1.04 program, the database record rows do not insert properly
  * Using DBF Viewer 2000 to remove the bad rows fixes the issues, and from there, the program does generate rows properly
  * The sample file included in this git repository has been corrected and does not require additional modification
* log4j configuration needs updated to pick up the log4j.properties file when run as an executable JAR
* Better configuration to ease generation of executable JAR
* Better error handling (i.e. should we re-attempt processing files stuck in `processing`?)