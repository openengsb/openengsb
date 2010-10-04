@echo off
@REM 
@REM Copyright 2010 OpenEngSB Division, Vienna University of Technology
@REM
@REM Licensed under the Apache License, Version 2.0 (the "License");
@REM you may not use this file except in compliance with the License.
@REM You may obtain a copy of the License at
@REM
@REM   http://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing, software
@REM distributed under the License is distributed on an "AS IS" BASIS,
@REM WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@REM See the License for the specific language governing permissions and
@REM limitations under the License.
@REM

if not "%ECHO%" == "" echo %ECHO%

setlocal
set DIRNAME=%~dp0%
set PROGNAME=%~nx0%
set ARGS=%*

title Karaf

goto BEGIN

:warn
    echo %PROGNAME%: %*
goto :EOF

:BEGIN

@REM # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #

if not "%KARAF_HOME%" == "" (
    call :warn Ignoring predefined value for KARAF_HOME
)
set KARAF_HOME=%DIRNAME%..
if not exist "%KARAF_HOME%" (
    call :warn KARAF_HOME is not valid: %KARAF_HOME%
    goto END
)

if not "%KARAF_BASE%" == "" (
    if not exist "%KARAF_BASE%" (
       call :warn KARAF_BASE is not valid: %KARAF_BASE%
       goto END
    )
)
if "%KARAF_BASE%" == "" (
  set KARAF_BASE=%KARAF_HOME%
)

if not "%KARAF_DATA%" == "" (
    if not exist "%KARAF_DATA%" (
        call :warn KARAF_DATA is not valid: %KARAF_DATA%
        goto END
    )
)
if "%KARAF_DATA%" == "" (
    set KARAF_DATA=%KARAF_BASE%\data
)        

set LOCAL_CLASSPATH=%CLASSPATH%
set DEFAULT_JAVA_OPTS=-server -Xmx512M -Dderby.system.home="%KARAF_DATA%\derby" -Dderby.storage.fileSyncTransactionLog=true -Dcom.sun.management.jmx@REMote
set CLASSPATH=%LOCAL_CLASSPATH%;%KARAF_BASE%\conf
set DEFAULT_JAVA_DEBUG_OPTS=-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005

if "%LOCAL_CLASSPATH%" == "" goto :KARAF_CLASSPATH_EMPTY
    set CLASSPATH=%LOCAL_CLASSPATH%;%KARAF_BASE%\conf
    goto :KARAF_CLASSPATH_END
:KARAF_CLASSPATH_EMPTY
    set CLASSPATH=%KARAF_BASE%\conf
:KARAF_CLASSPATH_END

@REM Setup Karaf Home
if exist "%KARAF_HOME%\conf\karaf-rc.cmd" call %KARAF_HOME%\conf\karaf-rc.cmd
if exist "%HOME%\karaf-rc.cmd" call %HOME%\karaf-rc.cmd

@REM Support for loading native libraries
set PATH=%PATH%;%KARAF_BASE%\lib;%KARAF_HOME%\lib

@REM Setup the Java Virtual Machine
if not "%JAVA%" == "" goto :Check_JAVA_END
    if not "%JAVA_HOME%" == "" goto :TryJDKEnd
        call :warn JAVA_HOME not set; results may vary
