package org.simoes.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.ghost4j.converter.ConverterException;
import org.ghost4j.converter.PDFConverter;
import org.ghost4j.document.DocumentException;
import org.ghost4j.document.PSDocument;
import org.xml.sax.SAXException;
import kz.ugs.lpd.services.TaskService;

import org.simoes.lpd.LPD;
import org.simoes.lpd.Main;

/** Класс парсер PS в PDF HTML/JPG
 *  @author ZTokbayev
 *
 */
public class Parser {
	
	private static volatile Parser _instance = null;
	
	private String fromHost;
	public static String pdfPath;
	public static String printer;
	public File psFile;
	private List <String> imgList = new ArrayList <String> ();
	private TaskService taskService;
	
	public TaskService getTaskService() {
		return taskService;
	}

	public void setTaskService(TaskService taskService) {
		this.taskService = taskService;
	}

	public static Logger logger = LogManager.getLogger(LPD.class);
	
	public static synchronized Parser getInstance() {
        if (_instance == null)
        	 synchronized (Parser.class) {
                 if (_instance == null)
                     _instance = new Parser();
             }
        return _instance;
    }
	
	private File createFileFromBytes(byte [] bFile, String filePath)	{
		 try {
        	//извлечение имени файла из полного пути
        	Path path = Paths.get(filePath);
        	Files.write(path, bFile);
        	File file = new File(filePath);
			logger.info("Создан файл " + filePath);
			return file;
				
			} catch (FileNotFoundException e) {
				logger.error(e.getMessage(), e);
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		 return null;
	}
	
    private boolean parsePsToPdf(String pdfFileDest)	{
    	//load PostScript document
        PSDocument document = new PSDocument();
        try {
        	/*
        	//извлечение имени файла из полного пути
        	Path path = Paths.get(psFileDest);
        	Files.write(path, bFile);
        	File psFile = new File(psFileDest);
        	*/
			document.load(psFile);
			
		} catch (FileNotFoundException e) {
			logger.error(e.getMessage(), e);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
     
        //create OutputStream
        FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(new File(pdfFileDest));
		} catch (FileNotFoundException e) {
			logger.error(e.getMessage(), e);
		}
     
        //create converter
        PDFConverter converter = new PDFConverter();
        converter.setMaxProcessCount(2);
        
        //set options
        converter.setPDFSettings(PDFConverter.OPTION_PDFSETTINGS_PREPRESS);
     
        //convert
        try {
			converter.convert(document, fos);
			logger.info("Создан PDF файл " + pdfFileDest);
			return true;
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		} catch (ConverterException e) {
			logger.error(e.getMessage(), e);
		} catch (DocumentException e) {
			logger.error(e.getMessage(), e);
		} finally{
			try {
				fos.close();
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		}
        return false;
    }
    
    /*
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
     */
    /*
    private String parsePdfToHTML(String filePath)	{
    	ContentHandler handler = new ToXMLContentHandler();
        AutoDetectParser parser = new AutoDetectParser();
        Metadata metadata = new Metadata();
        metadata.set(Metadata.CONTENT_TYPE, "application/pdf");	
        //metadata.set(Metadata.CONTENT_ENCODING, "UTF-8");
        InputStream is = null;
		try {
			is = new FileInputStream(filePath);
		} catch (FileNotFoundException e) {
			logger.error(e.getMessage(), e);
		}
        try {
			parser.parse(is, handler, metadata);
		} catch (IOException | SAXException | TikaException e) {
			logger.error(e.getMessage(), e);
		}
        return handler.toString();
    }
    */
    /**метод извлечения картинок в JPG из PDF файла и записи их на диск 
     * названия идут такие image01.jpg, image0x.jpg
     * @param content
     * @param destPath
     * @return
     * @throws IOException
     * @throws SAXException
     * @throws TikaException
     */
    /*
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
    */
    public static String getFileNameFromFullPath(String fullPath)	{
    	File f = new File(fullPath);
    	return f.getName(); 
    }

	public void startParse(byte [] bFile, String fromHost, String fileName)	{
		this.fromHost = fromHost;
		pdfPath = null;
		//JPrint.getInstance().findMappedPrinter();
		printer = JPrint.getInstance().getPreferrePrinter(fromHost);
		logger.info("Поступил файл на обработку: " + fileName);
		fileName = getFileNameFromFullPath(fileName);
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm-ss");
		LocalDateTime now = LocalDateTime.now();
		String tmpDir = Main.props.getProperty("STORAGE"); 	
		String destPath = tmpDir + fromHost + "/" + dtf.format(now) + "/";
		Path path = Paths.get(destPath);
		try {
			Files.createDirectories(path);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		
		String psFileDest = destPath + fileName + ".pjb";
		psFile = createFileFromBytes(bFile, psFileDest);
		
		if (psFile != null)	{
			String pdfFileDest = destPath + fileName + ".pdf";
			String htmlFilePath = destPath + fileName + ".html";
			
			logger.info("Кодировка системы " + System.getProperty("file.encoding"));
	
			if (parsePsToPdf(pdfFileDest))	{
				
				//удаляем большой Pbj файл
				FileUtil.deleteFile(psFile);
				
				//создаём задачу в БД
				//taskService = new TaskService();
				//taskService.createTask(fromHost);
				
				pdfPath = pdfFileDest;
				
				if (Boolean.valueOf(Main.props.getProperty("PDFTOHTML"))) {
					MyConverter.generateHTMLFromPDF(pdfFileDest, htmlFilePath);
				}
				
				if (Boolean.valueOf(Main.props.getProperty("EXTRACTIMAGE"))) {
					MyConverter.extractImage(pdfFileDest, destPath);	
				}
				
				if (Boolean.valueOf(Main.props.getProperty("PDFTOIMAGE"))) {
					imgList = MyConverter.generateImageFromPDF(pdfFileDest, destPath);	
				}
				
				if (Main.props.getProperty("PRINTFORMAT").equals("IMAGE")) {
					printToPhysicalPrinter(imgList);	
				} else	{
					printToPhysicalPrinter(pdfFileDest);
				}
			}
		}
	}
	
	//печать PDF на физ принтер
	private void printToPhysicalPrinter(String filePath)	{
		String printer = null;
		if (Main.props.getProperty("PRINTMODE").equals("local"))	{
			logger.info("Печать на дефолтный принтер компьютера");
			printer = JPrint.getDefaultPrinter();
			if (printer != null)	{
				logger.info("Печатаю список JPG файлов " + imgList.toString());
				JavaxPrint.getInstance().print(filePath, printer);
				logger.info("Печать на MISPrinter");
				JavaxPrint.getInstance().print(filePath, Main.props.getProperty("MISPRINTER"));
			}	else	{
				logger.error("Не найден принтер, отмена печати");
			}	
		}	else	{
			//или выбираем принтер по маппингу
			/*
			printer = JPrint.getInstance().getPreferrePrinter(fromHost);
			if (printer != null)	{
				logger.info("Печатаю список JPG файлов " + imgList.toString());
				JavaxPrint.getInstance().print(filePath, printer);
			}	else	{
				logger.error("Не найден принтер, отмена печати");
			}
			*/
		}
	}
	
	//печать PDF на физ принтер
	private void printToPhysicalPrinter(List <String> imgList)	{
		String printer = null;
		if (Main.props.getProperty("PRINTMODE").equals("local"))	{
			printer = JPrint.getInstance().getDefaultPrinter();
			if (printer != null)	{
				logger.info("Печатаю список JPG файлов " + imgList.toString());
				JavaxPrint.getInstance().print(imgList, printer);
				JavaxPrint.getInstance().print(imgList, Main.props.getProperty("MISPRINTER"));
			}	else	{
				logger.error("Не найден принтер, отмена печати");
			}	
		}	else	{
			//или выбираем принтер по маппингу
			printer = JPrint.getInstance().getPreferrePrinter(fromHost);
			if (printer != null)	{
				logger.info("Печатаю список JPG файлов " + imgList.toString());
				JavaxPrint.getInstance().print(imgList, printer);
			}	else	{
				logger.error("Не найден принтер, отмена печати");
			}
		}
	}
	
}
