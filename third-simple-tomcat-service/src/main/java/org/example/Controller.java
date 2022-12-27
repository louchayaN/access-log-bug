package org.example;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class Controller {

    @GetMapping("/test-final-api")
    private String getTestResult() {
        return "Processed by thread " + Thread.currentThread().getName();
    }

}