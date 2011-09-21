openengsb-2.0.0 2011-09-21 
--------------------------------------------

The first final release of the 2.x series of the OpenEngSB fixed 105 issues. One of the most important chnanges with the
2.0.0 release is the introduction of semantic versioning. There had been mostly minor fixes and enhancements to this work.
In addition tons of bug fixes and version upgrades. Finally the package structure had been completely refactored. This
also includes changes to all artifact ids and pom references.

### Details
** Bug
    * [OPENENGSB-1301] - ConfigPersistence does not work in itests
    * [OPENENGSB-1470] - artifactinstaller does not work in itests
    * [OPENENGSB-1570] - taskboxService.getTaskPanel throws wicket rendering error, when used in subproject at version: 1.2.0.RC1
    * [OPENENGSB-1680] - Superclass setup-method is not called before subclass setup-methods
    * [OPENENGSB-1779] - Instance Id cannot be reused
    * [OPENENGSB-1848] - Karaf seems to start fine, but doesnt load any bundles
    * [OPENENGSB-1859] - Connecotr & Domain links in documentation do not work
    * [OPENENGSB-1868] - Unused interface ContextConnectorService
    * [OPENENGSB-1875] - Icons are not shown correctly
    * [OPENENGSB-1879] - Choosing WiringPage auditing connector on wiring page throws ClassCastException
    * [OPENENGSB-1881] - Update service page throws a WicketNotSerializableException:
    * [OPENENGSB-1911] - Possible concurrent modification exception in DummyPersistence
    * [OPENENGSB-1912] - Clash between lib and framework exporting both xml.stream
    * [OPENENGSB-1918] - Remove developer and contributor from openengsb-root to openengsb
    * [OPENENGSB-1928] - license-mapping.xml lists antlr as Apache licensed
    * [OPENENGSB-1932] - ConnectorManager reads configs from ConfigPersistence which don't concern him. 
    * [OPENENGSB-1934] - Core-Common bundle needs the EKB bundle, features.xml isn't quite correct
    * [OPENENGSB-1935] - improve EDB performance
    * [OPENENGSB-1949] - Referencing openengsb-core in port features fails terrible at installing them one after the other
    * [OPENENGSB-1952] - Refreshing openengsb-core does not work
    * [OPENENGSB-1955] - ClassCastException in Testclient
    * [OPENENGSB-1960] - Not a directory bug thrown in ContextFilePersistenceService
    * [OPENENGSB-1969] - example remoteclient does not compile because of api-changes
    * [OPENENGSB-1998] - connector-deployer puts a string in service.ranking property
    * [OPENENGSB-2000] - ServiceEditorPanel uses a cast to String which fails for non Strings
    * [OPENENGSB-2002] - add itests for EDB
    * [OPENENGSB-2023] - WSPortIT has unneeded Thread.sleep calls
    * [OPENENGSB-2031] - AuditingDomain does not container a proper description
    * [OPENENGSB-2054] - WSPortIT has problems under windows
    * [OPENENGSB-2055] - TaskboxUIIT fails under windows

** Improvement
    * [OPENENGSB-279] - Use openengsb UI not as own war file but include pages into client configuration
    * [OPENENGSB-917] - Restructure domain model for ekb/edb
    * [OPENENGSB-1358] - Document features.xml
    * [OPENENGSB-1478] - kill orphan karaf-instances in itests spawned by previous exam-runs
    * [OPENENGSB-1571] - Increase default memory used during windows bootup
    * [OPENENGSB-1916] - Add description and details section to features.xml
    * [OPENENGSB-1921] - Use same mail dependency as karaf
    * [OPENENGSB-1929] - OpenEngSB window shows karaf instead of OpenEngSB
    * [OPENENGSB-1942] - EDB shall use slf4j for logging
    * [OPENENGSB-1964] - ConfigPersistenceServices should be consistently configured.
    * [OPENENGSB-1975] - Make JsonUtils#convertArgument private
    * [OPENENGSB-1999] - Remove export of javax.transaction* in jre.properties
    * [OPENENGSB-2003] - Remove no longer required transaction deps
    * [OPENENGSB-2004] - Remove referenced felix bundles
    * [OPENENGSB-2005] - Remove no longer required jgit libs

