<!--[if IE]>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN">
<![endif]-->
<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>OMS定时任务管理</title>
<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/plug-in/easyui/themes/default/easyui.css">
<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/plug-in/easyui/themes/icon.css">
<script type="text/javascript" src="<%=request.getContextPath()%>/plug-in/easyui/jquery.min.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/plug-in/easyui/jquery.easyui.min.js"></script>

<script type="text/javascript">
var _ctx = "<%=request.getContextPath()%>";
var _sessionId = "<%=request.getSession().getId()%>";
var _dmpid = "<%=request.getSession().getAttribute("dmpid")%>";

 
function closeWindow() {
	document.forms['jobplanForm'].reset();
	$('#dlgCommon').dialog('close');
}

function showMsg(message) {
	$.messager.show({
		title : '提示',
		msg : message,
		timeout : 1000,
		style : {
			right : '',
			top : 200 + document.body.scrollTop
					+ document.documentElement.scrollTop,
			bottom : ''
		}
	});
}


function dateFormatter(value){
	 var unixTimestamp = new Date(value);  
     return unixTimestamp.toLocaleString();  
}

function refreshGridData(){
	$('#scheduler_dg').datagrid('reload');
}

function getJobNames(){
	var rows = $('#scheduler_dg').datagrid('getChecked');
	if(rows.length <= 0){
		$.messager.alert("提示", "请选中记录！", "warning");
		return null;
	}
	var jobNames = new Array();
	for (var i=0;i<rows.length;i++)
	{
		jobNames.push(rows[i].jobName);
	}
	return jobNames;
}

function doStart(){
	var jobNames = getJobNames();
	if(jobNames){
		$.messager.confirm("操作提醒", "您确定要启动选中的定时任务吗？", function (data) {
	        if (data) {
	        	var param = {
	        			jobNames:jobNames
	        	}
	        	$.ajax({
	        	    type: "post",
	        	    async: false, //设为false就是同步请求
	        	    url: _ctx + "/omsJob/startJob.action?"+Math.random(),
	        	    data:param,
	                datatype: "json",
	        	    success: function (result) {
	        	    	var rs=result;
// 	        	    	var rs = JSON.parse(result);
	        	    	
	        	    	if (!rs.status) {
	        	    		$.messager.alert("提示", rs.msg, "warning");
	        	    	} else {
	        	    		refreshGridData();
	        	    		showMsg("操作成功！");
	        	    	}
	        	    }
	        	});
	        } else {
	            return;
	        }
	    });
	}
}

function doPause(){
	var jobNames = getJobNames();
	if(jobNames){
		$.messager.confirm("操作提醒", "您确定要暂停选中的定时任务吗？", function (data) {
	        if (data) {
	        	var param = {
	        			jobNames:jobNames
	        	}
	        	$.ajax({
	        	    type: "post",
	        	    async: false, //设为false就是同步请求
	        	    url: _ctx + "/omsJob/pauseJob.action?"+Math.random(),
	        	    data:param,
	                datatype: "json",
	        	    success: function (result) {
// 	        	    	var rs = JSON.parse(result);
	        	    	var rs=result;
// 	        	    	alert(rs.status)
	        	    	if (!rs.status) {
	        	    		$.messager.alert("提示", rs.msg, "warning");
	        	    	} else {
	        	    		refreshGridData();
	        	    		showMsg("操作成功！");
	        	    	}
	        	    }
	        	});
	        } else {
	            return;
	        }
	    });
	}
}

function openModifyJobPlanDialog(){
	var rows = $('#scheduler_dg').datagrid('getChecked');
	if(rows.length != 1){
		$.messager.alert("提示", "请选中一条记录！", "warning");
		return null;
	}
	$("#jobName").val(rows[0].jobName);
	$("#jobPlan").val(rows[0].jobPlan);
	$('#dlgCommon').dialog('open').dialog('setTitle', '修改执行计划');
}

function doModifyJobPlan(){
	var jobName = $("#jobName").val();
	var jobPlan = $("#jobPlan").val();
	if(!jobName || jobName == ""){
		$.messager.alert("提示", "任务名称不能为空！", "warning");
		return;
	}
	if(!jobPlan || jobPlan == ""){
		$.messager.alert("提示", "任何计划不能为空！", "warning");
		return;
	}
	
	var param = {
			jobName:jobName,
			cronExpression:jobPlan
	}
	$.ajax({
	    type: "post",
	    async: false, //设为false就是同步请求
	    url: _ctx + "/omsJob/modifyCronExpression.action?"+Math.random(),
	    data:param,
        datatype: "json",
	    success: function (result) {
	    	var rs=result;
// 	    	var rs = JSON.parse(result);
	    	if (!rs.status) {
	    		$.messager.alert("提示", rs.msg, "warning");
	    	} else {
	    		closeWindow();
	    		refreshGridData();
	    		showMsg("操作成功！");
	    	}
	    }
	});
}

