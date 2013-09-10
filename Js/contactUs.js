$ (document).ready (function ()
{
	// Setup the form for ajax submission
	var formSubmitOptions =
	{
		url : "./Php/sendContactForm.php",
		type : "post",
		// reset on success
		resetForm : true,
		beforeSubmit : function ()
		{
			// Ensure the form is valid.
			return $ ('#contactForm').validate ().form ();
		},
		success : messageSent,
		error : function (jqXHR, textStatus, errorThrown)
		{
			handleError (jqXHR, textStatus, errorThrown);
		}

	};
	$ ("#contactForm").ajaxForm (formSubmitOptions);

	$ ("#contactForm").validate ();

	$ ('div.header').load ('homeHeader.html', function ()
	{
		$ ('div.titleText').html ("Contact Us");
		$ ('.contactUsNavLink').addClass('selectedLink');
	});
});

function messageSent ()
{
	$ ("#contactForm").hide ("fade", "slow");

	$ (".formHeader").hide ("fade", "fast");
	$ (".formHeader").replaceWith ('<h3 class="formHeader">Message sent.</h3>');
	$ (".formHeader").show ("puff", "fast");
}

function handleError (xhr, status, error)
{

	var responseObject = JSON.parse (xhr.responseText);

	$ (".formHeader").append (
			'<p class="error">Error: ' + responseObject.message + '</a>');
}