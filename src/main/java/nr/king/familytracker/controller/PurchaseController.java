package nr.king.familytracker.controller;

import nr.king.familytracker.model.http.UpDateAppFlowRequestBody;
import nr.king.familytracker.model.http.UpdateAuditMasterRequestBody;
import nr.king.familytracker.model.http.homeModel.HomeModel;
import nr.king.familytracker.model.http.purchaseModel.PremiumModels;
import nr.king.familytracker.model.http.purchaseModel.PurchaseRequestModel;
import nr.king.familytracker.model.http.purchaseModel.PurchaseUpdateRequestModel;
import nr.king.familytracker.model.http.purchaseModel.UpdateUpiDetails;
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


    @PostMapping(value = "/v{version:[1]}/make-order" , produces = { "application/json" })
    public ResponseEntity makeOrder(@RequestBody PurchaseRequestModel purchaseRequestModel)
    {
        return purchaseService.makeOrder(purchaseRequestModel);
    }

    @PostMapping(value = "/v{version:[1]}/get-apiTransction",produces = { "application/json" })
    public ResponseEntity makeOrders(@RequestBody HomeModel homeModel)
    {
        return purchaseService.getUserAPI(homeModel);
    }


    @PostMapping(value = "/v{version:[1]}/update-apiTransction",produces = { "application/json" })
    public ResponseEntity updateInAppPurchase(@RequestBody PurchaseUpdateRequestModel purchaseUpdateRequestModel)
    {
        return purchaseService.inAppPurchase(purchaseUpdateRequestModel);
    }


    @PostMapping(value = "/v{version:[1]}/update-timing",produces = { "application/json" })
    public ResponseEntity updateUserTiming(@RequestBody HomeModel homeModel)
    {
        return purchaseService.updateTimingForTesting(homeModel);
    }


    @PostMapping(value = "/v{version:[1]}/update-flow",produces = { "application/json" })
    public ResponseEntity updateFlow(@RequestBody UpDateAppFlowRequestBody upDateAppFlowRequestBody)
    {
        return purchaseService.updateFlowRequest(upDateAppFlowRequestBody);
    }

    @PostMapping(value = "/v{version:[1]}/insert-audit",produces = { "application/json" })
    public ResponseEntity insertUserAudit(@RequestBody UpdateAuditMasterRequestBody updateAuditMasterRequestBody)
    {
        return purchaseService.insertUserAudit(updateAuditMasterRequestBody);
    }
}
