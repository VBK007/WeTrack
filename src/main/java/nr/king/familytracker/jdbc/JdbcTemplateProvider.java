package nr.king.familytracker.jdbc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class JdbcTemplateProvider {

    @Autowired
    private JdbcTemplate template;

    public JdbcTemplate getTemplate() {
        return template;
    }

}
