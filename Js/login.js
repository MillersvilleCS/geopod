$ (document).ready (function ()
{
	// Load the login form by default
	bindLoginEvents ();
	bindLogoutEvents ();
	bindRegistrationEvents ();
	bindPasswordRecoveryEvents ();
	bindNavEvents ();

	// Default to the login form.
	if (localStorage['validLogin'] == 'true')
	{
		// Show link to mission control and logout box
		showLogoutForm ();
	}
	else
	{
		// Show the login form by default
		showLoginForm ();
	}
});

// ** Visibility functions
function showLoginForm ()
{
	$ ("#loginForm").siblings ('form').hide ();
	$ ("#loginForm").show ();

	$ ("#nav-links").children ().show ();
	$ ("#loginLink").hide ();
}

function showLogoutForm ()
{
	$ ("#logoutForm").siblings ('form').hide ();
	$ ("#logoutForm").show ();

	$ ('.userName').html (localStorage['firstName']);

	$ ("#nav-links").children ().show ();
	$ ("#logoutLink").hide ();
}

function showRegisterForm ()
{
	$ ("#registerForm").siblings ('form').hide ();
	$ ("#registerForm").show ();

	$ ("#nav-links").children ().show ();
	$ ("#registerLink").hide ();

	var studentFormSelected = $ ("#student").prop ("selected");

	if (studentFormSelected)
	{
		$ ("#instructorFields").hide ();
	}
}

function showPasswordRecoveryForm ()
{
	$ ("#recoverPasswordForm").siblings ('form').hide ();
	$ ("#recoverPasswordForm").show ();

	$ ("#nav-links").children ().show ();
}

// ** Setup Navigation events

function bindNavEvents ()
{
	// Rebuild click events
	$ ("#loginLink a").click (function (event)
	{
		event.preventDefault ();
		showLoginForm ();
	});

	$ ("#registerLink a").click (function (event)
	{
		event.preventDefault ();
		showRegisterForm ();
	});

	$ ("#recoverPassword a").click (function (event)
	{
		event.preventDefault ();
		showPasswordRecoveryForm ();
	});
}

// ** Login functions

function bindLoginEvents ()
{
	$ ("#logoutForm").hide ();

	var loginFormOptions =
	{
		url : "./Php/login.php",
		type : "post",
		resetForm : true,
		success : function (responseText)
		{
			var response = JSON.parse (responseText);
			localStorage["validLogin"] = 'true';
			localStorage["userId"] = response.userId;
			localStorage["userName"] = response.userName;
			localStorage["firstName"] = response.firstName;
			localStorage["userStatusId"] = response.userStatusId;

			window.location = "./missionControl.html";
		},
		error : function (xhr, status, error)
		{
			var responseObject = JSON.parse (xhr.responseText);
			$ ("#serverError").html (
					'<p class="error">' + responseObject.message + '</p>');
		}
	};
	$ ("#loginForm").ajaxForm (loginFormOptions);
}

// ** Registration functions

function bindRegistrationEvents ()
{
	var formSubmitOptions =
	{
		url : "./Php/registration.php",
		type : "post",
		resetForm : true,
		beforeSubmit : function ()
		{
			// Ensure the form is valid
			return formIsValid = $ ('#registerForm').validate ().form ();
		},
		success : function ()
		{
			$ ("#serverResponse").html (
					'<h3 class="formHeader">Registration complete!</h3>');
			$ ("#serverResponse").show ();
			showLoginForm ();
		},
		error : function (xhr, status, error)
		{
			var responseObject = JSON.parse (xhr.responseText);
			$ ("#serverError").html (
					'<p class="error">Error ' + responseObject.code + ": "
							+ responseObject.message + '</a>');
		}

	};
	$ ("#registerForm").ajaxForm (formSubmitOptions);

	// Define the client-side form validation rules.
	$ ("#registerForm").validate (
	{
		// blow away all default options with these
		ignore : ":hidden",
		rules :
		{
			firstname :
			{
				required : true
			},
			lastname :
			{
				required : true
			},
			username :
			{
				required : true,
				remote : "./Php/checkUsername.php"
			},
			organization :
			{
				required : true
			},
			email :
			{
				required : true,
				email : true,
				remote : "./Php/checkEmail.php"
			},
			position :
			{
				required : true
			},
			phoneNumber :
			{
				required : true,
				phoneUS : true
			},
			website :
			{
				required : true
			}
		},
		messages :
		{
			username :
			{
				remote : jQuery.format ("{0} is already in use")
			},
			email :
			{
				required : "Please enter an email address",
				email : "Please enter a valid email address",
				remote : jQuery.format ("{0} is already in use")
			},
		}
	});

	$ ("#student").click (function ()
	{
		$ ("#instructorFields").hide ();
	});

	$ ("#instructor").click (function ()
	{
		$ ("#instructorFields").show ();
	});
	// End validation rules
}

// ** Registration functions

function bindPasswordRecoveryEvents ()
{
	var formSubmitOptions =
	{
		url : "Php/resetPassword.php",
		type : "post",
		resetForm : true, // reset on success
		success : function ()
		{
			$ ("#serverResponse").html (
					'<h3 class="formHeader">New password sent.</h3>');
			$ ("#serverResponse").show ();
			showLoginForm ();
		},
		error : function (xhr, status, error)
		{
			var responseObject = JSON.parse (xhr.responseText);
			$ ("#serverError").html (
					'<p class="error">Error: ' + responseObject.message
							+ '</a>');
		}

	};
	$ ("#recoverPasswordForm").ajaxForm (formSubmitOptions);

}

// ** Logout functions

function bindLogoutEvents ()
{
	var logoutFormOptions =
	{
		url : "Php/logout.php",
		resetForm : true, // reset on success
		success : function ()
		{
			localStorage.clear ();
			$ ("#serverResponse").html (
					'<h3 class="formHeader">Logged out.</h3>');
			$ ("#serverResponse").show ();
			showLoginForm ();
		}
	};
	$ ("#logoutForm").ajaxForm (logoutFormOptions);
}
