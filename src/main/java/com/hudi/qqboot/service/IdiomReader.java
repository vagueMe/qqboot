package com.hudi.qqboot.service;

import com.hudi.qqboot.entity.IdiomData;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 读取成语大全文档并转换为MessageData对象列表
 */
public class IdiomReader {

    // 定义声调符号数组
    private static final char[] TONE1 = {'ā', 'ē', 'ī', 'ō', 'ū', 'ǖ'};
    private static final char[] TONE2 = {'á', 'é', 'í', 'ó', 'ú', 'ǘ'};
    private static final char[] TONE3 = {'ǎ', 'ě', 'ǐ', 'ǒ', 'ǔ', 'ǚ'};
    private static final char[] TONE4 = {'à', 'è', 'ì', 'ò', 'ù', 'ǜ'};

    /**
     * 将带声调的拼音转换为不带声调的拼音
     * @param pinyin 带声调的拼音
     * @return 不带声调的拼音
     */
    public static String convertToneToNoTone(String pinyin) {
        if (pinyin == null || pinyin.isEmpty()) {
            return pinyin;
        }
        
        StringBuilder result = new StringBuilder();
        for (char c : pinyin.toCharArray()) {
            char convertedChar = convertToneCharToNoTone(c);
            result.append(convertedChar);
        }
        
        return result.toString();
    }
    
    /**
     * 将单个带声调字符转换为不带声调的字符
     * @param c 带声调的字符
     * @return 不带声调的字符
     */
    private static char convertToneCharToNoTone(char c) {
        if (c >= 'a' && c <= 'z') {
            return c; // 已经是不带声调的字符
        }
        
        // 检查是否是带声调的字符
        if (isToneChar(c)) {
            switch (c) {
                // 第一声
                case 'ā': return 'a';
                case 'ē': return 'e';
                case 'ī': return 'i';
                case 'ō': return 'o';
                case 'ū': return 'u';
                case 'ǖ': return 'v';
                // 第二声
                case 'á': return 'a';
                case 'é': return 'e';
                case 'í': return 'i';
                case 'ó': return 'o';
                case 'ú': return 'u';
                case 'ǘ': return 'v';
                // 第三声
                case 'ǎ': return 'a';
                case 'ě': return 'e';
                case 'ǐ': return 'i';
                case 'ǒ': return 'o';
                case 'ǔ': return 'u';
                case 'ǚ': return 'v';
                // 第四声
                case 'à': return 'a';
                case 'è': return 'e';
                case 'ì': return 'i';
                case 'ò': return 'o';
                case 'ù': return 'u';
                case 'ǜ': return 'v';
                default:
                    return c;
            }
        }
        
        return c; // 不是带声调的字符，直接返回
    }
    
    /**
     * 判断字符是否是带声调的字符
     * @param c 字符
     * @return 是否是带声调的字符
     */
    private static boolean isToneChar(char c) {
        return (c == 'ā' || c == 'ē' || c == 'ī' || c == 'ō' || c == 'ū' || c == 'ǖ' ||
                c == 'á' || c == 'é' || c == 'í' || c == 'ó' || c == 'ú' || c == 'ǘ' ||
                c == 'ǎ' || c == 'ě' || c == 'ǐ' || c == 'ǒ' || c == 'ǔ' || c == 'ǚ' ||
                c == 'à' || c == 'è' || c == 'ì' || c == 'ò' || c == 'ù' || c == 'ǜ');
    }

