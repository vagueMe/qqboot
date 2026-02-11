package com.hudi.qqboot;

import com.hudi.qqboot.entity.IdiomData;
import com.hudi.qqboot.service.IdiomDataService;
import com.hudi.qqboot.service.IdiomDataBatchService;
import com.hudi.qqboot.service.IdiomReader;
import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.community.model.dashscope.QwenEmbeddingModel;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.loader.ClassPathDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentByCharacterSplitter;
import dev.langchain4j.data.document.splitter.DocumentByRegexSplitter;
import dev.langchain4j.data.document.splitter.DocumentBySentenceSplitter;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.InputStream;
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

    @Autowired
    private QwenChatModel qwenChatModel;

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

    @Test
    public void test03() {
        // 读取文档成语大全
        Document document = ClassPathDocumentLoader.loadDocument("rag/成语大全.txt", new TextDocumentParser());
//        System.out.println("text = " + document.text());
        // 加入分割器，对文本分块
        DocumentByRegexSplitter splitter = new DocumentByRegexSplitter(
                "\\n+", // 分割符
                "", // 分割符
                300, // 每段最长字数
                10 // 自然语言最大重叠字数
        );
        List<TextSegment> split = splitter.split(document);

//        System.out.println(split);
        // 将分块使用向量模型转为向量
        List<Embedding> content = qwenEmbeddingModel.embedAll(split).content();
        // 将向量数据存入
        InMemoryEmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
        embeddingStore.addAll(content, split);

        // 构建查询向量
        Embedding queryEmbedding = qwenEmbeddingModel.embed("请给我一个成语").content();
        EmbeddingSearchRequest build = EmbeddingSearchRequest.builder()
                .maxResults(1)
                .queryEmbedding(queryEmbedding).build();
        // 查询
        EmbeddingSearchResult<TextSegment> search = embeddingStore.search(build);
        search.matches().forEach( match -> {
            System.out.println("匹配度：" + match.score());
            System.out.println("内容：" + match.embedded().text());
        });

        //创建内容
        EmbeddingStoreContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(qwenEmbeddingModel).
                maxResults(3)
                .minScore(0.7)
                .build();

        // 加入ai代理组手
        IdiomDataAi idiomDataAi = AiServices.builder(IdiomDataAi.class)
                .chatModel(qwenChatModel)
                .contentRetriever(contentRetriever)
                .build();

        String chat = idiomDataAi.chat("给我一个“坐”字开头的成语成语");
        System.out.println("chat = " + chat);
    }

    public interface IdiomDataAi {
        String chat(String message);
    }

}
