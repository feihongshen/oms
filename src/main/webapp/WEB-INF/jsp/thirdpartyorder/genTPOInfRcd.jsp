<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>重推外单</title>
<script type="text/javascript" src="<%=request.getContextPath()%>/plug-in/easyui/jquery.min.js"></script>
<script type="text/javascript">
var _ctx = "<%=request.getContextPath()%>";
function submit(){
	var cwbs = $("#cwbs").val();
	var acceptDept = $("#acceptDept").val();
	if(!acceptDept || acceptDept == ""){
		alert("接单机构不能为空");
		return;
	}
	if(!cwbs || cwbs == ""){
		alert("订单号不能为空");
		return;
	}
	
	var param = {
			cwbs:cwbs,
			acceptDept:acceptDept
	}
	$("#submitBtn").attr('value','请稍后...'); 
	$("#submitBtn").attr('disabled',true); 
	$.ajax({
	    type: "post",
	    async: false, //设为false就是同步请求
	    url: _ctx + "/thirdParty/insertToInfTable.action?"+Math.random(),
	    data:param,
        datatype: "text",
	    success: function (result) {
	    	$("#errorResult").val(result);
	    	if(!result){
	    		$("#errorResult").val("全部插入成功！");
	    	}
	    	$("#submitBtn").attr('value','提交'); 
	    	$("#submitBtn").attr('disabled',false); 
	    }
	});
}

</script>
</head>
<body>
	<table>
		<tr>	
			<td><span>接单机构（导入库房）tpsbranchcode：</span><input id="acceptDept" type="text"></td>
		</tr>
		<tr>
			<td><textarea id="cwbs" rows="20" cols="50">请输入订单号...</textarea></td>
			
			<td><textarea id="errorResult" rows="20" cols="50">此处显示插入失败的单号</textarea>
		</tr>
		<tr>
			<td align="right">
				<input type="button" id="submitBtn" onclick="submit();" value="提交">
			</td>
			<td>
			</td>
		</tr>
	</table>
</body>
</html>