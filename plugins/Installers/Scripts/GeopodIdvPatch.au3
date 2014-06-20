; Overwrite the local-visad.jar file.

; Includes
#include <FileConstants.au3>
#include <Array.au3>
#include <File.au3>
#include <IDVPathUtility.au3>

; Require Admin rights to execute
#RequireAdmin

; ************************************************************************************
; To change this to operate on a 64-bit OS with a 32-bit version of IDV, change
; $idvBeginPath to: $idvBeginPath = @HomeDrive & "\Program Files (x86)\"
$idvBeginPath = @HomeDrive & "\Program Files\"
$idvVersionToInstall = Null
; Check if IDV is installed.
$idvInstallDir = getIDVdir ($idvVersionToInstall, $idvBeginPath)
If $idvInstallDir <> Null Then
   ; Overwrite the local-visad.jar file.
   FileInstall (".\local-visad.jar", $idvInstallDir, $FC_OVERWRITE)
   MsgBox (0, "IDV Patch", "The IDV patch was successfully installed for " _
		   & $idvVersionToInstall & "!")
Else
   ; Display message to install IDV.
   MsgBox (0, "No IDV Folder", "Could not find the IDV folder. " _
		   & @CRLF & @CRLF & "IDV version " & $idvMinimumVersion & " or greater must be installed." _
		   & @CRLF & @CRLF & "Install IDV, then try again.")
EndIf