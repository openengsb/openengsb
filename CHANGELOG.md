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

