<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Success Callback</title>

<script
			src="https://ajax.googleapis.com/ajax/libs/jquery/1.12.4/jquery.min.js"></script>
		
		<script>
			$(document).ready(function(){
			   	//alert('window.location.href- ' + window.location.href);// Returns full URL
			   	//alert("Search - " + window.location.search );// Returns query only
			   	
			   	var refreshToken;
			   	var vars = [], hash;
			    var hashes = window.location.href.slice(window.location.href.indexOf('?') + 1).split('&');
			    for(var i = 0; i < hashes.length; i++)
			    {
			      hash = hashes[i].split('=');
			      vars.push(hash[0]);
			      vars[hash[0]] = hash[1];
			    }
			    
			   	var authCode = vars["code"];
			   	var authScope = vars["scope"];

				console.log('code- ' + authCode + ' scope- ' + authScope);

				$.getJSON({
					type : "GET",
					url: 'fetchgoogletoken',
			        data: ({authCode : authCode, authScope : authScope}),
			        complete: function(data) {
				          
			        	  var accessToken = data.toString();
				          console.log('AccessToken complete- ' + accessToken);
				          
				          document.getElementById('show-data').innerHTML = 'Token created in complete';
				          
				        },
			        success: function(data) {
			        	
			        	  var accessToken = data.toString();
			        	  console.log('AccessToken success- ' + accessToken);
			          
			        	  document.getElementById('show-data').innerHTML = 'Token created in success';
			        }
			      });

				
			});
		</script>
		
</head>
<body>

<div id="show-data"></div>

</body>
</html>