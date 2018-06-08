package kz.bsbnb.usci.receiver.test;

import kz.bsbnb.usci.model.Errors;
import kz.bsbnb.usci.receiver.validator.XSDValidator;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.xml.sax.SAXException;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.time.LocalDate;
import java.util.Date;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class XSDValidatorImplTest {
    private static final Logger logger = LoggerFactory.getLogger(XSDValidatorImplTest.class);

    private File file;

    @Autowired
    private XSDValidator xsdValidator;

    @Before
    public void setUp() {
    }

    @Test
    public void test0() throws FileNotFoundException{

        file = new File("D:\\test\\cr_2017-09-252.zip");
        ZipArchiveInputStream zais = new ZipArchiveInputStream(new FileInputStream(file));
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        inputFactory.setProperty("javax.xml.stream.isCoalescing", true);

        byte[] buffer = new byte[4096];
        ByteArrayOutputStream out;

        try {
            zais.getNextZipEntry();
            int len;
            out = new ByteArrayOutputStream(4096);
            while ((len = zais.read(buffer, 0, 4096)) > 0) {
                out.write(buffer, 0, len);
            }
        } catch (IOException e) {
            logger.error("Error in entity reader");
            throw new RuntimeException(e);
        }

        InputStream isXml = new ByteArrayInputStream(out.toByteArray());

        file = new File("D:\\test\\credit-registry.xsd");
        InputStream isXsd = new FileInputStream(file);

        try {
            if (xsdValidator.validateSchema(isXsd, isXml)) {
                logger.info("Валидация прошла успешно");
                //xmlEventReader = inputFactory.createXMLEventReader(new ByteArrayInputStream(out.toByteArray()));
            } else {
                throw new RuntimeException(Errors.compose(Errors.E193));
            }
        } catch (SAXException | IOException e) {
            logger.error("XML не прошёл проверку XSD: " + e.getMessage());
            throw new RuntimeException(e);
        }

    }
}