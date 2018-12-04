<%@ page import="app.Person" %>
<%@ page import="java.util.HashMap" %>
<?xml version="1.0" encoding="UTF-8" ?>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title>Информация о телефоне</title>
</head>
<body>

<%
    HashMap<String, String> jsp_parameters = new HashMap<String, String>();
    Person person = new Person();
    String error_message = "";

    if (request.getAttribute("jsp_parameters") != null) {
        jsp_parameters = (HashMap<String, String>) request.getAttribute("jsp_parameters");
    }

    if (request.getAttribute("person") != null) {
        person = (Person) request.getAttribute("person");
    }

    error_message = jsp_parameters.get("error_message");
%>

<table align="center" border="1" width="90%">
    <%
        if ((error_message != null) && (!error_message.equals(""))) {
    %>
    <tr>
        <td colspan="2" align="center"><span style="color:#ff0000"><%=error_message%></span></td>
    </tr>
    <%
        }
    %>
    <tr>
        <td>
            Информация о телефоне владельца:
            <%=person.getSurname()%>
            <%=person.getName()%>
            <%=person.getMiddlename()%>
        </td>
    </tr>
    <tr>
        <td>
            Номер:
            <input type="text" name="phoneNumber" value=" <%=person.getPhones()%> "/>
        </td>
    </tr>
    <tr>
        <td colspan="2" align="center">
            <input type="submit" name="<%=jsp_parameters.get("next_action")%>"
                   value="<%=jsp_parameters.get("next_action_label")%>"/>
            <br>
            <a href="<%=request.getContextPath()%>/?action=edit&id=<%=person.getId()%>">Вернуться к Данным о человеке</a>
        </td>
    </tr>
</table>

</body>
</html>
