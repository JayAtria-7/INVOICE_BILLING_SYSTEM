package com.yourcompany.invoicesystem.util;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Internationalization (i18n) Manager
 * Handles multi-language support for the application
 */
public class I18nManager {
    
    private static I18nManager instance;
    private ResourceBundle messages;
    private Locale currentLocale;
    
    // Supported languages
    public static final Locale ENGLISH = Locale.ENGLISH;
    public static final Locale SPANISH = new Locale("es");
    public static final Locale FRENCH = Locale.FRENCH;
    
    private I18nManager() {
        // Default to English
        setLocale(ENGLISH);
    }
    
    public static I18nManager getInstance() {
        if (instance == null) {
            synchronized (I18nManager.class) {
                if (instance == null) {
                    instance = new I18nManager();
                }
            }
        }
        return instance;
    }
    
    /**
     * Set the current locale/language
     * @param locale The locale to use
     */
    public void setLocale(Locale locale) {
        this.currentLocale = locale;
        try {
            messages = ResourceBundle.getBundle("com.yourcompany.invoicesystem.resources.messages", locale);
            Logger.info("Locale set to: " + locale.getDisplayLanguage());
        } catch (Exception e) {
            Logger.error("Error loading resource bundle for locale: " + locale, e);
            // Fallback to English
            messages = ResourceBundle.getBundle("com.yourcompany.invoicesystem.resources.messages", ENGLISH);
        }
    }
    
    /**
     * Get localized message by key
     * @param key The message key
     * @return The localized message
     */
    public String getMessage(String key) {
        try {
            return messages.getString(key);
        } catch (Exception e) {
            Logger.warn("Missing translation key: " + key);
            return "!" + key + "!";
        }
    }
    
    /**
     * Get localized message with parameters
     * @param key The message key
     * @param params Parameters to substitute
     * @return The localized message with substituted parameters
     */
    public String getMessage(String key, Object... params) {
        try {
            String message = messages.getString(key);
            return String.format(message, params);
        } catch (Exception e) {
            Logger.warn("Missing translation key or error formatting: " + key);
            return "!" + key + "!";
        }
    }
    
    /**
     * Get the current locale
     * @return Current locale
     */
    public Locale getCurrentLocale() {
        return currentLocale;
    }
    
    /**
     * Get all supported locales
     * @return Array of supported locales
     */
    public static Locale[] getSupportedLocales() {
        return new Locale[]{ENGLISH, SPANISH, FRENCH};
    }
    
    /**
     * Get locale display name
     * @param locale The locale
     * @return Display name (e.g., "English", "Español", "Français")
     */
    public static String getLocaleDisplayName(Locale locale) {
        if (locale.equals(ENGLISH)) return "English";
        if (locale.equals(SPANISH)) return "Español";
        if (locale.equals(FRENCH)) return "Français";
        return locale.getDisplayLanguage();
    }
    
    /**
     * Check if a locale is supported
     * @param locale The locale to check
     * @return true if supported
     */
    public static boolean isLocaleSupported(Locale locale) {
        for (Locale supported : getSupportedLocales()) {
            if (supported.equals(locale)) {
                return true;
            }
        }
        return false;
    }
}
