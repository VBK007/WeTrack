package nr.king.familytracker.provisioning;

import nr.king.familytracker.jdbc.JdbcTemplateProvider;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserProvisioning {

    @Autowired
    private JdbcTemplateProvider jdbcTemplateProvider;

    public void createSchema(int userId) {
        Flyway flyway = Flyway.configure()
                .dataSource(jdbcTemplateProvider.getTemplate().getDataSource())
                .locations("classpath:db/migration")
                .schemas(String.format("we_track_%s", userId))
                .load();
        flyway.migrate();
    }
}
