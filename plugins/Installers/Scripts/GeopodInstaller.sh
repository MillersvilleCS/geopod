#!/bin/sh

## Author: Millersville Geopod Team 2014
## File name: Idvinstaller.sh
##
## This script will copy two plugins to IDV directories so that IDV 
## will run Geopod.
##
## Prior to running this script, run the idv install script.
## When IDV prompts you to create a directory, use the default directory
## name and place it on the first level of your home directory.

##########################################################################
## Constants
IDV_MINIMUM_VERSION=5.0
PLUGIN_DIR=~/.unidata/idv/DefaultIdv/plugins/
GEOPOD_PLUGIN=GeopodPlugin.jar
VISAD_JAR=local-visad.jar

##########################################################################
function displayNoCompatibleIdvMessage ()
{
    echo "Could not find the IDV install directory."
    echo "IDV version $IDV_MINIMUM_VERSION or greater must be installed."
    echo "Install IDV, then try again."
}

##########################################################################
## Check if the Geopod Plugin directory exists.
if [ -d $PLUGIN_DIR ]; then

    ##Check if IDV is installed.
    if [ -d ~/IDV_* ]; then
	
	idvDirs=(~/IDV_*)

	## Find the IDV version most recently modified for installing.
	newestIdvPath=${idvDirs[0]}
	for dir in ${idvDirs[*]:1}
	do
	    if [ $dir -nt $newestIdvPath ]; then
		newestIdvPath=$dir
	    fi
	done
	
	## Check if bc is installed.
	## Without it, the current version of IDV cannot be evaluated to 
	## determine if a compatible version is installed.
	which bc >& /dev/null
	whichReturnStatus=$?

	if [ $whichReturnStatus == 1 ]; then
	  echo "No installed version of 'bc' was detected."
	  echo "Please install bc before continuing, or view Geopod's"
	  echo "\"If you encounter problems section\" to manually install Geopod."
	  exit
	fi

	## Check if the version is compatible, substituting
	## '' for 'u' to perform a numeric comparison.
	currentIdvVersion=$(basename $newestIdvPath | sed -e s/IDV_// -e s/u//)
	## Use the binary calculator to compare as floating point numbers.
	if [ "$(bc <<< "$currentIdvVersion < $IDV_MINIMUM_VERSION")" != 0 ]; then
	    displayNoCompatibleIdvMessage
	    exit
	fi

	## Install the plugin and overwrite the local-visad.jar file.
	mv ${VISAD_JAR} ${newestIdvPath}
	mv ${GEOPOD_PLUGIN} ${PLUGIN_DIR}
	echo "Geopod was successfully installed for $(basename $newestIdvPath)!"

    ## No installed version of IDV was found.  
    else
	displayNoCompatibleIdvMessage
    fi
else
    ## Display a message for a missing Plugins folders.
    echo "Could not find the plugins folder:" 
    echo $PLUGIN_DIR
    echo "Ensure IDV is properly installed."
    echo "Run IDV once, then try again."
fi


