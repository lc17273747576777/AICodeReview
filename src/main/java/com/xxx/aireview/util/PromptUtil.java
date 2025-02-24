package com.xxx.aireview.util;

import com.intellij.openapi.project.Project;
import com.xxx.aireview.config.ApiKeySettings;
import com.xxx.aireview.constant.Constants;

import java.util.Objects;

/**
 * PromptUtil
 *
 * @author xxx
 */
public class PromptUtil {

    public static final String DEFAULT_PROMPT_1 = prefixModify();
    public static final String DEFAULT_PROMPT_2 = prefixReconstruct();
    public static final String DEFAULT_PROMPT_3 = prefixFaultReview();

    public static int scenFlag = -1;

    public static String constructPrompt(Project project, String diff) {
        return "";
    }

    public static String constructReviewPrompt(Project project, String selectedCode) {
        String promptContent = "";
        ApiKeySettings settings = ApiKeySettings.getInstance();
        if (Constants.PROJECT_PROMPT.equals(settings.getPromptType())) {
            promptContent = FileUtil.loadProjectPrompt(project);
        } else {
            promptContent += settings.getCustomPrompts().get(scenFlag).getPrompt();
        }
        promptContent = promptContent.replace("{codeType}", settings.getCodeType());

        //增加提示：以纯文本的形式输出结果，不要包含任何的markdown格式
        promptContent += selectedCode;
        if (Objects.equals(settings.getCommitLanguage(), "中文"))
        {
            promptContent = promptContent + "\n\n记住: 输出为纯文本模式，要求交流的语言为中文";
        }
        if (Objects.equals(settings.getCommitLanguage(), "English")) {
            promptContent = promptContent + "\n\nnote: please communicate with English";
        }
        return promptContent;
    }

    private static String prefixModify() {
        return """
                你是一个代码规范优化助手，下面这段{codeType}代码可能有cleancode问题，请直接给出优化后的代码供用户阅读，生成的代码遵从以下规则：
                1、不改变代码本身的逻辑
                2、符合代码规范要求
                3、符合代码整洁要求
                以下是待优化代码段：
                """;
    }

    private static String prefixReconstruct() {
        return """
                你是一个代码重构专家，下面这段{codeType}代码可能有重构需求，请指出代码需要重构的原因，给出重构建议，并最终给出重构的完整代码结果，你的回复遵从以下规则：
                1、不改变代码本身的逻辑
                2、关注重复代码问题，如果有可提取的公共函数，需要给出修改建议
                3、特别关注是否有超大函数问题，如果某个函数行数特别多，逻辑非常复杂，则需要对该超大函数进行拆分
                4、变量名是否过多，如果过多则需要进行压缩数量或拆分函数
                5、函数的圈复杂度是否过高，如果过高则需要使用卫语句等方式降低圈复杂度
                以下是待优化代码段：
                """;
    }

    private static String prefixFaultReview() {
        return """
                你是一个代码检视专家，下面这段{codeType}代码可能有逻辑问题，请指出代码问题所在，给出优化建议，让用户明白自己能够在哪里进行改进，你的回复遵从以下规则：
                1、需指出代码具体的逻辑问题
                2、如果可以，列举问题可能出现的场景
                3、请用尽量委婉的语气，不确定的情况尽量不要视为问题
                4、如果确实没有问题，请简明答复
                以下是待优化代码段：
                """;
    }


}
