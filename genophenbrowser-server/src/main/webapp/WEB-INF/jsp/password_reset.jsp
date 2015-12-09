<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title>Password reset</title>
    <link rel="icon" type="image/ico" href="img/favicon.ico"/>
    <link rel="stylesheet" href="//netdna.bootstrapcdn.com/bootstrap/3.0.2/css/bootstrap.min.css">
    <link rel="stylesheet" href="<c:url value='css/login.css'/>"/>
    <link rel="stylesheet" href="<c:url value='css/entypo.min.css'/>"/>
    <script type="text/javascript">
        window.onload = function () {
            document.getElementById("password").focus();
        }
    </script>
</head>

<body>
<div class="forgot_password_container">
    <div class="content">
        <div>
            <form:form modelAttribute="reset_password">
                <h1>Change your password</h1>

                <div class="sub_container">
                    <p>Enter a new password below.</p>
                    <spring:bind path="reset_password">
                        <spring:hasBindErrors name="reset_password">
                            <div class="alert alert-danger"><form:errors htmlEscape="false"
                                                                         path="*"></form:errors></div>
                        </spring:hasBindErrors>
                    </spring:bind>
                    <c:if test="${not empty reset_password.status}">
                        <div class="alert alert-success"><c:out value="${reset_password.status}"></c:out></div>
                    </c:if>
                    <div class="form-group ${requestScope['org.springframework.validation.BindingResult.reset_password'].hasFieldErrors('password') ? 'has-error' : ''}">
                        <label class="sr-only" for="password">Password</label>
                        <form:input showPassword="true" path="password" size="40" id="password" name="password"
                                    placeholder="Password" class="form-control input-lg" type="password"/>
                        <span class="help-block"><form:errors path="password"></form:errors></span>
                    </div>
                    <div class="form-group ${requestScope['org.springframework.validation.BindingResult.reset_password'].hasFieldErrors('passwordValid') ? 'has-error' : ''}">
                        <label class="sr-only" for="confirmPassword">Password confirm</label>
                        <form:input showPassword="true" path="confirmPassword" size="40" name="confirmPassword"
                                    placeholder="Password Confirm" class="form-control input-lg" type="password"/>
                        <span class="help-block"><form:errors path="passwordValid"></form:errors></span>
                    </div>
                    <div class="form-group ">
                        <input type="submit" value="CHANGE PASSWORD" class="btn btn-danger btn-lg center-block"/>
                    </div>
                    <div class="clearfix"/>
                </div>
            </form:form>
        </div>
        <div class="sub_container" style="text-align:center">
            <div>
                Already have an account? <a href="<c:url value='login' />">Sign In</a>
            </div>
            <div>
                Don't have an account? <a href="<c:url value='registration' />">Sign Up</a>
            </div>
        </div>
    </div>
</div>
</body>
</html>