package nr.king.familytracker.repo.youtubeRepo;

import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

public class RestClient {

    private static final String GET_INVITATION = "https://api.github.com/orgs/ORG-Example/invitations";
    private static final String token = "THE TOKEN";

    static RestTemplate restTemplate = new RestTemplate();


    public static void main(String[] args) {

    }

    public static void callListOrganizationAPI(String url,String tokens){

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.set("Authorization","Bearer"+ tokens);
        HttpEntity<String> request = new HttpEntity<String>(headers);
        ResponseEntity<String> result =  restTemplate.exchange(url, HttpMethod.GET,request,String.class);
        String json = result.getBody();
        System.out.println(" Rest Url is "+result.getBody()+"\n"+
                url+"Response code is"+result.getStatusCode());
    }

}
