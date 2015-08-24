SEDDI is a cross platform, cross database, and even _cross dump_ importer for the StackExchange Data Dumps. 
Using the power of Java and the Hibernate ORM layer SEDDI allows you to import a dump from 2010 to SQL Server from Linux, the most recent dump to MySQL from Windows, or any other combination

Features include
 * Runs on any operation system that can run Java
 * Supports any database that has a JDBC driver and a Hibernate dialect
 * Flexible configuration allows custom mappings of data dump fields to database fields
 * *Native 7z archive support* - Import directly from the downloaded archive, no extraction needed!
 * Multithreading support
 * Batch imports

== Status ==

Most of the work has been completed. Remaining tasks
 * Replacing sevenzipjbining with soon to be released LZMA 7z extraction from Apache Commons Compress. Sevenzipjbining is just too fragile and relies on native libraries, causing frequent and unexplained JVM crashes ( http://sourceforge.net/p/sevenzipjbind/bugs/15/ )
 * Add support for loading custom mappings
 * Add support for selecting from a list of prebuilt custom mappings for the various existing data dumps
