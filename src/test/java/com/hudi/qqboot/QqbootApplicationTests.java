package com.hudi.qqboot;

import com.hudi.qqboot.entity.IdiomData;
import com.hudi.qqboot.service.IdiomDataService;
import com.hudi.qqboot.service.IdiomDataBatchService;
import com.hudi.qqboot.service.IdiomReader;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
class QqbootApplicationTests {
    @Autowired
    private IdiomDataService idiomDataService;
    
    @Autowired
    private IdiomDataBatchService idiomDataBatchService;

    @Test
    void contextLoads() {
        String filePath = "D:\\新文件 4.txt";
        List<IdiomData> idiomList = IdiomReader.readIdiomData(filePath);

        System.out.println("共读取到 " + idiomList.size() + " 条成语数据");

        // 打印前几条数据验证
        List<IdiomData> tempList = new ArrayList<>();
        for (int i = 0; i < Math.min(5, idiomList.size()); i++) {
            IdiomData data = idiomList.get(i);
            System.out.println("成语: " + data.getFullName() +
                    ", 拼音: " + data.getFullPy() +
                    ", 首字: " + data.getFirstName());
            tempList.add(data);
        }
        // 使用优化的批量插入方法
        long startTime = System.currentTimeMillis();
        idiomDataBatchService.batchInsertOptimized(idiomList);
        long endTime = System.currentTimeMillis();
        System.out.println("批量插入耗时: " + (endTime - startTime) + " ms");
    }



}
