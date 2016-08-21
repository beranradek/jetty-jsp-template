<%@page pageEncoding="UTF-8" %>
<%@page session="false" %>
<%@include file="include.jsp" %>
<!DOCTYPE html>
<html class="no-js">
<head>
	<c:set var="defaultTitle" value="My Site" />
	<c:set var="defaultDescription" value="My Site Description" />
	<meta charset="utf-8"/>
	<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1"/>
	<meta name="viewport" content="width=device-width, initial-scale=1.0"/>
	<meta name="author" content="Author"/>
	<meta name="description" content="<c:choose><c:when test="${not empty param.description}">${param.description}</c:when><c:otherwise>${defaultDescription}</c:otherwise></c:choose>"/>
	<meta name="keywords" content="my, site"/>
	<meta property="og:title" content="<c:choose><c:when test="${not empty param.title}">${param.title}</c:when><c:otherwise>${defaultTitle}</c:otherwise></c:choose>" />
	<meta property="og:description" content="<c:choose><c:when test="${not empty param.description}">${param.description}</c:when><c:otherwise>${defaultDescription}</c:otherwise></c:choose>" />
	<meta property="og:type" content="website" />
	<meta property="og:site_name" content="${defaultTitle}" />
	<title><c:choose><c:when test="${not empty param.title}">${param.title}</c:when><c:otherwise>${defaultTitle}</c:otherwise></c:choose></title>
	
</head>
<body>
