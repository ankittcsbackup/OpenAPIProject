package com.example.hello.api;

import com.example.hello.service.HelloService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HelloApiController implements HelloApi {
 private final HelloService service;
 public HelloApiController(HelloService service) {
  this.service = service;
 }
 public ResponseEntity<Map<String,String>> getHello() {
  return ResponseEntity.ok(Map.of("message", service.getHello()));
 }
}
