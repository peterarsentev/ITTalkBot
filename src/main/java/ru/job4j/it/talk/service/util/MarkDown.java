package ru.job4j.it.talk.service.util;

import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;
import com.vladsch.flexmark.util.data.MutableDataSet;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import java.util.ArrayList;
import java.util.List;

@Service
public class MarkDown {
    public String escapeMarkdownV2(String input) {
        return input
                .replace("|", "\\|")
                .replace("=", "\\=")
                .replace("{", "\\{")
                .replace("}", "\\}")
                .replace("[", "\\[")
                .replace("]", "\\]")
                .replace("(", "\\(")
                .replace(")", "\\)")
                .replace("~", "\\~")
                .replace(">", "\\>")
                .replace("#", "\\#")
                .replace("+", "\\+")
                .replace("-", "\\-")
                .replace(".", "\\.")
                .replace("!", "\\!");
    }

    public String correctPairSymbols(String input) {
        input = escapeMarkdownV2(input);
        List<Character> specialSymbols = new ArrayList<>();
        var result = new StringBuilder();
        for (char c : input.toCharArray()) {
            if (c == '*' || c == '_') {
                specialSymbols.add(c);
            }
        }
        for (char c : input.toCharArray()) {
            boolean addChar = true;
            if (specialSymbols.contains(c)) {
                long count = specialSymbols.stream().filter(ch -> ch == c).count();
                if (count % 2 == 1) {
                    result.append("\\" + c); // экранируем последний нечетный символ
                    addChar = false;
                } else {
                    result.append(c);
                    addChar = false;
                }
            }
            if (addChar) {
                result.append(c);
            }
        }
        return result.toString();
    }

    /**
     * Экранирует спецсимволы MarkdownV2 согласно документации Telegram.
     * Символы: _ * [ ] ( ) ~ ` > # + - = | { } . !
     */
    private String escapeMarkdown(String text) {
        if (text == null) {
            return null;
        }
        // Перечисляем спецсимволы, которые необходимо экранировать.
        String[] specialChars = {"_", "*", "[", "]", "(", ")", "~", "`", ">", "#", "+", "-", "=", "|", "{", "}", ".", "!"};
        for (String ch : specialChars) {
            // Заменяем каждый спецсимвол на экранированную версию (обратный слеш + символ)
            text = text.replace(ch, "\\" + ch);
        }
        return text;
    }

    public String html2md(String html) {
        return FlexmarkHtmlConverter.builder(new MutableDataSet()).build().convert(html);
    }

    public static void main(String[] args) {
        String html = "<h1>Hello, World!</h1><p>This is a paragraph.</p>";
        var options = new MutableDataSet();
        String markdown = FlexmarkHtmlConverter.builder(options).build().convert(html);
        System.out.println(markdown);
    }
}
