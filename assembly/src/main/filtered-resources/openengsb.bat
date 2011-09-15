@echo off
@REM
@REM Licensed to the Austrian Association for Software Tool Integration (AASTI)
@REM under one or more contributor license agreements. See the NOTICE file
@REM distributed with this work for additional information regarding copyright
@REM ownership. The AASTI licenses this file to you under the Apache License,
@REM Version 2.0 (the "License"); you may not use this file except in compliance
@REM with the License. You may obtain a copy of the License at
@REM
@REM     http://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing, software
@REM distributed under the License is distributed on an "AS IS" BASIS,
@REM WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@REM See the License for the specific language governing permissions and
@REM limitations under the License.
@REM

@REM Configure Environment
SET JAVA_MIN_MEM=${java.min.mem}
SET JAVA_MAX_MEM=${java.max.mem}
SET JAVA_PERM_MEM=${java.min.perm}
SET JAVA_MAX_PERM_MEM=${java.max.perm}

cd /d %~dp0%
karaf.bat

