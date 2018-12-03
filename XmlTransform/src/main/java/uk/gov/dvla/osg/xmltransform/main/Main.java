package uk.gov.dvla.osg.xmltransform.main;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.text.TextStringBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

public class Main {
    
    static final Logger LOGGER = LogManager.getLogger();
    
    public static void main(String[] args) {
        
        if (args.length != 2) {
            LOGGER.error("Incorrect number of arguments supplied.");
            System.exit(1);
        }
        
        // LOAD INPUT FILE
        String inputXmlFileName = args[0];
        File inputXmlFile = new File(inputXmlFileName);
        
        if (!inputXmlFile.exists()) {
            LOGGER.error("Input XML File [{}] does not exist on the filepath.", inputXmlFileName);
            System.exit(1);
        }
        
        LOGGER.info("Input file loaded {}", inputXmlFileName);
        
        // CREATE OUTPUT FILE
        String outputFileName = args[1];
        File outputFile = new File(outputFileName);
        
        if (outputFile.exists()) {
            LOGGER.error("Output File [{}] already exists on the filepath.", outputFileName);
            System.exit(1);
        }
        
        // READ INPUT FILE
        XmlMapper mapper = new XmlMapper();
        CollectionType mapCollectionType = mapper.getTypeFactory().constructCollectionType(List.class, Map.class);
        
        List<Map<String, String>> results = new ArrayList<Map<String, String>>();
        
        try {
            results = mapper.readValue(inputXmlFile, mapCollectionType);
        } catch (IOException ex) {
            LOGGER.error("Unable to parse XML Input file {} : {}", inputXmlFileName, ex.getMessage());
            System.exit(1);
        }
        
        // WRITE PIPE DELIMITED OUTPUT FILE
        TextStringBuilder sb = new TextStringBuilder();
        
        for (Map<String, String> result : results) {
            
            String content = result.entrySet()
                                .stream()
                                .map(e -> e.getValue())
                                .collect(Collectors.joining("|"));
            
            sb.appendln(content);
        }

        LOGGER.debug(sb.toString());
        
        try {
            FileUtils.writeStringToFile(outputFile, sb.toString(), StandardCharsets.UTF_8, false);
            LOGGER.info("Content written to: {}", outputFileName);
        } catch (IOException ex) {
            LOGGER.error("Unable to write data to output file {} : {}", outputFileName, outputFile);
        }

    }

}