** Library Upgrade
    * [OPENENGSB-1235] - Upgrade to pax-exam 2.3.0.M1
    * [OPENENGSB-1856] - Upgrade openjpa to 2.1.1
    * [OPENENGSB-1857] - Upgrade openjpa-maven-plugin to 1.2
    * [OPENENGSB-1926] - Upgrade to openengsb-root-20
    * [OPENENGSB-1946] - Upgrade to karaf-2.2.3
    * [OPENENGSB-1979] - Upgrade to paxexam-karaf-0.1.0
    * [OPENENGSB-1989] - Upgrade to guava 9
    * [OPENENGSB-1994] - Upgrade java mail api to 1.4.4
    * [OPENENGSB-2006] - Upgrade jaxb to 2.2.4
    * [OPENENGSB-2007] - Upgrade cxf to 2.4.2
    * [OPENENGSB-2008] - Upgrade spring-security to 3.0.7.RELEASE
    * [OPENENGSB-2009] - Upgrade jackson libs to 1.8.5
    * [OPENENGSB-2010] - Upgrade commons-compress to 1.2
    * [OPENENGSB-2011] - Upgrade commons-codec to 1.5.6
    * [OPENENGSB-2012] - Upgrade slf4j to 1.6.2
    * [OPENENGSB-2013] - Upgrade commons-pool to 1.5.6
    * [OPENENGSB-2014] - Upgrade h2 to 1.3.160
    * [OPENENGSB-2021] - Upgrade jsch to 0.1.44
    * [OPENENGSB-2043] - Upgrade to openengsb-root-22

** New Feature
    * [OPENENGSB-937] - Provide general possibility to handle oauth
    * [OPENENGSB-1651] - handle multiple connectors with same location
    * [OPENENGSB-1795] - add method to delete context
    * [OPENENGSB-1831] - Show openengsb system information if info is issued in karaf shell
    * [OPENENGSB-1852] - Create a new command to show all available domains
    * [OPENENGSB-1853] - Show openengsb system information if info is issued in karaf shell
    * [OPENENGSB-1945] - Add base infrastructure to build loom bridge components
    * [OPENENGSB-1973] - Allow custom transformations in remote mappings
    * [OPENENGSB-2018] - Allow version branding of admin UI
    * [OPENENGSB-2026] - Introduce openengsb-ui-admin project

** Task
    * [OPENENGSB-845] - Using a service involving wicket from a thread, not running in a wicketapplication fails.
    * [OPENENGSB-928] - auditing-root-connector-instance should be deployed via ConnectorDeployer (config-file)
    * [OPENENGSB-1693] - add an explanation for every bundle which uses Dynamic Import
    * [OPENENGSB-1695] - move ./docs/examples to ./examples
    * [OPENENGSB-1709] - Rename Level Enum in org.openengsb.domain.example.event.LogEvent to LogLevel
    * [OPENENGSB-1760] - Release framework-2.0.0
    * [OPENENGSB-1855] - increase timeout while service lookup in itests
    * [OPENENGSB-1871] - ArtifactId should be equal to symbolic name like for aries, pw, karaf, ...
    * [OPENENGSB-1877] - AbstractOsgiMockServiceTest does not mock registerService(String,Object,Dictionary)
    * [OPENENGSB-1878] - AbstractOsgiMockServiceTest does not work for ServiceTrackers
    * [OPENENGSB-1941] - Add intellij idea to sponsers page
    * [OPENENGSB-1944] - Update Felix as PMC
    * [OPENENGSB-1951] - Check if tcp://xyz:asf?blub also works in json-jms definitions
    * [OPENENGSB-1961] - Add oxygenxml to sponsors page
    * [OPENENGSB-1967] - add option to disable security features for remoting
    * [OPENENGSB-1968] - Use org.openengsb.labs.paxexam.karaf for integration testing
    * [OPENENGSB-1970] - correct scm-property in all framework-poms
    * [OPENENGSB-1971] - remove warnings
    * [OPENENGSB-1972] - run-script does not always repackage assembly correctly
    * [OPENENGSB-1992] - move homepage and branding from framework to homepage
    * [OPENENGSB-1993] - move documentation from framework to openengsb
    * [OPENENGSB-1995] - use new group/artifactId for java mail api
    * [OPENENGSB-2001] - Rename artifacts from openengsb to openengsb-framework
    * [OPENENGSB-2015] - Use servicemix wrapped guava libs instead of own
    * [OPENENGSB-2017] - Move branding from framework to openengsb
    * [OPENENGSB-2019] - Remove name from framework branding
    * [OPENENGSB-2020] - Remove versions from spring, spring-dm and http feature
    * [OPENENGSB-2022] - Replace own wrapped jsch wrapped lib with smx ones
    * [OPENENGSB-2029] - Change openengsb:info command to OpenEngSB Framework Information
    * [OPENENGSB-2032] - Move some plugin versions to openengsb-root

