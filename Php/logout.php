<?php

ini_set ('display_errors', 1);
session_start ();

// Destroy all of the session variables
$_SESSION = array (); 
// Reset the session ID
session_destroy (); 
