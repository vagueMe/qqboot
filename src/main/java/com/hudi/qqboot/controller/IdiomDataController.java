package com.hudi.qqboot.controller;

import cn.hutool.core.util.StrUtil;
import com.hudi.qqboot.entity.IdiomData;
import com.hudi.qqboot.service.IdiomDataService;
import com.hudi.qqboot.utils.CommonUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 成语数据控制器
 */
@Controller
@RequestMapping("/getIdiomData")
public class IdiomDataController {

    @Autowired
    private IdiomDataService idiomDataService;

    /**
     * 根据ID查询成语数据
     * @param id 成语ID
     * @return 成语数据
     */
    @GetMapping("/getByid")
    @ResponseBody
    public IdiomData getById(@RequestParam("id") String id) {
        return idiomDataService.getById(id);
    }

    /**
     * 根据拼音和汉字混合模式查询成语（模糊匹配）
     * @param pattern 查询模式，如 "jiu|3|liu|2|bin|1|ke|4"
     * @return 匹配的成语数据列表
     */
    @GetMapping("/searchByPattern")
    @ResponseBody
    public String searchByPattern(@RequestParam("pattern") String pattern) {
        String resMessage = "";
        if (pattern.startsWith("成语 ")) {
            // 随机
            List<IdiomData> idiomData = idiomDataService.searchByPattern(pattern);
            if (!idiomData.isEmpty()) {
                resMessage = "查询结果：\n" + CommonUtils.getString(pattern, idiomData);
            } else {
                resMessage = "没有找到匹配的成语";
            }
        }
        return resMessage;
    }


    /**
     * 成语查询页面
     * @return 页面视图
     */
    @GetMapping("/searchByPatternPage.html")
    public String searchByPatternPage() {
        return "search";
    }
}