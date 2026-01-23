package com.hudi.qqboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hudi.qqboot.entity.IdiomData;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface IdiomDataMapper extends BaseMapper<IdiomData> {
    // 继承 BaseMapper 后，自动拥有 CRUD 方法
    // 可以在这里添加自定义查询方法
    
    /**
     * 批量插入成语数据
     * @param idiomDataList 成语数据列表
     * @return 插入成功的记录数
     */
    @Insert({"<script>",
            "INSERT INTO t_idiom_data (id ,full_name, full_py, first_name, second_name, third_name, fourth_name, ",
            "first_p, second_p, third_p, fourth_p, first_y, second_y, third_y, fourth_y, explanation) ",
            "VALUES ",
            "<foreach collection=\"list\" item=\"item\" separator=\",\">",
            "(#{item.id},#{item.fullName}, #{item.fullPy}, #{item.firstName}, #{item.secondName}, #{item.thirdName}, #{item.fourthName}, ",
            "#{item.firstP}, #{item.secondP}, #{item.thirdP}, #{item.fourthP}, #{item.firstY}, #{item.secondY}, #{item.thirdY}, #{item.fourthY}, #{item.explanation})",
            "</foreach>",
            "</script>"})
    int batchInsert(List<IdiomData> idiomDataList);
}
