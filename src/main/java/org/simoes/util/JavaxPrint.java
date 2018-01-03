package org.simoes.util;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.attribute.*;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.MediaPrintableArea;
import javax.print.attribute.standard.MediaSize;
import javax.print.attribute.standard.PrinterResolution;
import javax.print.event.*;

import org.apache.log4j.Logger;

public class JavaxPrint { //implements Printable {

	private static volatile JavaxPrint _instance = null;
	//private Image image;

	static Logger logger = Logger.getLogger(JavaxPrint.class);

	public static synchronized JavaxPrint getInstance() {
		if (_instance == null)
			synchronized (JavaxPrint.class) {
				if (_instance == null)
					_instance = new JavaxPrint();
			}
		return _instance;
	}

	public PrintService getPrintService(String printerName) {
		try {
			if (printerName.contains("/"))
				printerName = printerName.replaceAll("/", "\\\\");
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		PrintService[] pservices = PrintServiceLookup.lookupPrintServices(DocFlavor.SERVICE_FORMATTED.PRINTABLE, null);
		for (PrintService s : pservices) {
			logger.info("Сравниваем принтер " + printerName + " с " + s.getName());
			try {
				if (printerName.equals(s.getName())) {
					logger.info("Нашёл принтер!");
					return s;
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
		// return PrintServiceLookup.lookupDefaultPrintService();
		logger.info("Не нашёл принтер");
		return null;
	}

	public void print(String filePath, String printerName) {
		logger.info("Метод печати файла " + filePath + " на принтер " + printerName);
		//количество попыток опроса печатного оборудования
		int tries = 3;
		//счётчик
		int i = 0;
		
		PrintService ps = null;
		while (i++ < tries)	{
			//пытаемся получить сервис сетевого принтера ОС по его имени
			logger.info("Пытаюсь получить сервис принтера в ОС..");
			ps = getPrintService(printerName);
			//Если сетевой принтер доступен в ОС
			if (ps != null) {
				logger.info("Сервис получен.");
				break;
			}	else	{
				logger.info("Сервис не получен, попытка " + i);
				//ждём две секунды
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					logger.error(e.getMessage(), e);
				}
			}
		}
		
		DocPrintJob job = ps.createPrintJob();
		logger.info("Создана задача для принтера " + ps.getName());

		job.addPrintJobListener(new PrintJobAdapter() {
			public void printDataTransferCompleted(PrintJobEvent event) {
				logger.info("data transfer complete");
			}

			public void printJobNoMoreEvents(PrintJobEvent event) {
				logger.info("received no more events");
			}
		});

		FileInputStream fis = null;
		try {
			File file = new File(filePath);
			fis = new FileInputStream(file);
		} catch (FileNotFoundException e1) {
			logger.error(e1.getMessage(), e1);
		}

		Doc doc = new SimpleDoc(fis, DocFlavor.INPUT_STREAM.AUTOSENSE, null);

		PrintRequestAttributeSet attrib = new HashPrintRequestAttributeSet();
		attrib.add(new Copies(1));
		try {
			job.print(doc, attrib);
		} catch (PrintException e) {
			logger.error(e.getMessage(), e);
		}
	}

	/*
	@Override
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
	  if (pageIndex == 0) {
          final Paper paper = pageFormat.getPaper();
          paper.setImageableArea(0.0, 0.0, pageFormat.getPaper().getWidth(), pageFormat.getPaper().getHeight());
          pageFormat.setPaper(paper);
          graphics.translate((int) pageFormat.getImageableX(), (int) pageFormat.getImageableY());
          graphics.drawImage(image, 0, 0, (int) pageFormat.getPaper().getWidth(), (int) pageFormat.getPaper().getHeight(), null);
          return PAGE_EXISTS;
      } else {
          return NO_SUCH_PAGE;
      }
    }
    */
	
	public void print(List <String> imgList, String printerName) {
		logger.info("Метод печати на принтер по списку JPG файлов " + printerName);
		//количество попыток опроса печатного оборудования
		int tries = 3;
		//счётчик
		int i = 0;
		
		PrintService ps = null;
		while (i++ < tries)	{
			//пытаемся получить сервис сетевого принтера ОС по его имени
			logger.info("Пытаюсь получить сервис принтера в ОС..");
			ps = getPrintService(printerName);
			//Если сетевой принтер доступен в ОС
			if (ps != null) {
				logger.info("Сервис получен.");
				break;
			}	else	{
				logger.info("Сервис не получен, попытка " + i);
				//ждём две секунды
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					logger.error(e.getMessage(), e);
				}
			}
		}

		DocPrintJob job = ps.createPrintJob();
		logger.info("Создана задача для принтера " + ps.getName());
		
		

		job.addPrintJobListener(new PrintJobAdapter() {
			public void printDataTransferCompleted(PrintJobEvent event) {
				logger.info("data transfer complete");
			}

			public void printJobNoMoreEvents(PrintJobEvent event) {
				logger.info("received no more events");
			}
		});
		
		FileInputStream fis = null;
		Doc doc = null;

		for (String fileItem : imgList) {
			try {
				File file = new File(fileItem);
				fis = new FileInputStream(file);
				DocAttributeSet das = new HashDocAttributeSet();
				
				int width = Math.round(MediaSize.ISO.A4.getX(MediaSize.MM));
				int height = Math.round(MediaSize.ISO.A4.getY(MediaSize.MM));

				das.add(new MediaPrintableArea(10, 10, width-10, height-10, MediaPrintableArea.MM));
				//add(new PrinterResolution(203, 203, PrinterResolution.DPI));  
				
				doc = new SimpleDoc(fis, DocFlavor.INPUT_STREAM.JPEG, das);  
				PrintRequestAttributeSet attrib = new HashPrintRequestAttributeSet();
				attrib.add(new Copies(1));
				
				try {
					logger.info("Попытка распечатать файл " + fileItem);
					job.print(doc, attrib);
				} catch (PrintException e) {
					logger.error(e.getMessage(), e);
				}
			} catch (FileNotFoundException e) {
				logger.error(e.getMessage(), e);
			}
		}

		
	}

}
