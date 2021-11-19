package ca.bc.gov.open.cdds.configuration;

import ca.bc.gov.open.cdds.models.ValidationFailLog;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import javax.xml.transform.TransformerException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.*;
import org.springframework.ws.soap.server.endpoint.interceptor.PayloadValidatingInterceptor;
import org.xml.sax.SAXParseException;

@Slf4j
public class CustomPayloadValidator extends PayloadValidatingInterceptor {

    @Override
    protected boolean handleRequestValidationErrors(
            MessageContext messageContext, SAXParseException[] errors) throws TransformerException {
        ObjectMapper objectMapper = new ObjectMapper();
        // if any validation errors, convert them to a string and throw on as Exception to be
        // handled by CustomSoapErrorMessageDispatcherServlet
        if (errors.length > 0) {
            Arrays.stream(errors)
                    .forEach(
                            e -> {
                                try {
                                    log.error(
                                            objectMapper.writeValueAsString(
                                                    new ValidationFailLog(e.getMessage())));
                                } catch (JsonProcessingException ignored) {
                                }
                            });
            //            Change error format here
            if (messageContext.getResponse() instanceof SoapMessage) {
                SoapMessage response = (SoapMessage) messageContext.getResponse();
                SoapBody body = response.getSoapBody();
                SoapFault fault =
                        body.addClientOrSenderFault(
                                getFaultStringOrReason(), getFaultStringOrReasonLocale());
                if (getAddValidationErrorDetail()) {
                    SoapFaultDetail detail = fault.addFaultDetail();
                    for (SAXParseException error : errors) {
                        SoapFaultDetailElement detailElement =
                                detail.addFaultDetailElement(getDetailElementName());
                        detailElement.addText(error.getMessage());
                    }
                }
            }
            return false;
        }
        return true;
    }
}
