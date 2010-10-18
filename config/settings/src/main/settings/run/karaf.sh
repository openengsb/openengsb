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

DIRNAME=`dirname $0`
PROGNAME=`basename $0`

#
# Check/Set up some easily accessible MIN/MAX params for JVM mem usage
#

if [ "x$JAVA_MIN_MEM" = "x" ]; then
    JAVA_MIN_MEM=128M
    export JAVA_MIN_MEM
fi

if [ "x$JAVA_MAX_MEM" = "x" ]; then
    JAVA_MAX_MEM=512M
    export JAVA_MAX_MEM
fi

warn() {
    echo "${PROGNAME}: $*"
}

die() {
    warn "$*"
    exit 1
}

maybeSource() {
    file="$1"
    if [ -f "$file" ] ; then
        . $file
    fi
}

detectOS() {
    # OS specific support (must be 'true' or 'false').
    cygwin=false;
    darwin=false;
    aix=false;
    os400=false;
    case "`uname`" in
        CYGWIN*)
            cygwin=true
            ;;
        Darwin*)
            darwin=true
            ;;
        AIX*)
            aix=true
            ;;
        OS400*)
            os400=true
            ;;
    esac
    # For AIX, set an environment variable
    if $aix; then
         export LDR_CNTRL=MAXDATA=0xB0000000@DSA
         export IBM_JAVA_HEAPDUMP_TEXT=true
         echo $LDR_CNTRL
    fi
}

unlimitFD() {
    # Use the maximum available, or set MAX_FD != -1 to use that
    if [ "x$MAX_FD" = "x" ]; then
        MAX_FD="maximum"
    fi

    # Increase the maximum file descriptors if we can
    if [ "$os400" = "false" ] && [ "$cygwin" = "false" ]; then
        MAX_FD_LIMIT=`ulimit -H -n`
        if [ "$MAX_FD_LIMIT" != 'unlimited' ]; then 
            if [ $? -eq 0 ]; then
                if [ "$MAX_FD" = "maximum" -o "$MAX_FD" = "max" ]; then
                    # use the system max
                    MAX_FD="$MAX_FD_LIMIT"
                fi

                ulimit -n $MAX_FD > /dev/null
                # echo "ulimit -n" `ulimit -n`
                if [ $? -ne 0 ]; then
                    warn "Could not set maximum file descriptor limit: $MAX_FD"
                fi
            else
                warn "Could not query system maximum file descriptor limit: $MAX_FD_LIMIT"
            fi
        fi
    fi
}

locateHome() {
    if [ "x$KARAF_HOME" != "x" ]; then
        warn "Ignoring predefined value for KARAF_HOME"
    fi
    
    # In POSIX shells, CDPATH may cause cd to write to stdout
    (unset CDPATH) >/dev/null 2>&1 && unset CDPATH

    KARAF_HOME=`cd $DIRNAME/..; pwd`
    if [ ! -d "$KARAF_HOME" ]; then
        die "KARAF_HOME is not valid: $KARAF_HOME"
    fi
}

locateBase() {
    if [ "x$KARAF_BASE" != "x" ]; then
        if [ ! -d "$KARAF_BASE" ]; then
            die "KARAF_BASE is not valid: $KARAF_BASE"
        fi
    else
        KARAF_BASE=$KARAF_HOME
    fi
}

locateData() {
    if [ "x$KARAF_DATA" != "x" ]; then
        if [ ! -d "$KARAF_DATA" ]; then
            die "KARAF_DATA is not valid: $KARAF_DATA"
        fi
    else
        KARAF_DATA=$KARAF_BASE/data
    fi
}

setupNativePath() {
    # Support for loading native libraries
    LD_LIBRARY_PATH="${LD_LIBRARY_PATH}:$KARAF_BASE/lib:$KARAF_HOME/lib"

    # For Cygwin, set PATH from LD_LIBRARY_PATH
    if $cygwin; then
        LD_LIBRARY_PATH=`cygpath --path --windows "$LD_LIBRARY_PATH"`
        PATH="$PATH;$LD_LIBRARY_PATH"
        export PATH
    fi
    export LD_LIBRARY_PATH
}

