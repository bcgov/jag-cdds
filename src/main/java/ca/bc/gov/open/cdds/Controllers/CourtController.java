package ca.bc.gov.open.cdds.Controllers;

import ca.bc.gov.open.cdds.Configuration.SoapConfig;
import ca.bc.gov.open.cdds.Exceptions.ORDSException;
import ca.bc.gov.open.cdds.Models.OrdsErrorLog;
import ca.bc.gov.open.cdds.Models.Serializers.InstantSerializer;
import ca.bc.gov.open.cdds.one.GetDigitalDisplayCourtListRequest;
import ca.bc.gov.open.cdds.two.GetDigitalDisplayCourtList;
import ca.bc.gov.open.cdds.two.GetDigitalDisplayCourtListResponse;
import ca.bc.gov.open.cdds.two.GetDigitalDisplayCourtListResponse2;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

@Endpoint
@Slf4j
public class CourtController {

    @Value("${cdds.host}")
    private String host = "https://127.0.0.1/";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public CourtController(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @PayloadRoot(namespace = SoapConfig.SOAP_NAMESPACE, localPart = "getDigitalDisplayCourtList")
    @ResponsePayload
    public GetDigitalDisplayCourtListResponse getDigitalDisplayCourtList(
            @RequestPayload GetDigitalDisplayCourtList search) throws JsonProcessingException {
        var inner =
                search.getGetDigitalDisplayCourtListRequest() != null
                                && search.getGetDigitalDisplayCourtListRequest()
                                                .getGetDigitalDisplayCourtListRequest()
                                        != null
                        ? search.getGetDigitalDisplayCourtListRequest()
                                .getGetDigitalDisplayCourtListRequest()
                        : new GetDigitalDisplayCourtListRequest();

        UriComponentsBuilder builder =
                UriComponentsBuilder.fromHttpUrl(host + "court-list")
                        .queryParam("requestAgenId", inner.getRequestAgencyIdentifierId())
                        .queryParam("requestPartId", inner.getRequestPartId())
                        .queryParam("requestDtm", InstantSerializer.convert(inner.getRequestDtm()))
                        .queryParam("agencyIdentifierId", inner.getAgencyIdentifierId())
                        .queryParam(
                                "appearanceDt", InstantSerializer.convert(inner.getAppearanceDt()))
                        .queryParam("ctrmRoomCd", inner.getCtrmRoomCd());

        try {
            HttpEntity<ca.bc.gov.open.cdds.one.GetDigitalDisplayCourtListResponse> resp =
                    restTemplate.exchange(
                            builder.toUriString(),
                            HttpMethod.GET,
                            new HttpEntity<>(new HttpHeaders()),
                            ca.bc.gov.open.cdds.one.GetDigitalDisplayCourtListResponse.class);

            var out = new GetDigitalDisplayCourtListResponse();
            var one = new GetDigitalDisplayCourtListResponse2();
            one.setGetDigitalDisplayCourtListResponse(resp.getBody());
            out.setGetDigitalDisplayCourtListResponse(one);
            return out;
        } catch (Exception ex) {
            log.error(
                    objectMapper.writeValueAsString(
                            new OrdsErrorLog(
                                    "Error received from ORDS",
                                    "getDigitalDisplayCourtList",
                                    inner)));
            throw new ORDSException();
        }
    }
}
