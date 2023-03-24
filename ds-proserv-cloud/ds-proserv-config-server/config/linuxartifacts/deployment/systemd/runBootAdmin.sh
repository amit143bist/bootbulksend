#!/bin/bash
SCRIPTNAME="runBootAdmin.sh"

case $1 in
start)
echo starting bootadmin@8010.service ...
sudo systemctl daemon-reload
sudo systemctl start bootadmin@8010.service
sudo systemctl -l status bootadmin@8010

;;
stop)
echo stopping bootadmin@8010.service ...
sudo systemctl stop bootadmin@8010.service
sudo systemctl -l status bootadmin@8010

;;
restart)
echo restarting bootadmin@8010.service ...
sudo systemctl daemon-reload
sudo systemctl stop bootadmin@8010
sudo systemctl start bootadmin@8010.service
sudo systemctl -l status bootadmin@8010

;;
*)
echo "Usage is: $SCRIPTNAME [start/stop]"
;;
esac