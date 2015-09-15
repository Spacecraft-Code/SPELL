#!/usr/bin/env bash

# Function to exit outputting an error message ###############################
die () {
  echo >&2 "$@"
  echo >&2 ""
  exit 1
}

# Function to show how to use the script #####################################
showHelp () {
  echo "Usage: `basename $0` <SRC_DIR> <PLUGIN_NAME> <DEST_DIR> <TEMPLATES_DIR> <ECLIPSE DIR> <LIBRARIES DIR> [NOPKGFLAG]"
}

# Function to exit outputting an error message and showing usage #############
dieWithHelp () {
  echo >&2 "$@"
  showHelp
  exit 1
}

# Function to check a directory exists and is readable #######################
checkDirectoryReadable () {
  DIRECTORY=$1
  test -d $DIRECTORY && test -r $DIRECTORY && test -x $DIRECTORY
}

# Function to check a file exists and is readable #######################
checkFileReadable () {
  FILE=$1
  test -f $FILE && test -r $FILE
}

# Function to check a directory exists and is writable #######################
checkDirectoryWritable () {
  DIRECTORY=$1
  test -d $DIRECTORY && test -w $DIRECTORY
}

# Function to convert relative paths to absolute ones ########################
getAbsPath () {
  DIRECTORY=$1
  [ ! -e $DIRECTORY ] && mkdir $DIRECTORY
  cd $DIRECTORY > /dev/null
  echo $PWD
  cd - > /dev/null
}

