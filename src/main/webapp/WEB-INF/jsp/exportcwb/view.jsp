<%@page import="cn.explink.domain.User"%>
<%@page import="cn.explink.domain.EmailDate"%>
<%@page import="cn.explink.domain.ServiceArea"%>
<%@page import="cn.explink.util.StringUtil"%>
<%@page import="cn.explink.domain.Branch"%>
<%@page import="cn.explink.domain.Customer"%>
<%@page import="cn.explink.util.Page"%>
<%@page import="cn.explink.domain.CwbOrder"%>
<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%
  List<CwbOrder> orderlist = (List<CwbOrder>)request.getAttribute("orderlist");
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<HTML>
<HEAD>
<TITLE></TITLE>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<link rel="stylesheet" href="<%=request.getContextPath()%>/css/explink.css" type="text/css">
<link rel="stylesheet" href="<%=request.getContextPath()%>/css/2.css" type="text/css">
<link rel="stylesheet" href="<%=request.getContextPath()%>/css/index.css" type="text/css">
<link rel="stylesheet" href="<%=request.getContextPath()%>/css/reset.css" type="text/css">
<link rel="stylesheet" href="<%=request.getContextPath()%>/css/redmond/jquery-ui-1.8.18.custom.css" type="text/css" media="all" />
<script src="<%=request.getContextPath()%>/js/jquery-1.7.1.min.js" type="text/javascript"></script>
<script src="<%=request.getContextPath()%>/js/jquery-ui-1.8.18.custom.min.js" type="text/javascript"></script>
<script src="<%=request.getContextPath()%>/js/jquery.ui.datepicker-zh-CN.js" type="text/javascript"></script>
<script src="<%=request.getContextPath()%>/js/jquery.ui.message.min.js" type="text/javascript"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/swfupload/swfupload.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/jquery.swfupload.js"></script>
<script type="text/javascript"	src="<%=request.getContextPath()%>/js/My97DatePicker/WdatePicker.js"></script>
<script language="javascript" src="<%=request.getContextPath()%>/js/js.js"></script>
<script type="text/javascript">
$(function(){
	$("#beginemaildate").datepicker();
	$("#endemaildate").datepicker();
});
</script>
</HEAD>
<body style="background:#eef9ff">

   <div class="menucontant">
		<form name="form1" id="searchForm" action ="<%=request.getContextPath()%>/order/select/1" method = "post">					
           <table width="100%" height="23" border="0" cellpadding="0" cellspacing="5" class="right_set1">
				<tr id="customertr" class=VwCtr style="display:">
					<td>收件人姓名：<label for="select"></label><input type ="text" name ="consigneename" /></td>
					<td>收件人电话：<input type ="text" name ="consigneephone" /></td>
					<td>收件人手机：<input type ="text" name ="consigneemobile" /></td>
					<td>商品名称：<input type ="text" name ="sendcarname" /></td>
				</tr>
				<tr>
					<td colspan="2" valign="top">发货时间段：
						<input type ="text" name ="beginemaildate" id="strtime" class="Wdate"  onClick="WdatePicker()" value ="<%=StringUtil.nullConvertToEmptyString(request.getParameter("beginemaildate")) %>">到　
                        <input type ="text" name= "endemaildate" id="endtime" class="Wdate" onClick="WdatePicker()"  value ="<%=StringUtil.nullConvertToEmptyString(request.getParameter("endemaildate")) %>"></td>
					<td colspan="2" valign="top">订单号：
						<textarea rows="2" cols="30" name ="ordercwb" id ="ordercwb"><%=StringUtil.nullConvertToEmptyString(request.getParameter("ordercwb")) %></textarea>[多个订单号用回车键隔开]
					<input value="查询" type="submit" class="input_button2"/>
					<input type ="button" id="btnval" value="导出excel" class="input_button2" onclick="javascript:location.href='<%=request.getContextPath()%>/exportcwb/exportdate'"/>
					</td>
				</tr>
				<tr>
					<td colspan="4">
					   <table width="100%" border="0" cellspacing="1" cellpadding="0" class="table_2" id="gd_table2">
							<tr class="font_1">
								<td width="10%" height="38" align="center" valign="middle" bgcolor="#eef6ff">订单编号</td>
								<td align="center" valign="middle" bgcolor="#eef6ff">商品名称</td>
								<td width="8%" align="center" valign="middle" bgcolor="#eef6ff">货物金额</td>
								<td width="6%" align="center" valign="middle" bgcolor="#eef6ff">收件人姓名</td>
								<td width="10%" align="center" valign="middle" bgcolor="#eef6ff">收件人电话/手机</td>
								<td width="15%" align="center" valign="middle" bgcolor="#eef6ff">收件人地址</td>
								<td width="10%" align="center" valign="middle" bgcolor="#eef6ff">发货时间</td>
								<td width="8%" align="center" valign="middle" bgcolor="#eef6ff">小件员</td>
								<td align="center" valign="middle" bgcolor="#eef6ff">客户要求</td>
								<td align="center" valign="middle" bgcolor="#eef6ff">操作</td>
							</tr>
							<% for(CwbOrder c : orderlist){ %>
							<tr valign="middle">
										<td align="center" valign="middle" ><a href ="javascript:getViewCwbBox(<%=c.getCwb()%>);"><%=c.getCwb()%></a></td>
										<td align="center" valign="middle" ><%=c.getSendcarname()%></td>
										<td align="center" valign="middle" ><%=c.getCaramount()%></td>
										<td align="center" valign="middle" ><%=c.getConsigneename()%> </td>
										<td align="center" valign="middle" ><% if(c.getConsigneephone() != null && !"".equals(c.getConsigneephone())){%><%=c.getConsigneephone()%> / <%}%><%=c.getConsigneemobile()%></td>
										<td align="center" valign="middle" ><%=c.getConsigneeaddress()%></td>
										<td align="center" valign="middle" ><%=c.getEmaildate()%></td>
										<td align="center" valign="middle" ><a href ="javascript:getViewBox(<%=c.getDeliverid()%>);" ><%=c.getDeliverid()%></a></td>
										<td align="center" ><textarea rows="2" cols="20" id ="<%=c.getCwb()%>"><%=c.getCustomercommand()%></textarea></td>
										<td align="center" valign="middle" >
										  <input type ="button"  value ="保存并提醒" onclick="update('<%=c.getDeliverid()%>','<%=c.getCwb()%>',$('#<%=c.getCwb()%>').val())">
										</td>
									</tr>
							 <%} %> 
							
					   </table>
					</td>
				</tr>
			</table>
	   </form>			
   </div> 
</body>

</HTML>
