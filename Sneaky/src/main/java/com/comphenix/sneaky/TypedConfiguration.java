package com.comphenix.sneaky;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Access the configuration in a strongly typed fasion.
 * 
 * @author Kristian
 */
public class TypedConfiguration {
	private final Configuration config;
	
	private static final String SECTION_LIST = "list";
	private static final String SECTION_MESSAGES = "messages";
	
	private static final String MESSAGE_ENABLED = "enabled_sneaking";
	private static final String MESSAGE_DISABLED = "disabled_sneaking";

	// Default messages
	private static final String DEFAULT_MESSAGE_ENABLED = "Enabled automatic sneaking for %s";
	private static final String DEFAULT_MESSAGE_DISABLED = "Disabled automatic sneaking for %s";
	
	public TypedConfiguration(Configuration config) {
		this.config = config;
	}
	
	/**
	 * Determine if the configuration has been created.
	 * @return TRUE if it has, FALSE otherwise.
	 */
	public boolean isCreated() {
		return config.getConfigurationSection(SECTION_LIST) != null;
	}
	
	public AutoSneakers getSneakers() {
		AutoSneakers sneakers = (AutoSneakers) config.get(SECTION_LIST);
		
		// Return a value, regardless
		if (sneakers != null)
			return sneakers;
		else
			return new AutoSneakers();
	}
	
	public void setSneakers(AutoSneakers value) {
		config.set(SECTION_LIST, value);
	}
	
	public String getEnabledMessageFormat() {
		return getSectionOrDefault(SECTION_MESSAGES).getString(MESSAGE_ENABLED, DEFAULT_MESSAGE_ENABLED);
	}
	
	public void setEnabledMessageFormat(String value) {
		getSectionOrDefault(SECTION_MESSAGES).set(MESSAGE_ENABLED, value);
	}
	
	public String getDisabledMessageFormat() {
		return getSectionOrDefault(SECTION_MESSAGES).getString(MESSAGE_DISABLED, DEFAULT_MESSAGE_DISABLED);
	}
	
	public void setDisabledMessageFormat(String value) {
		getSectionOrDefault(SECTION_MESSAGES).set(MESSAGE_DISABLED, value);
	}
	
	public String getFormattedMessage(boolean sneaking, String playerName) {
		return String.format(sneaking ? getEnabledMessageFormat() : getDisabledMessageFormat(), playerName);
	}
	
	private ConfigurationSection getSectionOrDefault(String name) {
		ConfigurationSection section = config.getConfigurationSection(name);
		
		if (section != null)
			return section;
		else
			return config.createSection(name);
	}
}
