package org.ytcuber.parser;

import jakarta.annotation.PostConstruct;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Component
public class InitializationReplacement {

    @PostConstruct
    public void init(){
        try {
            // Указываем URL-адрес веб-страницы
            Document doc = Jsoup.connect("https://newlms.magtu.ru/mod/folder/view.php?id=1223702").get();

            // Извлекаем все элементы с определенным селектором
            Elements elements = doc.select("a");

            boolean startPrinting = false;
            // Перебираем все извлеченные элементы и выводим их на экран
            for (Element element : elements) {
                String title = element.text(); // Название файла
                String link = element.absUrl("href"); // Ссылка на скачивание файла

                if (link.equals("https://newlms.magtu.ru/pluginfile.php/1936755/mod_folder/content/0/10-30-259_%20%D0%BF%D1%80%D0%B8%D0%BA%D0%B0%D0%B7%20%D0%BD%D0%B0%201%20%D0%B8%209%20%D0%BC%D0%B0%D1%8F.pdf?forcedownload=1")) {
                    startPrinting = true;
                }

                if (title.equals("◄ ОТДЕЛЕНИЕ № 4 «Образовательно-производственный центр»")) {
                    break;
                }

                if (startPrinting && !title.endsWith(".pdf")) {
                    System.out.println(new String(title.getBytes(StandardCharsets.UTF_8)));
                    // System.out.println("Ссылка: " + link);

                    // Скачиваем файл
                    try (InputStream in = new URL(link).openStream()) {
                        Files.copy(in, Path.of("C:/My_Space/More/Java/parser/mainexcel/replacement/" + title), StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
