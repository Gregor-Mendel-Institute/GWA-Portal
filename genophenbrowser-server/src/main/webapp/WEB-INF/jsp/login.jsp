<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title>Sign In</title>
    <link rel="icon" type="image/ico" href="/img/favicon.ico"/>
    <link rel="stylesheet" href="//cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/3.3.1/css/bootstrap.min.css"/>
    <link rel="stylesheet" href="//cdnjs.cloudflare.com/ajax/libs/font-awesome/4.2.0/css/font-awesome.min.css"/>
    <link rel="stylesheet" href="<c:url value='/css/login.css'/>"/>
    <link rel="stylesheet" href="<c:url value='/css/bootstrap-social.min.css'/>"/>
    <link rel="stylesheet" href="<c:url value='/css/entypo.min.css'/>"/>
</head>

<body>
<div class="sign_in_container">
    <div class="content">
        <div>
            <form class="login-form" action="j_spring_security_check?spring-security-redirect=/${url}" method="post">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                <input type="hidden" name="isRegularLogin" value="true"/>
                <h1>Sign In</h1>

                <div>
                    <c:if test="${not empty param.login_error}">
                        <div class="alert alert-danger">
                            Your login attempt was not successful, try again.<br/><br/>
                            Reason: <c:out value="${SPRING_SECURITY_LAST_EXCEPTION.message}"/>.
                        </div>
                    </c:if>
                </div>
                <div class="login_fields">
                    <p>Sign in using your registered account:

                    <div class="form-group">
                        <div class="input-group input-group-lg">
                            <span class="input-group-addon e_icon-user"></span>
                            <input id="j_username" name="j_username" size="20" maxlength="50" type="text"
                                   placeHolder="name@example.com" class="form-control"/>
                        </div>
                    </div>
                    <div class="form-group">
                        <div class="input-group input-group-lg">
                            <span class="input-group-addon e_icon-key"></span>
                            <input id="j_password" name="j_password" size="20" maxlength="50" type="password"
                                   placeHolder="Password" class="form-control"/>
                        </div>
                    </div>
                    <div class="form-group">
                        <div style="display:inline-block;">
                            <a href="<c:url value='/forgot-password' />">Forgot password?</a>
                        </div>
                        <div class="checkbox remember_me" style="margin-left:180px;display:inline-block;">
                            <label>
                                <input type='checkbox' id="_spring_security_remember_me"
                                       name='_spring_security_remember_me'>
                                Keep me signed in
                            </label>
                        </div>
                        <input type="submit" value="Sign In" class="btn btn-success btn-lg"
                               style="display:inline-block;margin-left:18px"/>
                    </div>
                </div>
                <div class="clearfix"></div>
            </form>
        </div>
        <div class="sub_container">
            <p>Sign in using an existing 3rd party account:</p>
            <a class="btn btn-block btn-social btn-lg btn-google-plus" href="<c:url value='/auth/google' />">
                <i class="fa fa-google"></i> Sign in with Google
            </a>

            <p></p>
            <a class="btn btn-block btn-social btn-lg btn-github" href="<c:url value='/auth/github' />">
                <i class="fa fa-github"></i> Sign in with Github
            </a>

            <p></p>
            <a class="btn btn-block btn-social btn-lg btn-facebook" href="<c:url value='/auth/facebook' />">
                <i class="fa fa-facebook"></i> Sign in with Facebook
            </a>

            <p></p>
        </div>
        <div class="sub_container">
            <div class="form_login_info">Don't have an account? <a href="<c:url value='/registration' />">click here to
                sign up</a></div>
        </div>
    </div>
</div>

</body>
</html>