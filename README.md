# Toy statistics generator for (Apache) formatted access logs

This code generates statistics from (Apache) formatted access logs. Specifically it generates:
* for each referrer domain it counts the number of unique IPs / day
* finds all the unique URLs in the logs
* finds all unique user agents in the logs

## Usage:

There are two implementations provided:
* one which loads all the logs into memory, generates the statistics and writes the result to standard out
* one which loads the logs into a (PostgreSQL) database and then runs queries against it to generate the final report

To run the in-memory variant:
* mvn package
* `java -Xmx8G -cp ./in-memory/target/in-memory-1.0-SNAPSHOT.jar net.greypanther.logparser.inmemory.Main [file(s) to parse]`
* the `[file(s) to parse]` can be one or more files (provided via their paths or using the `file://` URL scheme) or URLs (in which case it will stream the file from the given URL)
* if the filename / URL ends in `.gz`, the program will transparently decompress it
* output and logging is printed to standard out
* expected runtime: ~5mins for 10 files
* *Note*: the "unique IPs / day" report considers the "day" in the local timezone

To run the variant using DB:
* ensure that you have a running PostgreSQL server and a table in it with the following schema:
  ````
  CREATE TABLE access_log
  (
    host inet,
    "timestamp" timestamp without time zone,
    url text,
    referrer text,
    referrer_domain text,
    user_agent text
  )
  WITH (
    OIDS=FALSE
  );

* Update the `ConnectionSource` class in the `db-common` module so that it has the right server / user / password / database URL to connect to the above database
* mvn package
* import the files into the database: \
  `java -cp ./db-loader/target/db-loader-1.0-SNAPSHOT.jar net.greypanther.logparser.db.loader.Main [file(s) to parse]`
  * the `[file(s) to parse]` can be one or more files (provided via their paths or using the `file://` URL scheme) or URLs (in which case it will stream the file from the given URL)
  * if the filename / URL ends in `.gz`, the program will transparently decompress it
  * output and logging is printed to standard out
  * expected runtime: ~2.5 hours for 10 files
* generate the report from the imported data: \
  `java -cp ./db-report/target/db-report-1.0-SNAPSHOT.jar net.greypanther.logparser.db.report.Main`
  * expected runtime: < 1min

## Implementation details / technical discussion

The parser uses the [httpdlog-parser](https://github.com/nielsbasjes/logparser) library as a first step for parsing and then uses the built-in java date/time classes and URI/URL classes for further parsing. It also uses Google Guava to extract the domains from the host names of the referrer URLs (so that it extracts `example.co.uk` from `www.example.co.uk` instead of `co.uk`). This last step is based on [the public suffix list](https://publicsuffix.org/) and will need periodic updating.

For database access it Spring JdbcTemplate and for logging it uses SLF4J.

For this problem in particular it probably makes more sense to go with the "parse everything at once and generate the statistics in memory" since it has a better runtime and gives more flexibility (for example if we fix some of the parse errors, we can just re-run the process and have the previously excluded lines be included).

Comparatively, the database solution takes longer, needs duplicate storage (since we now store the data in the logfiles and in the database) and provides little value for ad-hoc queries. Also, if we introduce some improvement (for example update the public suffix list or fix some of the parsing issues), it is complicated to selectively update the correct rows in the table. Also, there are potential problems if the load job is interrupted (ie. partial data in the DB). This could be avoided by wrapping the inserts for each file into one transaction, however that has its own issues (the DB server needs more space in its WA logs to store the data temporarily until the transaction commits and using long-lived transactions can limit concurrency).

Finally, having a database means that we need some operational resources (ie. storage, CPU, memory and someone to monitor those, backup the DB periodically, etc).

## Known issues / possible improvements

* There are less than 0.5% of lines (16 932) which could not be parsed. Most of it seems to be due to them having multiple IPs in the "client IP" are: \
  `106.215.179.80, 37.228.104.86 -  -  [05/Apr/2015:13:49:32 +0000] "GET /v1/i...`
  * we need to understand why this is happening (are those clients coming through some kind of proxy?) and account for it

* The database table could be [partitioned by date on Postgres](http://www.postgresql.org/docs/9.4/static/ddl-partitioning.html) for better query performance

