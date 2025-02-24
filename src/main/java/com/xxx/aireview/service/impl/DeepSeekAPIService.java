package com.xxx.aireview.service.impl;

import com.xxx.aireview.constant.Constants;
import com.xxx.aireview.service.AIService;
import com.xxx.aireview.util.OpenAIUtil;

import java.util.List;
import java.util.function.Consumer;

public class DeepSeekAPIService implements AIService {
    @Override
    public boolean generateByStream() {
        return true;
    }

    @Override
    public String generateCommitMessage(String content) throws Exception {
        return "null";
    }

    @Override
    public void generateCommitMessageStream(String content, Consumer<String> onNext) throws Exception {
        OpenAIUtil.getAIResponseStream(Constants.DeepSeek, content, onNext);
    }

    public void generateListMessageStream(List<String> contents, Consumer<String> onNext) throws Exception {
    }
    @Override
    public boolean checkNecessaryModuleConfigIsRight() {
        return OpenAIUtil.checkNecessaryModuleConfigIsRight(Constants.DeepSeek);
    }
}
