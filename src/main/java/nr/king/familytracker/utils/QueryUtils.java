package nr.king.familytracker.utils;

import com.gofrugal.location_tracking.jdbc.JdbcTemplateProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class QueryUtils {

    @Autowired
    private JdbcTemplateProvider jdbcTemplateProvider;

    private static final Logger logger = LogManager.getLogger(QueryUtils.class);

    public String camelCaseConvertor(String text) {
        StringBuilder stringBuilder = new StringBuilder();
        boolean specialCharcter = false;
        boolean like = false;
        Pattern pattern = Pattern.compile("[a-zA-Z0-9]*");
        for (char character : text.toCharArray()) {
            if (!pattern.matcher(Character.toString(character)).matches() && !specialCharcter) {
                specialCharcter = true;
                if (":".equalsIgnoreCase(Character.toString(character))) {
                    like = true;
                    stringBuilder.append(character).append(":text ilike '%");
                } else
                    stringBuilder.append(character).append(" '");
                continue;
            }
            if (Character.isUpperCase(character) && !specialCharcter) {
                stringBuilder.append("_").append(character);
            } else {
                stringBuilder.append(character);
            }
        }
        if (like) {
            stringBuilder.append("%'");
        } else {
            stringBuilder.append("'");
        }
        return stringBuilder.toString();
    }

    public String convertCamelCase(String text) {
        StringBuilder stringBuilder = new StringBuilder();
        for (char character : text.toCharArray()) {
            if (Character.isUpperCase(character)) {
                stringBuilder.append("_").append(character);
            } else {
                stringBuilder.append(character);
            }
        }
        return stringBuilder.toString();
    }

    public String getWhereClause(String filter, String sort,  int pageIndex, int pageSize) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getConditions(filter));
        int offset = (pageSize * (pageIndex - 1));
        if (!sort.equalsIgnoreCase("")) {
            List<String> sortList = Arrays.asList(sort.split("[, ]+"));
            stringBuilder.append(" order by ");
            int isFirt = 0;
            for (String s : sortList) {
                if (!s.equalsIgnoreCase("asc") && !s.equalsIgnoreCase("desc")) {
                    if (isFirt == 1) stringBuilder.append(",");
                    isFirt = 1;
                    stringBuilder.append(convertCamelCase(s));
                } else {
                    stringBuilder.append(" ").append(s);
                }
            }
        }
        stringBuilder.append(String.format(" limit %s offset %s", pageSize, offset ));
        return stringBuilder.toString();
    }

    private String getConditions(String filter) {
        StringBuilder stringBuilder = new StringBuilder();
        boolean isFirst = true;
        if (!filter.equalsIgnoreCase("")) {
            stringBuilder.append(" where ");
            stringBuilder.append("( ");
            List<String> filters = Arrays.asList(filter.split(","));
            for (String s : filters) {
                if (s.contains("|")) {
                    List<String> orConditions = Arrays.asList(s.split("\\|"));
                    for (String s1 : orConditions) {
                        if (isFirst) isFirst = false;
                        else stringBuilder.append(" or ");
                        stringBuilder.append(camelCaseConvertor(s1));
                    }
                } else {
                    if (isFirst) isFirst = false;
                    else stringBuilder.append(" and ");
                    stringBuilder.append(camelCaseConvertor(s));
                }
            }
            stringBuilder.append(" )");
        }
        return stringBuilder.toString();
    }

    public String getCountWhere(String filter) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getConditions(filter));
        return stringBuilder.toString();
    }

    public String getPagination(int pageIndex, int pageSize) {
        StringBuilder stringBuilder = new StringBuilder();
        int offset = (pageSize * (pageIndex -1)) - 1;
        stringBuilder.append(String.format(" limit %s offset %s", pageSize, offset < 0 ? 0 : offset));
        return stringBuilder.toString();
    }

    public Map<String, Object> setPagination(String query, Map<String, Object> reponse, int pageIndex, int pageSize) {
        try {
            Integer count = jdbcTemplateProvider.getTemplate().queryForObject(query, Integer.class);
            count = count != null ? count : 0;
            reponse.put("count", count <= (pageSize * pageIndex) ? (count - (pageSize * (pageIndex - 1))) < 0 ? 0 : (count - (pageSize * (pageIndex - 1))) : pageSize);
            reponse.put("totalRecords", count);
            reponse.put("currentPage", pageIndex);
            reponse.put("perPage", pageSize);
            reponse.put("totalPages", count / pageSize + 1);
            return reponse;
        } catch (Exception e) {
            logger.error("Exception while setting pagination due to - " + e.getMessage(), e);
        }
        return reponse;
    }
}