** TBD
    * [OPENENGSB-1768] - enhance events with every stage


openengsb-1.3.0.M3 2011-07-15 
--------------------------------------------

The third milestone release of the OpenEngSB fixes 86 issues. The most important enhancement is the introduction
of pax-wicket into the OpenEngSB. This is the base for splitting up the UI and help to reuse and extend it into
user applications as required. A first prototype of the Enterprise Database 2.0 shows the way we want to go in
versioning tool data. A new pipline architecture allows secured remote calls and the console prototype shows where
we want to go in future 1.3 milestone releases. The newly added configuration backends help to configure the 
OpenEngSB using files. Finally bugs, identified in former 1.3.0.Mx releases, are fixed. Also worth mentioning: 
The speed and stability of the OpenEngSB had been increased.

### Highlights
  * Pax Wicket based UI
  * EDB 2.0 Protoype included
  * Stabilized remoting
  * Switched back to Apache Felix as OSGi engine.
  * Various documentaiton enhancements
  * Configuration persistence backend with various implementations
  * First prototype of karaf-console integration included
  * Secure external calls
  * Remote services are callable via filters instead of plain service-id properties.

### Details
** Bug
    * [OPENENGSB-1594] - JsonParseException: Unexpected character
    * [OPENENGSB-1597] - karaf.data, karaf.base and karaf.home are not set in itests
    * [OPENENGSB-1599] - ports-ws does not create a correct webservice wsdl
    * [OPENENGSB-1618] - connector-archetype should use separate domain-version
    * [OPENENGSB-1638] - OpenEngSB uses org.apache.aries.transaction.manager in version 0.2-incubating
    * [OPENENGSB-1662] - itests fail because of config-persistence-setup-issue
    * [OPENENGSB-1671] - Ports NullPointerException in case of a Void method
    * [OPENENGSB-1675] - ConnectorDescription: Use of obsolete class "Dictionary"
    * [OPENENGSB-1694] - update documentation about connector-config-files
    * [OPENENGSB-1714] - Features.xml still contains auditing connector & domain feature
    * [OPENENGSB-1715] - Abstract login in ui-admin references UserManagerImpl instead of interface.
    * [OPENENGSB-1719] - WS filter configuration misses encryption filter
    * [OPENENGSB-1720] - WS Incoming Port does not handle exceptions correct
    * [OPENENGSB-1771] - Documentation still points at old hudson instance
    * [OPENENGSB-1772] - Documentation does not clearify that we do not want to use our own mvn repository
    * [OPENENGSB-1774] - Json-Marshalling: ClassNotFoundException
    * [OPENENGSB-1800] - features.xml contains duplicated transaction manager
    * [OPENENGSB-1803] - Testclient cannot see domain-interfaces
    * [OPENENGSB-1810] - Creating two connectors with the same name throws an uncatched exception in the UI
    * [OPENENGSB-1816] - Messages via jms are always sent to RECEIVE

** Epic
    * [OPENENGSB-267] - adapt context store design and name
    * [OPENENGSB-781] - Use pax-wicket for UI
    * [OPENENGSB-876] - Engineering Database 2.0
    * [OPENENGSB-1565] - provide sample remote java-client-application in examples-directory

