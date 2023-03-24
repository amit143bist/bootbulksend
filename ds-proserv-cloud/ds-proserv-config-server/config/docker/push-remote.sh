#!/bin/sh

#enable read only 
TESTING=0
#enter docker orgname
#DOCKER_REPO="frankftsai78"
DOCKER_REPO="docusignps"
DOCKER_TAG="1.0-GUITAR"
IMAGENAME=ds-proserv-$1

#while true; do
#	read -p "ARE YOU SURE YOU WANT TO PUSH IMAGES TO REMOTE REPO ON ${DOCKER_REPO}? (y/n)" yn
#	case $yn in 
#		[Yy]* ) break;; #allow to continue
#		[Nn]* ) exit;;
#		* ) echo "Please enter y or n.";;
#	esac
#done

#authenticate docker
#docker login

#ignore services
ignore=(
ds-proserv-report-data,ds-proserv-report-shell
)

#tag docker images for push to docker repo
cd /Users/frank.tsai/dev/proservcloud/docker

if [ "$1" != "" ]; then
	if [ "$TESTING" != "1" ]; then
		#run output
		docker tag $IMAGENAME:$DOCKER_TAG $DOCKER_REPO/$IMAGENAME:$DOCKER_TAG
		docker push $DOCKER_REPO/$IMAGENAME:$DOCKER_TAG
	else
		#test output
		echo "docker tag $IMAGENAME:$DOCKER_TAG $DOCKER_REPO/$IMAGENAME:$DOCKER_TAG"
		echo "docker push $DOCKER_REPO/$IMAGENAME:$DOCKER_TAG"
	fi
else
	for image in $(docker images | grep ^ds-proserv | perl -nle 'm/^(ds-proserv-.*)\s.*1.0-GUITAR.*$/; print "$1"') 
	do
		if [[ " ${ignore[@]} " =~ " ${image} " ]]; then
			echo "ignoring $image"
		else
			if [ "$TESTING" != "1" ]; then
				#run output
				docker tag $image:$DOCKER_TAG $DOCKER_REPO/$image:$DOCKER_TAG
				docker push $DOCKER_REPO/$image:$DOCKER_TAG
			else
				#test output
				echo "docker tag $image:$DOCKER_TAG $DOCKER_REPO/$image:$DOCKER_TAG"
				echo "docker push $DOCKER_REPO/$image:$DOCKER_TAG"
			fi
		fi
	done
fi

echo "Push Completed"