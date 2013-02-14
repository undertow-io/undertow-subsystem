AS7 undertow subsystem
========================

This repository contains undertow subsystem and integration module that enables to install extension into existing AS8.0.x instalation.

To install to existing AS8.0 run
> mvn clean install -Pupdate-as -Djboss.home=/path/to/as8

The -Djboss.home is not nessesary if $JBOSS_HOME is already pointing at your JBoss installation.

after install is done you can run AS7 with undertow subsystem by running

> ./standalone.sh -c standalone-undertow.xml


