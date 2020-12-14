# cal-inject
Calendar injection utility to inject the contents of an ICS file into a DBF file to be used by [cal 1.04 for DOS](https://web.archive.org/web/20060506200416/http://members.aol.com/dosware/cal104.zip)

## Quick Start

### Build project
* `git clone https://github.com/cml37/cal-inject.git`
* `cd cal-inject`
* `gradlew shadowJar`

### Configure project

* You can optionally configure the following configuration options:
  * Edit `cal-inject-prefs.yml` to point to your `databaseFileName`
  * Edit `cal-inject-prefs.yml` to set a `watchPathBase`

### Run Project

* Run `java -jar -Dlog4j.configuration=file:"src/main/java/log4j.properties" build\libs\cal-inject-all.jar`
* Copy `sample.ics` to the "input" directory under your `watchBasePath` (i.e., the default is `watched\input`)
* The file `sample.ics` should move to the "processing" directory under your `watchBasePath` and start to process
* The file `sample.ics` should move to the "output" directory under your `watchBasePath` 
* From there, you can open the `databaseFileName` in a program such as DBF Viewer 2000 and see the added rows from the sample.ics file!
* The application will continue to watch for additional "input" files until terminated

## Known issues and enhancements
* Better error handling (i.e. should we re-attempt processing files stuck in `processing`?)