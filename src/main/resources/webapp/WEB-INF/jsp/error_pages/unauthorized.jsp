<%@page pageEncoding="UTF-8" %>
<%@include file="/WEB-INF/jsp/include.jsp" %>
<jsp:include page="/WEB-INF/jsp/header.jsp" />
<h1>Access denied</h1>
<hr/>
<p>You have no permission for this action. The following can help you:</p>
<ul>
    <li>Try to login to application,</li>
    <li>return to <a href="javascript:history.go(-1)">previous page</a>,</li>
    <li>go to the <a href="<c:url value="/" />">home page</a> of the site.</li>
</ul>

<jsp:include page="/WEB-INF/jsp/footer.jsp" />
