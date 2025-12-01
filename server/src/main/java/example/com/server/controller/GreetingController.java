package example.com.server.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class GreetingController {
    @GetMapping("/greet")
    public String greetingWithGet(@RequestParam(defaultValue = "Гость") String name){
        return "Hello " + name + "!";
    }
}