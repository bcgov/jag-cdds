package ca.bc.gov.open.cdds.controllers;

import ca.bc.gov.open.cdds.configuration.SoapConfig;
import ca.bc.gov.open.cdds.exceptions.ORDSException;
import ca.bc.gov.open.cdds.models.OrdsErrorLog;
import ca.bc.gov.open.cdds.models.RequestSuccessLog;
import ca.bc.gov.open.cdds.models.serializers.InstantSoapConverter;
import ca.bc.gov.open.cdds.one.Appearance;
import ca.bc.gov.open.cdds.one.GetDigitalDisplayCourtListRequest;
import ca.bc.gov.open.cdds.two.GetDigitalDisplayCourtList;
import ca.bc.gov.open.cdds.two.GetDigitalDisplayCourtListResponse;
import ca.bc.gov.open.cdds.two.GetDigitalDisplayCourtListResponse2;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

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
import org.springframework.ws.transport.context.TransportContext;
import org.springframework.ws.transport.context.TransportContextHolder;
import org.springframework.ws.transport.http.HttpServletConnection;

@Endpoint
@Slf4j
public class CourtController {

    @Value("${cdds.host}")
    private String host = "https://127.0.0.1/";

    @Value("${scj.host}")
    private String scjHost = "";

    private final RestTemplate restTemplate;
    private final RestTemplate scjRestTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public CourtController(RestTemplate restTemplate, RestTemplate scjRestTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.scjRestTemplate = scjRestTemplate;
        this.objectMapper = objectMapper;
    }

    @PayloadRoot(namespace = SoapConfig.SOAP_NAMESPACE, localPart = "getDigitalDisplayCourtList")
    @ResponsePayload
    public GetDigitalDisplayCourtListResponse getDigitalDisplayCourtList(
            @RequestPayload GetDigitalDisplayCourtList search) throws JsonProcessingException {
        addEndpointHeader("getDigitalDisplayCourtList");
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
                        .queryParam("requestDtm", inner.getRequestDtm())
                        .queryParam("agencyIdentifierId", inner.getAgencyIdentifierId())
                        .queryParam("appearanceDt", inner.getAppearanceDt())
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
            var in = resp.getBody();

            // Optionally add resonse from the SCJ endpoint before processing the results ...
            if (!scjHost.isEmpty()) {
                List<Appearance> scjAppearances = getScjDigitalDisplayCourtList(inner);
                in.getAppearance().addAll(scjAppearances);
            }

            for (var x : in.getAppearance()) {
                String newTime = InstantSoapConverter.convertFromAmTo24(x.getAppearanceTime());
                x.setAppearanceTime(newTime);
            }

            one.setGetDigitalDisplayCourtListResponse(in);
            out.setGetDigitalDisplayCourtListResponse(one);
            log.info(
                    objectMapper.writeValueAsString(
                            new RequestSuccessLog(
                                    "Request Success", "getDigitalDisplayCourtList")));
            return out;
        } catch (Exception ex) {
            log.error(
                    objectMapper.writeValueAsString(
                            new OrdsErrorLog(
                                    "Error received from ORDS",
                                    "getDigitalDisplayCourtList",
                                    ex.getMessage(),
                                    inner)));
            throw new ORDSException();
        }
    }

    private List<Appearance> getScjDigitalDisplayCourtList(
        GetDigitalDisplayCourtListRequest request) throws JsonProcessingException {

        UriComponentsBuilder builder =
            UriComponentsBuilder.fromHttpUrl(scjHost)
                .queryParam("AgencyIdentifierId", request.getRequestAgencyIdentifierId())
                .queryParam("AppearanceDt", request.getAppearanceDt())
                .queryParam("CtrmRoomCd", request.getCtrmRoomCd());

        try {
            HttpEntity<ca.bc.gov.open.cdds.one.GetDigitalDisplayCourtListResponse> resp =
                scjRestTemplate.exchange(
                    builder.toUriString(),
                    HttpMethod.GET,
                    new HttpEntity<>(new HttpHeaders()),
                    ca.bc.gov.open.cdds.one.GetDigitalDisplayCourtListResponse.class);

            log.info(
                objectMapper.writeValueAsString(
                    new RequestSuccessLog(
                        "Request Success", "getScjDigitalDisplayCourtList")));

            List<Appearance> scjAppearances = resp.getBody().getAppearance();
            return scjAppearances;
        } catch (Exception ex) {
            log.error(
                objectMapper.writeValueAsString(
                    new OrdsErrorLog(
                        "Error received from SCJ endpoint",
                        "getScjDigitalDisplayCourtList",
                        ex.getMessage(),
                        request)));
            throw new ORDSException();
        }
    }

    private void addEndpointHeader(String endpoint) {
        try {
            TransportContext context = TransportContextHolder.getTransportContext();
            HttpServletConnection connection = (HttpServletConnection) context.getConnection();
            connection.addResponseHeader("Endpoint", endpoint);
        } catch (Exception ex) {
            log.warn("Failed to add endpoint response header");
        }
    }
}
