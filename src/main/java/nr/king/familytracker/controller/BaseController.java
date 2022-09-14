package nr.king.familytracker.controller;

import nr.king.familytracker.exceptions.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import scala.math.Ordering;

import java.util.HashMap;
import java.util.Map;

import static nr.king.familytracker.constant.LocationTrackingConstants.*;
import static org.apache.logging.log4j.core.impl.ThrowableFormatOptions.MESSAGE;


@RestControllerAdvice
@RequestMapping("/we_track")
public class BaseController {

    private static final Logger logger = LogManager.getLogger(BaseController.class);

    @ExceptionHandler(FailedResponseException.class)
    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    public Map<String, String> failedResponse(FailedResponseException ex) {
        logger.error(String.format("Failed Response: %s", ex.getMessage()), ex);
        Map<String, String> responseMap = new HashMap<>();
        responseMap.put(STATUS, FALSE);
        responseMap.put(MESSAGE, ex.getMessage());

        return responseMap;
    }

    @ExceptionHandler(ConflictResponseException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> conflictResponse(ConflictResponseException ex) {
        logger.error(String.format("Conflict Response: %s", ex.getMessage()), ex);
        Map<String, String> responseMap = new HashMap<>();
        responseMap.put(STATUS, FALSE);
        responseMap.put(MESSAGE, ex.getMessage());
        return responseMap;
    }

    @ExceptionHandler(BadResponseException.class)
    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    public Map<String, String> badResponse(BadResponseException ex) {
        logger.error(String.format("Bad Response: %s", ex.getMessage()), ex);
        Map<String, String> responseMap = new HashMap<>();
        responseMap.put(STATUS, FALSE);
        responseMap.put(MESSAGE, ex.getMessage());
        return responseMap;
    }

    @ExceptionHandler(InvalidHeadersException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Map<String, String> badHeaders(BadResponseException ex) {
        logger.error(String.format("Bad Response: %s", ex.getMessage()), ex);
        Map<String, String> responseMap = new HashMap<>();
        responseMap.put(STATUS, FALSE);
        responseMap.put(MESSAGE, ex.getMessage());
        return responseMap;
    }

    @ExceptionHandler(InvalidDataException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public Map<String, String> invalidResponse(InvalidDataException ex) {
        logger.error(String.format("Invalid Response: %s", ex.getMessage()), ex);
        Map<String, String> responseMap = new HashMap<>();
        responseMap.put(STATUS, FAILED);
        responseMap.put(MESSAGE, ex.getMessage());
        return responseMap;
    }


}
