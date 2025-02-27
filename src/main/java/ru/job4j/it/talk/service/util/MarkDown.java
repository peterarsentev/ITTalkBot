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

    public String correctPairSymbols(String markdown) {
        markdown = escapeMarkdownV2(
                convertHeadersToTelegramFormat(markdown)
        );
        List<Character> specialSymbols = new ArrayList<>();
        var result = new StringBuilder();
        for (char c : markdown.toCharArray()) {
            if (c == '*' || c == '_') {
                specialSymbols.add(c);
            }
        }
        for (char c : markdown.toCharArray()) {
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

    public String html2md(String html) {
        return FlexmarkHtmlConverter.builder(new MutableDataSet()).build().convert(html);
    }

    public String convertHeadersToTelegramFormat(String markdown) {
        String[] lines = markdown.split("\n");
        StringBuilder converted = new StringBuilder();
        for (String line : lines) {
            if (line.trim().matches("^#{1,6}\\s+.*")) {
                String headerText = line.trim().replaceFirst("^#{1,6}\\s+", "");
                converted.append("*").append(headerText).append("*");
            } else {
                converted.append(line);
            }
            converted.append("\n");
        }
        return converted.toString();
    }
}
