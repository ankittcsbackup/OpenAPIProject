package com.example.hello;

import com.example.hello.service.HelloService;
import com.example.hello.service.HelloServiceImpl;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HelloServiceTest {
 private final HelloService service = new HelloServiceImpl();
 @Test
 void testHello() {
  assertEquals("Hello Contract-First API 🚀", service.getHello());
 }
}
