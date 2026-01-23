package com.hudi.qqboot.controller.page;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 成语数据控制器
 */
@Controller
@RequestMapping("/page")
public class PageController {

    /**
     * 成语查询页面
     * @return 页面视图
     */
    @GetMapping("/search.html")
    public String searchByPatternPage() {
        return "search";
    }
}