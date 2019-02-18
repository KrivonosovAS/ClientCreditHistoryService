package ru.credit_history_service.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.ws.transport.http.HttpComponentsMessageSender;
import ru.credit_history_service.xsd.Request;


import javax.xml.bind.Marshaller;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Configuration
public class ApplicationConfig {

    @Autowired
    private ResourceLoader resourceLoader;

    @Value("${server.endpoint}")
    private String serverAddress;

    @Value("${service.contextPath}")
    private String contextPath;

    @Value("${service.nameOfSavedFile}")
    private String nameOfSavedFile;

    @Bean
    public Request request()
    {
        Request request = new Request();
        request.setCorrelationId(UUID.randomUUID().toString());
        return request;
    }

    @Bean
    public HttpHeaders httpHeader()
    {
        HttpHeaders header = new HttpHeaders();
        header.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
        header.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + nameOfSavedFile);
        return header;
    }


    @Bean
    public SaajSoapMessageFactory messageFactory() throws SOAPException {
            MessageFactory soapMessageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
            SaajSoapMessageFactory messageFactory = new SaajSoapMessageFactory(soapMessageFactory);
            messageFactory.setSoapVersion(SoapVersion.SOAP_12);
            messageFactory.afterPropertiesSet();
            return messageFactory;
    }

    @Bean
    public Jaxb2Marshaller marshaller()
    {
        return getMarshaller(resourceLoader.getResource("classpath:xsd/request.xsd"));
    }

    @Bean
    public Jaxb2Marshaller unmarshaller()
    {
        return getMarshaller(resourceLoader.getResource("classpath:xsd/reply.xsd"));
    }

    @Bean
    public HttpComponentsMessageSender messageSender()
    {
        HttpComponentsMessageSender httpComponentsMessageSender = new HttpComponentsMessageSender();
        return httpComponentsMessageSender;

    }

    @Bean
    public WebServiceTemplate webServiceTemplate()
    {
        WebServiceTemplate webServiceTemplate = new WebServiceTemplate();
        webServiceTemplate.setMarshaller(marshaller());
        webServiceTemplate.setUnmarshaller(unmarshaller());
        webServiceTemplate.setMessageSender(messageSender());
        try {
            webServiceTemplate.setMessageFactory(messageFactory());
        } catch (SOAPException e) {
            e.printStackTrace();
        }
        webServiceTemplate.setDefaultUri(serverAddress);
        return webServiceTemplate;
    }

    private Jaxb2Marshaller getMarshaller(Resource schemaLocation) {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setContextPath(contextPath);
        marshaller.setSchema(schemaLocation);

        Map<String, Object> properties = new HashMap();
        properties.put(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setMarshallerProperties(properties);
        return marshaller;
    }
}
