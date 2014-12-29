Requirements :
--------------
- Java 1.8
- MySQL or PostgreSQL database

Configuration :
---------------
First, you need to configure NoteDown to use your database.

If you don't, NoteDown will use a temporary, in memory database, meaning all data will be lost upon application 
termination.

1. Copy `application-mysql.properties` or `application-postgresql.properties` (depending on your database) to
`application.properties`, in the same directory as notedown-0.1.0-RC.jar
2. Edit application.properties and change the following values :
    - spring.datasource.url
    - spring.datasource.username
    - spring.datasource.password

**You're done !**

Run :
--------------
To run the application, just launch the following command :

    java -jar notedown-0.1.0-RC.jar
    
The application should be running within seconds, and listening on http port 8080.