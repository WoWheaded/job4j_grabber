package ru.job4j.grabber;

import ru.job4j.quartz.AlertRabbit;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PsqlStore implements Store {

    private Connection cnn;

    public PsqlStore(Properties cfg) throws SQLException, IOException {
        try (InputStream in = AlertRabbit.class.getClassLoader().getResourceAsStream("grabber.properties")) {
            cfg.load(in);
        }
        try {
            Class.forName(cfg.getProperty("jdbc.driver"));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        cnn = DriverManager.getConnection(
                cfg.getProperty("jdbc.url"),
                cfg.getProperty("jdbc.username"),
                cfg.getProperty("jdbc.password")
        );
    }

    @Override
    public void save(Post post) {
        try (PreparedStatement statement =
                     cnn.prepareStatement("INSERT INTO post(name, text, link, created) VALUES(?, ?, ?, ?) "
                             + "ON CONFLICT (link) DO NOTHING", Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, post.getTitle());
            statement.setString(2, post.getDescription());
            statement.setString(3, post.getLink());
            statement.setTimestamp(4, Timestamp.valueOf(post.getCreated()));
            statement.execute();
            try (ResultSet generatedId = statement.getGeneratedKeys()) {
                if (generatedId.next()) {
                    post.setId(generatedId.getInt(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Post> getAll() {
        List<Post> result = new ArrayList<>();
        try (PreparedStatement statement =
                     cnn.prepareStatement("SELECT * FROM post")) {
            statement.execute();
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Post post = createPostFromResultSet(resultSet);
                    result.add(post);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public Post findById(int id) {
        Post post = null;
        try (PreparedStatement statement = cnn.prepareStatement("SELECT * FROM post WHERE id = ?")) {
            statement.setInt(1, id);
            statement.execute();
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    post = createPostFromResultSet(resultSet);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return post;
    }

    @Override
    public void close() throws SQLException {
        if (cnn != null) {
            cnn.close();
        }
    }

    private Post createPostFromResultSet(ResultSet resultSet) throws SQLException {
        int id = resultSet.getInt(1);
        String name = resultSet.getString("name");
        String text = resultSet.getString("text");
        String link = resultSet.getString("link");
        LocalDateTime created = resultSet.getTimestamp("created").toLocalDateTime();
        return new Post(id, name, text, link, created);
    }

    public static void main(String[] args) throws SQLException, IOException {
        Properties properties = new Properties();
        try (PsqlStore psqlStore = new PsqlStore(properties)) {
            psqlStore.save(new Post("1 tittle", "1 link", "1 descc", LocalDateTime.now()));
            psqlStore.save(new Post("2 tittle", "2 link", "2 descc", LocalDateTime.now()));
            psqlStore.save(new Post("3 tittle", "3 link", "3 descc", LocalDateTime.now()));
            List<Post> list = psqlStore.getAll();
            list.forEach(System.out::println);
            System.out.println(psqlStore.findById(1));
        }

    }
}
