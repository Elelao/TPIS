package tiw.is.scheduler.utils;

/** Uses an in-memory H2 Database for tests */
public class TestPersistenceManager extends PersistenceManager {
    public TestPersistenceManager() {
        setDbUrl("jdbc:h2:mem:test");
        setDdlStrategy("create");
    }
}
