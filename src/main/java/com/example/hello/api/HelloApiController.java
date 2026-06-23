package com.example.hello.api;

import com.example.hello.model.GetHello200Response;
import com.example.hello.service.HelloService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloApiController implements HelloApi {

 private static final Logger log = LoggerFactory.getLogger(HelloApiController.class);

 private final HelloService service;

 public HelloApiController(HelloService service) {
  this.service = service;
  log.info("HelloApiController initialised");
  // INFO: logged once at startup — confirms the controller bean was created.
  // Useful to verify Spring wired up the service dependency correctly.
 }

 public ResponseEntity<GetHello200Response> getHello() {
  log.info("GET /hello request received");
  // INFO: every API call is logged.
  // In Grafana Loki you can count these lines to see request volume over time.

  try {
   String message = service.getHello();
   log.debug("Service returned message: {}", message);
   // DEBUG: only visible when log level is DEBUG (local dev, troubleshooting).
   // Logs the actual value returned — too noisy for production INFO level.

   GetHello200Response response = new GetHello200Response().message(message);
   log.info("GET /hello responded 200 OK");
   // INFO: confirms the response was built successfully.
   // Paired with the request log above — if you see request but no response, something crashed.

   return ResponseEntity.ok(response);

  } catch (Exception e) {
   log.error("GET /hello failed with unexpected error: {}", e.getMessage(), e);
   // ERROR: logs the full exception with stack trace.
   // {} is SLF4J placeholder syntax — safer than string concatenation, no NPE risk.
   // The third argument `e` tells SLF4J to append the full stack trace to the log line.
   // This line flows to Loki and Grafana can alert on ERROR level logs.
   throw e;
  }
 }
}
