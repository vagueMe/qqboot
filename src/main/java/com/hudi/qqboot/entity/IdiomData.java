package com.hudi.qqboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author hudi
 * @date 06 1月 2026 15:39
 */
@Data
@TableName("t_idiom_data") // 指定表名
public class IdiomData {

    @TableId(type = IdType.ASSIGN_UUID) // 主键自增
    private String id; // 添加主键字段
    // 成语全名
    private String fullName;

    // 成语全拼
    private String fullPy;

    // 成语第一个汉字
    private String firstName;
    // 成语第二个汉字
    private String secondName;
    // 成语第三个汉字
    private String thirdName;
    // 成语第四个汉字
    private String fourthName;

    // 成语第一个拼音
    private String firstP;
    // 成语第二个拼音
    private String secondP;
    // 成语第三个拼音
    private String thirdP;
    // 成语第四个拼音
    private String fourthP;

    // 成语第一个拼音的属于第几声调
    private Integer firstY;
    // 成语第二个拼音的属于第几声调
    private Integer secondY;
    // 成语第三个拼音的属于第几声调
    private Integer thirdY;
    // 成语第四个拼音的属于第几声调
    private Integer fourthY;
    // 注释
    private String explanation;


}
