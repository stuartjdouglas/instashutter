<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
    <%@ page import="me.stuartdouglas.stores.*" %>
    <%@page import="java.util.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Insert title here</title>
</head>
<body>


<jsp:include page="header.jsp" />
<%
	UserSession currentSession = (UserSession) session.getAttribute("LoggedIn");
	if (currentSession != null) {
		String userName = currentSession.getUsername();
		if (currentSession.getUserSession()) {%>
			<h3><%= userName %></h3>
			
			
			
			
			
			<%
        java.util.LinkedList<Pic> lsPics = (java.util.LinkedList<Pic>) request.getAttribute("Pics");
            if (lsPics == null) {
        %>
        <p>No Pictures found</p>
        <%
        } else {
            Iterator<Pic> iterator;
            iterator = lsPics.iterator();
            while (iterator.hasNext()) {
                Pic p = (Pic) iterator.next();

        %>
        <%-- <%= p.getTitle() %>
       	<a href="/instashutter/profile/<%= p.getPostedUsername() %>">@<%= p.getPostedUsername() %></a> --%>
       	Title:<%= p.getTitle() %>
        <a href="/instashutter/Image/<%=p.getSUUID()%>" ><img src="/instashutter/Thumb/<%=p.getSUUID()%>"></a><br/>
        
        
		<form name="input" action="/instashutter/Images/" method="get">
		
		<input type="text" name="user" value="<%=p.getSUUID()%>">
		<input type="submit" value="Submit">
		</form>
		
		
<%

            }
            }
        %>
			
			
			
			
			
		<%}} else {%>
		<p>You shouldn't be here</p>
		<%}%>
		




</body>
</html>