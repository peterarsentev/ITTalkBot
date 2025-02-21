package ru.job4j.it.talk.service.util;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MD5Corrector {
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

    public static void main(String[] args) {
        var text = "ods will help you create a reliable and appealing application for practicing spoken language skills in a foreign language.\n";
        var resp = new MD5Corrector().correctPairSymbols(text);
        System.out.println(resp); // Должен вывести *Тест*, так как символы имеют пару

    }
}
