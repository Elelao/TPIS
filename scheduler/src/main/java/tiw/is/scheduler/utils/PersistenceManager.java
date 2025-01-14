package tiw.is.scheduler.utils;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.HashMap;
import java.util.Map;

/**
 * Permet de créer des entity manager par programmation.
 * C'est une alternative à la création via un fichier de configuration classique.
 * À noter qu'on a quand même besoin de créer un fichier de configuration, même s'il
 * ne contiendra qu'une persistence unit vide, juste avec le bon nom.
 */
public class PersistenceManager {
    private String entityManagerName = "pu-scheduler";
    private String dbHost = System.getenv().getOrDefault("DB_HOST", "localhost");
    private String dbName = System.getenv().getOrDefault("DB_NAME", "scheduler-db");
    private String dbUrl = null;
    private String dbUser = System.getenv()
            .getOrDefault("DB_USER", System.getenv().getOrDefault("POSTGRES_USER", "scheduler"));
    private String dbPassword = System.getenv()
            .getOrDefault("DB_PASSWORD", System.getenv()
                    .getOrDefault("POSTGRES_PASSWORD", System.getenv().getOrDefault("DB_NAME", "scheduler-mdp")));
    private String ddlStrategy = "update";

    public void setDbHost(String dbHost) {
        this.dbHost = dbHost;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public void setDbPassword(String dbPassword) {
        this.dbPassword = dbPassword;
    }

    public void setDbUrl(String dbUrl) {
        this.dbUrl = dbUrl;
    }

    public void setDbUser(String dbUser) {
        this.dbUser = dbUser;
    }

    public void setEntityManagerName(String entityManagerName) {
        this.entityManagerName = entityManagerName;
    }

    public void setDdlStrategy(String ddlStrategy) {
        this.ddlStrategy = ddlStrategy;
    }

    public EntityManagerFactory createEntityManagerFactory() {
        Map<String, Object> config = new HashMap<>();
        String computedDbUrl = dbUrl == null ? System.getenv()
                .getOrDefault("DB_URL", "jdbc:postgresql://" + dbHost + "/" + dbName) : dbUrl;
        config.put("jakarta.persistence.jdbc.url", computedDbUrl);
        config.put("jakarta.persistence.jdbc.user", dbUser);
        config.put("jakarta.persistence.jdbc.password", dbPassword);
        config.put("hibernate.hbm2ddl.auto", ddlStrategy);
        config.put("hibernate.current_session_context_class", "thread");
        return Persistence.createEntityManagerFactory(entityManagerName, config);
    }

/*
    public static EntityManagerFactory createDefaultEntityManagerFactory() {
        return new PersistenceManager().createEntityManagerFactory();
    }
*/
}
