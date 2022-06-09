package nr.king.familytracker.migration;

import nr.king.familytracker.jdbc.JdbcTemplateProvider;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class DataBaseMigration {

    @Autowired
    private JdbcTemplateProvider jdbcTemplateProvider;

   @PostConstruct
    public void doPatchForSchema() {
        jdbcTemplateProvider.getTemplate().query("select schema_name from location_tracking_users", resultSet -> {
            Flyway flyway1 = Flyway.configure().dataSource(jdbcTemplateProvider.getTemplate().getDataSource())
                    .locations("classpath:db/migration").schemas(resultSet.getString("schema_name")).load();
            flyway1.migrate();
        });
    }
}
