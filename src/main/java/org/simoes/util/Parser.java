package org.simoes.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.tika.detect.EncodingDetector;
import org.apache.tika.exception.TikaException;
import org.apache.tika.extractor.EmbeddedDocumentExtractor;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.ocr.TesseractOCRConfig;
import org.apache.tika.parser.pdf.PDFParser;
import org.apache.tika.parser.pdf.PDFParserConfig;
import org.apache.tika.parser.txt.CharsetDetector;
import org.apache.tika.sax.ExpandedTitleContentHandler;
import org.apache.tika.sax.ToXMLContentHandler;
import org.ghost4j.converter.ConverterException;
import org.ghost4j.converter.PDFConverter;
import org.ghost4j.document.DocumentException;
import org.ghost4j.document.PSDocument;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import kz.ugs.callisto.system.propertyfilemanager.PropsManager;

import org.simoes.lpd.LPD;

/** Класс парсер PS в PDF HTML/JPG
 *  @author ZTokbayev
 *
 */
public class Parser {
	
	public static Logger logger = LogManager.getLogger(LPD.class);
	public static final String htmlEncoding = "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />";
	private List <Object> listFilesForCallisto = new ArrayList();
	
    private boolean parsePsToPdf(byte [] bFile, String psFileDest, String pdfFileDest)	{
    	//load PostScript document
        PSDocument document = new PSDocument();
        try {
        	Path path = Paths.get(psFileDest);
        	Files.write(path, bFile);
			document.load(new File(psFileDest));
		} catch (FileNotFoundException e) {
			logger.error(e.getMessage(), e);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
     
        //create OutputStream
        FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(new File(pdfFileDest));
			logger.info("Создан PS файл " + pdfFileDest);
		} catch (FileNotFoundException e) {
			logger.error(e.getMessage(), e);
		}
     
        //create converter
        PDFConverter converter = new PDFConverter();
     
        //set options
        converter.setPDFSettings(PDFConverter.OPTION_PDFSETTINGS_PREPRESS);
     
        //convert
        try {
			converter.convert(document, fos);
			return true;
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		} catch (ConverterException e) {
			logger.error(e.getMessage(), e);
		} catch (DocumentException e) {
			logger.error(e.getMessage(), e);
		} finally{
			IOUtils.closeQuietly(fos);
		}
        return false;
    }
    