** Improvement
    * [OPENENGSB-1008] - Add external, interesting tutorials to hp and usermanual
    * [OPENENGSB-1187] - configure karaf-ports in tests to something different than opencit uses
    * [OPENENGSB-1194] - Workflow Validation before export
    * [OPENENGSB-1285] - Exception after creating an proxy
    * [OPENENGSB-1600] - Start JMS ports modules only when required
    * [OPENENGSB-1619] - Reference to raw changelog instead of formatted on HP
    * [OPENENGSB-1658] - allow osgi-filters in remote-calls instead of just service-ids
    * [OPENENGSB-1678] - Make the datasource for the EDB configureable via property placeholders
    * [OPENENGSB-1682] - Removing nodes has to be possible
    * [OPENENGSB-1687] - User can remove a Workflow
    * [OPENENGSB-1692] - Update documentation for osgi.bnd
    * [OPENENGSB-1718] - Comment in AbstractExamTestHelper about debug/log is not resistent about formatting
    * [OPENENGSB-1780] - Include apache features.xml directly in the openengsb feature.xml
    * [OPENENGSB-1799] - Use script based solution instead of pax:provision
    * [OPENENGSB-1808] - Increase readability of org.apache.karaf.features.cfg
    * [OPENENGSB-1811] - ConnectorRegistrationManagerImpl#finishCreatingInstance does not handle case that factory returns null

** Library Upgrade
    * [OPENENGSB-1086] - upgrade drools to 5.2
    * [OPENENGSB-1621] - Upgrade to openengsb-root-18
    * [OPENENGSB-1698] - Upgrade xmlsec to 1.4.5_1
    * [OPENENGSB-1699] - Upgrade cxf to 2.4.1
    * [OPENENGSB-1700] - Upgrade jaxb to 1.8.0
    * [OPENENGSB-1701] - Upgrade JSR311 to 1.8.0
    * [OPENENGSB-1702] - Upgrade jaxws to 1.8.0
    * [OPENENGSB-1703] - Upgrade saaj to 1.8.0
    * [OPENENGSB-1704] - Upgrade asm to 3.3_2
    * [OPENENGSB-1705] - Upgrade wss4j to 1.6.1
    * [OPENENGSB-1706] - Upgrade stax to 1.8.0
    * [OPENENGSB-1707] - Upgrade activation to 1.8.0
    * [OPENENGSB-1713] - Upgrade xjc to 2.2.1.1_2
    * [OPENENGSB-1730] - Upgrade jaxb to 2.2.3
    * [OPENENGSB-1731] - Upgrade jackson libs to 1.8.2
    * [OPENENGSB-1732] - Upgrade jgit to 0.12.1
    * [OPENENGSB-1733] - Upgrade geronimo servlet to 1.2
    * [OPENENGSB-1734] - Upgrade javax mail api to 1.4.1_4
    * [OPENENGSB-1766] - Upgrade to openengsb-root-19
    * [OPENENGSB-1778] - Upgrade to karaf-2.2.2
    * [OPENENGSB-1781] - Upgrade pax-wicket to 0.7.0
    * [OPENENGSB-1798] - Upgrade pax-wicket to 0.7.1
    * [OPENENGSB-1802] - Upgrade to pax-wicket 1.7.2

** New Feature
    * [OPENENGSB-1103] - Persist Workflow via PersistenceService
    * [OPENENGSB-1226] - Implement secure way for Authentication for external calls
    * [OPENENGSB-1603] - Create new console module in openengsb-core 
    * [OPENENGSB-1690] - use events instead of strings in auditingdomain
    * [OPENENGSB-1745] - Add simple script to show the possible tags in submodule folders
    * [OPENENGSB-1776] - Use PaxWicketMountPoint annotation support for page mounting

** Task
    * [OPENENGSB-1562] - add environment-variable to override the log-level in Tests
    * [OPENENGSB-1564] - put jaxb-annotations in models in core-api
    * [OPENENGSB-1607] - Replace osgi.bnd file by directly in-pom replacement
    * [OPENENGSB-1614] - Add documentation for pipeline API
    * [OPENENGSB-1646] - Upgrade submodules to latest clienprojects
    * [OPENENGSB-1659] - create alterntive run-script without openengsb:provision
    * [OPENENGSB-1660] - Add templates for eclipse and intellij to etc/
    * [OPENENGSB-1661] - update howto about installing domains and connectors
    * [OPENENGSB-1665] - fix warnings in openengsb project
    * [OPENENGSB-1670] - deprecate ContextCurrentService.{get,set}ThreadLocalContext methods
    * [OPENENGSB-1696] - move security-filters to security-bundle
    * [OPENENGSB-1697] - Switch back to Apache Felix
    * [OPENENGSB-1710] - Remove CipherUtilTest#encryptWrongKey test
    * [OPENENGSB-1744] - Upgrade maven connector reference to openengsb-connector-maven-1.2.3
    * [OPENENGSB-1759] - Release openengsb-1.3.0.M3
    * [OPENENGSB-1773] - Purpose and configuration of openengsb-maven-plugin missing in documentation
    * [OPENENGSB-1786] - Move workflow tests from itest project into workflow project


