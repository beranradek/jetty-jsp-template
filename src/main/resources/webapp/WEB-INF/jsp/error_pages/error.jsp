<%@page pageEncoding="UTF-8" %>
<%@page isErrorPage="true" %>
<%@page session="false" %>
<%@include file="/WEB-INF/jsp/include.jsp" %>
<jsp:include page="/WEB-INF/jsp/header.jsp" />

<div class="container">

<h1>Unexpected error</h1>

<p>We apologize for the inconvenience, some unexpected error occurred. Please contact administrator of the site.
Error description: ${pageContext.exception.message}</p>

<p>The following can help you:</p>
<ul>
    <li>Return to <a href="javascript:history.go(-1)">previous page</a>,</li>
    <li>go to the <a href="<c:url value="/" />">home page</a> of the site.</li>
</ul>

</div>

<jsp:include page="/WEB-INF/jsp/footer.jsp" />