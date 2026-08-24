package com.payflow.payflow;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController // Tells Spring: "this class handles HTTP requests and returns data directly"
public class HealthController {

    @GetMapping("/api/health") // Maps GET requests at /api/health to this method
    public String health(){
        return "PayFlow is alive";
    }
}
