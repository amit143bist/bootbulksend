<!DOCTYPE html>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<html lang="en">
<head>
<title>POC GMAIL Auth</title>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">

<link href="/css/main.css" rel="stylesheet" />
<link href="/css/spf.css" rel="stylesheet" />
<link href="/css/login.css" rel="stylesheet" />

<link rel="stylesheet"
	href="https://maxcdn.bootstrapcdn.com/font-awesome/4.5.0/css/font-awesome.min.css">
<script
	src="https://ajax.googleapis.com/ajax/libs/jquery/2.1.0/jquery.min.js"></script>


<link rel="stylesheet"
	href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css">
<script
	src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js"></script>
<script
	src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/js/bootstrap.min.js"></script>

</head>
<body>
	<div class="row headerSPF">
		<div class="col-sm-1" style="text-align: center"></div>
		<div class="col-sm-8 collapsed">
			<h3>
				<i>POC</i>
			</h3>
		</div>
		<div class="col-sm-2" style="text-align: center"></div>
	</div>

	<div class="row">
		<div class="col-md-6">&nbsp;</div>
		<div class="col-md-6">&nbsp;</div>
		<div class="col-md-6">&nbsp;</div>
		<div class="col-md-6">&nbsp;</div>
		<div class="col-md-6">&nbsp;</div>
		<div class="col-md-6">&nbsp;</div>
		<div class="col-md-6">&nbsp;</div>
		<div class="col-md-6">&nbsp;</div>
		<div class="col-md-6">&nbsp;</div>
		<div class="col-md-6">&nbsp;</div>
		<div class="col-md-6">&nbsp;</div>
	</div>

	<div class="container">
		<div class="row">
			<section>
				<form class="form-signin mg-btm">
					<h3 class="heading-desc">Authorize with Gmail</h3>
					<div class="social-box">
						<div class="row mg-btm">
							<div class="col-md-12">
								<a href="${googleOAuthUrl}" class="btn btn-primary btn-block"> <i
									class="icon-facebook"></i> Authorize with Gmail
								</a>
							</div>
						</div>
					</div>
				</form>
			</section>
		</div>
	</div>

</body>

</html>