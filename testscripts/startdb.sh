docker stop oracledb
docker rm oracledb
docker run --cpuset-cpus='10,11' --name oracledb \
-p 1521:1521 -p 5500:5500 \
-e ORACLE_SID=ORCLCDB \
-e ORACLE_PDB=ORCLPDB1 \
-e ORACLE_PWD=Welcome01 \
-e ORACLE_EDITION=standard \
-e ORACLE_CHARACTERSET=AL32UTF8 \
-v /home/maarten/oradata:/opt/oracle/oradata \
oracle/database:19.3.0-se2
