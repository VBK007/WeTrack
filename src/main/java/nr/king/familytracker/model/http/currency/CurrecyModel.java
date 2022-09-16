package nr.king.familytracker.model.http.currency;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.lang.Double;
import java.lang.Integer;
import java.lang.String;
import java.util.List;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CurrecyModel implements Serializable {
    private List<? extends ListofCountry> listofCountry;

    public List<? extends ListofCountry> getListofCountry() {
        return this.listofCountry;
    }

    public void setListofCountry(List<? extends ListofCountry> listofCountry) {
        this.listofCountry = listofCountry;
    }


    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ListofCountry implements Serializable {
        private String symbol;
        private String code;
        private String symbol_native;
        private String countryCode;
        private Integer decimal_digits;
        private Double rounding;
        private String moneyOneDay;
        private String moneyOneWeek;
        private String moneyOneMonth;
        private String moneyThreeMonth;
        private String moneyOneYear;
        private Long id;
    }
}
