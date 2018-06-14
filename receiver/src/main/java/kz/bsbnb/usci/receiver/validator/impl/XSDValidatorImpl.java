package kz.bsbnb.usci.receiver.validator.impl;

import kz.bsbnb.usci.model.ErrorMessage;
import kz.bsbnb.usci.receiver.validator.XSDValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.io.InputStream;

import java.util.List;

/**
 * @author BSB
 */

@Service
public class XSDValidatorImpl implements XSDValidator {
    private static final Logger logger = LoggerFactory.getLogger(XSDValidatorImpl.class);
    
    List<ErrorMessage> validate(InputStream xsd, InputStream xml) {
        return null;
    }

    private class ErrorHandlerImpl implements ErrorHandler {
        private boolean isValid = true;
        private String errMessage;

        @Override
        public void warning(SAXParseException exception) throws SAXException {
            System.err.println("Предупреждение: " + exception.getException());
        }

        @Override
        public void error(SAXParseException exception) throws SAXException {
            isValid = false;
            errMessage = exception.getMessage();
        }

        @Override
        public void fatalError(SAXParseException exception) throws SAXException {
            isValid = false;
            errMessage = exception.getMessage();
        }
    }

    @Override
    public boolean validateSchema(InputStream xsdInputStream, InputStream xmlInputStream) throws IOException, SAXException {

        Source xsd = new StreamSource(xsdInputStream);
        Source xml = new StreamSource(xmlInputStream);
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = schemaFactory.newSchema(xsd);

        Validator validator = schema.newValidator();

        ErrorHandlerImpl errorHandlerImpl = new ErrorHandlerImpl();
        validator.setErrorHandler(errorHandlerImpl);

        validator.validate(xml);

        if (!errorHandlerImpl.isValid) {
            logger.error("XML не прошёл проверку XSD: " + errorHandlerImpl.errMessage);
        }

        return errorHandlerImpl.isValid;
    }




}
