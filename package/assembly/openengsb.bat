@echo off
rem
rem   Copyright 2010 OpenEngSB Division, Vienna University of Technology
rem
rem   Licensed under the Apache License, Version 2.0 (the "License");
rem   you may not use this file except in compliance with the License.
rem   You may obtain a copy of the License at
rem
rem       http://www.apache.org/licenses/LICENSE-2.0
rem
rem   Unless required by applicable law or agreed to in writing, software
rem   distributed under the License is distributed on an "AS IS" BASIS,
rem   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
rem   See the License for the specific language governing permissions and
rem   limitations under the License.
rem


setlocal
set CUR_DIR=%~dp0%
set HOME_DIR=%CUR_DIR%..

rem set java opts
set JAVA_OPTS=-server -XX:PermSize=256m -XX:MaxPermSize=256m -Xmx1024M -Dderby.system.home="%HOME_DIR%\data\derby" -Dderby.storage.fileSyncTransactionLog=true -Dcom.sun.management.jmxremote

rem start servicemix
servicemix.bat


