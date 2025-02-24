package com.xxx.aireview.service;


import com.xxx.aireview.config.ApiKeySettings;
import com.xxx.aireview.constant.Constants;
import com.xxx.aireview.service.impl.*;
import com.xxx.aireview.util.PromptUtil;
import com.intellij.openapi.project.Project;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class CommitMessageService {
    private final AIService aiService;

    ApiKeySettings settings = ApiKeySettings.getInstance();

    public CommitMessageService() {
        String selectedClient = settings.getSelectedClient();
        ApiKeySettings.ModuleConfig selectedModuleConfig = new ApiKeySettings.ModuleConfig();
        selectedModuleConfig.setUrl(Constants.moduleConfigs.get(selectedClient).getUrl());
        selectedModuleConfig.setApiKey(Constants.moduleConfigs.get(selectedClient).getApiKey());
        Map<String, ApiKeySettings.ModuleConfig> moduleConfigs = new HashMap<>();
        moduleConfigs.put(selectedClient, selectedModuleConfig);
        settings.setModuleConfigs(moduleConfigs);
        this.aiService = getAIService(selectedClient);
    }

    public boolean checkNecessaryModuleConfigIsRight() {
        return aiService.checkNecessaryModuleConfigIsRight();
    }

    public String generateCommitMessage(Project project, String diff) throws Exception {
        String prompt = PromptUtil.constructPrompt(project, diff);
        return aiService.generateCommitMessage(prompt);
    }

    public void generateCommitMessageStream(Project project, String diff, Consumer<String> onNext, Consumer<Throwable> onError) throws Exception {
        String prompt = PromptUtil.constructPrompt(project, diff);
//        System.out.println(prompt);
        aiService.generateCommitMessageStream(prompt, onNext);
    }

    public void generateReviewMessageStream(String codeText, Consumer<String> onNext, Consumer<Throwable> onError) throws Exception {
        aiService.generateCommitMessageStream(codeText, onNext);
    }
    public void ResponseStream(List<String> historyTextList, Consumer<String> onNext, Consumer<Throwable> onError) throws Exception {
        aiService.generateListMessageStream(historyTextList, onNext);
    }

    public boolean generateByStream() {
        return aiService.generateByStream();
    }

    public static AIService getAIService(String selectedClient) {
        return switch (selectedClient) {
            case Constants.Ollama -> new OllamaService();
            case Constants.DeepSeek -> new DeepSeekAPIService();
            default -> throw new IllegalArgumentException("Invalid LLM client: " + selectedClient);
        };
    }

}