    private String detectCharset(String filePath)	{
    	Path path = Paths.get(filePath);
    	CharsetDetector charDetect = new CharsetDetector();
    	try {
    		charDetect.setText(Files.readAllBytes(path));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		String charSet = charDetect.detect().getName();
		logger.info("charset: " + charSet);
		return charSet;
    }

    
    private String parsePdfToHTML(String filePath) throws IOException, SAXException, TikaException {
        ContentHandler handler = new ToXMLContentHandler();
        AutoDetectParser parser = new AutoDetectParser();
        Metadata metadata = new Metadata();
        InputStream is = new FileInputStream(filePath);
        parser.parse(is, handler, metadata);
        return handler.toString();
    }	
    
    private String convertToHtml(byte [] bFile)	{
	    ByteArrayOutputStream out = new ByteArrayOutputStream();
	    SAXTransformerFactory factory = (SAXTransformerFactory)
	     SAXTransformerFactory.newInstance();
	    TransformerHandler handler = null;
		try {
			handler = factory.newTransformerHandler();
		} catch (TransformerConfigurationException e) {
			logger.error(e.getMessage(), e);
		}
	    handler.getTransformer().setOutputProperty(OutputKeys.METHOD, "html");
	    handler.getTransformer().setOutputProperty(OutputKeys.INDENT, "yes");
	    handler.getTransformer().setOutputProperty(OutputKeys.ENCODING, "UTF-8");
	    handler.setResult(new StreamResult(out));
	    ExpandedTitleContentHandler handler1 = new ExpandedTitleContentHandler(handler);
	    AutoDetectParser parser = new AutoDetectParser();
	    try {
			parser.parse(new ByteArrayInputStream(bFile), handler1, new Metadata());
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		} catch (SAXException e) {
			logger.error(e.getMessage(), e);
		} catch (TikaException e) {
			logger.error(e.getMessage(), e);
		}
	    try {
			return new String(out.toByteArray(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage(), e);
		}
	    return null;
    }
    
    /**метод извлечения картинок в JPG из PDF файла и записи их на диск 
     * названия идут такие image01.jpg, image0x.jpg
     * @param content
     * @param destPath
     * @return
     * @throws IOException
     * @throws SAXException
     * @throws TikaException
     */
    private ContentHandler parsePdfToJpg(byte[] content, final String destPath)throws IOException, SAXException, TikaException{           
    	
        Metadata metadata = new Metadata();
        ParseContext context = new ParseContext();
        ContentHandler handler =   new ToXMLContentHandler();
        PDFParser parser = new PDFParser(); 

        PDFParserConfig config = new PDFParserConfig();
        config.setExtractInlineImages(true);
        config.setExtractUniqueInlineImagesOnly(true);

        parser.setPDFParserConfig(config);


        EmbeddedDocumentExtractor embeddedDocumentExtractor = 
                new EmbeddedDocumentExtractor() {
            @Override
            public boolean shouldParseEmbedded(Metadata metadata) {
                return true;
            }
            @Override
            public void parseEmbedded(InputStream stream, ContentHandler handler, Metadata metadata, boolean outputHtml)
                    throws SAXException, IOException {
                Path outputFile = new File(destPath + metadata.get(Metadata.RESOURCE_NAME_KEY)).toPath();
                Files.copy(stream, outputFile);
            }
        };

        context.set(PDFParser.class, parser);
        context.set(EmbeddedDocumentExtractor.class,embeddedDocumentExtractor );

        try (InputStream stream = new ByteArrayInputStream(content)) {
            parser.parse(stream, handler, metadata, context);
        }	catch (Exception e)	{
        	logger.error("Похоже что нет картинок в PDF. " + e.getMessage(), e);
        }

        return handler;
    }
    
    private ContentHandler parsePdfToJpg(String pdfFileDest, final String destPath)throws IOException, SAXException, TikaException{           
    	
        Metadata metadata = new Metadata();
        ParseContext context = new ParseContext();
        ContentHandler handler =   new ToXMLContentHandler();
        PDFParser parser = new PDFParser(); 

        PDFParserConfig pdfConfig = new PDFParserConfig();
        pdfConfig.setExtractInlineImages(true);
        pdfConfig.setExtractUniqueInlineImagesOnly(true);
        
        parser.setPDFParserConfig(pdfConfig);


        EmbeddedDocumentExtractor embeddedDocumentExtractor = 
                new EmbeddedDocumentExtractor() {
            @Override
            public boolean shouldParseEmbedded(Metadata metadata) {
                return true;
            }
            @Override
            public void parseEmbedded(InputStream stream, ContentHandler handler, Metadata metadata, boolean outputHtml)
                    throws SAXException, IOException {
                Path outputFile = new File(destPath + metadata.get(Metadata.RESOURCE_NAME_KEY)).toPath();
                Files.copy(stream, outputFile);
            }
        };

        context.set(PDFParser.class, parser);
        context.set(EmbeddedDocumentExtractor.class,embeddedDocumentExtractor );
        byte[] bFile = Files.readAllBytes(new File(pdfFileDest).toPath());
        try (InputStream stream = new ByteArrayInputStream(bFile)) {
            parser.parse(stream, handler, metadata, context);
        }	catch (Exception e)	{
        	logger.error("Похоже что нет картинок в PDF. " + e.getMessage(), e);
        }

        return handler;
    }

	public void startParse(byte [] bFile, String fromHost, String fileName)	{
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm-ss");
		LocalDateTime now = LocalDateTime.now();
		String tmpDir = PropsManager.getInstance().getProperty("TEMP_DIR"); 		
		String destPath = tmpDir + fromHost + "/" + dtf.format(now) + "/";
		Path path = Paths.get(destPath);
		try {
			Files.createDirectories(path);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		
		fileName = destPath + fileName;
		String psFileDest = fileName + ".pjb";
		String pdfFileDest = fileName + ".pdf";
		String htmlFileDest = fileName + ".html";
		Parser parser = new Parser();
		String html = null;
		
		//logger.info("Кодировка системы " + System.getProperty("file.encoding"));
		if (parser.parsePsToPdf(bFile, psFileDest, pdfFileDest))	{
			logger.info("успешная конвертация " + psFileDest + " в PDF, парсим в HTML");
			//печать PDF на физ принтер
			JPrint print = new JPrint();
			File file = new File(pdfFileDest);
			print.print(file, print.getPreferrePrinter(fromHost));
			
			//logger.info("Кодировка PDF " + parser.detectCharset(pdfFileDest));
			try {
				html = parser.parsePdfToHTML(pdfFileDest);
	    		File in = new File(htmlFileDest);
	    		FileUtils.writeStringToFile(in, html, "UTF-8");
				logger.info("успешная конвертация в HTML");
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			} catch (SAXException e) {
				logger.error(e.getMessage(), e);
			} catch (TikaException e) {
				logger.error(e.getMessage(), e);
			}
			try {
				//parser.parsePdfToJpg(printJob.getDataFile().getContents(), destPath);
				parser.parsePdfToJpg(pdfFileDest, destPath);
				logger.info("успешная конвертация в JPG");
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			} catch (SAXException e) {
				logger.error(e.getMessage(), e);
			} catch (TikaException e) {
				logger.error(e.getMessage(), e);
			}
		}		
	}
	
}
