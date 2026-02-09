package com.hudi.qqboot.config;

import cn.hutool.core.io.FileUtil;
import com.deepoove.poi.XWPFTemplate;
import com.deepoove.poi.config.Configure;
import com.deepoove.poi.plugin.table.LoopColumnTableRenderPolicy;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Data
@ConfigurationProperties(prefix = "bot")
public class BotConfig {
    private String qq;
    private String qqName;

    private String password;
    @Value("${deepseek.api.url}")
    private String deepSeekApiUrl;
    @Value("${deepseek.api.key}")
    private String deepSeekApiKey;

    @Value("${qw.api.key}")
    private String qwApiKey;

    @Value("${qw.api.url}")
    private String qwApiUrl;

    @Value("${bot.listener.group}")
    private List<String> listenerGroup;

    @Value("${bot.listener.msgBegin:}")
    private List<String> msgBegin;

    @Autowired
    private Environment environment;

    public static void main(String[] args) throws IOException {

        Map<String, Object> data = new HashMap<>();

        // 文件标题


        // 领导信息（动态数量）
        List<Map<String, String>> leaders = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            Map<String, String> temp = new HashMap<>();
            temp.put("name", "名称" + i);
            temp.put("select", "选择" + i);
            temp.put("select_1", "饿饿" + i);
            temp.put("textfield_4", "看看" + i);
            temp.put("textfield_7", "哈哈" + i);
            if (i != 3) {
                temp.put("textfield_8", "来了" + i);
            }

            leaders.add(temp);
        }
        data.put("goods", leaders);
        data.put("mj", "内部系统");
        File file = FileUtil.file("D:\\opt\\template.docx");


        LoopColumnTableRenderPolicy hackLoopTableRenderPolicy = new LoopColumnTableRenderPolicy();
        Configure config = Configure.builder().bind("goods", hackLoopTableRenderPolicy).build();
        XWPFTemplate template = XWPFTemplate.compile(file, config).render(data);
        template.writeToFile("D:\\opt\\out_render_loopcolumn.docx");
    }

    public static double calculateAmount(double principal, double annualRate, double years) {
        // 每日利率 = 年利率 / 365
        double dailyRate = annualRate / 365;
        // 总天数 = 年数 × 365
        double totalDays = years * 365;
        // 复利公式：A = P × (1 + r)^n
        return principal * Math.pow(1 + dailyRate, totalDays);
    }
}