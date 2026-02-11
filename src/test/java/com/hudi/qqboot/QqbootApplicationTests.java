package com.hudi.qqboot;

import com.hudi.qqboot.entity.IdiomData;
import com.hudi.qqboot.service.IdiomDataService;
import com.hudi.qqboot.service.IdiomDataBatchService;
import com.hudi.qqboot.service.IdiomReader;
import dev.langchain4j.community.model.dashscope.QwenEmbeddingModel;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
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

    @Autowired
    private QwenEmbeddingModel qwenEmbeddingModel;

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

    @Test
    public void enbeddingTest() {
        Response<Embedding> embed = qwenEmbeddingModel.embed("你好，我叫xx");
        System.out.println(embed.content().toString());
        System.out.println(embed.content().vector().length);
    }


    @Test
    public void test02() {
        // 使用InMemory向量数据库
        InMemoryEmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
        // 利用向量模型进行向量化，然后存储向量到向量数据库
        TextSegment segment1 = TextSegment.from("""
                预订航班:
                - 通过我们的网站或移动应用程序预订。
                - 预订时需要全额付款。
                - 确保个人信息（姓名、ID 等）的准确性，因为更正可能会产生 25 的费用。
                """);
        Embedding embedding1 = qwenEmbeddingModel.embed(segment1).content();
        embeddingStore.add(embedding1, segment1);

        TextSegment segment2 = TextSegment.from("""
                取消预订:
                - 最晚在航班起飞前 48 小时取消。
                - 取消费用：经济舱 75 美元，豪华经济舱 50 美元，商务舱 25 美元。
                - 退款将在 7 个工作日内处理。
                """);
        Embedding embedding2 = qwenEmbeddingModel.embed(segment2).content();
        embeddingStore.add(embedding2, segment2);

        // 需要查询的内容 向量化
        Embedding queryEmbedding = qwenEmbeddingModel.embed("退票要多少钱").content();

        // 从数据库中查询
        // 构建查询条件
        EmbeddingSearchRequest build = EmbeddingSearchRequest.builder()
//                .maxResults(1)
                .queryEmbedding(queryEmbedding)
                .build();
        // 查询
        EmbeddingSearchResult<TextSegment> search = embeddingStore.search(build);
        search.matches().forEach(match -> {
            System.out.println("匹配度：" + match.score());
            System.out.println("内容：" + match.embedded().text());
        });
    }


}
