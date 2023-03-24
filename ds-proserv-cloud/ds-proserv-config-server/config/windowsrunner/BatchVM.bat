set _JAVA_OPTIONS=-Xms256m -Xmx2048m -XX:NewSize=64m -XX:MaxNewSize=128m -XX:MaxMetaspaceSize=128m -XX:CompressedClassSpaceSize=256m -Xss512k -XX:InitialCodeCacheSize=128m -XX:ReservedCodeCacheSize=256m -XX:MaxDirectMemorySize=256m -XX:+UseG1GC -XX:+UseStringDeduplication -XX:+HeapDumpOnOutOfMemoryError -XX:+UseGCOverheadLimit -XX:HeapDumpPath=C:\IHDA\dumps -XX:OnOutOfMemoryError="shutdown -r"

set logFileMaxSize="500MB"

start "ds-proserv-boot-admin" /B cmd.exe /k java -jar -Dserver.port=8690 -Dspring.profiles.active=test -Dmig.peer1-address=peer1:8190 -Dmig.peer2-address=peer2:8191 -Dlogging.file.name=C:/IHDA/logs/boot-admin_8690.log -Dlogging.file.max-size=%logFileMaxSize% C:\IHDA\proservcloud\ds-proserv-cloud\ds-proserv-boot-admin\target\ds-proserv-boot-admin-1.0-GUITAR.jar

start "ds-proserv-appdata1" /B cmd.exe /k java -jar -Dserver.port=8691 -Dspring.profiles.active=test -Dmig.peer1-address=peer1:8190 -Dmig.peer2-address=peer2:8191 -Dlogging.file.name=C:/IHDA/logs/appdata_8691.log -Dlogging.file.max-size=%logFileMaxSize% C:\IHDA\proservcloud\ds-proserv-cloud\ds-proserv-appdata\target\ds-proserv-appdata-1.0-GUITAR.jar

start "ds-proserv-appdata2" /B cmd.exe /k java -jar -Dserver.port=8692 -Dspring.profiles.active=test -Dmig.peer1-address=peer1:8190 -Dmig.peer2-address=peer2:8191 -Dlogging.file.name=C:/IHDA/logs/appdata_8692.log -Dlogging.file.max-size=%logFileMaxSize% C:\IHDA\proservcloud\ds-proserv-cloud\ds-proserv-appdata\target\ds-proserv-appdata-1.0-GUITAR.jar

start "ds-proserv-appdata3" /B cmd.exe /k java -jar -Dserver.port=8693 -Dspring.profiles.active=test -Dmig.peer1-address=peer1:8190 -Dmig.peer2-address=peer2:8191 -Dlogging.file.name=C:/IHDA/logs/appdata_8693.log -Dlogging.file.max-size=%logFileMaxSize% C:\IHDA\proservcloud\ds-proserv-cloud\ds-proserv-appdata\target\ds-proserv-appdata-1.0-GUITAR.jar

start "ds-proserv-authentication0" /B cmd.exe /k java -jar -Dserver.port=8390 -Dspring.profiles.active=test -Dmig.peer1-address=peer1:8190 -Dmig.peer2-address=peer2:8191 -Dlogging.file.name=C:/IHDA/logs/authentication_8390.log -Dlogging.file.max-size=%logFileMaxSize% C:\IHDA\proservcloud\ds-proserv-cloud\ds-proserv-authentication\target\ds-proserv-authentication-1.0-GUITAR.jar

start "ds-proserv-authentication1" /B cmd.exe /k java -jar -Dserver.port=8391 -Dspring.profiles.active=test -Dmig.peer1-address=peer1:8190 -Dmig.peer2-address=peer2:8191 -Dlogging.file.name=C:/IHDA/logs/authentication_8391.log -Dlogging.file.max-size=%logFileMaxSize% C:\IHDA\proservcloud\ds-proserv-cloud\ds-proserv-authentication\target\ds-proserv-authentication-1.0-GUITAR.jar

start "ds-proserv-bulksend" /B cmd.exe /k java -jar -Dserver.port=8890 -Dspring.profiles.active=test -Dmig.peer1-address=peer1:8190 -Dmig.peer2-address=peer2:8191 -Dlogging.file.name=C:/IHDA/logs/bulksend_8890.log -Dlogging.file.max-size=%logFileMaxSize% C:\IHDA\proservcloud\ds-proserv-cloud\ds-proserv-bulksend-batch\target\ds-proserv-bulksend-batch-1.0-GUITAR.jar

start "ds-proserv-connect-listener1" /B cmd.exe /k java -jar -Dserver.port=8590 -Dspring.profiles.active=test -Dmig.peer1-address=peer1:8190 -Dmig.peer2-address=peer2:8191 -Dlogging.file.name=C:/IHDA/logs/connect-listener_8590.log -Dlogging.file.max-size=%logFileMaxSize% C:\IHDA\proservcloud\ds-proserv-cloud\ds-proserv-connect-listener\target\ds-proserv-connect-listener-1.0-GUITAR.jar

