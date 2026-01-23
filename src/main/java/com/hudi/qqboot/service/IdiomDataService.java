package com.hudi.qqboot.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hudi.qqboot.entity.IdiomData;
import com.hudi.qqboot.mapper.IdiomDataMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class IdiomDataService extends ServiceImpl<IdiomDataMapper, IdiomData> {
    // 继承 ServiceImpl 后，自动拥有常用的业务方法

    /**
     * 优化的批量插入方法
     * @param entityList 实体列表
     * @return 是否成功
     */
    public boolean saveBatchOptimized(List<IdiomData> entityList) {
        if (ObjectUtils.isEmpty(entityList)) {
            return true;
        }

        // 分批处理，避免单次处理过多数据导致内存溢出或超时
        int batchSize = 1000; // 每批处理1000条
        for (int i = 0; i < entityList.size(); i += batchSize) {
            int end = Math.min(i + batchSize, entityList.size());
            List<IdiomData> batchList = entityList.subList(i, end);

            // 使用 MyBatis-Plus 的 saveBatch 方法，但指定批次大小
            this.saveBatch(batchList, batchSize);
        }

        return true;
    }

    /**
     * 根据拼音和汉字混合模式查询成语（模糊匹配）
     * @param pattern 查询模式，如 "jiu|3|liu|2|bin|1|ke|4"
     * @return 匹配的成语数据列表
     */
    public List<IdiomData> searchByPattern(String pattern) {
        if (StrUtil.isBlank(pattern) || !pattern.startsWith("成语 ")) {
            return new ArrayList<>();
        }
        pattern = pattern.replace("成语 ", "");
        String temp = pattern.replace("。", "").replace("*","").replace(".","").replace(" ","");
        if (StrUtil.isBlank(pattern) || StrUtil.isBlank(temp)) {
            // 随机获取5条成语数据
            return getRandomIdiomDataFromDB();
        }
        pattern = pattern.replace("。", ".");
        String[] originalParts = StrUtil.splitToArray(pattern, ".");;

        String[] parts;
        // 确保数组长度为8，不足的用*补全
        if (originalParts.length >= 8) {
            parts = originalParts;
        } else {
            parts = new String[8];
            for (int i = 0; i < 8; i++) {
                if (i < originalParts.length) {
                    parts[i] = originalParts[i];
                } else {
                    parts[i] = "*";
                }
            }
        }

        // 构建查询条件
        QueryWrapper<IdiomData> queryWrapper = new QueryWrapper<>();

        // 检查第一个字符（汉字或拼音）
        if (!"*".equals(parts[0].trim())) {
            queryWrapper.and(wrapper -> wrapper.like("first_name", parts[0].trim()).or().like("first_p", parts[0].trim()));
        }

        // 检查第一个声调
        if (!"*".equals(parts[1].trim())) {
            try {
                Integer tone = Integer.parseInt(parts[1].trim());
                queryWrapper.eq("first_y", tone);
            } catch (NumberFormatException e) {
                // 如果不是数字，忽略这个条件
            }
        }

        // 检查第二个字符（汉字或拼音）
        if (!"*".equals(parts[2].trim())) {
            queryWrapper.and(wrapper -> wrapper.like("second_name", parts[2].trim()).or().like("second_p", parts[2].trim()));
        }

        // 检查第二个声调
        if (!"*".equals(parts[3].trim())) {
            try {
                Integer tone = Integer.parseInt(parts[3].trim());
                queryWrapper.eq("second_y", tone);
            } catch (NumberFormatException e) {
                // 如果不是数字，忽略这个条件
            }
        }

        // 检查第三个字符（汉字或拼音）
        if (!"*".equals(parts[4].trim())) {
            queryWrapper.and(wrapper -> wrapper.like("third_name", parts[4].trim()).or().like("third_p", parts[4].trim()));
        }

        // 检查第三个声调
        if (!"*".equals(parts[5].trim())) {
            try {
                Integer tone = Integer.parseInt(parts[5].trim());
                queryWrapper.eq("third_y", tone);
            } catch (NumberFormatException e) {
                // 如果不是数字，忽略这个条件
            }
        }

        // 检查第四个字符（汉字或拼音）
        if (!"*".equals(parts[6].trim())) {
            queryWrapper.and(wrapper -> wrapper.like("fourth_name", parts[6].trim()).or().like("fourth_p", parts[6].trim()));
        }

        // 检查第四个声调
        if (!"*".equals(parts[7].trim())) {
            try {
                Integer tone = Integer.parseInt(parts[7].trim());
                queryWrapper.eq("fourth_y", tone);
            } catch (NumberFormatException e) {
                // 如果不是数字，忽略这个条件
            }
        }

        // 添加分页限制，最多返回10条记录
        return this.list(queryWrapper.last("LIMIT 10"));
    }

    /**
     * 从数据库中随机获取3条成语数据
     * @return 成语数据列表
     */
    public List<IdiomData> getRandomIdiomDataFromDB() {
        // 获取总数
        long total = this.count();
        if (total == 0) {
            return new ArrayList<>();
        }

        // 生成随机偏移量
        long randomOffset = (long) (Math.random() * total);

        // 使用 offset 和 limit 获取随机记录
        QueryWrapper<IdiomData> queryWrapper = new QueryWrapper<>();
        queryWrapper.last("LIMIT 5 OFFSET " + randomOffset);

        List<IdiomData> result = this.list(queryWrapper);
        return result;
    }
}