    /**
     * 读取成语大全文档并转换为MessageData对象列表
     * @param filePath 文档路径
     * @return MessageData对象列表
     */
    public static List<IdiomData> readIdiomData(String filePath) {
        List<IdiomData> messageDataList = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(filePath), StandardCharsets.UTF_8))) { // 使用GBK编码读取

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue; // 跳过空行
                }

                // 解析每一行成语数据
                IdiomData messageData = parseIdiomLine(line);
                if (messageData != null) {
                    messageDataList.add(messageData);
                }
            }

        } catch (IOException e) {
            System.err.println("读取文件时发生错误: " + e.getMessage());
            e.printStackTrace();
        }

        return messageDataList;
    }

    /**
     * 解析单行成语数据
     * @param line 单行数据
     * @return MessageData对象
     */
    private static IdiomData parseIdiomLine(String line) {
        // 正则表达式匹配成语格式
        // 匹配成语名、拼音和解释
        if (line == null || line.isEmpty()) {
            return null;
        }

        String[] parts = line.split("拼音：");
        if (parts.length < 2) {
            System.out.println("parts = " + line);
            return null;
        }
        String idiomName = parts[0].trim();
        String[] split = parts[1].split("释义：");
        if (split.length < 2) {
            System.out.println("parts = " + line);
            return null;
        }
        String pinyin = split[0];
        String explanation = split[1];

        IdiomData messageData = new IdiomData();

        // 设置成语全名
        messageData.setFullName(idiomName);
        messageData.setFullPy(pinyin);
        messageData.setExplanation(explanation);

        // 分别设置每个汉字
        if (idiomName.length() >= 4) {
            messageData.setFirstName(String.valueOf(idiomName.charAt(0)));
            messageData.setSecondName(String.valueOf(idiomName.charAt(1)));
            messageData.setThirdName(String.valueOf(idiomName.charAt(2)));
            messageData.setFourthName(String.valueOf(idiomName.charAt(3)));
        }

        // 解析拼音
        String[] pinyinParts = pinyin.split("\\s+");
        if (pinyinParts.length >= 4) {
            messageData.setFirstP(convertToneToNoTone(pinyinParts[0]));
            messageData.setSecondP(convertToneToNoTone(pinyinParts[1]));
            messageData.setThirdP(convertToneToNoTone(pinyinParts[2]));
            messageData.setFourthP(convertToneToNoTone(pinyinParts[3]));
        }

        // 提取声调信息（从拼音中提取数字，代表声调）
        extractToneInfo(messageData, pinyin);

        return messageData;
    }

    /**
     * 从拼音中提取声调信息
     * @param messageData MessageData对象
     * @param pinyin 拼音字符串
     */
    private static void extractToneInfo(IdiomData messageData, String pinyin) {
        String[] pinyinParts = pinyin.split("\\s+");
        if (pinyinParts.length >= 4) {
            messageData.setFirstY(extractTone(pinyinParts[0]));
            messageData.setSecondY(extractTone(pinyinParts[1]));
            messageData.setThirdY(extractTone(pinyinParts[2]));
            messageData.setFourthY(extractTone(pinyinParts[3]));
        }
    }

    public static  int extractTone(String pinyin) {
        int tone = 0;
        for (char c : pinyin.toCharArray()) {
            tone = getTone(c);
            if (tone > 0){
                return tone;
            }
        }
        return tone;
    }

    public static int getTone(char toneChar) {
        if (Arrays.binarySearch(TONE1, toneChar) >= 0) return 1;
        if (Arrays.binarySearch(TONE2, toneChar) >= 0) return 2;
        if (Arrays.binarySearch(TONE3, toneChar) >= 0) return 3;
        if (Arrays.binarySearch(TONE4, toneChar) >= 0) return 4;
        return 0; // 无声调
    }

    // 测试方法
    public static void main(String[] args) {
        String filePath = "D:\\新文件 4.txt";
        List<IdiomData> idiomList = readIdiomData(filePath);

        System.out.println("共读取到 " + idiomList.size() + " 条成语数据");

        // 打印前几条数据验证
        for (int i = 0; i < Math.min(5, idiomList.size()); i++) {
            IdiomData data = idiomList.get(i);
            System.out.println("成语: " + data.getFullName() +
                    ", 拼音: " + data.getFullPy() +
                    ", 首字: " + data.getFirstName());
        }
    }
}
