<%--
  Created by IntelliJ IDEA.
  User: uemit.seren
  Date: 27.11.13
  Time: 10:40
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html>
<head>
    <link rel="icon" type="image/ico" href="/img/favicon.ico"/>
    <link rel="stylesheet" href="//netdna.bootstrapcdn.com/bootstrap/3.0.2/css/bootstrap.min.css">
    <link rel="stylesheet" href="<c:url value='/css/entypo.min.css' />"/>
    <link rel="stylesheet" href="<c:url value='/css/error.css' />"/>
    <link rel="stylesheet" href="https://fonts.googleapis.com/css?family=Open+Sans:400,600,800">
    <title>Error</title>

</head>

<body>
<div class="error_container">
    <div class="error_box">
        <div class="small_text">
            Whoa! What are you doing here?
        </div>
        <div class="big_text">
            404
        </div>
        <div class="small_text">You are not where you're supposed to be</div>
        <div class="error_actions">
            <a href="/" class="btn btn-warning btn-lg">
                <i class="e_icon-left-open"></i>
                &nbsp;
                Back to Home
            </a>
            <a href="mailto:uemit.seren@gmail.com" target="_blank" class="btn btn-default btn-lg">
                <i class="e_icon-mail"></i>
                &nbsp;
                Contact Support
            </a>
        </div>
    </div>
</div>
</body>
</html>