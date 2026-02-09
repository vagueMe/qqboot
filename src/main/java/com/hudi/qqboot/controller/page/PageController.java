package com.hudi.qqboot.controller.page;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/page")
public class PageController {

    @GetMapping("/search")
    public String search() {
        return "search";
    }

    @GetMapping("/pattern-search")
    public String patternSearch() {
        return "searchByPatternPage";
    }

    @GetMapping("/stream-demo")
    public String streamDemo() {
        return "stream-demo";
    }

    @GetMapping("/simple-stream-test")
    public String simpleStreamTest() {
        return "simple-stream-test";
    }
}