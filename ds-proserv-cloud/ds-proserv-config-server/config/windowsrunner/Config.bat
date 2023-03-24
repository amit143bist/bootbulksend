set _JAVA_OPTIONS=-Xms64m -Xmx512m -XX:NewSize=64m -XX:MaxNewSize=64m -XX:MaxMetaspaceSize=128m -XX:CompressedClassSpaceSize=64m -Xss256k -XX:InitialCodeCacheSize=16m -XX:ReservedCodeCacheSize=64m -XX:MaxDirectMemorySize=32m -XX:+UseG1GC -XX:+UseStringDeduplication -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=C:\IHDA\dumps -XX:OnOutOfMemoryError="shutdown -r" -XX:+UseGCOverheadLimit

set logFileMaxSize="500MB"

start "ds-proserv-config" cmd.exe /k java -jar -Dserver.port=8090 -Dlogging.file.name=C:/IHDA/logs/config_8090.log -Dlogging.file.max-size=%logFileMaxSize% -Dcloud.config.searchLocations=/C:/IHDA/Config/ C:\IHDA\proservcloud\ds-proserv-cloud\ds-proserv-config-server\target\ds-proserv-config-server-1.0-GUITAR.jar