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
    <meta name=gwt:property content='baseUrl=browser/'>
    <title>GWA-Portal</title>
    <link rel="icon" type="image/ico" href="img/favicon.ico"/>
    <link rel="stylesheet" href="<c:url value='css/entypo.min.css'/>"/>
    <link rel="stylesheet" href="<c:url value='css/animate.min.css'/>"/>
    <style>
        /* required because clean.css GWT theme was removed */
        table td {
            font-size: small;
        }
        /* for preloader */
        #loader-wrapper {
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            z-index: 1000
        }

        #loader {
            display: block;
            position: relative;
            left: 50%;
            top: 50%;
            width: 150px;
            height: 150px;
            margin: -75px 0 0 -75px;
            border-radius: 50%;
            border: 3px solid transparent;
            border-top-color: #3498db;
            -webkit-animation: spin 2s linear infinite;
            animation: spin 2s linear infinite;
            z-index: 1001
        }

        #loader:before {
            content: "";
            position: absolute;
            top: 5px;
            left: 5px;
            right: 5px;
            bottom: 5px;
            border-radius: 50%;
            border: 3px solid transparent;
            border-top-color: #e74c3c;
            -webkit-animation: spin 3s linear infinite;
            animation: spin 3s linear infinite
        }

        #loader:after {
            content: "";
            position: absolute;
            top: 15px;
            left: 15px;
            right: 15px;
            bottom: 15px;
            border-radius: 50%;
            border: 3px solid transparent;
            border-top-color: #f9c922;
            -webkit-animation: spin 1.5s linear infinite;
            animation: spin 1.5s linear infinite
        }

        @-webkit-keyframes spin {
            0% {
                -webkit-transform: rotate(0deg);
                -ms-transform: rotate(0deg);
                transform: rotate(0deg)
            }
            100% {
                -webkit-transform: rotate(360deg);
                -ms-transform: rotate(360deg);
                transform: rotate(360deg)
            }
        }

        @keyframes spin {
            0% {
                -webkit-transform: rotate(0deg);
                -ms-transform: rotate(0deg);
                transform: rotate(0deg)
            }
            100% {
                -webkit-transform: rotate(360deg);
                -ms-transform: rotate(360deg);
                transform: rotate(360deg)
            }
        }

        #loader-wrapper .loader-section {
            position: fixed;
            top: 0;
            width: 51%;
            height: 100%;
            background: #222;
            z-index: 1000;
            -webkit-transform: translateX(0);
            -ms-transform: translateX(0);
            transform: translateX(0)
        }

        #loader-wrapper .loader-section.section-left {
            left: 0
        }

        #loader-wrapper .loader-section.section-right {
            right: 0
        }

        .loaded #loader-wrapper .loader-section.section-left {
            -webkit-transform: translateX(-100%);
            -ms-transform: translateX(-100%);
            transform: translateX(-100%);
            -webkit-transition: all .7s .3s cubic-bezier(0.645, 0.045, 0.355, 1.000);
            transition: all .7s .3s cubic-bezier(0.645, 0.045, 0.355, 1.000)
        }

        .loaded #loader-wrapper .loader-section.section-right {
            -webkit-transform: translateX(100%);
            -ms-transform: translateX(100%);
            transform: translateX(100%);
            -webkit-transition: all .7s .3s cubic-bezier(0.645, 0.045, 0.355, 1.000);
            transition: all .7s .3s cubic-bezier(0.645, 0.045, 0.355, 1.000)
        }

        .loaded #loader {
            opacity: 0;
            -webkit-transition: all .3s ease-out;
            transition: all .3s ease-out
        }

        .loaded #loader-wrapper {
            visibility: hidden;
            -webkit-transform: translateY(-100%);
            -ms-transform: translateY(-100%);
            transform: translateY(-100%);
            -webkit-transition: all .3s 1s ease-out;
            transition: all .3s 1s ease-out
        }
    </style>
    <!-- FIXME use until https://github.com/eemi2010/gwt-tour/issues/1 is fixed -->

    <script type="text/javascript">
        window.erraiBusRemoteCommunicationEnabled = false;
        <sec:authorize access="isAuthenticated()">
        var userData = {'user': '<sec:authentication htmlEscape="false" property="principal.json"/>'};
        window.erraiBusRemoteCommunicationEnabled = true;
        </sec:authorize>
        var appData = {
            'jBrowseUrl': '${jbrowseUrl}',
            'geneInfoUrl': '${geneInfoUrl}',
            'gaTrackingId': '${gaTrackingId}',
            'contactEmail': '${contactEmail}'
        };
        <%@ include file="/browser/browser.nocache.js" %>
    </script>
    <script async type="text/javascript" src="<c:url value='js/favico.min.js' />"></script>
</head>

<body>
<div id="loader-wrapper">
    <div id="loader"></div>
    <div class="loader-section section-left"></div>
    <div class="loader-section section-right"></div>
</div>

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

