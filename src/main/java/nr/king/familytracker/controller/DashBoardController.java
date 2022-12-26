package nr.king.familytracker.controller;

import nr.king.familytracker.model.http.dashboardModel.DashBoardRequestBody;
import nr.king.familytracker.model.http.dashboardModel.FlashSales;
import nr.king.familytracker.model.http.dashboardModel.PublicEventRequestBody;
import nr.king.familytracker.model.http.fcmModels.FcmModelData;
import nr.king.familytracker.model.http.homeModel.HomeModel;
import nr.king.familytracker.model.http.messages.AdminMessageBody;
import nr.king.familytracker.model.http.messages.AdminMessages;
import nr.king.familytracker.model.http.messages.MessageRequestBody;
import nr.king.familytracker.service.DashBoardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DashBoardController extends BaseController {
    @Autowired
    private DashBoardService dashBoardService;

    @PostMapping(value = "/v{version:[1]}/dashboard", produces = {"application/json"})
    public ResponseEntity getDahboardResponse(@RequestBody DashBoardRequestBody homeModel) {
        return  dashBoardService.getDashBoardFragment(homeModel);
    }


    @PostMapping(value = "/v{version:[1]}/public-event", produces = {"application/json"})
    public ResponseEntity showPublicEvent(@RequestBody PublicEventRequestBody publicEventRequestBody)
    {
        return dashBoardService.publishPublicEvent(publicEventRequestBody);
    }


    @PostMapping(value = "/v{version:[1]}/post-EventClicked", produces = {"application/json"})
    public ResponseEntity showPublicEvent(@RequestBody FlashSales flashSales)
    {
        return dashBoardService.postPublicEventByUser(flashSales);
    }


    @PostMapping(value = "/v{version:[1]}/post-message", produces = {"application/json"})
    public ResponseEntity addUserMessage(@RequestBody MessageRequestBody messageRequestBody)
    {
        return dashBoardService.postMessageToUser(messageRequestBody);
    }

    @PostMapping(value = "/v{version:[1]}/post-fcmmessage",produces = {"application/json"})
    public ResponseEntity sendMessage(@RequestBody FcmModelData fcmModelData)
    {
        return dashBoardService.postPushNotification(fcmModelData);
    }

    @PostMapping(value = "/v{version:[1]}/get-allPostMesage", produces = {"application/json"})
    public ResponseEntity getAllUserDetails(@RequestBody AdminMessageBody messageRequestBody)
    {
        return dashBoardService.getAllUserMessage(messageRequestBody);
    }


    @PostMapping(value = "/v{version:[1]}/get-adashboard", produces = {"application/json"})
    public ResponseEntity adminDash(@RequestBody AdminMessages adminMessages)
    {
     return dashBoardService.adminDashBoard(adminMessages);
    }


}
