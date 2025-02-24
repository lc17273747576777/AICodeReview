package com.xxx.aireview.service;

import com.xxx.aireview.util.OpenAIUtil;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * AIService
 *
 * @author xxx
 */
public interface AIService {

    boolean generateByStream();

    String generateCommitMessage(String content) throws Exception;

    void generateCommitMessageStream(String content, Consumer<String> onNext) throws Exception;

    void generateListMessageStream(List<String> contents, Consumer<String> onNext) throws Exception;

    boolean checkNecessaryModuleConfigIsRight();


    default boolean validateConfig(Map<String, String> config) {
        int statusCode;
        try {
            HttpURLConnection connection = OpenAIUtil.getHttpURLConnection(config.get("url"), config.get("module"), config.get("apiKey"), "hi");
            statusCode = connection.getResponseCode();
        } catch (IOException e) {
            return false;
        }
        return statusCode == 200;
    }
}
