(function () {
	$(function () {
	  $('[data-toggle="tooltip"]').tooltip()
	});

    var app = angular.module('angularSPF', ['ngSanitize']);

    app.controller('SPFController', [ '$scope', '$http', function ($scope, $http) {

		var container = this;
		container.showComplete = false;
		container.validValue = [];
		
		$scope.isEmpty = function (str){
			return (!str || 0 === str.length);
		}
		
		$scope.isBlank = function (str){
			return (!str || /^\s*$/.test(str));
		}
		
		$scope.sendEnvelopes = function()
		{
					console.log('In Send Envelopes');
					container.validValue = [];
					
					var data = new FormData($('input[name^="media"]'));     
				   	jQuery.each($('input[name^="media"]')[0].files, function(i, file) {
				   	    data.append("file", file);
				   	});
				   	
				   	var emailSubject = $('input[name="emailSubject"]').val();
				   	var emailBlurb = $('input[name="emailBlurb"]').val();
				   	var requestorEmail = $('input[name="requestorEmail"]').val();
				   	
				   	console.log('emailSubject ' + emailSubject + ' emailBlurb ' + emailBlurb);
				   	
				   	var counter = 0;
				   	var nameArr = [];
				   	
				   	$('input[name^="name"]').each(function(){
         						
         					console.log($(this).val());
         					
         					if($(this).val().length > 0){
         						
         						nameArr.push($(this).val());
         						counter++;
         					}
                       }
                    );
                    
                    var emailArr = [];
                    
                    $('input[name^="mail"]').each(function(){
         						
         					console.log($(this).val());
         					
         					if($(this).val().length > 0){
         						
         						emailArr.push($(this).val());
         					}
                        }      
                    );
                    
                    var json = '{';
                    
                    json = json + '"useCaseName":"Estimates",';
                    json = json + '"requestorEmail":"' + requestorEmail + '",';
                    json = json + '"emailSubject":"' + emailSubject + '",';
                    json = json + '"emailBlurb":"' + emailBlurb + '",';
                    json = json + '"inlineTemplateSequenceNumber":"1",';
                    json = json + '"recipientInfoList": [';
                    
                    for(var i=0;i<counter;i++){
                    
                    	json = json + '{ "name":"' + nameArr[i] + '",';
                    	json = json + '  "email":"' + emailArr[i] + '",';
                    	json = json + '  "embeddedRecipientStartURL":"SIGN_AT_DOCUSIGN"}';
                    	
                    	console.log('i- ' + i + ' counter- ' + counter );
                    	if (i != counter-1) json = json + ',';
                    	
                    }
                    
                    json = json + ']}';
                    
 				    data.append('estimateInfo', new Blob([json], {
		                type: "application/json"
		            }));
		            
		            console.log('data- ' + data);
					
					$.ajax({
					    /*headers: {
			                'X-DocuSign-Authentication': '{"Username": "docusign.sso+mockup@gmail.com", "Password":"testing1", "IntegratorKey":"16f81d9e-e9ee-408d-bc60-d6e1aecd9756"}'
			            },*/
						type : "POST",
						processData: false,
						dataType: 'json',
						data: data,
						cache: false,
						contentType: false,
						url: 'createEstimates',
				        success: function(result) {
				        	
				        	console.log('In success- ' + result.envelopeId);
				        	container.showComplete = true;
				        	container.validValue.push(result.envelopeId);
				        	$scope.$apply();
				        },
						error: function(result) {
							
							console.log('In error- ' + result);
						}
				      });

		}
		
		$scope.showCompleteTab = function (){
			
			container.showComplete = true;
		}
		
    } ]);
})();