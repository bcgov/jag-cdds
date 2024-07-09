package ca.bc.gov.open.cdds.configuration;

import ca.bc.gov.open.cdds.models.serializers.InstantDeserializer;
import ca.bc.gov.open.cdds.models.serializers.InstantSerializer;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.xml.soap.SOAPMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.config.annotation.WsConfigurerAdapter;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.ws.soap.server.endpoint.interceptor.PayloadValidatingInterceptor;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.wsdl11.SimpleWsdl11Definition;
import org.springframework.ws.wsdl.wsdl11.Wsdl11Definition;
import org.springframework.xml.validation.XmlValidator;
import org.springframework.xml.validation.XmlValidatorFactory;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.xml.xsd.XsdSchema;
import org.springframework.xml.xsd.XsdSchemaCollection;

@EnableWs
@Configuration
@Slf4j
public class SoapConfig extends WsConfigurerAdapter {
    @Value("${cdds.username}")
    private String username;

    @Value("${cdds.password}")
    private String password;

    @Value("${scj.username}")
    private String scjUsername;

    @Value("${scj.password}")
    private String scjPassword;

    public static final String SOAP_NAMESPACE = "http://courts.gov.bc.ca/xml/ns/cdds/v1";

    @Override
    public void addInterceptors(List<EndpointInterceptor> interceptors) {
        //      There are 2 versions of schemas to de couple model generation and request validation
        //      Suffix v means its a validation schema
        //        var validatingInterceptor1 = interceptor1();
        // interceptors.add(validatingInterceptor1);
    }

    private PayloadValidatingInterceptor interceptor1() {
        CustomPayloadValidator validatingInterceptor = new CustomPayloadValidator();
        // validatingInterceptor.setValidateRequest(true);
        validatingInterceptor.setXsdSchemaCollection(
                new XsdSchemaCollection() {
                    @Override
                    public XsdSchema[] getXsdSchemas() {
                        return new XsdSchema[] {
                            new SimpleXsdSchema(
                                    new ClassPathResource("validation/cdds-models-1v.xsd")),
                            new SimpleXsdSchema(
                                    new ClassPathResource("validation/cdds-models-2v.xsd")),
                            new SimpleXsdSchema(
                                    new ClassPathResource("validation/cdds-models-3v.xsd"))
                        };
                    }

                    @Override
                    public XmlValidator createValidator() {
                        try {
                            return XmlValidatorFactory.createValidator(
                                    getSchemas(), "http://www.w3.org/2001/XMLSchema");
                        } catch (Exception e) {
                            log.warn("XSD schema validation failed");
                        }
                        return null;
                    }

                    public Resource[] getSchemas() {
                        return new Resource[] {
                            new ClassPathResource("validation/cdds-models-1v.xsd"),
                            new ClassPathResource("validation/cdds-models-2v.xsd"),
                            new ClassPathResource("validation/cdds-models-3v.xsd")
                        };
                    }
                });
        return validatingInterceptor;
    }

    @Bean
    public ServletRegistrationBean<MessageDispatcherServlet> messageDispatcherServlet(
            ApplicationContext applicationContext) {
        MessageDispatcherServlet servlet = new MessageDispatcherServlet();
        servlet.setApplicationContext(applicationContext);
        servlet.setTransformWsdlLocations(true);
        return new ServletRegistrationBean<>(servlet, "/ws/*");
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
        var restTemplate = restTemplateBuilder.basicAuthentication(username, password).build();
        restTemplate.getMessageConverters().add(0, createMappingJacksonHttpMessageConverter());
        return restTemplate;
    }

    @Bean
    public RestTemplate scjRestTemplate(RestTemplateBuilder restTemplateBuilder) {
        var restTemplate = restTemplateBuilder.basicAuthentication(scjUsername, scjPassword).build();
        restTemplate.getMessageConverters().add(0, createMappingJacksonHttpMessageConverter());
        return restTemplate;
    }

    private MappingJackson2HttpMessageConverter createMappingJacksonHttpMessageConverter() {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper());
        return converter;
    }

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Instant.class, new InstantDeserializer());
        module.addSerializer(Instant.class, new InstantSerializer());
        objectMapper.registerModule(module);
        return objectMapper;
    }

    @Bean
    public SaajSoapMessageFactory messageFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(SOAPMessage.WRITE_XML_DECLARATION, "true");
        SaajSoapMessageFactory messageFactory = new SaajSoapMessageFactory();
        messageFactory.setMessageProperties(props);
        messageFactory.setSoapVersion(SoapVersion.SOAP_12);
        return messageFactory;
    }

    @Bean(name = "JusticeCDDS.wsProvider:cdds")
    public Wsdl11Definition JusticeCddsWSDL() {
        SimpleWsdl11Definition wsdl11Definition = new SimpleWsdl11Definition();
        wsdl11Definition.setWsdl(new ClassPathResource("xsdSchemas/JusticeCdds.wsdl"));
        return wsdl11Definition;
    }
}
