/*
 * Decompiled with CFR 0.152.
 */
package meteordevelopment.meteorclient.gui.utils;

import java.util.ArrayList;
import java.util.List;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.systems.hud.elements.TextHud;
import meteordevelopment.meteorclient.utils.misc.MeteorStarscript;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.starscript.compiler.Parser;

public class StarscriptTextBoxRenderer
implements WTextBox.Renderer {
    private static final String[] KEYWORDS = new String[]{"null", "true", "false", "and", "or"};
    private static final Color RED = new Color(225, 25, 25);
    private String lastText;
    private final List<Section> sections = new ArrayList<Section>();

    @Override
    public void render(GuiRenderer renderer, double x, double y, String text, Color color) {
        if (this.lastText == null || !this.lastText.equals(text)) {
            this.generate(renderer.theme, text, color);
        }
        for (Section section : this.sections) {
            renderer.text(section.text, x, y, section.color, false);
            x += renderer.theme.textWidth(section.text);
        }
    }

    @Override
    public List<String> getCompletions(String text, int position) {
        ArrayList<String> completions = new ArrayList<String>();
        MeteorStarscript.ss.getCompletions(text, position, (completion, function) -> completions.add((String)(function ? completion + "(" : completion)));
        completions.sort(String::compareToIgnoreCase);
        return completions;
    }

    private void generate(GuiTheme theme, String text, Color defaultColor) {
        this.lastText = text;
        this.sections.clear();
        Parser.Result result = Parser.parse(text);
        StringBuilder sb = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();
        int depth = 0;
        for (int i = 0; i < text.length(); ++i) {
            char ch;
            char c = text.charAt(i);
            boolean addChar = true;
            int charDepth = depth;
            if (result.hasErrors()) {
                if (i == result.errors.getFirst().character) {
                    this.sections.add(new Section(sb.toString(), charDepth > 0 ? theme.starscriptTextColor() : defaultColor));
                    sb.setLength(0);
                } else if (i > result.errors.getFirst().character) {
                    sb.append(c);
                    continue;
                }
            }
            Section section = null;
            switch (c) {
                case '#': {
                    while (i + 1 < text.length() && this.isDigit(ch = text.charAt(i + 1))) {
                        sb2.append(ch);
                        ++i;
                    }
                    if (sb2.isEmpty()) break;
                    String str = sb2.toString();
                    section = new Section("#" + str, TextHud.getSectionColor(Integer.parseInt(str)));
                    sb2.setLength(0);
                    break;
                }
                case '{': 
                case '}': {
                    depth = c == '{' ? ++depth : --depth;
                    section = new Section(Character.toString(c), theme.starscriptBraceColor());
                }
            }
            if (section == null && depth > 0) {
                if (c == '.') {
                    this.sections.add(new Section(sb.toString(), theme.starscriptAccessedObjectColor()));
                    this.sections.add(new Section(".", theme.starscriptDotColor()));
                    sb.setLength(0);
                    addChar = false;
                } else {
                    switch (c) {
                        case '(': 
                        case ')': {
                            section = new Section(Character.toString(c), theme.starscriptParenthesisColor());
                            break;
                        }
                        case ',': {
                            section = new Section(",", theme.starscriptCommaColor());
                            break;
                        }
                        case '%': 
                        case '*': 
                        case '+': 
                        case '-': 
                        case '/': 
                        case ':': 
                        case '?': 
                        case '^': {
                            if (c == '-' && i + 1 < text.length() && this.isDigit(text.charAt(i + 1))) break;
                            section = new Section(Character.toString(c), theme.starscriptOperatorColor());
                            break;
                        }
                        case '!': 
                        case '<': 
                        case '=': 
                        case '>': {
                            boolean equals;
                            boolean bl = equals = i + 1 < text.length() && text.charAt(i + 1) == '=';
                            if (equals) {
                                ++i;
                            }
                            section = new Section((String)(equals ? c + "=" : Character.toString(c)), theme.starscriptOperatorColor());
                            break;
                        }
                        case '\"': 
                        case '\'': {
                            sb2.append(c);
                            while (i + 1 < text.length()) {
                                ch = text.charAt(i + 1);
                                if (ch != '\"' && ch != '\'') {
                                    sb2.append(ch);
                                    ++i;
                                    continue;
                                }
                                sb2.append(ch);
                                ++i;
                                break;
                            }
                            section = new Section(sb2.toString(), theme.starscriptStringColor());
                            sb2.setLength(0);
                        }
                    }
                    if (section == null) {
                        if (this.isDigit(c) || c == '-' && i + 1 < text.length() && this.isDigit(text.charAt(i + 1))) {
                            sb2.append(c);
                            while (i + 1 < text.length() && this.isDigit(ch = text.charAt(i + 1))) {
                                sb2.append(ch);
                                ++i;
                            }
                            if (i + 1 < text.length() && text.charAt(i + 1) == '.' && i + 2 < text.length() && this.isDigit(text.charAt(i + 2))) {
                                sb2.append('.');
                                ++i;
                                while (i + 1 < text.length() && this.isDigit(ch = text.charAt(i + 1))) {
                                    sb2.append(ch);
                                    ++i;
                                }
                            }
                            section = new Section(sb2.toString(), theme.starscriptNumberColor());
                            sb2.setLength(0);
                        } else {
                            for (String keyword : KEYWORDS) {
                                if (!this.isKeyword(text, i, keyword)) continue;
                                section = new Section(keyword, theme.starscriptKeywordColor());
                                i += keyword.length() - 1;
                                break;
                            }
                        }
                    }
                }
            }
            if (section != null) {
                if (!sb.isEmpty()) {
                    this.sections.add(new Section(sb.toString(), charDepth > 0 ? theme.starscriptTextColor() : defaultColor));
                    sb.setLength(0);
                }
                this.sections.add(section);
                continue;
            }
            if (!addChar) continue;
            sb.append(c);
        }
        if (!sb.isEmpty()) {
            this.sections.add(new Section(sb.toString(), result.hasErrors() ? RED : defaultColor));
        }
    }

    private boolean isKeyword(String text, int i, String keyword) {
        char c;
        if (i > 0 && ((c = text.charAt(i - 1)) >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c == '_')) {
            return false;
        }
        for (int j = 0; j < keyword.length(); ++j) {
            if (i + j < text.length() && text.charAt(i + j) == keyword.charAt(j)) continue;
            return false;
        }
        return true;
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private record Section(String text, Color color) {
    }
}

