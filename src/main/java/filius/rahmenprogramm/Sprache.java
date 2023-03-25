package filius.rahmenprogramm;

import java.util.Locale;

/**
 * Representative für alle Sprachen, die das Programm unterstützt.
 */
public enum Sprache {

	FRANCAIS("Français", Locale.FRANCE),
	DEUTSCH("Deutsch", Locale.GERMAN),
	ENGLISH("English", Locale.UK);

	private final String sprache;
	private final Locale locale;

	Sprache(String sprache, Locale locale) {
		this.sprache = sprache;
		this.locale = locale;
	}

	public String getSpracheAsString() {
		return sprache;
	}

	public Locale getLocale() {
		return locale;
	}

	public static Sprache getSprache(String sprache) {
		for (Sprache s : Sprache.values()) {
			if (s.getSpracheAsString().equals(sprache)) {
				return s;
			}
		}
		return null;
	}
}
