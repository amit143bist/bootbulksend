set _JAVA_OPTIONS=-Xms64m -Xmx512m -XX:NewSize=64m -XX:MaxNewSize=64m -XX:MaxMetaspaceSize=128m -XX:CompressedClassSpaceSize=64m -Xss256k -XX:InitialCodeCacheSize=16m -XX:ReservedCodeCacheSize=64m -XX:MaxDirectMemorySize=32m -XX:+UseG1GC -XX:+UseStringDeduplication -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=C:\IHDA\dumps -XX:OnOutOfMemoryError="shutdown -r" -XX:+UseGCOverheadLimit

set logFileMaxSize="500MB"

start "ds-proserv-eureka-server1" /B cmd.exe /k java -jar -Dspring.profiles.active=peer1 -Dmig.peer1Port=8190 -Dmig.peer2Port=8191 -Dlogging.file.name=C:/IHDA/logs/eureka-server1_8190.log -Dlogging.file.max-size=%logFileMaxSize% C:\IHDA\proservcloud\ds-proserv-cloud\ds-proserv-eureka-server\target\ds-proserv-eureka-server-1.0-GUITAR.jar

start "ds-proserv-eureka-server2" /B cmd.exe /k java -jar -Dspring.profiles.active=peer2 -Dmig.peer1Port=8190 -Dmig.peer2Port=8191 -Dlogging.file.name=C:/IHDA/logs/eureka-server2_8191.log -Dlogging.file.max-size=%logFileMaxSize% C:\IHDA\proservcloud\ds-proserv-cloud\ds-proserv-eureka-server\target\ds-proserv-eureka-server-1.0-GUITAR.jar