:TryJRE
    start /w regedit /e __reg1.txt "HKEY_LOCAL_MACHINE\SOFTWARE\JavaSoft\Java Runtime Environment"
    if not exist __reg1.txt goto :TryJDK
    type __reg1.txt | find "CurrentVersion" > __reg2.txt
    if errorlevel 1 goto :TryJDK
    for /f "tokens=2 delims==" %%x in (__reg2.txt) do set JavaTemp=%%~x
    if errorlevel 1 goto :TryJDK
    set JavaTemp=%JavaTemp%##
    set JavaTemp=%JavaTemp:                ##=##%
    set JavaTemp=%JavaTemp:        ##=##%
    set JavaTemp=%JavaTemp:    ##=##%
    set JavaTemp=%JavaTemp:  ##=##%
    set JavaTemp=%JavaTemp: ##=##%
    set JavaTemp=%JavaTemp:##=%
    del __reg1.txt
    del __reg2.txt
    start /w regedit /e __reg1.txt "HKEY_LOCAL_MACHINE\SOFTWARE\JavaSoft\Java Runtime Environment\%JavaTemp%"
    if not exist __reg1.txt goto :TryJDK
    type __reg1.txt | find "JavaHome" > __reg2.txt
    if errorlevel 1 goto :TryJDK
    for /f "tokens=2 delims==" %%x in (__reg2.txt) do set JAVA_HOME=%%~x
    if errorlevel 1 goto :TryJDK
    del __reg1.txt
    del __reg2.txt
    goto TryJDKEnd
:TryJDK
    start /w regedit /e __reg1.txt "HKEY_LOCAL_MACHINE\SOFTWARE\JavaSoft\Java Development Kit"
    if not exist __reg1.txt (
        call :warn Unable to retrieve JAVA_HOME
        goto END
    )
    type __reg1.txt | find "CurrentVersion" > __reg2.txt
    if errorlevel 1 (
        call :warn Unable to retrieve JAVA_HOME
        goto END
    )
    for /f "tokens=2 delims==" %%x in (__reg2.txt) do set JavaTemp=%%~x
    if errorlevel 1 (
        call :warn Unable to retrieve JAVA_HOME
        goto END
    )
    set JavaTemp=%JavaTemp%##
    set JavaTemp=%JavaTemp:                ##=##%
    set JavaTemp=%JavaTemp:        ##=##%
    set JavaTemp=%JavaTemp:    ##=##%
    set JavaTemp=%JavaTemp:  ##=##%
    set JavaTemp=%JavaTemp: ##=##%
    set JavaTemp=%JavaTemp:##=%
    del __reg1.txt
    del __reg2.txt
    start /w regedit /e __reg1.txt "HKEY_LOCAL_MACHINE\SOFTWARE\JavaSoft\Java Development Kit\%JavaTemp%"
    if not exist __reg1.txt (
        call :warn Unable to retrieve JAVA_HOME from JDK
        goto END
    )
    type __reg1.txt | find "JavaHome" > __reg2.txt
    if errorlevel 1 (
        call :warn Unable to retrieve JAVA_HOME
        goto END
    )
    for /f "tokens=2 delims==" %%x in (__reg2.txt) do set JAVA_HOME=%%~x
    if errorlevel 1 (
        call :warn Unable to retrieve JAVA_HOME
        goto END
    )
    del __reg1.txt
    del __reg2.txt
:TryJDKEnd
    if not exist "%JAVA_HOME%" (
        call :warn JAVA_HOME is not valid: "%JAVA_HOME%"
        goto END
    )
    set JAVA=%JAVA_HOME%\bin\java
:Check_JAVA_END

if "%JAVA_OPTS%" == "" set JAVA_OPTS=%DEFAULT_JAVA_OPTS%

if "%KARAF_DEBUG%" == "" goto :KARAF_DEBUG_END
    @REM Use the defaults if JAVA_DEBUG_OPTS was not set
    if "%JAVA_DEBUG_OPTS%" == "" set JAVA_DEBUG_OPTS=%DEFAULT_JAVA_DEBUG_OPTS%

    set "JAVA_OPTS=%JAVA_DEBUG_OPTS% %JAVA_OPTS%"
    call :warn Enabling Java debug options: %JAVA_DEBUG_OPTS%
:KARAF_DEBUG_END

if "%KARAF_PROFILER%" == "" goto :KARAF_PROFILER_END
    set KARAF_PROFILER_SCRIPT=%KARAF_HOME%\conf\profiler\%KARAF_PROFILER%.cmd

    if exist "%KARAF_PROFILER_SCRIPT%" goto :KARAF_PROFILER_END
    call :warn Missing configuration for profiler '%KARAF_PROFILER%': %KARAF_PROFILER_SCRIPT%
    goto END
