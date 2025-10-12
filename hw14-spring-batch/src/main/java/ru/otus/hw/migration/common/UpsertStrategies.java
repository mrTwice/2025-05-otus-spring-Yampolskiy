package ru.otus.hw.migration.common;

import lombok.experimental.UtilityClass;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.Normalizer;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

@UtilityClass
public class UpsertStrategies {


    public static <T> T upsertMongo(MongoTemplate template, Query query, Update update, Class<T> clazz) {
        return template.findAndModify(query, update,
                FindAndModifyOptions.options().upsert(true).returnNew(true), clazz);
    }

    public static <T> T upsertMongo(MongoTemplate template, Query query, Update update, Class<T> clazz,
                                    Consumer<T> afterSave) {
        T saved = upsertMongo(template, query, update, clazz);
        if (afterSave != null && saved != null) {
            afterSave.accept(saved);
        }
        return saved;
    }

    public static <T> T upsertJpa(Supplier<Optional<T>> finder,
                                  Supplier<T> creator,
                                  Consumer<T> updater,
                                  Consumer<T> saver) {
        T entity = finder.get().orElseGet(creator);
        updater.accept(entity);
        saver.accept(entity);
        return entity;
    }

    public static <T, I> T upsertJpa(EntityManager em,
                                      Supplier<Optional<I>> pkFinder,
                                      Supplier<T> creator,
                                      Consumer<T> updater,
                                      Class<T> entityClass) {
        T entity = pkFinder.get()
                .map(id -> em.find(entityClass, id, LockModeType.NONE))
                .orElseGet(creator);
        updater.accept(entity);
        if (em.contains(entity) || getId(entity) != null) {
            entity = em.merge(entity);
        } else {
            em.persist(entity);
        }
        return entity;
    }

    @SuppressWarnings("unchecked")
    private static Object getId(Object entity) {
        try {
            return entity.getClass().getMethod("getId").invoke(entity);
        } catch (Exception e) {
            return null;
        }
    }

    public static String norm(String s) {
        if (s == null) {
            return "";
        }
        String n = Normalizer.normalize(s, Normalizer.Form.NFKC);
        n = n.trim().replaceAll("\\s+", " ");
        return n.toLowerCase(Locale.ROOT);
    }

    public static String bookKey(String authorFullName, String title) {
        return norm(authorFullName) + "||" + norm(title);
    }

    public static String sha256Hex(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest((text == null ? "" : text).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return norm(text);
        }
    }
}
