package org.simoes.util;

import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import javax.print.DocPrintJob;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;

import org.apache.log4j.Logger;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.printing.PDFPageable;

public class JPrint {
	
	private static volatile JPrint _instance = null;
	
	static Logger logger = Logger.getLogger(JPrint.class);

	public static synchronized JPrint getInstance() {
        if (_instance == null)
        	 synchronized (JPrint.class) {
                 if (_instance == null)
                     _instance = new JPrint();
             }
        return _instance;
    }
	
    public synchronized void print(String filePath, String printerNameDesired) {   
    logger.debug("Печать на физический принтер " + printerNameDesired);
    // Get printers
    PrintService[] services = PrinterJob.lookupPrintServices();
    DocPrintJob docPrintJob = null;
        try {
        	File dFile = new File(filePath);
          PDDocument pdf = PDDocument.load(dFile, MemoryUsageSetting.setupTempFileOnly());
          PrinterJob job = PrinterJob.getPrinterJob();
          //printerNameDesired = StringEscapeUtils.escapeJavaScript(printerNameDesired);
          try {
          if (printerNameDesired.contains("/"))
        	  printerNameDesired = printerNameDesired.replaceAll("/", "\\\\");
          } catch (Exception e) {
        	  logger.error(e.getMessage(), e);
          }
          for (int i = 0; i < services.length; i++) 	{
        	  if (services[i].getName().equalsIgnoreCase(printerNameDesired))	{
        		  docPrintJob = services[i].createPrintJob();
        		  logger.debug("Найден принтер в ОС " + services[i].getName());
        	  }
          }

          job.setPrintService(docPrintJob.getPrintService());
          job.setPageable(new PDFPageable(pdf));
          try {
        	   
        	  job.print();  
        	  pdf.close();
          } catch (Exception e) {
        	  logger.error(e.getMessage(), e);
          }
        } catch (Exception e) {
          logger.debug("[FAIL]" + e.getMessage(), e);
        }      
    }
    
    private static String replaceSlash(String val)	{
    	 try {
             if (val.contains("/"))
           	  return val.replaceAll("/", "\\\\");
             } catch (Exception e) {
           	  logger.error(e.getMessage(), e);
             }
    	 return val;
    }
    
    public static boolean findMappedPrinter()	{
    	 // Get printers
        PrintService[] services = PrinterJob.lookupPrintServices();
            try {
              for (int i = 0; i < services.length; i++) 	{
            	  logger.info("Принтер в системе: " + services[i].getName() + " (" + replaceSlash(services[i].getName()) + ")");
            	  if (services[i].getName().equalsIgnoreCase(Parser.printer))	{
            		  logger.debug("Найден принтер в ОС " + services[i].getName());
            		  return true;
            	  }
              }
            } catch (Exception e) {
              logger.error(e.getMessage(), e);
            }
        return false;
    }
    
    public static String getPreferrePrinter(String host)	{
    	logger.info("Trying to find mapped printer for host " + host);
    	Properties appProps = new Properties();
    	FileInputStream input = null;
		try {
			input = new FileInputStream("printers.properties");
			appProps.load(input);
			String printerFromConfig = appProps.getProperty(host);
			logger.info("Возвращаем принтер из конфига " + printerFromConfig + " для хоста " + host);
			return replaceSlash(printerFromConfig);
			}	catch (IOException e) {
				logger.error(e.getMessage(), e);
			} finally {
				if (input != null) {
					try {
						input.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
    	
			}
		logger.info("Не найден принтер в конфиге для хоста " + host);
		return null;
    }
    
    public static String getDefaultPrinter()	{
    	return PrintServiceLookup.lookupDefaultPrintService().getName();
    }
    
}