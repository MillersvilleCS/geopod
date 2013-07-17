REM
REM This batch file copies patched files into the IDV directory. 
REM This is required for the Geopod plugin to work correctly.
REM
REM This script likely needs to be run with Administrator privlages
REM so it can write to the Program Files directory.
REM
@echo off

IF EXIST "C:\Program Files\IDV_3.0b1\" (

echo Trying to replace IDV files...
xcopy %0\..\runIDV.bat "C:\Program Files\IDV_3.0b1\" /Y
xcopy %0\..\local-visad.jar "C:\Program Files\IDV_3.0b1\" /Y
echo Done!

) ELSE (

echo "Error: IDV directory not found. Aborting copy."

)

PAUSE