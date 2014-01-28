#!/bin/sh

export CLASSPATH="vodimo.jar"
log4j="resources/log4j2.xml"

java -cp ".:${CLASSPATH}" -Dlog4j.configurationFile=$log4j com.vodimo.core.VodimoApplication backtest "01/1/2012" "30"

