// JQuery extension to clear a form 
$.fn.clearForm = function ()
{
  return this.each (function ()
  {
    var type = this.type;
    var tag = this.tagName.toLowerCase ();
    if (tag == "form")
      return $ ('":input', this).clearForm ();
    if (type == "text" || type == "password" || tag == "textarea")
      this.value = "";
    else if (type == "checkbox" || type == "radio")
      this.checked = false;
    else if (tag == "select")
      this.selectedIndex = -1;
  });
};

// JQuery not required
// Setting up a missionControl "namespace"
(function (missionControl, undefined)
{
  // Set all of a form's elements to "" or nothing-selected
  missionControl.clearForm = function (form)
  {
    var elements = form.elements;
    for ( var i = 0; i < elements.length; ++i)
    {
      fieldType = elements[i].type.toLowerCase ();
      switch (fieldType)
      {
        case "text":
        case "password":
        case "textarea":
          elements[i].value = "";
          break;

        case "radio":
        case "checkbox":
          elements[i].checked = false;
          break;

        case "select-one":
        case "select-multi":
          elements[i].selectedIndex = -1;
          break;

        default:
          break;
      }
    }
  };

  missionControl.version = "1.0";

}) (window.missionControl = window.missionControl || { });

