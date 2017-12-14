package org.simoes.util;

import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import javax.print.DocPrintJob;
import javax.print.PrintService;

import org.apache.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.printing.PDFPageable;
import org.ghost4j.converter.ConverterException;
import org.ghost4j.converter.PDFConverter;
import org.ghost4j.document.DocumentException;
import org.ghost4j.document.PSDocument;
import org.simoes.lpd.LPD;

import kz.ugs.callisto.system.propertyfilemanager.PropsManager;

public class JPrint {
	
	static Logger logger = Logger.getLogger(JPrint.class);

    public void print(File dFile, String printerNameDesired) {   
    logger.debug("JPrint) Print to specified printer..");
    // Get printers
    PrintService[] services = PrinterJob.lookupPrintServices();
    DocPrintJob docPrintJob = null;
        try {
          PDDocument pdf = PDDocument.load(dFile);
          PrinterJob job = PrinterJob.getPrinterJob();
          for (int i = 0; i < services.length; i++) 	{
        	  logger.debug("Printer name: " + services[i].getName());
        	  if (services[i].getName().equalsIgnoreCase(printerNameDesired)) {
        		  docPrintJob = services[i].createPrintJob();
        	  }
          }

          job.setPrintService(docPrintJob.getPrintService());
          job.setPageable(new PDFPageable(pdf));
          //docPrintJob = service[i].createPrintJob();
          job.print();

        } catch (Exception e) {
          logger.debug("[FAIL]" + e.getMessage(), e);
        }      
    }
    
    public String getPreferrePrinter(String host)	{
    	return PropsManager.getInstance().getProperty(host);
    }
    
}