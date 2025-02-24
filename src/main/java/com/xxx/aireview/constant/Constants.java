package com.xxx.aireview.constant;

import com.xxx.aireview.config.ApiKeySettings;

import java.util.HashMap;
import java.util.Map;

/**
 * Constants
 *
 * @author xxx
 */
public class Constants {

    public static final String TASK_TITLE = "Generating review message";

    public static final String[] languages = {"中文", "English"};
    public static final String[] codeTypes = {"C", "C++", "Python", "Java", "Go", "Kotlin", "PHP", "Ruby", "JavaScript"};

    public static final String PROJECT_PROMPT_FILE_NAME = "project-prompt.txt";
    public static final String PROJECT_PROMPT = "Project Prompt";
    public static final String CUSTOM_PROMPT = "Custom Prompt";

    public static String[] getAllPromptTypes() {
        return new String[]{PROJECT_PROMPT, CUSTOM_PROMPT};
    }

    public static final String DeepSeek = "DeepSeek(unavailable for now)";
    public static final String Ollama = "Ollama";

    public static final String[] LLM_CLIENTS = {Ollama, DeepSeek};

    public static final Map<String, String[]> CLIENT_MODULES = new HashMap<>() {
        {
            put(Ollama, new String[]{"deepseek-R1:14B:latest", "deepseek-r1:32b", "deepseek-r1:70b(unavailable for now)"});
            put(DeepSeek, new String[]{"deepseek-chat"});
       }
    };

    public static Map<String, ApiKeySettings.ModuleConfig> moduleConfigs = new HashMap<>() {
        {
            put(DeepSeek, new ApiKeySettings.ModuleConfig("", ""));
            put(Ollama, new ApiKeySettings.ModuleConfig("http://localhost:11434/api/generate", ""));
        }
    };

    public static final Map<String, String> CLIENT_HELP_URLS = new HashMap<>() {
        {
            put(Constants.DeepSeek, "https://platform.deepseek.com/api_keys");
        }
    };
}
