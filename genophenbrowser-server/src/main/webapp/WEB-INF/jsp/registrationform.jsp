<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title>Sign up</title>
    <link rel="icon" type="image/ico" href="img/favicon.ico"/>
    <link rel="stylesheet" href="//netdna.bootstrapcdn.com/bootstrap/3.0.2/css/bootstrap.min.css">
    <link rel="stylesheet" href="<c:url value='css/login.css'/>"/>
    <link rel="stylesheet" href="<c:url value='css/entypo.min.css'/>"/>
    <script type="text/javascript">
        window.onload = function () {
            document.getElementById("firstName").focus();
        }
    </script>
</head>

<body>
<div class="sign_up_container">
    <div class="content">
        <div>
            <form:form modelAttribute="registration">
                <h1>Create Your Account</h1>

                <div class="sub_container">
                    <p>Create your account</p>
                    <spring:bind path="registration">
                        <spring:hasBindErrors name="registration">
                            <div class="alert alert-danger"><form:errors htmlEscape="false"
                                                                         path="*"></form:errors></div>
                        </spring:hasBindErrors>
                    </spring:bind>
                    <div class="form-group ${requestScope['org.springframework.validation.BindingResult.registration'].hasFieldErrors('firstname') ? 'has-error' : ''}">
                        <label class="sr-only" for="firstname">Firstname</label>
                        <form:input path="firstname" size="40" name="firstname" placeholder="Firstname"
                                    class="form-control input-lg"/>
                        <span class="help-block"><form:errors path="firstname"></form:errors></span>
                    </div>
                    <div class="form-group ${requestScope['org.springframework.validation.BindingResult.registration'].hasFieldErrors('lastname') ? 'has-error' : ''}">
                        <label class="sr-only" for="lastname">Lastname</label>
                        <form:input path="lastname" size="40" name="lastname" placeholder="Lastname"
                                    class="form-control input-lg"/>
                        <span class="help-block"><form:errors path="lastname"></form:errors></span>
                    </div>
                    <div class="form-group ${requestScope['org.springframework.validation.BindingResult.registration'].hasFieldErrors('email') ? 'has-error' : ''}">
                        <label class="sr-only" for="email">E-mail</label>
                        <form:input path="email" size="40" name="email" placeholder="Email"
                                    class="form-control input-lg"/>
                        <span class="help-block"><form:errors path="email" htmlEscape="false"></form:errors></span>
                    </div>
                    <div class="form-group ${requestScope['org.springframework.validation.BindingResult.registration'].hasFieldErrors('password') ? 'has-error' : ''}">
                        <label class="sr-only" for="password">Password</label>
                        <form:input showPassword="true" path="password" size="40" name="password" placeholder="Password"
                                    class="form-control input-lg" type="password"/>
                        <span class="help-block"><form:errors path="password"></form:errors></span>
                    </div>
                    <div class="form-group ${requestScope['org.springframework.validation.BindingResult.registration'].hasFieldErrors('passwordValid') || requestScope['org.springframework.validation.BindingResult.registration'].hasFieldErrors('confirmPassword') ? 'has-error' : ''}">
                        <label class="sr-only" for="confirmPassword">Password confirm</label>
                        <form:input showPassword="true" path="confirmPassword" size="40" name="confirmPassword"
                                    placeholder="Password Confirm" class="form-control input-lg" type="password"/>
                        <span class="help-block"><form:errors path="passwordValid"></form:errors>
                        </span>
                    </div>
                    <div class="form-group">
                        <input type="submit" value="Register" class="btn btn-success btn-lg pull-right"/>
                    </div>
                    <div class="clearfix"/>
                </div>
            </form:form>
        </div>
        <div class="sub_container" style="text-align:center">
            <div>
                <a href="<c:url value='/forgot-password' />">Forgot password?</a>
            </div>
            <div>
                Already have an account? <a href="<c:url value='/login' />">Sign In</a>
            </div>
        </div>
    </div>
</div>
</body>
</html>