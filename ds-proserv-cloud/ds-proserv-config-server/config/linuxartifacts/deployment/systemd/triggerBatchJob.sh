#!/bin/bash
# Bash Menu Script Example
SCRIPTNAME="triggerBatchJob.sh"

HORIZONTALLINE="=========================================================================="
clear
echo -e "\n$HORIZONTALLINE"
echo "                                 Select Menu"
echo "Populate CommandWithTime.txt or CommandWithHours.txt with correct values"
echo -e "$HORIZONTALLINE"

PS3='Please enter your choice [1,2,3]: '
options=("TriggerByTime" "TriggerByHours" "Quit")
select opt in "${options[@]}"
do
    case $opt in
        "TriggerByTime")
            echo "This is the contents for your CommandWithTime.txt ..."

            file="/home/docusign/deployment/systemd/CommandWithTime.txt"
            while IFS= read -r line
            do
                # display $line or do somthing with $line
                printf '%s\n' "$line"
            done <"$file"

            read -p "Is this the right content, would you like to continue? [y/n]" -n 1 -r

            echo    # (optional) move to a new line
            if [[ $REPLY =~ ^[Yy]$ ]]
            then
                echo " Ok Running...."
                java -Dlogging.file=/home/docusign/deployment/shellstarter/shellstarter.log -Dlogging.file.max-size=500MB -Dspring.profiles.active=prod -jar /home/docusign/deployment/shellstarter/ds-docusign-shell-1.0-GUITAR.jar @/home/docusign/deployment/systemd/CommandWithTime.txt

            fi
            break
            ;;
        "TriggerByHours")
            echo "This is the contents for your CommandWithHours.txt ..."

            file="/home/docusign/deployment/systemd/CommandWithHours.txt"
            while IFS= read -r line
            do
                # display $line or do somthing with $line
                printf '%s\n' "$line"
            done <"$file"

            read -p "Is this the right content, would you like to continue? [y/n]" -n 1 -r

            echo    # (optional) move to a new line
            if [[ $REPLY =~ ^[Yy]$ ]]
            then
                echo " Ok Running...."
                java -Dlogging.file=/home/docusign/deployment/shellstarter/shellstarter.log -Dlogging.file.max-size=500MB -Dspring.profiles.active=prod -jar /home/docusign/deployment/shellstarter/ds-proserv-shell-1.0-GUITAR.jar @/home/docusign/deployment/systemd/CommandWithHours.txt

            fi
            break
            ;;
        
        "Quit")
            break
            ;;
        *) echo "invalid option $REPLY";;
    esac
done
