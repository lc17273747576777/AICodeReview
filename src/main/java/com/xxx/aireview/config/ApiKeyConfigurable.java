package com.xxx.aireview.config;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.table.DefaultTableModel;

import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xxx.aireview.constant.Constants;
import com.xxx.aireview.pojo.PromptInfo;
import com.intellij.openapi.options.Configurable;

public class ApiKeyConfigurable implements Configurable {

    public static final Logger log = LoggerFactory.getLogger(ApiKeyConfigurable.class);
    public ApiKeyConfigurableUI ui;
    public final ApiKeySettings settings = ApiKeySettings.getInstance();

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "AI Code Review";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        ui = new ApiKeyConfigurableUI();
        loadSettings();
        return ui.getMainPanel();
    }

    @Override
    public boolean isModified() {
        return true;
    }

    @Override
    public void apply() {
        if (ui == null) {
            return;
        }
        // 保存当前设置到临时变量
        String selectedClient = (String) ui.getClientComboBox().getSelectedItem();
        String selectedModule = (String) ui.getModuleComboBox().getSelectedItem();
        String commitLanguage = (String) ui.getLanguageComboBox().getSelectedItem();
        String codeType = (String) ui.getCodeTypesComboBox().getSelectedItem();
        // 应用设置
        settings.setSelectedClient(selectedClient);
        settings.setSelectedModule(selectedModule);
        settings.setCommitLanguage(commitLanguage);
        settings.setCodeType(codeType);

        // 保存prompt内容
        Object selectedPromptType = ui.getPromptTypeComboBox().getSelectedItem();
        if (Constants.CUSTOM_PROMPT.equals((String) selectedPromptType)) {
            saveCustomPromptsAndChoosedPrompt();
        }
        // 保存prompt类型
        settings.setPromptType((String) selectedPromptType);
    }

    @Override
    public void reset() {
        loadSettings();
    }

    @Override
    public void disposeUIResources() {
        ui = null;
    }

    public void loadSettings() {
        if (ui != null) {
            ui.getClientComboBox().setSelectedItem(settings.getSelectedClient());
            ui.getModuleComboBox().setSelectedItem(settings.getSelectedModule());
            ui.getLanguageComboBox().setSelectedItem(settings.getCommitLanguage());
            // 设置表格数据
            loadCustomPrompts();
            // 设置下拉框选中项
            loadChoosedPrompt();
            // 设置提示类型
            ui.getPromptTypeComboBox().setSelectedItem(settings.getPromptType());
        }
    }

    public void loadCustomPrompts() {
        DefaultTableModel model = (DefaultTableModel) ui.getCustomPromptsTable().getModel();
        model.setRowCount(0);
        for (PromptInfo prompt : settings.getCustomPrompts()) {
            if (prompt != null) {
                model.addRow(new String[] { prompt.getDescription(), prompt.getPrompt() });
            }
        }
    }

    public void loadChoosedPrompt() {
        if (settings.getCustomPrompt() != null) {
            DefaultTableModel model = (DefaultTableModel) ui.getCustomPromptsTable().getModel();
            int rowCount = model.getRowCount();
            for (int i = 0; i < rowCount; i++) {
                String description = (String) model.getValueAt(i, 0);
                String prompt = (String) model.getValueAt(i, 1);
                if (settings.getCustomPrompt().getDescription().equals(description)
                        && settings.getCustomPrompt().getPrompt().equals(prompt)) {
                    ui.getCustomPromptsTable().setRowSelectionInterval(i, i);
                }
            }
        }
    }

    public void saveCustomPromptsAndChoosedPrompt() {
        DefaultTableModel model = (DefaultTableModel) ui.getCustomPromptsTable().getModel();
        int rowCount = model.getRowCount();
        int selectedRow = ui.getSELECTED_ROW();
        List<PromptInfo> customPrompts = new ArrayList<>();
        for (int i = 0; i < rowCount; i++) {
            String description = (String) model.getValueAt(i, 0);
            String prompt = (String) model.getValueAt(i, 1);
            PromptInfo promptInfo = new PromptInfo(description, prompt);
            customPrompts.add(i, promptInfo);

            // 处理选中的行数据作为新的prompt
            if (selectedRow == i) {
                settings.setCustomPrompt(promptInfo);
            }
        }
        settings.setCustomPrompts(customPrompts);
    }
}