@echo off

rem script parameters
set jardir=
set springApplicationName="dsauthentication"
set jarFile="%jardir%/ds-proserv-authentication-1.0-GUITAR.jar"
set proxyHost="[proxyHost]"
set proxyPort="[proxyPort]"
set serverPort="[serverPort]"
set springBootProfileName="dev"
set logFileMaxSize="300MB"
set logfilePath="%jardir%\\%serverPort%.log"

echo Starting the %springApplicationName% microservice

rem execute
java -jar -Dlogging.file=%logfilePath% -Dlogging.file.max-size=%logFileMaxSize% -Dspring-boot.run.profiles=%springBootProfileName% -Dhttp.proxyHost=%proxyHost% -Dhttp.proxyPort=%proxyPort% -Dhttps.proxyHost=%proxyHost% -Dhttps.proxyPort=%proxyPort% %jarFile