package com.xxx.aireview.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xxx.aireview.config.ApiKeySettings;
import com.xxx.aireview.constant.Constants;
import com.xxx.aireview.service.AIService;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import java.net.http.*;
import java.util.*;
/**
 * OllamaService
 *
 * @author xxx
 */
public class OllamaService implements AIService {
    @Override
    public boolean generateByStream() {
        return true;
    }

    @Override
    public String generateCommitMessage(String content) throws Exception {
        ApiKeySettings settings = ApiKeySettings.getInstance();
        String selectedModule = settings.getSelectedModule();
        ApiKeySettings.ModuleConfig moduleConfig = settings.getModuleConfigs().get(settings.getSelectedClient());
        String aiResponse = getAIResponse(selectedModule, moduleConfig.getUrl(), content);
        return aiResponse.replaceAll("```", "");
    }

    @Override
    public void generateCommitMessageStream(String content, Consumer<String> onNext) throws Exception {
        getAIResponseStream(content, onNext);
    }
    @Override
    public void generateListMessageStream(List<String> contents, Consumer<String> onNext) throws Exception {
        getAIResponseStreamHistory(contents, onNext);
    }

    @Override
    public boolean checkNecessaryModuleConfigIsRight() {
        ApiKeySettings settings = ApiKeySettings.getInstance();
        ApiKeySettings.ModuleConfig moduleConfig = settings.getModuleConfigs().get(Constants.Ollama);
        if (moduleConfig == null) {
            return false;
        }
        String selectedModule = settings.getSelectedModule();
        String url = moduleConfig.getUrl();
        return StringUtils.isNotEmpty(selectedModule) && StringUtils.isNotEmpty(url);
    }

    @Override
    public boolean validateConfig(Map<String, String> config) {
        int statusCode;
        try {
            HttpURLConnection connection = getHttpURLConnection(config.get("module"), config.get("url"), "hi");
            statusCode = connection.getResponseCode();
        } catch (IOException e) {
            return false;
        }
        return statusCode == 200;
    }

    private static String getAIResponse(String module, String url, String textContent) throws Exception {
        HttpURLConnection connection = getHttpURLConnection(module, url, textContent);

        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
        }

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonResponse = objectMapper.readTree(response.toString());
        return jsonResponse.path("response").asText();
    }

    private static @NotNull HttpURLConnection getHttpURLConnection(String module, String url, String textContent)
            throws IOException {

        GenerateRequest request = new GenerateRequest(module, textContent, false);
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonInputString = objectMapper.writeValueAsString(request);

        URI uri = URI.create(url);
        HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        return connection;
    }

    private static class GenerateRequest {
        private String model;
        private String prompt;
        private boolean stream;

        public GenerateRequest(String model, String prompt, boolean stream) {
            this.model = model;
            this.prompt = prompt;
            this.stream = stream;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public String getPrompt() {
            return prompt;
        }

        public void setPrompt(String prompt) {
            this.prompt = prompt;
        }

        public boolean isStream() {
            return stream;
        }

        public void setStream(boolean stream) {
            this.stream = stream;
        }
    }
    private static class ChatRequest {
        private String model;
        private List<Map<String, String>> messages;
        private boolean stream;

        public ChatRequest(String model, List<Map<String, String>> messages, boolean stream) {
            this.model = model;
            this.messages = messages;
            this.stream = stream;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public List<Map<String, String>> getMessages() {
            return messages;
        }

        public void setMessages(List<Map<String, String>> messages) {
            this.messages = messages;
        }

        public boolean isStream() {
            return stream;
        }

        public void setStream(boolean stream) {
            this.stream = stream;
        }
    }

    private List<Map<String, String>> addUserMessage(List<String> contents) {
        List<Map<String, String>> messageHistory = new ArrayList<>();
        for (String content : contents) {
            Map<String, String> singleMessage = new HashMap<>();
            singleMessage.put("role","user");
            singleMessage.put("content", content);
            messageHistory.add(singleMessage);
        }
        return messageHistory;
    }

    private static final ObjectMapper mapper = new ObjectMapper();

    public static String chatWithHistory(List<Map<String, String>> history) throws Exception {
        ApiKeySettings settings = ApiKeySettings.getInstance();
        String selectedModule = settings.getSelectedModule();
        ApiKeySettings.ModuleConfig moduleConfig = settings.getModuleConfigs().get(Constants.Ollama);
        ObjectMapper objectMapper = new ObjectMapper();
        // 构建请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", selectedModule);  // 根据实际模型修改
        requestBody.put("messages", history);
        requestBody.put("stream", true);

        // 使用Jackson生成JSON
        String jsonBody = mapper.writeValueAsString(requestBody);

        // 创建HTTP请求
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(moduleConfig.getUrl()))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        // 发送请求并获取响应
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return response.body();
        } else {
            throw new RuntimeException("API请求失败: " + response.body());
        }
    }
    private void getAIResponseStreamHistory(List<String> textContents, Consumer<String> onNext) throws Exception {
//        List<Message> history = addUserMessage(textContents);
        ApiKeySettings settings = ApiKeySettings.getInstance();
        String selectedModule = settings.getSelectedModule();
        ApiKeySettings.ModuleConfig moduleConfig = settings.getModuleConfigs().get(settings.getSelectedClient());

        List<Map<String, String>> userMessagesText = addUserMessage(textContents);
        ChatRequest chatRequest = new ChatRequest(selectedModule, userMessagesText, true);
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonInputString = objectMapper.writeValueAsString(chatRequest);
        String chatUri = moduleConfig.getUrl().replace("api/generate", "api/chat");

        URI uri = URI.create(chatUri);
        HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
//


//        ObjectMapper objectMapper = new ObjectMapper();
        // 构建请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", selectedModule);  // 根据实际模型修改
        requestBody.put("messages", userMessagesText);
        requestBody.put("stream", true);
        requestBody.put("temperature", 0.7); // 可选，根据需要调整
        requestBody.put("max_tokens", 300);
        // 使用Jackson生成JSON
        String jsonBody = mapper.writeValueAsString(requestBody);

        // 创建HTTP请求
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(chatUri))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        // 发送请求并获取响应
//        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());


        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                JsonNode jsonResponse = objectMapper.readTree(line);
                String response = jsonResponse.path("message").get("content").asText();
                if (!response.isEmpty()) {
                    onNext.accept(response);
                }
            }
        }
    }
    private void getAIResponseStream(String textContent, Consumer<String> onNext) throws Exception {
        ApiKeySettings settings = ApiKeySettings.getInstance();
        String selectedModule = settings.getSelectedModule();
        ApiKeySettings.ModuleConfig moduleConfig = settings.getModuleConfigs().get(settings.getSelectedClient());

        GenerateRequest request = new GenerateRequest(selectedModule, textContent, true);
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonInputString = objectMapper.writeValueAsString(request);

        URI uri = URI.create(moduleConfig.getUrl());
        HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                JsonNode jsonResponse = objectMapper.readTree(line);
                String response = jsonResponse.path("response").asText();
                if (!response.isEmpty()) {
                    onNext.accept(response);
                }
            }
        }
    }
}