# Grab command line arguments ################################################
test $# -le 7 || dieWithHelp "Wrong number of arguments" 
test $# -ge 6 || dieWithHelp "Wrong number of arguments" 
SRC_DIR=`getAbsPath $1`
ECLIPSE_PLUGIN=$2
DEPLOY_DIR=`getAbsPath $3`
TEMPLATES_DIR=`getAbsPath $4`
ECLIPSE_BASE=`getAbsPath $5`/
ECLIPSE_LIBRARIES_DIR=`getAbsPath $6`
OBJS_DIR=$DEPLOY_DIR/obj/$ECLIPSE_PLUGIN
LIB_DIR=$DEPLOY_DIR/lib_plugins
if (( $# == 6 ))
then
	NO_PACKAGE=0
else
	NO_PACKAGE=1
fi

# Traces for debugging purposes. Uncomment if needed #########################
#echo "================================================================="
#echo "Source dir is $SRC_DIR"
#echo "Plugin name is $ECLIPSE_PLUGIN"
#echo "Objs dir is $OBJS_DIR"
#echo "Lib dir is $LIB_DIR"
#echo "Eclipse base is $ECLIPSE_BASE"
#echo "Deploy dir is $DEPLOY_DIR"
#echo "Templates dir is $TEMPLATES_DIR"
#echo "================================================================="


# Check stuff ################################################################
echo "    Checking stuff..."
checkDirectoryWritable $LIB_DIR/ || mkdir -p $LIB_DIR
checkDirectoryWritable $LIB_DIR || die "$LIB_DIR is not writable"
checkDirectoryReadable $TEMPLATES_DIR/ || die "Unable to find build file templates"
checkDirectoryReadable $SRC_DIR/$ECLIPSE_PLUGIN/ || die "Plugin directory $SRC_DIR/$ECLIPSE_PLUGIN/ not readable"
checkDirectoryReadable $ECLIPSE_BASE || die "It seems Eclipse is not installed at $ECLIPSE_BASE"
checkDirectoryReadable $ECLIPSE_LIBRARIES_DIR || die "$ECLIPSE_LIBRARIES_DIR directory should contain eclipse libraries"

# Check if compilation is needed #############################################
JARFILE=`find $DEPLOY_DIR -maxdepth 1 -type f -name "${ECLIPSE_PLUGIN}_*.jar"`
if [[ ! -z "$JARFILE" ]] # the jar file exists
then
    echo "    Plugin found: $JARFILE"
	CHECK=`find $SRC_DIR/$ECLIPSE_PLUGIN -type f -cnewer $JARFILE`
	if [[ -z "$CHECK" ]]
	then
		echo "    No need to recompile"
		exit 0
	fi
fi

# Build up variables #########################################################
FAKE_FEATURE=feature.$ECLIPSE_PLUGIN
BUILD_DIR=$OBJS_DIR
PLUGINS_DIR=$BUILD_DIR/plugins
FEATURES_DIR=$BUILD_DIR/features
FAKE_FEATURE_DIR=$FEATURES_DIR/$FAKE_FEATURE
CONFIG_DIR=$BUILD_DIR/config
MANIFEST_FILE=$PLUGINS_DIR/$ECLIPSE_PLUGIN/META-INF/MANIFEST.MF
ECLIPSE_LAUNCHER_JAR=`find $ECLIPSE_BASE -name org.eclipse.equinox.launcher*.jar | grep -v source`
BUILD_FILE=$CONFIG_DIR/build.xml
OUTPUT_ZIP_FILE=$BUILD_DIR/I.TestBuild/$FAKE_FEATURE-TestBuild.zip
COMPILATION_LOG=$DEPLOY_DIR/$ECLIPSE_PLUGIN.log
BUILD_CONFIG_TEMP=$OBJS_DIR/configuration_dir

# Function to extract the plugin dependencies from the manifest and...
# ...convert them to feature format. #########################################
dumpDependencies() {
  checkFileReadable $MANIFEST_FILE || return
  FLATTENED_MANIFEST=`cat $MANIFEST_FILE | xargs`
  DEPENDENCIES=`echo $FLATTENED_MANIFEST \
                 | sed 's/.*Require-Bundle:[ ]*//g' \
                 | sed 's/[a-zA-Z\-]*:.*//g' \
                 | sed 's/,[ ]*/\n/g' \
                 | sed 's/[ ]*$//g'`

  if [ "$DEPENDENCIES" != "" ]; then
    echo "<requires>"
    echo $DEPENDENCIES | sed 's/^\(.*\)$/      <import plugin=\"\1\"\/>/g'
    echo "</requires>"
  fi
}

# Function to unclutter compilation log. #####################################
digestCompilationLog() {
  grep '\(WARNING\|ERROR\)' \
    | sed 's/[ \t]*\[javac\] / /g' \
    | sed "s,$PLUGINS_DIR\/,,g" |uniq
}


# Function to check whether compilation was successful or not. ###############
isCompilationSuccessful() {
  grep '\<SUCCESSFUL\>' $COMPILATION_LOG > /dev/null   
}

# Function to perform compilation scenario set up ############################
setUpCompilationScenario() {
  echo "    Setting up compilation scenario..."
  [[ -d $OBJS_DIR ]] && rm -rf $OBJS_DIR
  mkdir -p $OBJS_DIR
  checkDirectoryWritable $BUILD_DIR || mkdir -p $BUILD_DIR
  mkdir -p $PLUGINS_DIR
  mkdir -p $FEATURES_DIR
  mkdir -p $FAKE_FEATURE_DIR
  mkdir -p $CONFIG_DIR
  [[ -d $BUILD_CONFIG_TEMP ]] && rm -rf $BUILD_CONFIG_TEMP
  mkdir -p $BUILD_CONFIG_TEMP

  find $TEMPLATES_DIR/config -type f -not -wholename "*/.svn*" -exec cp '{}' $CONFIG_DIR \;
  find $TEMPLATES_DIR/feature -type f -not -wholename "*/.svn*" -exec cp '{}' $FAKE_FEATURE_DIR \;

  # Replace template strings with actual plugin/feature name...
  find $BUILD_DIR -type f -not -wholename "*/.svn*" -exec sed -i '{}' -e "s/TEMPLATE_FEATURE/$FAKE_FEATURE/g" \;
  find $BUILD_DIR -type f -not -wholename "*/.svn*" -exec sed -i '{}' -e "s/TEMPLATE_PLUGIN/$ECLIPSE_PLUGIN/g" \;
  find $BUILD_DIR -type f -not -wholename "*/.svn*" -exec sed -i '{}' -e "s,TEMPLATE_BUILD_DIR,$BUILD_DIR,g" \;
  find $BUILD_DIR -type f -not -wholename "*/.svn*" -exec sed -i '{}' -e "s,TEMPLATE_LIB_DIR,$LIB_DIR,g" \;
  cat $FAKE_FEATURE_DIR/feature.xml.part1 > $FAKE_FEATURE_DIR/feature.xml
  dumpDependencies >> $FAKE_FEATURE_DIR/feature.xml
  cat $FAKE_FEATURE_DIR/feature.xml.part2 >> $FAKE_FEATURE_DIR/feature.xml
  rm $FAKE_FEATURE_DIR/feature.xml.part1 $FAKE_FEATURE_DIR/feature.xml.part2

  # Copy eclipse platform plugins to lib directory
  rsync -r $ECLIPSE_LIBRARIES_DIR/ $LIB_DIR

}

# Function to compile the eclipse plugin #####################################
compile() {
  echo "    Compiling (full log at $COMPILATION_LOG)..."
  rsync -rL --delete $SRC_DIR/$ECLIPSE_PLUGIN $PLUGINS_DIR
  java -Xms512m -Xmx512m -jar $ECLIPSE_LAUNCHER_JAR \
     -application org.eclipse.ant.core.antRunner \
     -configuration $BUILD_CONFIG_TEMP \
     -buildfile $BUILD_FILE \
     -Dbuilder=$CONFIG_DIR \
     | tee $COMPILATION_LOG \
     | digestCompilationLog
  isCompilationSuccessful || die "*** Compilation FAILED (Error 1) ***"
  echo "    *** Compilation SUCCESSFUL ***"
}

# Function to copy compilation results to lib directory ######################
deploy() {
  echo "    Deploying plugin (no package: $NO_PACKAGE)..."

  # Remove any previous occurence of the plugin, if any
  CHECK=`find $DEPLOY_DIR -name "${ECLIPSE_PLUGIN}_*"`
  [[ ! -z "$CHECK" ]] && rm -rf $CHECK

  # Obtain the jar file
  unzip -joqq $OUTPUT_ZIP_FILE "*.jar" -d $DEPLOY_DIR 
  JARFILE=`find $DEPLOY_DIR -name "${ECLIPSE_PLUGIN}_*"`
  echo "    Plugin package is $JARFILE"

  # If no-packaging is requested
  if (( $NO_PACKAGE == 0 ))
  then
    # Put the jar file in the lib dir also, to satisfy other plugin dependencies
    cp $JARFILE $LIB_DIR/.
  else
    # Unpack the jar file into a directory with the same name
    DIRNAME=`basename $JARFILE | sed 's/.jar//'`
    unzip -qq $JARFILE -d $DEPLOY_DIR/$DIRNAME
    rm -f $JARFILE
    # Copy the plugin directory in the lib dir also, to satisfy other plugin dependencies
    cp -r $DEPLOY_DIR/$DIRNAME $LIB_DIR/.
  fi
}

# Function to perform cleanup ################################################
cleanUp() {
  rm -rf $BUILD_DIR
  rm -rf $DEPLOY_DIR/obj
  rm -rf $BUILD_CONFIG_TEMP
}
  

# Main #######################################################################

echo "    Processing eclipse plugin $ECLIPSE_PLUGIN...."
setUpCompilationScenario
compile
deploy 
cleanUp
echo "    Done with eclipse plugin $ECLIPSE_PLUGIN."
# done.
