package nr.king.familytracker.controller;

import nr.king.familytracker.model.http.homeModel.HomeModel;
import nr.king.familytracker.model.http.purchaseModel.PurchaseRequestModel;
import nr.king.familytracker.service.PurchaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PurchaseController  extends BaseController{

    @Autowired
    private PurchaseService purchaseService;


    @PostMapping("/v{version:[1]}/make-order")
    public ResponseEntity makeOrder(@RequestBody PurchaseRequestModel purchaseRequestModel)
    {
        return purchaseService.makeOrder(purchaseRequestModel);
    }

    @PostMapping("/v{version:[1]}/get-apiTransction")
    public ResponseEntity makeOrders(@RequestBody HomeModel homeModel)
    {
        return purchaseService.getUserAPI(homeModel);
    }

    @PostMapping("/v{version:[1]}/update-timing")
    public ResponseEntity updateUserTiming(@RequestBody HomeModel homeModel)
    {
        return purchaseService.updateTimingForTesting(homeModel);
    }


}
