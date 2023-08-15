Grabber Project
The program aggregates all vaccines from a website and put to a database

The system starts according to the schedule - once a minute.

The launch period is specified in the settings - app.properties.

The first site will be career.habr.com.

It has a section https://career.habr.com/vacancies/java_developer.

The program will work with information from this.

The program should read all vacancies related to Java and write them to the database.

Access to the interface will be via the REST API.
Updates

1. New sites can be added to the project without changing the code.

2. In the project, you can do parallel parsing of sites.