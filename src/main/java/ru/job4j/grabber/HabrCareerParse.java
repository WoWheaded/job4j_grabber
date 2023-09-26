package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.utils.DateTimeParser;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

public class HabrCareerParse {

    private static final String SOURCE_LINK = "https://career.habr.com";
    private static final String PAGE_LINK = String.format("%s/vacancies/java_developer?page=", SOURCE_LINK);

    public static void main(String[] args) throws IOException {
        for (int i = 1; i <= 5; i++) {
            Connection connection = Jsoup.connect(PAGE_LINK + i);
            System.out.println(i + " СТРАНИЦА");
            getJobs(connection);
        }
    }

    private static void getJobs(Connection connection) throws IOException {
        Document document = connection.get();
        Elements rows = document.select(".vacancy-card__inner");
        DateTimeParser dateTimeParser = new HabrCareerDateTimeParser();
        rows.forEach(row -> {
            Element titleElement = row.select(".vacancy-card__title").first();
            Element linkElement = titleElement.child(0);
            String vacancyName = titleElement.text();
            String dataTimeOfVacancy = row.select(".vacancy-card__date time").first().attr("datetime");
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-mm-ddТhh:mm:ss");
            dataTimeOfVacancy = dateTimeParser.parse(dataTimeOfVacancy).format(dateTimeFormatter);
            String link = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
            System.out.printf("%s -- %s %s%n", dataTimeOfVacancy, vacancyName, link);
        });
    }
}