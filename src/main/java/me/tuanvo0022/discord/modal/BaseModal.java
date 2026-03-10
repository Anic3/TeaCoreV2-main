package me.tuanvo0022.discord.modal;

import me.tuanvo0022.Main;
import me.tuanvo0022.managers.ConfigManager;

import net.dv8tion.jda.api.modals.Modal;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.ModalTopLevelComponent;

import org.bukkit.configuration.ConfigurationSection;

import java.util.List;
import java.util.ArrayList;

public abstract class BaseModal {
    protected final String path;

    public BaseModal(String path) {
        this.path = path;
    }

    protected Modal build(Main plugin, String modalId) {
        ConfigManager config = plugin.getConfigManager();

        String title = config.getString(path, "title");
        
        ConfigurationSection inputs = config.getConfigurationSection(path, "inputs");
        if (inputs == null) {
            throw new IllegalStateException("Modal config missing 'inputs' section: " + path);
        }
        
        List<ModalTopLevelComponent> rows = new ArrayList<>();
        
        for (String key : inputs.getKeys(false)) {
            ConfigurationSection sec = inputs.getConfigurationSection(key);
            if (sec == null) continue;
            
            String id = sec.getString("id");
            String label = sec.getString("label");
            String placeholder = sec.getString("placeholder");

            int min = sec.getInt("min-length", 1);
            int max = sec.getInt("max-length", 256);
            boolean required = sec.getBoolean("required", true);

            TextInputStyle style = TextInputStyle.valueOf(
                sec.getString("style", "PARAGRAPH").toUpperCase()
            );

            TextInput input = TextInput.create(id, style)
                .setPlaceholder(placeholder)
                .setMinLength(min)
                .setMaxLength(max)
                .setRequired(required)
                .build();
                
            rows.add(Label.of(label, input));
        }
        
        return Modal.create(modalId, title)
            .addComponents(rows)
            .build();
    }
}