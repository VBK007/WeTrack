package nr.king.familytracker.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import nr.king.familytracker.model.http.ApiResponse;
import nr.king.familytracker.model.http.currency.CurrecyModel;
import nr.king.familytracker.repo.CountryCurrencyRepo;
import nr.king.familytracker.utils.CommonUtils;
import nr.king.familytracker.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class CountryCurrencService {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CommonUtils commonUtils;

    @Autowired
    private CountryCurrencyRepo filterRepo;

    @Autowired
    private ResponseUtils responseUtils;

    public ResponseEntity insertAppCountryValues(CurrecyModel filterHistoryModel)
    {
        try {
            return filterRepo.insertAppCountryValues(filterHistoryModel);
        }
        catch (Exception exception)
        {
            return responseUtils.constructResponse(406,commonUtils.writeAsString(objectMapper,
                    new ApiResponse(false,"Unable to Update Country  Values")));
        }
    }



}