start "ds-proserv-connect-listener2" /B cmd.exe /k java -jar -Dserver.port=8591 -Dspring.profiles.active=test -Dmig.peer1-address=peer1:8190 -Dmig.peer2-address=peer2:8191 -Dlogging.file.name=C:/IHDA/logs/connect-listener_8591.log -Dlogging.file.max-size=%logFileMaxSize% C:\IHDA\proservcloud\ds-proserv-cloud\ds-proserv-connect-listener\target\ds-proserv-connect-listener-1.0-GUITAR.jar

start "ds-proserv-connect-listener3" /B cmd.exe /k java -jar -Dserver.port=8592 -Dspring.profiles.active=test -Dmig.peer1-address=peer1:8190 -Dmig.peer2-address=peer2:8191 -Dlogging.file.name=C:/IHDA/logs/connect-listener_8592.log -Dlogging.file.max-size=%logFileMaxSize% C:\IHDA\proservcloud\ds-proserv-cloud\ds-proserv-connect-listener\target\ds-proserv-connect-listener-1.0-GUITAR.jar

start "ds-proserv-coredata1" /B cmd.exe /k java -jar -Dserver.port=8291 -Dspring.profiles.active=test -Dmig.peer1-address=peer1:8190 -Dmig.peer2-address=peer2:8191 -Dlogging.file.name=C:/IHDA/logs/coredata2_8291.log -Dlogging.file.max-size=%logFileMaxSize% C:\IHDA\proservcloud\ds-proserv-cloud\ds-proserv-coredata\target\ds-proserv-coredata-1.0-GUITAR.jar

start "ds-proserv-coredata2" /B cmd.exe /k java -jar -Dserver.port=8292 -Dspring.profiles.active=test -Dmig.peer1-address=peer1:8190 -Dmig.peer2-address=peer2:8191 -Dlogging.file.name=C:/IHDA/logs/coredata2_8292.log -Dlogging.file.max-size=%logFileMaxSize% C:\IHDA\proservcloud\ds-proserv-cloud\ds-proserv-coredata\target\ds-proserv-coredata-1.0-GUITAR.jar

start "ds-proserv-envelopedata1" /B cmd.exe /k java -jar -Dserver.port=8491 -Dspring.profiles.active=test -Dmig.peer1-address=peer1:8190 -Dmig.peer2-address=peer2:8191 -Dlogging.file.name=C:/IHDA/logs/envelopedata_8491.log -Dlogging.file.max-size=%logFileMaxSize% C:\IHDA\proservcloud\ds-proserv-cloud\ds-proserv-envelopedata\target\ds-proserv-envelopedata-1.0-GUITAR.jar

start "ds-proserv-envelopedata2" /B cmd.exe /k java -jar -Dserver.port=8492 -Dspring.profiles.active=test -Dmig.peer1-address=peer1:8190 -Dmig.peer2-address=peer2:8191 -Dlogging.file.name=C:/IHDA/logs/envelopedata_8492.log -Dlogging.file.max-size=%logFileMaxSize% C:\IHDA\proservcloud\ds-proserv-cloud\ds-proserv-envelopedata\target\ds-proserv-envelopedata-1.0-GUITAR.jar

start "ds-proserv-envelopedata3" /B cmd.exe /k java -jar -Dserver.port=8493 -Dspring.profiles.active=test -Dmig.peer1-address=peer1:8190 -Dmig.peer2-address=peer2:8191 -Dlogging.file.name=C:/IHDA/logs/envelopedata_8493.log -Dlogging.file.max-size=%logFileMaxSize% C:\IHDA\proservcloud\ds-proserv-cloud\ds-proserv-envelopedata\target\ds-proserv-envelopedata-1.0-GUITAR.jar

start "ds-proserv-exceptionbatch" /B cmd.exe /k java -jar -Dserver.port=8990 -Dspring.profiles.active=test -Dmig.peer1-address=peer1:8190 -Dmig.peer2-address=peer2:8191 -Dlogging.file.name=C:/IHDA/logs/exceptionbatch_8990.log -Dlogging.file.max-size=%logFileMaxSize% C:\IHDA\proservcloud\ds-proserv-cloud\ds-proserv-exception-batch\target\ds-proserv-exception-batch-1.0-GUITAR.jar

start "ds-proserv-shell" /B cmd.exe /k java -jar -Dserver.port=8790 -Dspring.profiles.active=test -Dmig.peer1-address=peer1:8190 -Dmig.peer2-address=peer2:8191 -Dlogging.file.name=C:/IHDA/logs/shell_8790.log -Dlogging.file.max-size=%logFileMaxSize% C:\IHDA\proservcloud\ds-proserv-cloud\ds-proserv-shell\target\ds-proserv-shell-1.0-GUITAR.jar