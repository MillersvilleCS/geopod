; Install the Geopod plugin.

; Includes
#include <FileConstants.au3>
#include <IdvPathUtility.au3>

; Constants
Local Const $pluginDir = @UserProfileDir & "\.unidata\idv\DefaultIdv\plugins"
Local Const $pluginFilename = "\GeopodPlugin.jar"

; ************************************************************************************
; Check if the Geopod Plugin directory exists.
If FileExists($pluginDir) Then
   ; To change this to operate on a 64-bit OS with a 32-bit version of IDV, change
   ; $idvBeginPath to: $idvBeginPath = @HomeDrive & "\Program Files (x86)\"
   $idvBeginPath = @HomeDrive & "\Program Files\"
   $idvVersionToInstall = Null
   ; Check if the required version of IDV is installed.
   $idvInstallDir = getIDVdir ($idvVersionToInstall, $idvBeginPath)
   If $idvVersionToInstall <> Null Then
	  ; Install the Geopod plugin.
	  FileInstall (".\GeopodPlugin.jar", $pluginDir & $pluginFilename, $FC_OVERWRITE)
	  MsgBox (0, "Geopod Installer", "The Geopod plugin was successfully installed for " _
			  & $idvVersionToInstall & "!")
   Else
	  ; Display message to install IDV.
	  MsgBox (0, "No IDV Folder", "Could not find the IDV folder. " _
			  & @CRLF & @CRLF & "IDV version " & $idvMinimumVersion & " or greater must be installed." _
			  & @CRLF & @CRLF & "Please contact your System Administrator for IDV to be installed.")
   EndIf
Else
   ; Display message for a missing Plugins folder.
   MsgBox (0, "No Plugins Folder", "Could not find the Plugins folder: " _
		  & @CRLF & @CRLF & $pluginDir _
		  & @CRLF & @CRLF & "Ensure IDV is properly installed." _
		  & @CRLF & @CRLF & "Run IDV once, then try again.")
EndIf