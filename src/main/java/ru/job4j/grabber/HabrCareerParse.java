package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.utils.DateTimeParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HabrCareerParse implements Parse {

    private static final String SOURCE_LINK = "https://career.habr.com";
    private static final String PAGE_LINK = String.format("%s/vacancies/java_developer", SOURCE_LINK);
    private static final int COUNT = 5;
    private static DateTimeParser dateTimeParser;

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    private String retrieveDescription(String link) {
        Connection connection = Jsoup.connect(link);
        Element rows = null;
        try {
            rows = connection.get().selectFirst(".style-ugc");
        } catch (IOException e) {
            throw new IllegalArgumentException();
        }
        return rows.text();
    }

    @Override
    public List<Post> list(String link) {
        List<Post> list = new ArrayList<>();
        for (int i = 1; i <= COUNT; i++) {
            try {
                Connection connection = Jsoup.connect(link + i);
                Document document = connection.get();
                Elements rows = document.select(".vacancy-card__inner");
//                rows.forEach(row -> {
//                    list.add(getPost(row));
//                });
                rows.forEach(row -> list.add(getPost(row)));
            } catch (IOException e) {
                throw new IllegalArgumentException();
            }
        }
        return list;
    }

    private Post getPost(Element row) {
        Element titleElement = row.select(".vacancy-card__title").first();
        Element linkElement = titleElement.child(0);
        String date = row.selectFirst(".basic-date").attr("datetime");
        String vacancyName = titleElement.text();
        String link = linkElement.attr("abs:href");
        return new Post(
                vacancyName,
                link,
                retrieveDescription(link),
                dateTimeParser.parse(date));
    }
}

//    public static void main(String[] args) throws IOException {
//        for (int i = 1; i <= 5; i++) {
////            Connection connection = Jsoup.connect(PAGE_LINK);
//            Connection connection = Jsoup.connect(PAGE_LINK + "?page=" + i);
//            Document document = connection.get();
//            Elements rows = document.select(".vacancy-card__inner");
//            rows.forEach(row -> {
//                Element titleElement = row.select(".vacancy-card__title").first();
//                Element linkElement = titleElement.child(0);
//                String date = row.selectFirst(".basic-date").attr("datetime");
//                String vacancyName = titleElement.text();
//                String link = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
//                System.out.printf("%s %s %s%n", vacancyName, link, date);
//            });
//        }
//    }

