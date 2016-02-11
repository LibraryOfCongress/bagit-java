import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.core.FileAppender

import static ch.qos.logback.classic.Level.*

appender("STDOUT", ConsoleAppender) {
  encoder(PatternLayoutEncoder) {
    pattern = "%date %level [%thread] %logger{10} [%file:%line] %msg%n"
  }
  filter(ch.qos.logback.classic.filter.ThresholdFilter) {
    level = DEBUG
  }
}

appender("FILEOUT", FileAppender) {
  append = false
  file = "${System.getProperty("user.home")}/.bagit/logs/bagit.log"
  encoder(PatternLayoutEncoder) {
    pattern = "%date %level [%thread] %logger{10} [%file:%line] %msg%n"
  }
  filter(ch.qos.logback.classic.filter.ThresholdFilter) {
    level = INFO
  }
}

appender("DEBUGOUT", FileAppender) {
  append = false
  file = "${System.getProperty("user.home")}/.bagit/logs/debug.log"
  encoder(PatternLayoutEncoder) {
    pattern = "%date %level [%thread] %logger{10} [%file:%line] %msg%n"
  }
  filter(ch.qos.logback.classic.filter.ThresholdFilter) {
    level = DEBUG
  }
}

appender("REQUESTS", FileAppender) {
  file = "${System.getProperty("app.home", "/tmp")}/logs/requests.log"
  encoder(PatternLayoutEncoder) {
    pattern = "%date %level [%thread] %logger{10} [%file:%line] %msg%n"
  }
}

root(DEBUG, ["STDOUT", "FILEOUT", "DEBUGOUT"])