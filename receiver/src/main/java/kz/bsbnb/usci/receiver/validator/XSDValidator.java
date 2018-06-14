package kz.bsbnb.usci.receiver.validator;

import org.xml.sax.SAXException;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author BSB
 */

public interface XSDValidator {

    boolean validateSchema(InputStream xsdInputStream, InputStream xmlInputStream) throws IOException, SAXException;

}
