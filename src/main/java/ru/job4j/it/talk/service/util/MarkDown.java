package ru.job4j.it.talk.service.util;

import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;

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

    public String html2mdv2(String text) {
        if (text == null) {
            return null;
        }

        // 1. Заменяем HTML-теги на временные токены.
        // Bold: <b> и <strong>
        text = text.replaceAll("(?i)<(b|strong)>(.*?)</\\1>", "{BOLD_START}$2{BOLD_END}");
        // Italic: <i> и <em>
        text = text.replaceAll("(?i)<(i|em)>(.*?)</\\1>", "{ITALIC_START}$2{ITALIC_END}");
        // Underline: <u>
        text = text.replaceAll("(?i)<u>(.*?)</u>", "{UNDERLINE_START}$1{UNDERLINE_END}");
        // Strikethrough: <s> и <strike>
        text = text.replaceAll("(?i)<(s|strike)>(.*?)</\\1>", "{STRIKE_START}$2{STRIKE_END}");
        // Inline code: <code>
        text = text.replaceAll("(?i)<code>(.*?)</code>", "{CODE_START}$1{CODE_END}");
        // Preformatted block: <pre>
        text = text.replaceAll("(?i)<pre>(.*?)</pre>", "{PRE_START}$1{PRE_END}");
        // Ссылка: <a href="url">text</a>
        text = text.replaceAll("(?i)<a\\s+href\\s*=\\s*\"(.*?)\"\\s*>(.*?)</a>", "{LINK_START:$1}$2{LINK_END}");

        // Заменяем переносы строк (тег <br>)
        text = text.replaceAll("(?i)<br\\s*/?>", "\n");

        // Удаляем остальные HTML-теги, если они есть.
        text = text.replaceAll("<[^>]+>", "");

        // 2. Экранируем спецсимволы MarkdownV2 для всего текста.
        text = escapeMarkdown(text);

        // 3. Восстанавливаем временные токены в маркеры MarkdownV2.
        text = text.replace("{BOLD_START}", "*").replace("{BOLD_END}", "*");
        text = text.replace("{ITALIC_START}", "_").replace("{ITALIC_END}", "_");
        text = text.replace("{UNDERLINE_START}", "__").replace("{UNDERLINE_END}", "__");
        text = text.replace("{STRIKE_START}", "~").replace("{STRIKE_END}", "~");
        text = text.replace("{CODE_START}", "`").replace("{CODE_END}", "`");
        // Для pre-блока используем тройные обратные кавычки, добавляя переносы строк
        text = text.replace("{PRE_START}", "```\n").replace("{PRE_END}", "\n```");
        // Ссылку конвертируем в формат [text](url)
        text = text.replaceAll("\\{LINK_START:(.*?)\\}(.*?)\\{LINK_END\\}", "[$2]($1)");

        return text;
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

    public String extractTextFromHtml(String html) {
        return Jsoup.parse(html).text();  // Извлекаем только текст, без HTML-тегов
    }

    public static void main(String[] args) {
        var text = "<p>что напечатает код?<\\/p>\\n<pre><code class=\\\"language-java\\\">public static void main(String[] args) {\\n    process(3);\\n}\\n\\nprivate static void process(int value) {\\n    double half = value / 2;\\n    System.out.println(half);\\n}\\n<\\/code><\\/pre>\\n\",\"id\":2,\"position\":2,\"title\":\"Округление целочисленного деления при преобразовании в double\",\"explanation\":\"<p>Причина, по которой код напечатает <strong>1.0<\\/strong>, связана с особенностями деления целых чисел в Java.<\\/p>\\n<p>В строке:<\\/p>\\n<pre><code class=\\\"language-java\\\">double half = value / 2;\\n<\\/code><\\/pre>\\n<p>значение <tt>value<\\/tt> имеет тип <tt>int<\\/tt>, а число <tt>2<\\/tt> также является целым числом. В Java, когда два целых числа делятся друг на друга, результатом будет <strong>целочисленное деление<\\/strong>, то есть дробная часть отбрасывается.<\\/p>\\n<p>В данном случае, когда <tt>value<\\/tt> равно 3, операция <tt>3 / 2<\\/tt> выполняется как целочисленное деление, давая результат <strong>1<\\/strong>, а не 1.5. После этого результат <strong>1<\\/strong> преобразуется в тип <tt>double<\\/tt>, поэтому переменной <tt>half<\\/tt> присваивается значение <strong>";
        var resp = new MarkDown().html2mdv2(text);
        System.out.println(resp); // Должен вывести *Тест*, так как символы имеют пару
    }
}
