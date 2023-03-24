#!/bin/sh

#enter docker orgname
#DOCKER_REPO="frankftsai78"
DOCKER_REPO="docusignps"
#enter token which can be found when running a docker command from the docker website vi chrome network inspection
TOKEN="eyJ4NWMiOlsiTUlJQytUQ0NBcCtnQXdJQkFnSUJBREFLQmdncWhrak9QUVFEQWpCR01VUXdRZ1lEVlFRREV6dFNUVWxHT2xGTVJqUTZRMGRRTXpwUk1rVmFPbEZJUkVJNlZFZEZWVHBWU0ZWTU9rWk1WalE2UjBkV1dqcEJOVlJIT2xSTE5GTTZVVXhJU1RBZUZ3MHlNVEF4TWpVeU16RTFNREJhRncweU1qQXhNalV5TXpFMU1EQmFNRVl4UkRCQ0JnTlZCQU1UTzFWUFNWSTZSRTFKVlRwWVZWSlJPa3RXUVV3NlNrcExWenBMTkZKRk9rVk9URXM2UTFkRlREcEVTazlJT2xKWE4xYzZUa1JLVkRwVldFNVlNSUlCSWpBTkJna3Foa2lHOXcwQkFRRUZBQU9DQVE4QU1JSUJDZ0tDQVFFQW52QXlaRStPbGR4K2N4UUtEQVlLZkxySWJOaytoZ2hIN05cL2ZMWkxUOERhdU8xdGhNZ2hqbGNwaEVWQ25hMWUwRmk4dWxSVnhYbUd1allUM1ducWxnZmkzZlhNRFwvQlBRTmlkWHZkeWprbDFZS3dPTkl3TkFWMnRXbExxaXFsdGhSWkFnTFdvWWZZMXZQMHFKTFZBbWt5bUkrOXRBcEMxNldNZ1ZFcHJGdE1rNnV0NDlMcDlUR1J0aDJQbHVWc3RSQ1hVUGp4bjI0d3NnYlUwVStjWTJSNEpyZmVJdzN0T1ZKbXNESkNaYW5SNmVheFYyVFZFUkxoZnNGVTlsSHAzcldCZ1RuNVRCSHlMRDNRdGVFXC8yd1wvc1wvcUxZcmdIK1hCMmZBazJPd1NIRG5YWDg4WWVJd0EyVGJJMDdYNVwvMUJ1bGlMK1A3bjllQU9UZmw5MVZWcDREd0lEQVFBQm80R3lNSUd2TUE0R0ExVWREd0VCXC93UUVBd0lIZ0RBUEJnTlZIU1VFQ0RBR0JnUlZIU1VBTUVRR0ExVWREZ1E5QkR0VlQwbFNPa1JOU1ZVNldGVlNVVHBMVmtGTU9rcEtTMWM2U3pSU1JUcEZUa3hMT2tOWFJVdzZSRXBQU0RwU1Z6ZFhPazVFU2xRNlZWaE9XREJHQmdOVkhTTUVQekE5Z0R0U1RVbEdPbEZNUmpRNlEwZFFNenBSTWtWYU9sRklSRUk2VkVkRlZUcFZTRlZNT2taTVZqUTZSMGRXV2pwQk5WUkhPbFJMTkZNNlVVeElTVEFLQmdncWhrak9QUVFEQWdOSUFEQkZBaUVBMGQ3eXVrNCtYSVpua1BvdElWR0J4cFFKd2kzNDB0TFJvdHdDOXg2Qml1Y0NJSEVKYjJYaDRDOG1hVmJzUTF3dlRIK2FEZXRVeEFLbWViR1drcXpnUnVnVCJdLCJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzZXNzaW9uX2lkIjoiMjRENTNBMkRDMUE3RjlFMDExRUEyNzlEQ0VGMjMyMzUiLCJpYXQiOjE2MTczOTI2NDcsImV4cCI6MTYxOTk4NDY0Nywic3ViIjoiMjkyZDEyYmVmZWVkNDljNWE0MGE5YTJhOTVkN2U3ZTAiLCJ1c2VybmFtZSI6ImZyYW5rZnRzYWk3OCIsImp0aSI6IjI0RDUzQTJEQzFBN0Y5RTAxMUVBMjc5RENFRjIzMjM1IiwidXNlcl9pZCI6IjI5MmQxMmJlZmVlZDQ5YzVhNDBhOWEyYTk1ZDdlN2UwIiwiZW1haWwiOiIifQ.LH9atJwWmlgzyHosyhcD3qQBAAEozVwgBI7ZCZIuVvnf20jEPL-0ptNEZpbL-XixXy_m3aFgG0nP_XH6E1F0r9-B2Hff2aO6Mf3xDb4ALd30noBsNKJrDMSuYqXPSXcaHA-5uQIDm8ahD9YU9kRf_prvsxIDbZl-rmVOE_lptVeKRM-QgQ1heUwwhtTyf9NJA2nDn7J6J8meo3ZU68Q0VsL4077LgKHYa8dWGxv1ibqXBdZHj5GngXkQp-cTjbQUhV1zFij98swNK9iHBs2_TBbD6c7Utt0AFJdFeHNa_ImJB05oAMW5t6CYMe3yS6MZ9PJ4Flsknn3CxVizdOLiCw"
#you can find this also from the dev tools network inspection from chrome
CSRFTOKEN="/fFmRG8ndsfrghfJrFawuafPLDQ15imrrtqco3/ZYFI="

#run curl to pull list of images from the repo and pipe into jq 
images=$(curl --location --request GET "https://hub.docker.com/v2/repositories/${DOCKER_REPO}/?page_size=100" \
--header "Cookie: csrftoken=${CSRFTOKEN}; token=${TOKEN}" | jq -r '.results|.[]|.name')
	echo "images: $images"
imagesarray=( $images )

if [ ${#imagesarray[@]} -eq 0 ]; then

	echo "Repository ${DOCKER_REPO} is empty"

else

	echo "Getting ready to delete the following images: ${images[*]}"

	for image in "${imagesarray[@]}"
		do
			echo "Updating permissions to $image..."
			curl --location --request POST "https://hub.docker.com/v2/repositories/${DOCKER_REPO}/${image}/groups/" \
				--header "Authorization: JWT ${TOKEN}" \
				--header "Content-Type: application/json" \
				--header "Accept: application/json" \
				--header "X-CSRFToken: ${CSRFTOKEN}" \
				--header "Cookie: csrftoken=${CSRFTOKEN}; token=${TOKEN}" \
				--data-raw '{
    "group_id": 450858,
    "groupid": 450858,
    "group_name": "ihdaaccess",
    "groupname": "ihdaaccess",
    "permission": "read"
}'
		done

	echo "Images group upated"

fi