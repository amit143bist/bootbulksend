set _JAVA_OPTIONS=-Xms256m -Xmx2048m -XX:NewSize=64m -XX:MaxNewSize=128m -XX:MaxMetaspaceSize=128m -XX:CompressedClassSpaceSize=256m -Xss512k -XX:InitialCodeCacheSize=128m -XX:ReservedCodeCacheSize=256m -XX:MaxDirectMemorySize=256m -XX:+UseG1GC -XX:+UseStringDeduplication -XX:+HeapDumpOnOutOfMemoryError -XX:+UseGCOverheadLimit -XX:HeapDumpPath=C:\IHDA\dumps -XX:OnOutOfMemoryError="shutdown -r"

set logFileMaxSize="500MB"

start "ds-proserv-zuul_proxy" /B cmd.exe /k java -jar -Dserver.port=9190 -Dspring.profiles.active=test -Dmig.peer1-address=peer1:8190 -Dmig.peer2-address=peer2:8191 -Dlogging.file.name=C:/IHDA/logs/zuul_proxy_9190.log -Dlogging.file.max-size=%logFileMaxSize% C:\IHDA\proservcloud\ds-proserv-cloud\ds-proserv-zuul-proxy\target\ds-proserv-zuul-proxy-1.0-GUITAR.jar