pathCanonical() {
    local dst="${1}"
    while [ -h "${dst}" ] ; do
        ls=`ls -ld "${dst}"`
        link=`expr "$ls" : '.*-> \(.*\)$'`
        if expr "$link" : '/.*' > /dev/null; then
            dst="$link"
        else
            dst="`dirname "${dst}"`/$link"
        fi
    done
	local bas=`basename "${dst}"`
	local dir=`dirname "${dst}"`
    if [ "$bas" != "$dir" ]; then
		dst="`pathCanonical "$dir"`/$bas"
    fi
    echo "${dst}" | sed -e 's#//#/#g' -e 's#/./#/#g' -e 's#/[^/]*/../#/#g'
}

locateJava() {
    # Setup the Java Virtual Machine
    if $cygwin ; then
        [ -n "$JAVA" ] && JAVA=`cygpath --unix "$JAVA"`
        [ -n "$JAVA_HOME" ] && JAVA_HOME=`cygpath --unix "$JAVA_HOME"`
    fi

	if [ "x$JAVA_HOME" = "x" ] && [ "$darwin" = "true" ]; then
		JAVA_HOME=/System/Library/Frameworks/JavaVM.framework/Home
	fi
    if [ "x$JAVA" = "x" ] && [ -r /etc/gentoo-release ] ; then
        JAVA_HOME=`java-config --jre-home`
    fi
    if [ "x$JAVA" = "x" ]; then
        if [ "x$JAVA_HOME" != "x" ]; then
            if [ ! -d "$JAVA_HOME" ]; then
                die "JAVA_HOME is not valid: $JAVA_HOME"
            fi
            JAVA="$JAVA_HOME/bin/java"
        else
            warn "JAVA_HOME not set; results may vary"
            JAVA=`type java`
            JAVA=`expr "$JAVA" : '.*is \(.*\)$'`
            if [ "x$JAVA" = "x" ]; then
                die "java command not found"
            fi
        fi
    fi
    if [ "x$JAVA_HOME" = "x" ]; then
        JAVA_HOME="$(dirname $(dirname $(pathCanonical "$JAVA")))"
    fi
}

detectJVM() {
   #echo "`$JAVA -version`"
   # This service should call `java -version`,
   # read stdout, and look for hints
   if $JAVA -version 2>&1 | grep "^IBM" ; then
       JVM_VENDOR="IBM"
   # on OS/400, java -version does not contain IBM explicitly
   elif $os400; then
       JVM_VENDOR="IBM"
   else
       JVM_VENDOR="SUN"
   fi
   # echo "JVM vendor is $JVM_VENDOR"
}

setupDebugOptions() {
    if [ "x$JAVA_OPTS" = "x" ]; then
        JAVA_OPTS="$DEFAULT_JAVA_OPTS"
    fi
    export JAVA_OPTS

    # Set Debug options if enabled
    if [ "x$KARAF_DEBUG" != "x" ]; then
        # Use the defaults if JAVA_DEBUG_OPTS was not set
        if [ "x$JAVA_DEBUG_OPTS" = "x" ]; then
            JAVA_DEBUG_OPTS="$DEFAULT_JAVA_DEBUG_OPTS"
        fi

        JAVA_OPTS="$JAVA_DEBUG_OPTS $JAVA_OPTS"
        warn "Enabling Java debug options: $JAVA_DEBUG_OPTS"
    fi
}

setupDefaults() {
    DEFAULT_JAVA_OPTS="-Xms$JAVA_MIN_MEM -Xmx$JAVA_MAX_MEM "

    #Set the JVM_VENDOR specific JVM flags
    if [ "$JVM_VENDOR" = "SUN" ]; then
        DEFAULT_JAVA_OPTS="-server $DEFAULT_JAVA_OPTS -Dcom.sun.management.jmxremote"
    elif [ "$JVM_VENDOR" = "IBM" ]; then
        if $os400; then
            DEFAULT_JAVA_OPTS="$DEFAULT_JAVA_OPTS"
        elif $aix; then
            DEFAULT_JAVA_OPTS="-Xverify:none -Xlp $DEFAULT_JAVA_OPTS"
        else
            DEFAULT_JAVA_OPTS="-Xverify:none $DEFAULT_JAVA_OPTS"
        fi
    fi

    if [ -z "$CLASSPATH" ]; then
        CLASSPATH="$KARAF_BASE/bundles/org.apache.felix.main_${felix.version}.jar"
    else
        CLASSPATH="$CLASSPATH:$KARAF_BASE/bundles/org.apache.felix.main_${felix.version}.jar"
    fi
    DEFAULT_JAVA_DEBUG_OPTS="-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"

    ##
    ## TODO: Move to conf/profiler/yourkit.{sh|cmd}
    ##
    # Uncomment to enable YourKit profiling
    #DEFAULT_JAVA_DEBUG_OPTS="-Xrunyjpagent"
}

