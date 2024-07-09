package ca.bc.gov.open.cdds;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ca.bc.gov.open.cdds.controllers.CourtController;
import ca.bc.gov.open.cdds.exceptions.ORDSException;
import ca.bc.gov.open.cdds.one.Appearance;
import ca.bc.gov.open.cdds.one.GetDigitalDisplayCourtListResponse;
import ca.bc.gov.open.cdds.two.GetDigitalDisplayCourtList;
import ca.bc.gov.open.cdds.two.GetDigitalDisplayCourtListRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class CourtControllerTests {
    @Autowired private CourtController courtController;

    @Mock private RestTemplate restTemplate = new RestTemplate();

    @Autowired private ObjectMapper objectMapper;

    @Autowired private MockMvc mockMvc;

    @SuppressWarnings("unchecked")
    @Test
    public void getDigitalDisplayCourtList() throws IOException {
        //  Init service under test
        courtController = new CourtController(restTemplate, objectMapper);

        var req = new GetDigitalDisplayCourtList();
        var one = new GetDigitalDisplayCourtListRequest();
        var two = new ca.bc.gov.open.cdds.one.GetDigitalDisplayCourtListRequest();

        two.setAgencyIdentifierId("A");
        two.setAppearanceDt("A");
        two.setCtrmRoomCd("A");
        two.setRequestPartId("A");
        two.setRequestDtm("A");
        two.setRequestAgencyIdentifierId("A");

        one.setGetDigitalDisplayCourtListRequest(two);
        req.setGetDigitalDisplayCourtListRequest(one);

        ResponseEntity<GetDigitalDisplayCourtListResponse> responseEntity1 = generateResponse("ORDS-CDDS");
        ResponseEntity<GetDigitalDisplayCourtListResponse> responseEntity2 = generateResponse("SCJ-CDDS");

        // Set up to mock ORDS and SCJ response
        when(restTemplate.exchange(
                        Mockito.any(String.class),
                        Mockito.eq(HttpMethod.GET),
                        Mockito.<HttpEntity<String>>any(),
                        Mockito.<Class<GetDigitalDisplayCourtListResponse>>any()))
                .thenReturn(responseEntity1, responseEntity2);

        var out = courtController.getDigitalDisplayCourtList(req);

        // Assert response is correct
        assert out != null;
        List<Appearance> appearances = out.getGetDigitalDisplayCourtListResponse().getGetDigitalDisplayCourtListResponse().getAppearance();
        assert appearances.size() == 2;
        assert appearances.get(0).getCourtListTypeDsc() == "ORDS-CDDS";
        assert appearances.get(1).getCourtListTypeDsc() == "SCJ-CDDS";
    }

    private ResponseEntity<GetDigitalDisplayCourtListResponse> generateResponse(String data) {

        var clr = new GetDigitalDisplayCourtListResponse();
        clr.setResponseCd(data);
        clr.setResponseMessageTxt(data);

        Appearance a = new Appearance();
        a.setLastNm(data);
        a.setGiven1Nm(data);
        a.setInitialNm(data);
        a.setCtrmRoomCd(data);
        a.setCourtListTypeDsc(data);
        a.setStatusDsc(data);
        a.setAppearanceTime("22-APR-19 09.00.00.000000 AM");
        a.setAppearanceReasonCd(data);
        a.setAppearanceReasonDsc(data);
        a.setCourtDivisionCd(data);
        a.setCourtLevelCd(data);
        a.setCourtClassCd(data);
        a.setFileNumberTxt(data);
        a.setCounselFullNm(data);
        clr.getAppearance().addAll(Collections.singletonList(a));

        ResponseEntity<GetDigitalDisplayCourtListResponse> responseEntity =
                new ResponseEntity<>(clr, HttpStatus.OK);

        return responseEntity;
    }

    @Test
    public void setSyncSyncCivilHearingRestrictionTest() throws JsonProcessingException {
        courtController = new CourtController(restTemplate, objectMapper);

        when(restTemplate.exchange(
                        Mockito.any(String.class),
                        Mockito.eq(HttpMethod.GET),
                        Mockito.<HttpEntity<String>>any(),
                        Mockito.<Class<Object>>any()))
                .thenThrow(new RestClientException("BAD"));

        try {
            courtController.getDigitalDisplayCourtList(new GetDigitalDisplayCourtList());
        } catch (ORDSException ex) {
            // Exception caught as expected
            assert true;
            return;
        }
        // Test should never reach here
        assert false;
    }

    @Test
    public void securityTestFail_Then403() throws Exception {
        var response =
                mockMvc.perform(post("/ws").contentType(MediaType.TEXT_XML))
                        .andExpect(status().is4xxClientError())
                        .andReturn();
        assert response.getResponse().getStatus() == 401;
    }
}
