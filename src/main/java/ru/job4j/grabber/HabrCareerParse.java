package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.utils.DateTimeParser;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HabrCareerParse implements Parse {

    private static final String SOURCE_LINK = "https://career.habr.com";
    private static final String PAGE_LINK = String.format("%s/vacancies/java_developer?page=", SOURCE_LINK);
    private static int page = 5;
    private int id = 0;

    private final DateTimeParser dateTimeParser;

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    private static String retrieveDescription(String link) {
        Document document = null;
        try {
            document = Jsoup.connect(link).get();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return document.select(".vacancy-description__text").text();
    }

    public List<Post> createPost() throws IOException {
        List<Post> postList = new ArrayList<>();
        for (int i = 1; i <= page; i++) {
            postList.addAll(list(PAGE_LINK + i));
        }
        return postList;
    }

    @Override
    public List<Post> list(String link) throws IOException {
        List<Post> postList = new ArrayList<>();
        Connection connection = Jsoup.connect(link);
        Document document = connection.get();
        Elements rows = document.select(".vacancy-card__inner");
        rows.forEach(row -> {
            Element titleElement = row.select(".vacancy-card__title").first();
            Element linkElement = titleElement.child(0);
            String vacancyName = titleElement.text();
            String dataTimeOfVacancy = row.select(".vacancy-card__date time").first().attr("datetime");
            String linkElementAsString = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
            String description = retrieveDescription(linkElementAsString);
            Post post = new Post(id++, vacancyName, linkElementAsString, description, dateTimeParser.parse(dataTimeOfVacancy));
            postList.add(post);
        });
        return postList;
    }

    public static void main(String[] args) throws IOException {
        HabrCareerParse habrCareerParse = new HabrCareerParse(new HabrCareerDateTimeParser());
        List<Post> postList = habrCareerParse.createPost();
        postList.forEach(System.out::println);
    }
}