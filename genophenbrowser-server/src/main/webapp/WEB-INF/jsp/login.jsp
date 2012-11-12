<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"   "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<title>Login</title>
		<link rel="stylesheet" href="<c:url value='/css/login.css'/>" />
		<link rel="stylesheet" href="<c:url value='/css/openid-shadow.css'/>" />
		<link rel="stylesheet" href="<c:url value='/css/960_12_col.css'/>" />
		<link rel="stylesheet" href="<c:url value='/css/m-styles.min.css'/>" /> 
		
    	<script type="text/javascript" src="<c:url value='/js/jquery-1.2.6.min.js'/>"></script>
		<script type="text/javascript" src="<c:url value='/js/openid-jquery.js'/>"></script>
		<script type="text/javascript" src="<c:url value='/js/openid-en.js' />"></script>
		<script type="text/javascript" src="<c:url value='/js/m-radio.min.js' />"></script>
		<script type="text/javascript">
		    $(document).ready(function() {
		        openid.init('openid_identifier');
		        //openid.setDemoMode(true); 
		    });
		</script>
		
		<!-- /Simple OpenID Selector -->
	</head>

<body>
    <div class="container_12">  
        <div class="grid_12">  
      		<div class="header"><h1>Log In</h1></div>
			<c:if test="${not empty param.login_error}">
			  <font color="red">
			    Your login attempt was not successful, try again.<br/><br/>
			    Reason: <c:out value="${SPRING_SECURITY_LAST_EXCEPTION.message}"/>.
			  </font>
			</c:if>
			<div class="form_login">
				<div class="form_login_info">
				   Sign in with your local account:
				</div>
				<form class="login-form" action="j_spring_security_check?spring-security-redirect=/${url}" method="post" >
				<input id="j_username" name="j_username" size="20" maxlength="50" type="text" placeHolder="name@example.com"/>
				<input id="j_password" name="j_password" size="20" maxlength="50" type="password" placeHolder="Password"/>
				<input type="submit" value="Login"/></p>
			</form>
		</div>
		<div class="form_login">
	<!-- Simple OpenID Selector -->
			<form action="<c:url value='j_spring_openid_security_check?spring-security-redirect=/${url}'/>" method="post" id="openid_form">
	    		<input type="hidden" name="action" value="verify" />
	            <div id="openid_choice">
	                <div class="form_login_info">Or, do you already have an account on one of these sites? Click the logo to log in with it here:</div>
	                <div id="openid_btns"></div>
	
	            </div>
	
	            <div id="openid_input_area">
	                <input id="openid_identifier" name="openid_identifier" type="text" value="http://" />
	                <input id="openid_submit" type="submit" value="Sign-In"/>
	            </div>
	            <noscript>
	            <p>OpenID is a service that allows you to log-on to many different websites using a single identity.
	            Find out <a href="http://openid.net/what/">more about OpenID</a> and <a href="http://openid.net/get/">how to get an OpenID enabled account</a>.</p>
	            </noscript>
			</form>
		</div>
		<div class="form_login">
			<div class="form_login_info">If you don’t already have an account on any of the above</div>
			<div class="form_link"><a href="<c:url value='/registration' />">click here to sign up</a></div>
		</div>
	  <div class="clear"></div>  
    </div>  
	
</body>
</html>