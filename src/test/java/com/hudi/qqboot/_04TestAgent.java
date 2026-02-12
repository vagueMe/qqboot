package com.hudi.qqboot;

import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author hudi
 * @date 11 2月 2026 17:25
 */
@SpringBootTest
public class _04TestAgent {

   /* @Autowired
    QwenStreamingChatModel qwen;

    @Autowired
    OpenAiStreamingChatModel deepseek;

    interface GreetingExpert {

        @UserMessage("以下文本是什么任务： {{it}}")
        TaskTypeEnum isTask(String text);

    }

    interface ChatBot {

        @SystemMessage("你是一名航空公司客服代理，请为客户服务：")
        String reply(String userMessage);
    }

    class MilesOfSmiles {

        private GreetingExpert greetingExpert;
        private ChatBot chatBot;

        public MilesOfSmiles(GreetingExpert greetingExpert, ChatBot chatBot) {
            this.greetingExpert = greetingExpert;
            this.chatBot = chatBot;
        }

        public String handle(String userMessage) {
            TaskTypeEnum task = greetingExpert.isTask(userMessage);

            switch (task) {
                case MODIFY_TICKET:
                case QUERY_TICKET:
                case CANCEL_TICKET:
                    return task.getName() + "调用service方法处理";
                case OTHER:
                    return chatBot.reply(userMessage);
            }
            return null;
        }

    }

    @Test
    void test() {
        GreetingExpert greetingExpert = AiServices.create(GreetingExpert.class, deepseek);

        ChatBot chatBot = AiServices.create(ChatBot.class, qwen);

        MilesOfSmiles milesOfSmiles = new MilesOfSmiles(greetingExpert, chatBot);

        String greeting = milesOfSmiles.handle("我要退票！");
        System.out.println(greeting);


    }*/
}
