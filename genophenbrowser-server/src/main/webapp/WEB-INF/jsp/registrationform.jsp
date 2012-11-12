<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"   "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<title>Registration</title>
		<link rel="stylesheet" href="<c:url value='/css/login.css'/>" />
		<link rel="stylesheet" href="<c:url value='/css/960_12_col.css'/>" />
		<link rel="stylesheet" href="<c:url value='/css/m-styles.min.css'/>" /> 
		<style>
			.form-item { margin: 20px 0; }
			.form-label { font-weight: bold; }
			.form-global-error-message { width: 500px; padding: 6px; background: #E2ABAB; color: #FFF; font-weight: bold; }
			.form-error-message { color: #900; }
			.form-error-field { background-color: #FFC; }
		</style>
		<script type="text/javascript" src="<c:url value='/js/m-radio.min.js' />"></script>
		<script type="text/javascript">
			window.onload = function() {
				document.getElementById("firstName").focus();
			}
		</script>
	</head>

<body>
 <div class="container_12">  
        <div class="grid_12">  
      		<div class="header"><h1>Create Account</h1></div>
		
		<form:form modelAttribute="registration">
			<spring:bind path="registration">
				<spring:hasBindErrors name="registration">
					<div class="form-global-error-message"><form:errors></form:errors></div>
				</spring:hasBindErrors>
			</spring:bind>
			<div class="form-item">
				<div class="form-label">First name:</div>
				<form:input path="firstname" size="40" cssErrorClass="form-error-field"/>
				<div class="form-error-message"><form:errors path="firstname"></form:errors></div>
			</div>
			<div class="form-item">
				<div class="form-label">Last name:</div>
				<form:input path="lastname" size="40" cssErrorClass="form-error-field"/>
				<div class="form-error-message"><form:errors path="lastname"></form:errors></div>
			</div>
			<div class="form-item">
				<div class="form-label">E-mail address:</div>
				<form:input path="email" size="40" cssErrorClass="form-error-field"/> We won't spam you
				<div class="form-error-message"><form:errors path="email" htmlEscape="false"></form:errors></div>
			</div>
			<div class="form-item">
				<div class="form-label">Password:</div>
				<form:password path="password" showPassword="true" size="40" cssErrorClass="form-error-field"/>
				<div class="form-error-message"><form:errors path="password"></form:errors></div>
			</div>
			<div class="form-item">
				<div class="form-label">Confirm password:</div>
				<form:password path="confirmPassword" showPassword="true" size="40" cssErrorClass="form-error-field"/>
				<div class="form-error-message"><form:errors path="passwordValid"></form:errors></div>
			</div>
			<div class="form-item">
				<input type="submit" value="Register"/>
			</div>
		</form:form>
		</div>
		</div>
</body>
</html>