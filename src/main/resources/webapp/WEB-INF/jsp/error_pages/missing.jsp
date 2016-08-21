<%@page pageEncoding="UTF-8" %>
<%@page session="false" %>
<%@include file="/WEB-INF/jsp/include.jsp" %>
<jsp:include page="/WEB-INF/jsp/header.jsp" />

<div class="container">

<h1>Requested page does not exist</h1>
<hr/>
<p>We apologize for the inconvenience, requested page or another resource of information does not exist.</p>
<p>The following can help you:</p>
<ul>
    <li>If you entered the address into your browser manually, check the spelling,</li>
    <li>return to <a href="javascript:history.go(-1)">previous page</a>,</li>
    <li>go to the <a href="<c:url value="/" />">home page</a> of the site.</li>
</ul>

<p>Error 404 - page not found</p>

</div>

<jsp:include page="/WEB-INF/jsp/footer.jsp" />
