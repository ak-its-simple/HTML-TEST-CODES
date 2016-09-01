$(document).ready(function(){
	
	$("#datepicker").datepicker();
	jQuery.validator.setDefaults({
		  // where to display the error relative to the element
		  errorPlacement: function(error, element) {
		      error.appendTo(element.parent().find('div.myErrors'));
		     }
		 });
	$(".sampleCode").validate({
		rules:{
			fullname:{
				required:true
			},
	        
	        
			mail:{
				required:true,
				email:true
			},
			myURL:{
				required:true,
				url:true
			},
			empId:{
				required:true,
			    minlength:6

			},
			suggestion: "required",
			explain: {
			     required: function(element) {
			      return $("input:radio[name=suggested]:checked").val() == 'Y';
			       }
			    }
		},
	        messages:{
	        fullname : "Please enter your name",
	        empId: {
	          required: "Please provide a employee Id",
	          minlength: "Your Id must be at least 6 characters long"
	        },
	        mail: "Please enter a valid email address",
	        myURL :"Please enter a valid URL"
		},
	        	   submitHandler: function(form) {
	        		      form.submit();
	        		      alert('Form validation was a success, please proceed!');
	  }	
	});
});
