package com.example.hello.service;

import org.springframework.stereotype.Service;

@Service
public class HelloServiceImpl implements HelloService {
 public String getHello() {
  return "Hello Contract-First API 🚀";
 }
}
