package nr.king.familytracker.model.http.currency;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.lang.Boolean;
import java.lang.Double;
import java.lang.Integer;
import java.lang.String;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CurrencyConvertionClass implements Serializable {

    private String date;

    private Double result;

    private Boolean success;

    private Query query;

    private Info info;

    public static class Query implements Serializable {
        private Integer amount;

        private String from;

        private String to;

    }

    public static class Info implements Serializable {
        private Double rate;

        private Integer timestamp;

    }
}
