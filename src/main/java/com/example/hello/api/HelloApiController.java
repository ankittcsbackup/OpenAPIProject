package com.example.hello.api;

import com.example.hello.model.GetHello200Response;
import com.example.hello.service.HelloService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloApiController implements HelloApi {
 private final HelloService service;
 public HelloApiController(HelloService service) {
  this.service = service;
 }
 public ResponseEntity<GetHello200Response> getHello() {
  return ResponseEntity.ok(new GetHello200Response().message(service.getHello()));
 }
}
