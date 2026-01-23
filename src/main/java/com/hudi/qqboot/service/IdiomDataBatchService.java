package com.hudi.qqboot.service;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.hudi.qqboot.entity.IdiomData;
import com.hudi.qqboot.mapper.IdiomDataMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 专门用于批量导入数据的服务类
 */
@Service
public class IdiomDataBatchService {

    @Autowired
    private IdiomDataMapper idiomDataMapper;

    /**
     * 最优化的批量插入方法，适用于大量数据
     * @param dataList 要插入的数据列表
     * @return 插入成功的记录数
     */
    @Transactional
    public int batchInsertOptimized(List<IdiomData> dataList) {
        if (ObjectUtils.isEmpty(dataList)) {
            return 0;
        }
        
        // 根据数据量动态调整批次大小
        int dataSize = dataList.size();
        int optimalBatchSize = dataSize > 10000 ? 2000 : 1000;
        
        int totalInserted = 0;
        for (int i = 0; i < dataSize; i += optimalBatchSize) {
            int end = Math.min(i + optimalBatchSize, dataSize);
            List<IdiomData> batchList = dataList.subList(i, end);
            
            int insertedInBatch = idiomDataMapper.batchInsert(batchList);
            totalInserted += insertedInBatch;
        }
        
        return totalInserted;
    }
}