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
	private static final String SECTION_LIMITS = "limits";
	
	private static final String MESSAGE_ENABLED = "enabled_sneaking";
	private static final String MESSAGE_DISABLED = "disabled_sneaking";
	private static final String MESSAGE_COOLDOWN = "cooldown";
	private static final String MESSAGE_COOLDOWN_EXPIRED = "cooldown_expired";
	
	private static final String COOLDOWN_KEY = "cooldown";
	private static final String DURATION_KEY = "duration";
	
	// Default messages
	private static final String DEFAULT_MESSAGE_ENABLED = "Enabled automatic sneaking for %s";
	private static final String DEFAULT_MESSAGE_DISABLED = "Disabled automatic sneaking for %s";
	private static final String DEFAULT_MESSAGE_COOLDOWN = "Sneaking is disabled for another %s seconds";
	private static final String DEFAULT_MESSAGE_COOLDOWN_EXPIRED = "Automatic sneaking can now be activated.";
	
	// These are disabled by default
	private static final double DEFAULT_COOLDOWN = 0;
	private static final double DEFAULT_DURATION = 0;
	
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
	
	public String getCooldownMessageFormat() {
		return getSectionOrDefault(SECTION_MESSAGES).getString(MESSAGE_COOLDOWN, DEFAULT_MESSAGE_COOLDOWN);
	}
	
	public void setCooldownMessageFormat(String value) {
		getSectionOrDefault(SECTION_MESSAGES).set(MESSAGE_COOLDOWN, value);
	}
	
	public String getCooldownExpiredMessage() {
		return getSectionOrDefault(SECTION_MESSAGES).getString(MESSAGE_COOLDOWN_EXPIRED, DEFAULT_MESSAGE_COOLDOWN_EXPIRED);
	}
	
	public void setCooldownExpiredMessage(String value) {
		getSectionOrDefault(SECTION_MESSAGES).set(MESSAGE_COOLDOWN_EXPIRED, value);
	}
	
	/**
	 * Retrieve the cooldown in fractional seconds.
	 * @return Cooldown in fractional seconds.
	 */
	public double getCooldown() {
		Object value = getSectionOrDefault(SECTION_LIMITS).get(COOLDOWN_KEY);
		
		if (value == null)
			return DEFAULT_COOLDOWN;
		else
			return ((Number) value).doubleValue();
	}
	
	/**
	 * Set the cooldown in fractional seconds.
	 * @param value - new cooldown.
	 */
	public void setCooldown(double value) {
		getSectionOrDefault(SECTION_LIMITS).set(COOLDOWN_KEY, value);
	}
	
	/**
	 * Retrieve the duration in fractional seconds.
	 * @return Duration in fractional seconds.
	 */
	public double getDuration() {
		Object value = getSectionOrDefault(SECTION_LIMITS).get(DURATION_KEY);
		
		if (value == null)
			return DEFAULT_DURATION;
		else
			return ((Number) value).doubleValue();
	}
	
	/**
	 * Set the duration in fractional seconds.
	 * @param value - new duration.
	 */
	public void setDuration(double value) {
		getSectionOrDefault(SECTION_LIMITS).set(DURATION_KEY, value);
	}
	
	public String getFormattedMessage(boolean sneaking, String playerName) {
		return String.format(sneaking ? getEnabledMessageFormat() : getDisabledMessageFormat(), playerName);
	}
	
	public String getFormattedMessage(double cooldown) {
		return String.format(getCooldownMessageFormat(), cooldown);
	}
	
	private ConfigurationSection getSectionOrDefault(String name) {
		ConfigurationSection section = config.getConfigurationSection(name);
		
		if (section != null)
			return section;
		else
			return config.createSection(name);
	}
}
