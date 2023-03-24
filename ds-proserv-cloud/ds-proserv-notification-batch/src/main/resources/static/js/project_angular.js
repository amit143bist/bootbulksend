var app = angular.module('angularSPF', ['ngRoute','datatables','ui.bootstrap']);

app.controller('ModalCtrl', function($scope, $http, $timeout, $location, $window, $sce, DTOptionsBuilder) {

	var container = this;
	container.showBody = false;
	container.showIframe = false;
	container.custEnvelopes = [];
	container.iFrameURL = $sce.trustAsResourceUrl('https://agile-sands-24492.herokuapp.com/portalLanding');
	
	// DataTables configurable options
    $scope.dtOptions = DTOptionsBuilder.newOptions()
        /*.withDisplayLength(5)*/
        /*.withOption('bLengthChange', false)*/
        .withOption('lengthMenu', [5, 10, 50, 100]);

	$scope.roomId = 0;
	
	$scope.openRecipientView = function(envelopeId, recipientEmail){

    	console.log('openRecipientView envelopeId- '
				+ envelopeId + ' recipientEmail- '
				+ recipientEmail);
				
		var url = '/redirectToRecipientViewUrl?envelopeId=' + envelopeId + '&recipientEmail=' + recipientEmail;
		
		console.log('url-' + url);
		$window.location.href = url;
			
	}
		
	$scope.openRecipientViewAsIframe = function(envelopeId, recipientEmail){

    	console.log('openRecipientViewAsIframe envelopeId- '
				+ envelopeId + ' recipientEmail- '
				+ recipientEmail);
				
		$.ajax({
			type : 'GET',
			url : 'redirectToRecipientViewUrlIframe',
			dataType : 'json',
			data: ({envelopeId:envelopeId, recipientEmail: recipientEmail}),
			success : function(respData) {

				console.log('url in openRecipientViewAsIframe- ' + respData.embeddedUrl);
				container.showIframe = true;
				container.iFrameURL = $sce.trustAsResourceUrl(respData.embeddedUrl);
				$scope.$apply();
				
			},
			error : function(respData) {
				$scope.$apply();
			}
		});
		
	}
		
	$scope.openRecipientViewAsPopUpIframe = function(envelopeId, recipientEmail){

    	console.log('openRecipientViewAsPopUpIframe envelopeId- '
				+ envelopeId + ' recipientEmail- '
				+ recipientEmail);
				
	    container.showIframe = false;
				
		$.ajax({
			type : 'GET',
			url : 'redirectToRecipientViewUrlIframe',
			dataType : 'json',
			data: ({envelopeId:envelopeId, recipientEmail: recipientEmail}),
			success : function(respData) {

				console.log('url in openRecipientViewAsPopUpIframe- ' + respData.embeddedUrl);

				$window.open(respData.embeddedUrl);
				
			},
			error : function(respData) {
				$scope.$apply();
			}
		});
		
	}

	$scope.fetchAllEnvelopesJSON = function(recipientEmail) {

		console.log('recipientEmail ' + recipientEmail);
		container.custEnvelopes = [];
		
		$.ajax({
			type : 'GET',
			url : 'fetchRecipientEnvelopes',
			dataType : 'json',
			data: ({recipientEmail: recipientEmail}),
			success : function(respData) {

				$.each(respData, function(index, element) {

							console.log('dashBoardData- ' + index + ' elementName- '
							+ JSON.stringify(element));

						container.custEnvelopes.push(element);
				});
				
				$scope.$broadcast('dataloaded');
				container.showBody = true;
				$scope.$apply();
			},
			error : function(respData) {
				$scope.$apply();
			}
		});

	};
});