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
import java.time.Instant;
import java.util.Collections;
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

        var clr = new GetDigitalDisplayCourtListResponse();
        clr.setResponseCd("A");
        clr.setResponseMessageTxt("A");

        Appearance a = new Appearance();
        a.setLastNm("A");
        a.setGiven1Nm("A");
        a.setInitialNm("A");
        a.setCtrmRoomCd("A");
        a.setCourtListTypeDsc("A");
        a.setStatusDsc("A");
        a.setAppearanceTime("22-APR-19 09.00.00.000000 AM");
        a.setAppearanceReasonCd("A");
        a.setAppearanceReasonDsc("A");
        a.setCourtDivisionCd("A");
        a.setCourtLevelCd("A");
        a.setCourtClassCd("A");
        a.setFileNumberTxt("A");
        a.setCounselFullNm("A");
        clr.setAppearance(Collections.singletonList(a));

        ResponseEntity<GetDigitalDisplayCourtListResponse> responseEntity =
                new ResponseEntity<>(clr, HttpStatus.OK);

        //     Set up to mock ords response
        when(restTemplate.exchange(
                        Mockito.any(String.class),
                        Mockito.eq(HttpMethod.GET),
                        Mockito.<HttpEntity<String>>any(),
                        Mockito.<Class<GetDigitalDisplayCourtListResponse>>any()))
                .thenReturn(responseEntity);

        var out = courtController.getDigitalDisplayCourtList(req);

        //     Assert response is correct
        assert out != null;
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
