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

