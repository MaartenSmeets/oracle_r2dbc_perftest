# oracle_r2dbc_perftest
Performance test of Oracle R2DBC driver (when available)

## Preparations

### Create DB
https://github.com/oracle/docker-images/tree/main/OracleDatabase/SingleInstance

Download installer LINUX.X64_193000_db_home.zip and put in docker-images/OracleDatabase/SingleInstance/dockerfiles/19.3.0/

Build the image
./buildContainerImage.sh -v 19.3.0 -s

Prepare a directory for the DB datafiles
mkdir /home/maarten/oradata
chmod a+xrw /home/maarten/oradata

Run the container
docker run --name oracledb \
-p 1521:1521 -p 5500:5500 \
-e ORACLE_SID=ORCLCDB \
-e ORACLE_PDB=ORCLPDB1 \
-e ORACLE_PWD=Welcome01 \
-e ORACLE_EDITION=standard \
-e ORACLE_CHARACTERSET=AL32UTF8 \
-v /home/maarten/oradata:/opt/oracle/oradata \
oracle/database:19.3.0-se2

ctrl-c to stop or docker stop
docker start oracledb

Access the DB using SQLPlus from within the container itself
docker exec -ti oracledb sqlplus pdbadmin@ORCLPDB1

## Allow connecting
Allow connecting to the container using 127.0.0.1/localhost by taking into account the OOB issue:
- for Java: https://stackoverflow.com/questions/16918024/is-there-a-solution-to-jdbc-driver-bug-in-out-of-band-breaks -> <spring-boot.run.jvmArguments>-Doracle.net.disableOob=true</spring-boot.run.jvmArguments>
- for SQLDeveloper: https://thtechnology.com/2015/08/01/sql-developer-dropping-connections-solved/

## Prepare DB contents

As sys
ALTER SESSION SET CONTAINER = orclpdb1;
create user testuser identified by "testuser" CONTAINER=CURRENT;
grant connect, resource, dba to testuser

As testuser
CREATE TABLE testtable (
    id          NUMBER PRIMARY KEY,
    ts          TIMESTAMP DEFAULT on null current_timestamp
);

## Simple Spring Boot service tests with Curl

POST
curl -d '{"id":1}' -H 'Content-Type: application/json' http://localhost:8080/entry

GET
curl localhost:8080/entry/1
