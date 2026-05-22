/*
 * Concepts: Encapsulation, Swing UI Styling
 */
package ui;

import java.awt.Color;
import java.awt.Font;

public class UITheme {
    // BookMyShow-like palette (approx)
    public static final Color NAVY = new Color(0x333545);
    public static final Color INDIGO = new Color(0xF84464);
    public static final Color INDIGO_DARK = new Color(0xE23150);
    public static final Color BG = new Color(0xF2F2F2);
    public static final Color CARD = new Color(0xFFFFFF);
    public static final Color MUTED = new Color(0x6B7280);
    public static final Color BORDER = new Color(0xE2E2E2);
    public static final Color SUCCESS = new Color(0x16A34A);
    public static final Color WARNING = new Color(0xF59E0B);
    public static final Color GOLD = new Color(0xF5C542);

    public static final Font TITLE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font SUBTITLE = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font MONO = new Font("Consolas", Font.PLAIN, 12);

    private UITheme() {}
}
