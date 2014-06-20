; ************************************************************************************
; Function to return the path and current version of the IDV directory
;
; Return:
; 		The path of the newest IDV directory  if it exists,  and Null if it does not.
; $idvVersionToInstall:
;		will contain the most current version of IDV installed when the user has
;		multiple versions installed.
; ************************************************************************************

; Includes
#include <Array.au3>
#include <File.au3>

; Constants
Global Const $idvMinimumVersion = "5.0"
Local Const $visadFileName = "\local-visad.jar"

; ************************************************************************************
Func getIDVdir (ByRef $idvVersionToInstall, $idvBeginPath)
   ; Check in the 'Program Files' directory
   If FileExists ($idvBeginPath & "IDV_*") Then
	  $idvVersionList = _FileListToArray ($idvBeginPath, "IDV_*", 2)
   Else
	  ; No installed versions were found.
	  return Null
   EndIf

    ; Create an array containing only numeric values
   $modifiedVersions = _FileListToArray($idvBeginPath, "IDV_*", 2)
   For $i = 1 To $modifiedVersions[0]
	  $modifiedVersions[$i] = StringReplace($modifiedVersions[$i], "IDV_", "")
	  ; Search for and replace u's since IDV normally uses 'u' for updated versions
	  $modifiedVersions[$i] = StringReplace($modifiedVersions[$i], "u", "")
   Next
   ; Replace the size index
    $modifiedVersions[0] = 0

   ; Do not return a path if the minimum version of IDV is not installed.
   $arrayMaxIndex = _ArrayMaxIndex ($modifiedVersions, 1)
   If Number($modifiedVersions[$arrayMaxIndex]) < Number($idvMinimumVersion) Then
	  Return Null
   EndIf

   ; Return the most recent version.
   $idvVersionToInstall = $idvVersionList[$arrayMaxIndex]
   $idvPath = $idvBeginPath & $idvVersionToInstall & $visadFilename
   Return ($idvPath)
EndFunc

