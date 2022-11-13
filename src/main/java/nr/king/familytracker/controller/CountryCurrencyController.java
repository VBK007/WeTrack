package nr.king.familytracker.controller;

import com.fasterxml.jackson.databind.ser.Serializers;
import nr.king.familytracker.model.http.currency.CurrecyModel;
import nr.king.familytracker.model.http.purchaseModel.PurchaseRequestModel;
import nr.king.familytracker.service.CountryCurrencService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CountryCurrencyController extends BaseController {

    @Autowired
    private CountryCurrencService countryCurrencService;

    @PostMapping(value = "/v{version:[1]}/updateMoney-order",produces = { "application/json" })
    public ResponseEntity updateMoneyOrder(@RequestBody CurrecyModel purchaseRequestModel)
    {
        return countryCurrencService.insertAppCountryValues(purchaseRequestModel);
    }


}
