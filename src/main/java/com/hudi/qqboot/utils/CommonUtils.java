package com.hudi.qqboot.utils;

import cn.hutool.core.util.StrUtil;
import com.hudi.qqboot.entity.IdiomData;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author hudi
 * @date 13 1月 2026 17:10
 */
public class CommonUtils {

    @NotNull
    public static String getString(String pattern, List<IdiomData> idiomData) {
        String resMessage = "";
        boolean one = false;
        List<IdiomData> temp;
        if (idiomData.size() > 5) {
            // 当大于5时，随机取其中的5个
            java.util.Collections.shuffle(idiomData);
            temp = idiomData.subList(0, 5);
        } else {
            temp = idiomData;
        }
        if (temp.size() == 1 && !pattern.equals("成语 ")) {
            one = true;
        }
        String join = StrUtil.join("\n", temp.stream().map(IdiomData::getFullName).collect(Collectors.toList()));
        resMessage = resMessage + join;
        if (one) {
            resMessage = resMessage + "\n全拼：" + temp.get(0).getFullPy() +  "\n释义：" + temp.get(0).getExplanation();
        }
        return resMessage;
    }
}
