# oracle_r2dbc_perftest
Performance test of Oracle R2DBC driver (when available)

#Preparations

Create DB:
https://github.com/oracle/docker-images/tree/main/OracleDatabase/SingleInstance

Download installer LINUX.X64_193000_db_home.zip and put in docker-images/OracleDatabase/SingleInstance/dockerfiles/19.3.0/

Build the image
./buildContainerImage.sh -v 19.3.0 -s

Allow connecting to the container using 127.0.0.1/localhost by taking into account the OOB issue:
- for Java: https://stackoverflow.com/questions/16918024/is-there-a-solution-to-jdbc-driver-bug-in-out-of-band-breaks -> <spring-boot.run.jvmArguments>-Doracle.net.disableOob=true</spring-boot.run.jvmArguments>
- for SQLDeveloper: https://thtechnology.com/2015/08/01/sql-developer-dropping-connections-solved/

As sys
ALTER SESSION SET CONTAINER = orclpdb1;
create user testuser identified by "testuser" CONTAINER=CURRENT;
grant connect, resource, dba to testuser

As testuser
CREATE TABLE testtable (
    id          NUMBER PRIMARY KEY,
    ts          TIMESTAMP DEFAULT on null current_timestamp
);

#Simple tests with Curl

POST
curl -d '{"id":1}' -H 'Content-Type: application/json' http://localhost:8080/entry

GET
curl localhost:8080/entry/1
