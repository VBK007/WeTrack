package nr.king.familytracker.repo.youtubeRepo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.MediaType;
import nr.king.familytracker.exceptions.FailedResponseException;
import nr.king.familytracker.model.http.ApiResponse;
import nr.king.familytracker.model.http.HttpResponse;
import nr.king.familytracker.model.http.homeModel.HomeModel;
import nr.king.familytracker.utils.CommonUtils;
import nr.king.familytracker.utils.HttpUtils;
import nr.king.familytracker.utils.ResponseUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.net.http.HttpHeaders;
import java.util.ArrayList;
import java.util.Arrays;

@Repository
public class YouTubeDashboardServiceRepo {

    @Autowired
    private CommonUtils commonUtils;

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ResponseUtils responseUtils;


    @Autowired
    private HttpUtils httpUtils;

    private static final Logger logger = LogManager.getLogger(YouTubeDashboardServiceRepo.class);

    public ResponseEntity getYouTubeDashBoard(HomeModel homeModel) {
        try {

        } catch (Exception exception) {
            logger.error("Exception in getYouTubeDashboard is" + exception.getMessage(), exception);
            throw new FailedResponseException(exception.getMessage());
        }
        return responseUtils.constructResponse(200, commonUtils.writeAsString(objectMapper,
                new ApiResponse()));
    }

    public ResponseEntity syncMaterial(HomeModel homeModel) {
        try {
            logger.info("home Model is " + commonUtils.writeAsString(objectMapper,homeModel));
            ArrayList<String> plantList = new ArrayList<>();
            plantList.add(homeModel.getId());
            ArrayList<String> materialList = getMaterials();
            for (int i = 0; i < plantList.size(); i++) {
                for (int j = 0; j < materialList.size(); j++) {
                    HttpResponse httpResponse = httpUtils.doGetRequest(getUrl(plantList.get(i), materialList.get(j)),
                            commonUtils.getMaterialSync(homeModel.getPackageName())
                    );
                    logger.info("Responses is " + httpResponse.getResponseCode() + "" + httpResponse.getResponse());
                }
            }

            return responseUtils.constructResponse(200, commonUtils.writeAsString(objectMapper, new ApiResponse()));


        } catch (Exception exception) {
            logger.error("Exception in syncMaterial is" + exception.getMessage(), exception);
            throw new FailedResponseException(exception.getMessage());
        }
    }

    private String getUrl(String plant, String materialCode) {
        return "https://vega-manna-uat.olamdigital.com/x-master/syncQualityParameter?Werks=" + plant + "&Matnr=" + materialCode + "&key=VEGA_GT10_COFF_SAP";
    }


    private ArrayList<String> getMaterials() {
        ArrayList<String> materialList = new ArrayList<>();
        materialList.add("100000067904");
        materialList.add("100000067903");
        materialList.add("100000052381");
        materialList.add("100000052102");
        materialList.add("100000051673");
        materialList.add("100000051484");
        materialList.add("100000044785");
        materialList.add("100000036246");
        materialList.add("100000036245");
        materialList.add("100000036244");
        materialList.add("100000036243");
        materialList.add("100000036242");
        materialList.add("100000036241");
        materialList.add("100000036230");
        materialList.add("100000036229");
        materialList.add("100000036228");
        materialList.add("100000036227");
        materialList.add("100000036226");
        materialList.add("100000036216");
        materialList.add("100000036215");
        materialList.add("100000036214");
        materialList.add("100000036213");
        materialList.add("100000036212");
        materialList.add("100000036211");
        materialList.add("100000036190");
        materialList.add("100000036188");
        materialList.add("100000036187");
        materialList.add("100000036183");
        materialList.add("100000036182");
        materialList.add("100000033928");
        materialList.add("100000033927");
        materialList.add("100000033926");
        materialList.add("100000033925");
        materialList.add("100000033924");
        materialList.add("100000033923");
        materialList.add("100000033922");
        materialList.add("100000033921");
        materialList.add("100000027290");
        materialList.add("100000027289");
        materialList.add("100000027288");
        materialList.add("100000020249");
        materialList.add("100000020247");
        materialList.add("100000020089");
        materialList.add("100000020086");
        materialList.add("100000020085");
        materialList.add("100000020084");
        materialList.add("100000020083");
        return materialList;
    }
}
