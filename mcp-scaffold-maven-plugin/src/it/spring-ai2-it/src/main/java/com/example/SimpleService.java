package com.example;

import org.springframework.stereotype.Service;

@Service
public class SimpleService {

  /**
   * Says hello to someone.
   *
   * @param name the name of the person
   * @return a greeting message
   */
  public String sayHello(String name) {
    return "Hello, " + name;
  }
}