init() {
    # Determine if there is special OS handling we must perform
    detectOS

    # Unlimit the number of file descriptors if possible
    unlimitFD

    # Locate the Karaf home directory
    locateHome

    # Locate the Karaf base directory
    locateBase
    
    # Locate the Karaf data directory
    locateData

    # Setup the native library path
    setupNativePath

    # Locate the Java VM to execute
    locateJava

    # Determine the JVM vendor
    detectJVM

    # Setup default options
    setupDefaults

    # Install debug options
    setupDebugOptions

}

run() {
    OPTS="-Dkaraf.startLocalConsole=true -Dkaraf.startRemoteShell=true"
    MAIN=org.apache.felix.main.Main
    case "$1" in
        'stop')
            MAIN=org.apache.felix.main.Stop
            shift
            ;;
        'console')
            shift
            ;;
        'server')
            OPTS="-Dkaraf.startLocalConsole=false -Dkaraf.startRemoteShell=true"
            shift
            ;;
        'client')
            OPTS="-Dkaraf.startLocalConsole=true -Dkaraf.startRemoteShell=false"
            shift
            ;;
    esac

    if $cygwin; then
        KARAF_HOME=`cygpath --path --windows "$KARAF_HOME"`
        KARAF_BASE=`cygpath --path --windows "$KARAF_BASE"`
        KARAF_DATA=`cygpath --path --windows "$KARAF_DATA"`
        CLASSPATH=`cygpath --path --windows "$CLASSPATH/bundles/org.apache.felix.main_${felix.version}.jar"`
    fi
    cd $KARAF_BASE
    exec $JAVA $JAVA_OPTS -Djava.endorsed.dirs="${JAVA_HOME}/jre/lib/endorsed:${JAVA_HOME}/lib/endorsed:${KARAF_HOME}/lib/endorsed" -Djava.ext.dirs="${JAVA_HOME}/jre/lib/ext:${JAVA_HOME}/lib/ext:${KARAF_HOME}/lib/ext" -Dkaraf.instances="${KARAF_HOME}/instances" -Dkaraf.home="$KARAF_HOME" -Dkaraf.base="$KARAF_BASE" -Dkaraf.data="$KARAF_DATA" -Dfelix.config.properties="file:$KARAF_BASE/felix/config.ini" -Dkaraf.startRemoteShell=true -Dorg.ops4j.pax.runner.platform.console=false -Dkaraf.systemBundlesStartLevel=0 -Dorg.osgi.service.http.port="${org.osgi.service.http.port}" -Dorg.osgi.framework.system.packages="javax.accessibility,javax.activation;version=1.1.1,javax.activity,javax.annotation,javax.annotation.processing,javax.crypto,javax.crypto.interfaces,javax.crypto.spec,javax.imageio,javax.imageio.event,javax.imageio.metadata,javax.imageio.plugins.bmp,javax.imageio.plugins.jpeg,javax.imageio.spi,javax.imageio.stream,javax.jws;version=2.0.0,javax.jws.soap;version=2.0.0,javax.lang.model,javax.lang.model.element,javax.lang.model.type,javax.lang.model.util,javax.management,javax.management.loading,javax.management.modelmbean,javax.management.monitor,javax.management.openmbean,javax.management.relation,javax.management.remote,javax.management.remote.rmi,javax.management.timer,javax.naming,javax.naming.directory,javax.naming.event,javax.naming.ldap,javax.naming.spi,javax.net,javax.net.ssl,javax.print,javax.print.attribute,javax.print.attribute.standard,javax.print.event,javax.rmi,javax.rmi.CORBA,javax.rmi.ssl,javax.script,javax.security.auth,javax.security.auth.callback,javax.security.auth.kerberos,javax.security.auth.login,javax.security.auth.spi,javax.security.auth.x500,javax.security.cert,javax.security.sasl,javax.sound.midi,javax.sound.midi.spi,javax.sound.sampled,javax.sound.sampled.spi,javax.sql,javax.sql.rowset,javax.sql.rowset.serial,javax.sql.rowset.spi,javax.swing,javax.swing.border,javax.swing.colorchooser,javax.swing.event,javax.swing.filechooser,javax.swing.plaf,javax.swing.plaf.basic,javax.swing.plaf.metal,javax.swing.plaf.multi,javax.swing.plaf.synth,javax.swing.table,javax.swing.text,javax.swing.text.html,javax.swing.text.html.parser,javax.swing.text.rtf,javax.swing.tree,javax.swing.undo,javax.tools,javax.xml,javax.xml.crypto,javax.xml.crypto.dom,javax.xml.crypto.dsig,javax.xml.crypto.dsig.dom,javax.xml.crypto.dsig.keyinfo,javax.xml.crypto.dsig.spec,javax.xml.datatype,javax.xml.namespace,javax.xml.parsers,javax.xml.soap,javax.xml.stream,javax.xml.stream.events,javax.xml.stream.util,javax.xml.transform,javax.xml.transform.dom,javax.xml.transform.sax,javax.xml.transform.stax,javax.xml.transform.stream,javax.xml.validation,javax.xml.ws,javax.xml.ws.handler,javax.xml.ws.handler.soap,javax.xml.ws.http,javax.xml.ws.soap,javax.xml.ws.spi,javax.xml.xpath,org.ietf.jgss,org.omg.CORBA,org.omg.CORBA.DynAnyPackage,org.omg.CORBA.ORBPackage,org.omg.CORBA.TypeCodePackage,org.omg.CORBA.portable,org.omg.CORBA_2_3,org.omg.CORBA_2_3.portable,org.omg.CosNaming,org.omg.CosNaming.NamingContextExtPackage,org.omg.CosNaming.NamingContextPackage,org.omg.Dynamic,org.omg.DynamicAny,org.omg.DynamicAny.DynAnyFactoryPackage,org.omg.DynamicAny.DynAnyPackage,org.omg.IOP,org.omg.IOP.CodecFactoryPackage,org.omg.IOP.CodecPackage,org.omg.Messaging,org.omg.PortableInterceptor,org.omg.PortableInterceptor.ORBInitInfoPackage,org.omg.PortableServer,org.omg.PortableServer.CurrentPackage,org.omg.PortableServer.POAManagerPackage,org.omg.PortableServer.POAPackage,org.omg.PortableServer.ServantLocatorPackage,org.omg.PortableServer.portable,org.omg.SendingContext,org.omg.stub.java.rmi,org.w3c.dom,org.w3c.dom.bootstrap,org.w3c.dom.css,org.w3c.dom.events,org.w3c.dom.html,org.w3c.dom.ls,org.w3c.dom.ranges,org.w3c.dom.stylesheets,org.w3c.dom.traversal,org.w3c.dom.views,org.xml.sax,org.xml.sax.ext,org.xml.sax.helpers,org.osgi.framework;version=1.5.0,org.osgi.framework.launch;version=1.0.0,org.osgi.framework.hooks.service;version=1.0.0,org.osgi.service.packageadmin;version=1.2.0,org.osgi.service.startlevel;version=1.1.0,org.osgi.service.url;version=1.0.0,org.osgi.util.tracker;version=1.4.0,javax.xml.bind;version=2.2.0,javax.xml.bind.annotation;version=2.2.0,javax.xml.bind.annotation.adapters;version=2.2.0,javax.xml.bind.helpers;version=2.2.0,javax.xml.bind.util;version=2.2.0,javax.xml.bind.attachment;version=2.2.0,javax.transaction.xa,javax.transaction,sun.reflect" -Dorg.osgi.framework.bootdelegation=* -Dfelix.fileinstall.filter="${release.felix.fileinstall.filter}" -Dkaraf.startLocalConsole=true -Dorg.osgi.service.http.port.secure=${org.osgi.service.http.port.secure} -Dopenengsb.version.number=${openengsb.version.number} -Dopenengsb.version.name=${openengsb.version.name} -Dfelix.fileinstall.dir="$KARAF_BASE/${release.felix.fileinstall.dir}" -Dfelix.log.level=${felix.log.level} -Dfelix.fileinstall.poll=${release.felix.fileinstall.poll} -Dfelix.fileinstall.noInitialDelay=${release.felix.fileinstall.noInitialDelay} ${client.properties.linux} -Djava.util.logging.config.file="$KARAF_BASE/config/java.util.logging.properties" $OPTS -classpath "$CLASSPATH" $MAIN "$@"
}

main() {
    init
    run "$@"
}

main "$@"