openengsb-1.3.0.M2 2011-05-20 
--------------------------------------------

The second milestone release of the OpenEngSB fixes 43 issues. The most important enhancements are the possibility
to wire connectors via the admin UI and the rewrite of the ports-infrastructure. This allows an easy introduction
of security and more complex operations in future 1.3 milestone releases. Besides, new example connectors had been
added. The OpenEngSB uses the latest versions of Apache Karaf (2.2.1), Aries Blueprint (0.3.1) and OpenEngSB Root (17)
again. In addition the remote infrastructure had been extended and received further stabilization. Finally bugs,
identified in former 1.3.x releases, are fixed. Also worth mentioning: The speed and stability of the OpenEngSB had 
been increased.

### Highlights
  * Karaf shows information to each bundle now using "osgi:info ID" in the console
  * New administration page to wire connectors via the UI
  * Ports remote structure is now based on filter/pipelining
  * Completely rewritten C# examples
  * Using the latest Apache Karaf version (2.2.1)
  * Finally fixing JMS restart/integration test problems
  * Based on openengsb-framework 1.3.0.M2 connectors/domains could finally run accross various versions
  * Ports-JMS does no longer require an external JMS Server
  * Wicket is no longer required in the openengsb-core feature
  * Using higher default memory/perm-space increases startup speed and stability.

### Details
** Bug
    * [OPENENGSB-1212] - JMS Integration tests fail
    * [OPENENGSB-1244] - Create config-factories via features <config> tag
    * [OPENENGSB-1351] - JMS does not "survive" a second start
    * [OPENENGSB-1473] - Messages enqueued to "receive" are automatically dequeued
    * [OPENENGSB-1476] - maven-tidy plugin invalidates xhtml by line-wrapping
    * [OPENENGSB-1477] - manual images are not shown on homepage
    * [OPENENGSB-1513] - package name is not considered when reloading rulebase
    * [OPENENGSB-1514] - CI tutorial sample project link is invalid
    * [OPENENGSB-1522] - AuthenticationCredentialsNotFoundException thrown on jms call handle
    * [OPENENGSB-1530] - mvn install build failure under windows
    * [OPENENGSB-1533] - Licenses in all nmsprovider-activemq.config are not correct
    * [OPENENGSB-1535] - OpenEngSB versions are generated wrong for connectors/domains
    * [OPENENGSB-1537] - Not all submodules in .gitmodule define urls the same schema
    * [OPENENGSB-1566] - bundle-info patch broke infrastructure-jms
    * [OPENENGSB-1569] - Classloading in JMS Ports is messed
    * [OPENENGSB-1602] - openengsb:provision does not start because of permission problem
    * [OPENENGSB-1616] - README does not reflect the current state-of-the-art
    * [OPENENGSB-1617] - NOTICE file should not be executable

** Improvement
    * [OPENENGSB-1323] - sort imports in import-managing-UI
    * [OPENENGSB-1326] - Extract entire common call-infrastructure from ports
    * [OPENENGSB-1327] - Make entire ports infrastructure internal
    * [OPENENGSB-1407] - Make it possible to configure queues on localhost for jms-json remote
    * [OPENENGSB-1511] - Include into maintenance faq that this policy apply to all components
    * [OPENENGSB-1538] - Update recommended eclipse plugins
    * [OPENENGSB-1556] - Increase default memory used with karaf
    * [OPENENGSB-1567] - Add bundle-info template to connector/domain/client project archetype
    * [OPENENGSB-1601] - Couple wicket to ui-common feature instead to core common
    * [OPENENGSB-1608] - Define openengsb domain/connector export versions as variable

** Library Upgrade
    * [OPENENGSB-1272] - Upgrade to karaf-2.2.1
    * [OPENENGSB-1545] - Upgrade aries blueprint to 0.3.1
    * [OPENENGSB-1555] - Upgrade to openengsb-root-16
    * [OPENENGSB-1582] - Upgrade to openengsb-root-17