:KARAF_PROFILER_END

set CLASSPATH=%CLASSPATH%;%KARAF_HOME%\bundles\org.apache.felix.main_${felix.version}.jar

@REM Execute the JVM or the load the profiler
if "%KARAF_PROFILER%" == "" goto :RUN
    @REM Execute the profiler if it has been configured
    call :warn Loading profiler script: %KARAF_PROFILER_SCRIPT%
    call %KARAF_PROFILER_SCRIPT%

:RUN
    SET OPTS=-Dkaraf.startLocalConsole=true -Dkaraf.startRemoteShell=true
    SET MAIN=org.apache.felix.main.Main
    SET SHIFT=false
    if "%1" == "stop" goto :EXECUTE_STOP
    if "%1" == "console" goto :EXECUTE_CONSOLE
    if "%1" == "server" goto :EXECUTE_SERVER
    if "%1" == "client" goto :EXECUTE_CLIENT
    goto :EXECUTE

:EXECUTE_STOP
    SET MAIN=org.apache.felix.main.Stop
    SET SHIFT=true
    goto :EXECUTE

:EXECUTE_CONSOLE
    SET SHIFT=true
    goto :EXECUTE

:EXECUTE_SERVER
    SET OPTS=-Dkaraf.startLocalConsole=false -Dkaraf.startRemoteShell=true
    SET SHIFT=true
    goto :EXECUTE

:EXECUTE_CLIENT
    SET OPTS=-Dkaraf.startLocalConsole=true -Dkaraf.startRemoteShell=false
    SET SHIFT=true
    goto :EXECUTE

:EXECUTE
    if "%SHIFT%" == "true" SET ARGS=%2 %3 %4 %5 %6 %7 %8
    if not "%SHIFT%" == "true" SET ARGS=%1 %2 %3 %4 %5 %6 %7 %8
    @REM Execute the Java Virtual Machine
    cd %KARAF_BASE%
    "%JAVA%" %JAVA_OPTS% %OPTS% -classpath "%CLASSPATH%" -Djava.endorsed.dirs="%JAVA_HOME%\jre\lib\endorsed;%JAVA_HOME%\lib\endorsed;%KARAF_HOME%\lib\endorsed" -Djava.ext.dirs="%JAVA_HOME%\jre\lib\ext;%JAVA_HOME%\lib\ext;%KARAF_HOME%\lib\ext" -Dkaraf.instances="%KARAF_HOME%\instances" -Dkaraf.home="%KARAF_HOME%" -Dkaraf.base="%KARAF_BASE%" -Dkaraf.data="%KARAF_DATA%" -Dfelix.config.properties="file:%KARAF_BASE%/felix/config.ini" -Dkaraf.startRemoteShell=true -Dorg.ops4j.pax.runner.platform.console=false -Dkaraf.systemBundlesStartLevel=0 -Dorg.osgi.service.http.port="${org.osgi.service.http.port}" -Dfelix.fileinstall.filter="${release.felix.fileinstall.filter}" -Dkaraf.startLocalConsole=true -Dorg.osgi.service.http.port.secure=${org.osgi.service.http.port.secure} -Dopenengsb.version.number=${openengsb.version.number} -Dopenengsb.version.name=${openengsb.version.name} -Dfelix.fileinstall.dir="%KARAF_BASE%/${release.felix.fileinstall.dir}" -Dfelix.log.level=${felix.log.level} -Dfelix.fileinstall.poll=${release.felix.fileinstall.poll} -Dfelix.fileinstall.noInitialDelay=${release.felix.fileinstall.noInitialDelay} ${client.properties.windows} -Djava.util.logging.config.file="%KARAF_BASE%/config/java.util.logging.properties" %MAIN% %ARGS%

@REM # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #

:END

endlocal

if not "%PAUSE%" == "" pause

:END_NO_PAUSE

