package com.example.hello.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class HelloServiceImpl implements HelloService {

 private static final Logger log = LoggerFactory.getLogger(HelloServiceImpl.class);
 // One Logger per class — standard Java logging pattern.
 // LoggerFactory.getLogger(HelloServiceImpl.class) tags every log line
 // with the fully qualified class name so you know exactly where it came from.

 public String getHello() {
  log.debug("getHello() invoked — building greeting message");
  // DEBUG: entry point of the method.
  // Only visible in local dev (application.yml sets com.example.hello to DEBUG).
  // Turned off in production via INFO level to reduce log volume.

  String message = "Hello Contract-First API 🚀";

  log.debug("Greeting message built: {}", message);
  // DEBUG: logs the value being returned.
  // {} placeholder — SLF4J only evaluates this string if DEBUG is enabled,
  // so there is zero performance cost in production when level is INFO.

  log.info("getHello() completed successfully");
  // INFO: confirms the business method ran without error.
  // In Loki you can query: {app="hello-api"} |= "completed successfully"
  // to see a history of all successful calls.

  return message;
 }
}
