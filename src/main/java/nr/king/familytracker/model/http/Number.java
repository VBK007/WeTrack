package nr.king.familytracker.model.http;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Number{
    public boolean isActive;
    public String phoneNumber;
    public Object onPhone;
}