** New Feature
    * [OPENENGSB-948] - Add OSGI-INF/bundle.info as used in Karaf to the openengsb bundles
    * [OPENENGSB-1292] - create wiring-page (to replace context-editor-page)
    * [OPENENGSB-1482] - Refactor ports implementation to "pipeline"-infrastructure (to loosen the coupling)
    * [OPENENGSB-1534] - Add openengsb-connector csharp example

** Task
    * [OPENENGSB-1089] - Update dev documentation to reflect that each commit should include [OPENENGSB-ISSUEID] in its message
    * [OPENENGSB-1329] - Create Demo-scenario for humantasks
    * [OPENENGSB-1417] - Redesign remote messaging from ground up
    * [OPENENGSB-1484] - Release openengsb-1.3.0.M2
    * [OPENENGSB-1507] - Push connector and domain submodules to latest version in features
    * [OPENENGSB-1509] - For the submodules pull AND push urls should be specified
    * [OPENENGSB-1532] - Integrate OpenEngSB with http://www.ohloh.net/


openengsb-1.3.0.M1 2011-05-03 
--------------------------------------------

First milestone release of the openengsb-1.3.0 series not including any real new features by now but rather 
stabilizing current release.

### Highlights
  * JMS ports and docs imporvements
  * New tutorial about starting with the OpenEngSB

### Details
** Bug
    * [OPENENGSB-1333] - ConnectorDeployer should not overwrite changes made outside of the files
    * [OPENENGSB-1344] - ConfigPersistence is retrieved too early if retrieved as instance variable
    * [OPENENGSB-1346] - ports-ws does not provide a method in the WS
    * [OPENENGSB-1347] - OpenEngSB does not start ports-ws if ports-jms is started during ui-admin
    * [OPENENGSB-1360] - Delete and call buttons on testclient page do not get deactivated during execution
    * [OPENENGSB-1361] - Domains could not be called on testclient
    * [OPENENGSB-1392] - Remote connection to AMQ returns connection refused
    * [OPENENGSB-1393] - Classpath loading exception with cxf ws json service
    * [OPENENGSB-1400] - Variables in pom.xml files are not replaced during build/release
    * [OPENENGSB-1404] - Referencing serviceUtils in bundle exporting it have to be done via blueprint
    * [OPENENGSB-1406] - CallRouter could not be retrieved
    * [OPENENGSB-1408] - Default registered auditing service does not follow naming specification
    * [OPENENGSB-1412] - JSON object marshaller does not accept annotations
    * [OPENENGSB-1414] - DefaultRequestHandler uses current classes instead of transfared to find method
    * [OPENENGSB-1419] - memory-auditing-connectors always return the same instanceid ("auditing")
    * [OPENENGSB-1422] - auditing-domain-provider is not exported with the correct properties
    * [OPENENGSB-1469] - using connector-deployer with no attributes breaks Neodatis-persistence
    * [OPENENGSB-1471] - maven-tidy plugin invalidates xhtml by line-wrapping quickfix
    * [OPENENGSB-1472] - Error in JSON sample in the Documentation of the nightly build chp 6.1.1.1
    * [OPENENGSB-1481] - OpenEngSB Wrapped Dependencies have to be defined in poms/pom.xml instead of root pom

** Improvement
    * [OPENENGSB-1265] - Move HowTo's to DocBook
    * [OPENENGSB-1389] - Move Quickstart section into howto section of manual and link it
    * [OPENENGSB-1390] - Documentation howto.remote talks about Rules although processes are meant
    * [OPENENGSB-1416] - JSON responses does not contains sent information
    * [OPENENGSB-1480] - Add aspectj also to dependency section

** Library Upgrade
    * [OPENENGSB-1355] - upgrade to openengsb-root-15
    * [OPENENGSB-1413] - Upgrade to jackson 1.8.0

** New Feature
    * [OPENENGSB-1403] - Show openengsb name in karaf console branding

** Task
    * [OPENENGSB-960] - Document support strategy on hp
    * [OPENENGSB-1154] - Define release name using Q Q
    * [OPENENGSB-1348] - release openengsb-1.3.0.M1
    * [OPENENGSB-1350] - Upgrade all submodules to the finally released connectors with the release
    * [OPENENGSB-1362] - Do not startup openengsb-ports-jms in framework distribution per default

** TBD
    * [OPENENGSB-1405] - Keeping the same release name for an entire 1.x.* version

