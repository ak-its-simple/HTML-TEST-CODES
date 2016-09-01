<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Demo JSP page</title>
<link rel="stylesheet" type="text/css" href="master.css"></link>
<link rel="stylesheet" href="//code.jquery.com/ui/1.12.0/themes/base/jquery-ui.css">
<link rel="stylesheet" href="style.css">
<script type="text/javascript"
	src="http://ajax.googleapis.com/ajax/libs/jquery/2.1.3/jquery.min.js">
</script>
<script
 src="http://ajax.microsoft.com/ajax/jquery.validate/1.7/jquery.validate.js"
 type="text/javascript">
 </script>
<script src="https://code.jquery.com/ui/1.12.0/jquery-ui.js"></script>


<script src="script.js"></script>
</head>
<body>
	<div class="allContent">
	<%@include file="/header.jsp" %>
	<div class="myContent">
	<form class="sampleCode" method="post" action="RegistrationController">
	
		<table class ="myTable" align="center" name="t1">
		
			<tr>
				<td>&nbsp;</td>
			</tr>
			<tr>	
				<td>Name</td>
				<td><input class="textField" type="text" name="fullname" size="40" value="" placeholder="Full Name"/>
					<div class="myErrors"></div></td>
			</tr>
		
			<tr>
				<td>Email</td>
				<td><input class="textField" type="text" name="mail" size="40" value="" placeholder="abc@domain"/>
					<div class="myErrors"></div></td>
			</tr>

			<tr>
				<td>Website</td>
				<td><input class="textField" type="text" name="myURL" size="40" value="" placeholder="www.name.domain"/>
					<div class="myErrors"></div></td>
			</tr>

			<tr>
				<td>Date of Birth</td>
				<td><input class="textField" type="text" id="datepicker" name="dob" size="10" value=""/>
					<div class="myErrors"></div></td>
			</tr>

			<tr>
				<td>Employee Id</td>
				<td><input class="textField" type="text" name="empId" size="40" value=""/>
				<div class="myErrors"></div>
				</td>
			</tr>

			<tr>
				<td>Gender</td>
				<td>
					<label><input type="radio" name="myRadioButton" value="M" /> Male</label>
					<label><input type="radio" name="myRadioButton" value="F" /> Female</label> 
				<div class="myErrors"></div>
				</td>
			</tr>
			<tr>
				<td>Projects</td>
				<td>
					<label><input type="checkbox" name="myCheckbox" value="project1" /> Cetera</label>
			       	<label><input type="checkbox" name="myCheckbox" value="project2" /> Citibank</label>
			       	<label><input type="checkbox" name="myCheckbox" value="project3" /> SnagFilms</label>
			       	<label><input type="checkbox" name="myCheckbox" value="project4" /> Belden </label>
			       <div class="myErrors"></div>
				</td>
			</tr>
			<tr>
				<td>Vertical</td>
				<td>
					<select class="textField" id="mySelect" name="mySelect" title="Please select your vertical" >
						<option value="">Choose your vertical</option>
						<option value="Financial Services">Financial Services</option>
						<option value="Communication Engineering">Communication Engineering</option>
						<option value="Pharma and Life Sciences">Pharma and Life Sciences</option>
					</select>
					<div class="myErrors"></div>
				</td>
			</tr>
			<tr>
			<td>Do you want to leave any suggestions ?</td>
			<td>
				<label><input type="radio" name="suggestion" value="Y" /> Yes</label>
				<label><input type="radio" name="suggestion" value="N" /> No</label>
				<div class="myErrors"></div>
			</td>
			</tr>
			<tr>
			<td>If yes, please write in detail</td>
			<td>
				
        		<textarea id="explain" name="explain" rows="5" cols="60" maxlength="300"></textarea>
        		<div class="myErrors"></div>
        		
       		</td>
			</tr>
			<tr>
       			<td colspan="2">
       				 &nbsp;
       			</td>
      		</tr>
      		<tr>
       		<td>
        			&nbsp;
       		</td>
       		<td>
       			 <input id="submitForm" type="submit" value="Submit Form" />
      		 </td>
      		</tr>
      		
		</table>
		
	</form>
	</div>
</div>
<%@include file="/footer.jsp"%>
<div></div>
</body>
</html>