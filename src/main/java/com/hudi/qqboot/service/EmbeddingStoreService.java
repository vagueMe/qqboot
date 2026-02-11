package com.hudi.qqboot.service;

import com.hudi.qqboot.config.AiConfig;
import dev.langchain4j.community.model.dashscope.QwenEmbeddingModel;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.ClassPathDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentByRegexSplitter;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * @author hudi
 * @date 11 2月 2026 16:05
 */
@Service
public class EmbeddingStoreService {

    @Autowired
    QwenEmbeddingModel qwenEmbeddingModel;

    @Autowired
    EmbeddingStore embeddingStore;


    @PostConstruct // 在 Bean 初始化完成后立即执行
    public boolean initEmbedding() {
        boolean flag = false;
        System.out.println("即将进入初始化");
        if (AiConfig.tempInt == 0) {
            Document document = ClassPathDocumentLoader.loadDocument("rag/成语大全.txt", new TextDocumentParser());
            // 将文本分块
            DocumentByRegexSplitter splitter = new DocumentByRegexSplitter(
                    "\\n+",
                    "",
                    400,
                    10
            );
            List<TextSegment> split = splitter.split(document);
            // 将分块使用向量模型转为向量
            Response<List<Embedding>> listResponse = qwenEmbeddingModel.embedAll(split);
            // 存入向量模型
            embeddingStore.addAll(listResponse.content(), split);
            AiConfig.tempInt = AiConfig.tempInt + 1;
            flag = true;
            System.out.println("初始化数据成功");
        }
        System.out.println("初始化数据结束");
        return flag;
    }
}

