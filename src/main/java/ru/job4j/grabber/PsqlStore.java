package ru.job4j.grabber;

import java.io.InputStream;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PsqlStore implements Store, AutoCloseable {
    private Connection cnn;

    public PsqlStore(Properties cfg) {
        try {
            Class.forName(cfg.getProperty("jdbc.driver"));
            cnn = DriverManager.getConnection(
                    cfg.getProperty("jdbc.url"),
                    cfg.getProperty("jdbc.username"),
                    cfg.getProperty("jdbc.password")
            );
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void save(Post post) {
        try (PreparedStatement p = cnn.prepareStatement("insert into post(name,text,link,created) "
                + "values(?,?,?,?) ON CONFLICT (link) DO NOTHING", Statement.RETURN_GENERATED_KEYS)) {
            p.setString(1, post.getTitle());
            p.setString(2, post.getDescription());
            p.setString(3, post.getLink());
            p.setTimestamp(4, Timestamp.valueOf(post.getCreated()));
            p.execute();
            try (ResultSet generatedKeys = p.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    post.setId(generatedKeys.getInt("id"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Post> getAll() {
        List<Post> posts = new ArrayList<>();
        try (PreparedStatement p = cnn.prepareStatement("select * from post")) {
            try (ResultSet resultSet = p.executeQuery()) {
                while (resultSet.next()) {
                    posts.add(generatePost(resultSet));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return posts;
    }

    @Override
    public Post findById(int id) {
        Post post = null;
        try (PreparedStatement p = cnn.prepareStatement("select * from post where id = ?")) {
            p.setInt(1, id);
            try (ResultSet resultSet = p.executeQuery()) {
                if (resultSet.next()) {
                    post = generatePost(resultSet);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return post;
    }

    private Post generatePost(ResultSet resultSet) throws SQLException {
        return new Post(
                resultSet.getInt("id"),
                resultSet.getString("name"),
                resultSet.getString("text"),
                resultSet.getString("link"),
                resultSet.getTimestamp("created").toLocalDateTime()
        );
    }

    @Override
    public void close() throws Exception {
        if (cnn != null) {
            cnn.close();
        }
    }

    public static void main(String[] args) {
        try (InputStream in = PsqlStore.class.getClassLoader().getResourceAsStream("rabbit.properties")) {
            Properties config = new Properties();
            config.load(in);
            try (PsqlStore psq = new PsqlStore(config)) {
                psq.save(new Post("title", "link-5", "description", LocalDateTime.of(2023, 2, 12, 14, 54)));
                System.out.println(psq.getAll());
                System.out.println(psq.findById(1));
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}