package ru.credit_history_service.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import ru.credit_history_service.xsd.Payload;
import ru.credit_history_service.xsd.Reply;
import ru.credit_history_service.xsd.Request;


import javax.xml.soap.*;



@Controller
public class WebController
{
    private WebServiceTemplate webServiceTemplate;
    private Request request;
    private HttpHeaders header;

    @Autowired
    public WebController(WebServiceTemplate webServiceTemplate,
                         Request request, HttpHeaders header)
    {
        this.webServiceTemplate=webServiceTemplate;
        this.request=request;
        this.header=header;
    }

    @GetMapping("/")
    public String clientForm(Model model)
    {
        model.addAttribute("payload", new Payload());
        return "home";
    }

    @PostMapping("/")
    public HttpEntity<byte[]> sendRequest(@ModelAttribute Payload payload) throws WrongMessageException
    {
        request.setPayload(payload);
        sendAndReceive(request);

        final Reply jaxbElement = (Reply) sendAndReceive(request);
        byte[] clientHistory = jaxbElement.getPayload().getDocument();

        if(clientHistory == null || jaxbElement.getStatus() != 2 || clientHistory.length == 0)
        {
            throw new WrongMessageException("Wrong statusId or empty Document.");
        }

        header.setContentLength(clientHistory.length);
        return new HttpEntity<>(clientHistory, header);
    }

    private Object sendAndReceive(Request request)
    {
        return webServiceTemplate.marshalSendAndReceive(request, this::formatSoapRequest);
    }

    private void formatSoapRequest(WebServiceMessage webServiceMessage)
    {
        try
        {
            SaajSoapMessage saajSoapMessage = (SaajSoapMessage) webServiceMessage;
            SOAPMessage soapMessage = saajSoapMessage.getSaajMessage();
            SOAPPart soapPart = soapMessage.getSOAPPart();
            SOAPEnvelope envelope = soapPart.getEnvelope();
            SOAPHeader header = soapMessage.getSOAPHeader();
            SOAPBody body = soapMessage.getSOAPBody();

            envelope.removeNamespaceDeclaration(envelope.getPrefix());
            envelope.addNamespaceDeclaration("soapenv", "http://www.w3.org/2003/05/soap-envelope");
            envelope.addNamespaceDeclaration("urn", "urn:mFlow");
            envelope.setPrefix("soapenv");

            header.setPrefix("soapenv");
            body.setPrefix("soapenv");
        }
        catch (SOAPException e)
        {
            e.printStackTrace();
        }
    }

    public class WrongMessageException extends IllegalArgumentException
    {
        public WrongMessageException(String message)
        {
            super(message);
        }
    }

}
