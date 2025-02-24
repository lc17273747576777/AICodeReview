package com.xxx.aireview;

import com.xxx.aireview.constant.Constants;
import com.xxx.aireview.service.CommitMessageService;
import com.xxx.aireview.util.IdeaDialogUtil;
import com.xxx.aireview.config.ApiKeyConfigurable;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.xxx.aireview.util.PromptUtil;
import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.editor.*;
import com.intellij.ui.components.JBScrollPane;
import javax.swing.*;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.Objects;
import java.awt.*;
import java.awt.event.*;


/**
 * Action 类，用于生成 Git commit 消息
 * 继承自 AnAction 以集成到 IDEA 的操作系统中
 */
public class GenerateCommitMessageAction extends AnAction {
    /**
     * 获取CommitMessage对象
     */

    public void update(AnActionEvent e) {
        final Editor editor = e.getData(CommonDataKeys.EDITOR);
        e.getPresentation().setVisible(editor != null && editor.getSelectionModel().hasSelection());
    }


    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }
        if (Objects.equals(e.getPresentation().getText(), "代码整洁优化")) {
            PromptUtil.scenFlag = 0;
        }
        if (Objects.equals(e.getPresentation().getText(), "代码重构建议")) {
            PromptUtil.scenFlag = 1;
        }
        if (Objects.equals(e.getPresentation().getText(), "代码逻辑检视")) {
            PromptUtil.scenFlag = 2;
        }

        java.util.List<String> historyPromts = new ArrayList();
        JFrame frame = new JFrame("AI code review:");
        frame.setSize(1200, 800);
//        frame.setLayout(new BorderLayout());
        JTextArea textArea = new JTextArea();
        textArea.setEditable(true);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setFont(new Font("等宽字体", Font.PLAIN, 16));
        textArea.setMargin(new Insets(10, 10, 10, 10));
        JBScrollPane scrollPane = new JBScrollPane(textArea);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                textArea.setSize(scrollPane.getViewport().getSize());
                textArea.revalidate(); // 触发重绘
            }
        });
        JButton confirmButton = new JButton("追加提问");
        confirmButton.setSize(48, 16);
        JTextField inputArea = new JTextField(1);
        inputArea.setSize(frame.getWidth()-50, 16);
        JPanel addTxtPanel = new JPanel(new BorderLayout(10, 0));
//        frame.setLayout(new BorderLayout());

        addTxtPanel.add(inputArea, BorderLayout.CENTER);
        addTxtPanel.add(confirmButton, BorderLayout.EAST);
        addTxtPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(addTxtPanel, BorderLayout.SOUTH);

        frame.setVisible(true);
        CommitMessageService commitMessageService = new CommitMessageService();
        final Editor editor = e.getRequiredData(CommonDataKeys.EDITOR);
        final SelectionModel selection = editor.getSelectionModel();
        String originalText = selection.getSelectedText();
        historyPromts.add(PromptUtil.constructReviewPrompt(project, originalText));
        textArea.append("待检视代码:\n" + originalText + "\n\n");
        ProgressManager.getInstance().run(new Task.Backgroundable(project, Constants.TASK_TITLE, true) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                try {
                    commitMessageService.generateReviewMessageStream(originalText,
                            token -> ApplicationManager.getApplication().invokeLater(() -> {
                                if (Objects.equals(textArea.getText(), "")) {
                                    textArea.setText(token);
                                } else {
                                    textArea.append(token);
                                }
                            }),
                            // onError 处理错误
                            error -> ApplicationManager.getApplication().invokeLater(() -> {
                                IdeaDialogUtil.showError(project, "Error generating review message: <br>" + getErrorMessage(error.getMessage()), "Error");
                            }));
                } catch (IllegalArgumentException ex) {
                    IdeaDialogUtil.showWarning(project, ex.getMessage(), "AI review Message Warning");
                } catch (Exception ex) {
                    IdeaDialogUtil.showError(project, "Error generating review message: <br>" + getErrorMessage(ex.getMessage()), "Error");
                }
            }
        });
        confirmButton.addActionListener(e2 -> {
            String newPromt = inputArea.getText().trim();
            historyPromts.add(newPromt);
            inputArea.setText("");
            textArea.append("\n\n----追加提问----:\n" + newPromt + "\n\n");
            ProgressManager.getInstance().run(new Task.Backgroundable(project, Constants.TASK_TITLE, true) {
                @Override
                public void run(@NotNull ProgressIndicator indicator) {
                    try {
                        commitMessageService.ResponseStream(historyPromts,
                                token -> ApplicationManager.getApplication().invokeLater(() -> {
                                    if (Objects.equals(textArea.getText(), "")) {
                                        textArea.setText(token);
                                    } else {
                                        textArea.append(token);
                                    }
                                }),
                                // onError 处理错误
                                error -> ApplicationManager.getApplication().invokeLater(() -> {
                                    IdeaDialogUtil.showError(project, "Error generating review message: <br>" + getErrorMessage(error.getMessage()), "Error");
                                }));
                    } catch (IllegalArgumentException ex) {
                        IdeaDialogUtil.showWarning(project, ex.getMessage(), "AI review Message Warning");
                    } catch (Exception ex) {
                        IdeaDialogUtil.showError(project, "Error generating review message: <br>" + getErrorMessage(ex.getMessage()), "Error");
                    }
                }
            });
        });
    }

    private static @NotNull String getErrorMessage(String errorMessage) {
        if (errorMessage.contains("429")) {
            errorMessage = "Too many requests. Please try again later.";
        } else if (errorMessage.contains("Read timeout") || errorMessage.contains("Timeout") || errorMessage.contains("timed out")) {
            errorMessage = "Read timeout. Please try again later. <br> " +
                    "This may be caused by the API key or network issues or the server is busy.";
        } else if (errorMessage.contains("400")) {
            errorMessage = "Bad Request. Please try again later.";
        } else if (errorMessage.contains("401")) {
            errorMessage = "Unauthorized. Please check your API key.";
        } else if (errorMessage.contains("403")) {
            errorMessage = "Forbidden. Please check your API key.";
        } else if (errorMessage.contains("404")) {
            errorMessage = "Not Found. Please check your API key.";
        } else if (errorMessage.contains("500")) {
            errorMessage = "Internal Server Error. Please try again later.";
        } else if (errorMessage.contains("502")) {
            errorMessage = "Bad Gateway. Please try again later.";
        } else if (errorMessage.contains("503")) {
            errorMessage = "Service Unavailable. Please try again later.";
        } else if (errorMessage.contains("504")) {
            errorMessage = "Gateway Timeout. Please try again later.";
        }
        return errorMessage;
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        // 指定在后台线程更新 Action 状态，提高性能
        return ActionUpdateThread.BGT;
    }

}