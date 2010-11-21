#!/bin/bash
#
# Copyright 2010 OpenEngSB Division, Vienna University of Technology
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

M2_REPO=~/.m2/repository

PLUGIN_VERSION="1.1.0-SNAPSHOT"
PLUGIN_GROUP_ID_PATH="org/openengsb/tooling/pluginsuite"
PLUGIN_ARTIFACT_ID="maven-openengsb-plugin"

GOAL="licenseCheck"

RELATIVE_PLUGINDIR=../../tooling/$PLUGIN_ARTIFACT_ID

function check_m2_userdir(){
  if [ -d $M2_REPO ]; then
    echo 0
  else
    echo 1
  fi
}

function check_for_plugin(){
  if [ -f $M2_REPO/$PLUGIN_GROUP_ID_PATH/$PLUGIN_ARTIFACT_ID/$PLUGIN_VERSION/"$PLUGIN_ARTIFACT_ID-$PLUGIN_VERSION.jar" ]; then
    echo 0
  else
    echo 1
  fi
}

function install_plugin(){
  cd $SCRIPT_DIR/$RELATIVE_PLUGINDIR
  echo "Installing $PLUGIN_ARTIFACT_ID!"
  mvn clean install
}

SCRIPT_DIR=`pwd`

if [ $1 ]; then
  M2_REPO=$1
fi

if [ ! `check_m2_userdir` -eq 0 ]; then
  echo "ERROR: Couldn't find $M2_REPO! If your local mvn repository is located anywhere else you can specify it by assemble.sh <path/to/repo>"
  exit 1
fi

if [ ! `check_for_plugin` -eq 0 ]; then
  install_plugin
fi

cd $SCRIPT_DIR/../..

echo "Invoking maven-openengsb-plugin"

mvn org.openengsb.tooling.pluginsuite:$PLUGIN_ARTIFACT_ID:$PLUGIN_VERSION:$GOAL
