<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
</head>
<body>
<form action="${pageContext.request.contextPath }/orderflow/createDelete" method="post">
<textarea rows="20" cols="16" name="cwbs"></textarea>
<input type="text" name="name"/>
<input type="submit"/>
</form>
<hr>
${sqls}
</body>
</html>