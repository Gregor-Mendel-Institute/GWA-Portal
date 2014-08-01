<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ page session="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="content-type" content="text/html; charset=UTF-8"/>
    <meta name="google-site-verification" content="Z9H5Jrii90VQNMVAIR8xlTtjRJjH6Qm2NX35kLVh_OQ"/>
    <title>GWA-Portal</title>
    <link rel="icon" type="image/ico" href="/img/favicon.ico"/>
    <link rel="stylesheet" href="<c:url value='/css/entypo.css'/>"/>
    <link rel="stylesheet" href="<c:url value='/css/animate.min.css'/>"/>
    <script type="text/javascript">
        <sec:authorize access="isAuthenticated()">
        var userData = {'user': '<sec:authentication htmlEscape="false" property="principal.json"/>'};
        </sec:authorize>
        var appData = {'data': '${appData}', 'jBrowseUrl':${jbrowseUrl}}
    </script>
    <!-- FIXME use until https://github.com/eemi2010/gwt-tour/issues/1 is fixed -->
    <script async type="text/javascript"
            src="<c:url value='/js/favico.min.js' />"></script>
    <script type="text/javascript" src="<c:url value='/browser/browser.nocache.js'/>"></script>
</head>

<body>

<iframe src="javascript:''" id="__gwt_historyFrame" tabIndex='-1'
        style="position:absolute;width:0;height:0;border:0"></iframe>

<noscript>
    <div style="width: 22em; position: absolute; left: 50%; margin-left: -11em; color: red; background-color: white; border: 1px solid red; padding: 4px; font-family: sans-serif">
        Your web browser must have JavaScript enabled
        in order for this application to display correctly.
    </div>
</noscript>
</body>
</html>