function doExcuteNow(){
	var rows = $('#scheduler_dg').datagrid('getChecked');
	if(rows.length != 1){
		$.messager.alert("提示", "请选中一条记录！", "warning");
		return null;
	}
	
	$.messager.confirm("操作提醒", "您确定要暂停选中的定时任务吗？", function (data) {
		if(data){
			var param = {
					jobName:rows[0].jobName,
			}
			$.ajax({
			    type: "post",
			    async: false, //设为false就是同步请求
			    url: _ctx + "/omsJob/executeJobNow.action?"+Math.random(),
			    data:param,
		        datatype: "json",
			    success: function (result) {
			    	var rs=result;
// 			    	var rs = JSON.parse(result);
			    	if (!rs.status) {
			    		$.messager.alert("提示", rs.msg, "warning");
			    	} else {
			    		showMsg("操作成功！");
			    	}
			    }
			});
		}else{
			return
		}
	
	});
}
</script>
</head>
<body class="easyui-layout" leftmargin="0" topmargin="0">
	<input id="dmpValue" type="hidden" value="${sessionScope.dmpid}"></input>
	<div id="alertMessage"></div>
	<div data-options="region:'center'" style="overflow-x: hidden; overflow-y: auto;">
		<table id="scheduler_dg" style="height: 480px" class="easyui-datagrid" toolbar="#scheduler_toolbar"
			pagination="false" showFooter="true"  fitColumns="true" 
			singleSelect="false" checkOnSelect="false" selectOnCheck="true" url="<%=request.getContextPath()%>/omsJob/findJobList.action">
			<thead>
				<tr>
					<th field="cb" checkbox="true"></th>
					<th field="jobId" width="50">任务ID</th>
					<th field="jobName" width="200">任务名称</th>
					<th field="jobDesc" width="200">任务描述</th>
					<th field="statusName" width="100">状态</th>
					<th field="jobPlan" width="200">执行计划</th>
					<th field="previousFireTime" formatter="dateFormatter" width="150">上次执行时间</th>
					<th field="nextFireTime" formatter="dateFormatter" width="150">下次时间</th>
					<th field="jobPlanDesc" width="100">执行计划描述</th>
				</tr>
			</thead>
		</table>
		<div id="scheduler_toolbar">
			<table  cellspacing="0" border="0" cellpadding="0" width="100%">
					<tr>
						<td colspan="8">
							<a href="#" class="easyui-linkbutton" onclick="refreshGridData();" iconCls="icon-reload">刷新</a>
							<a href="#" class="easyui-linkbutton" onclick="doStart();" iconCls="icon-edit">启动</a>
							<a href="#" class="easyui-linkbutton" onclick="doPause();" iconCls="icon-edit">暂停</a>
							<a href="#" class="easyui-linkbutton" onclick="doExcuteNow();" iconCls="icon-search">立即执行</a>
							<a href="#" class="easyui-linkbutton" onclick="openModifyJobPlanDialog();" iconCls="icon-edit">修改执行计划</a>
						</td>
					</tr>
			</table>
		</div>
	</div>


	<div id="dlgCommon" class="easyui-dialog" style="width: 500px; height: 268px; padding: 10px 10px"
		closed="true" buttons="#dlgCommon-buttons">
		<div style="margin-left: 15px;margin-top:10px" id="htmlContent">
		  	<form id="jobplanForm" action="">
				<table>
					<tr>
						<td>任务名称：</td>					
						<td><input id="jobName" name="jobName" readonly="readonly" style="width:350px;font-size: large;font-weight: bold;"/></td>
					</tr>
					<tr>
						<td style="">执行计划：<font color="red">*</font></td>					
						<td><input id="jobPlan" name="jobPlan" style="width:350px;font-size: large;font-weight: bold;" /></td>
					</tr>
				</table>
			</form>
		</div>
	</div>
	<div id="dlgCommon-buttons">
		<a href="#" class="easyui-linkbutton" onclick="doModifyJobPlan()" data-options="iconCls:'icon-save'">确定</a>
		<a href="#" class="easyui-linkbutton" onclick="closeWindow()" data-options="iconCls:'icon-cancel'">关闭</a>
		<!-- <div class="btn btn-default" id="modifyJobPlanBtn" onclick="doModifyJobPlan()">
			确定
		</div>
		<div class="btn btn-default" id="closeBtn" onclick="closeWindow()">
			关闭
		</div> -->
	</div>

</body>
</html>
