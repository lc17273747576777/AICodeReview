package com.xxx.aireview.config;

import com.xxx.aireview.constant.Constants;
import com.xxx.aireview.pojo.PromptInfo;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@State(name = "com.xxx.aireview.config.ApiKeySettings", storages = { @Storage("aireviewCommitSettings.xml") })
public class ApiKeySettings implements PersistentStateComponent<ApiKeySettings> {
    private String selectedClient = "Ollama";
    private String selectedModule = "deepseek-R1:14B:latest";
    private String commitLanguage = "中文";
    private String codeType = "";
    private String promptType = Constants.CUSTOM_PROMPT;

    // prompt from table
    private List<PromptInfo> customPrompts = new ArrayList<>();

    // current prompt by user choose
    private PromptInfo customPrompt = new PromptInfo("", "");

    private Map<String, ModuleConfig> moduleConfigs = new HashMap<>();

    public static ApiKeySettings getInstance() {
        return ApplicationManager.getApplication().getService(ApiKeySettings.class);
    }


    @Nullable
    @Override
    public ApiKeySettings getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull ApiKeySettings state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    public String getSelectedClient() {
        return selectedClient;
    }

    public void setSelectedClient(String selectedClient) {
        this.selectedClient = selectedClient;
    }

    public String getCommitLanguage() {
        return commitLanguage;
    }

    public void setCommitLanguage(String commitLanguage) {
        this.commitLanguage = commitLanguage;
    }

    public List<PromptInfo> getCustomPrompts() {
        if (customPrompts == null || customPrompts.isEmpty()) {
            customPrompts = PromptInfo.defaultPrompts();
        }
        return customPrompts;
    }

    public void setCustomPrompts(List<PromptInfo> customPrompts) {
        this.customPrompts = customPrompts;
    }

    public PromptInfo getCustomPrompt() {
        return customPrompt;
    }

    public void setCustomPrompt(PromptInfo customPrompt) {
        this.customPrompt = customPrompt;
    }

    public String getPromptType() {
        return promptType;
    }

    public void setPromptType(String promptType) {
        this.promptType = promptType;
    }

    public String getSelectedModule() {
        return selectedModule;
    }

    public void setSelectedModule(String selectedModule) {
        this.selectedModule = selectedModule;
    }

    public Map<String, ModuleConfig> getModuleConfigs() {
        return moduleConfigs;
    }

    public void setModuleConfigs(Map<String, ModuleConfig> moduleConfigs) {
        this.moduleConfigs = moduleConfigs;
    }

    public String getCodeType() {
        return codeType;
    }
    public void setCodeType(String codeType) {
        this.codeType = codeType;
    }

    public static class ModuleConfig {
        private String url;
        private String apiKey;

        public ModuleConfig() {
        }

        public ModuleConfig(String url, String apiKey) {
            this.url = url;
            this.apiKey = apiKey;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }
    }
}