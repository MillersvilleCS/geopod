; Install the Geopod plugin and overwrite the local-visad.jar file with the modified class files.

; Includes
#include <FileConstants.au3>
#include <Array.au3>
#include <File.au3>
#include <IdvPathUtility.au3>

; Require Admin rights to execute
#RequireAdmin

; Constants
Local Const $pluginDir = @UserProfileDir & "\.unidata\idv\DefaultIdv\plugins"
Local Const $pluginFilename = "\GeopodPlugin.jar"

; ************************************************************************************
; Check if the Geopod Plugin directory exists.
If FileExists($pluginDir) Then
   $idvBeginPath = @HomeDrive & "\Program Files\"
   $idvVersionToInstall = Null

   ; Check if IDV is installed.
   $idvInstallDir = getIDVdir ($idvVersionToInstall, $idvBeginPath)
   If $idvInstallDir <> Null Then
	  ; Install the Geopod plugin and overwrite the local-visad.jar file.
	  FileInstall (".\GeopodPlugin.jar", $pluginDir & $pluginFilename, $FC_OVERWRITE)
	  FileInstall (".\local-visad.jar", $idvInstallDir, $FC_OVERWRITE)
	  MsgBox (0, "Geopod Installer", "Geopod was successfully installed for " _
			  & $idvVersionToInstall & "!")
   Else
	  ; Display message to install IDV.
	  MsgBox (0, "No IDV Folder", "Could not find the IDV folder. " _
	   & @CRLF & @CRLF & "IDV version " & $idvMinimumVersion & " or greater must be installed." _
	   & @CRLF & @CRLF & "Install IDV, then try again.")
   EndIf
Else
   ; Display message for a missing Plugins folder.
   MsgBox (0, "No Plugins Folder", "Could not find the Plugins folder: " _
		   & @CRLF & @CRLF & $pluginDir _
		   & @CRLF & @CRLF & "Ensure IDV is properly installed." _
		   & @CRLF & @CRLF & "Run IDV once, then try again.")
EndIf