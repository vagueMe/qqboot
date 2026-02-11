package com.hudi.qqboot.service;

import cn.hutool.core.date.DateUtil;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author hudi
 * @date 10 2月 2026 10:56
 */
@Service
public class ToolsService {

    @Tool("你知道我叫什么的名字")
    public String name(@P("姓名") String name) {
        System.out.println("name = " + name);
        if ("胡迪".equals(name)) {
            name = name + "老师,今天天气真不错,哈哈";
        }
        return "Hello, " + name + "!";
    }

    @Tool("今天是几号")
    public String weather() {
        Date  date = new Date();
        System.out.println("今天是几号");
        return "今天是" + DateUtil.formatDateTime(date);
    }
}
