package org.simoes.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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
import javax.print.event.*;

import org.apache.log4j.Logger;

import kz.ugs.callisto.system.propertyfilemanager.PropsManager;

public class JavaxPrint { //implements Printable {

	private static volatile JavaxPrint _instance = null;
	private static final Integer SLEEPTIME = Integer.valueOf(PropsManager.getInstance().getProperty("SLEEP")) * 1000;
	private static final Integer TRIES = Integer.valueOf(PropsManager.getInstance().getProperty("TRIES"));
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
		if (printerName == null) return null;
		logger.info("Попытка получить сервис принтера в ОС");
		try {
			if (printerName.contains("/"))
				printerName = printerName.replaceAll("/", "\\\\");
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		PrintService[] pservices = null;
		try	{
			pservices = PrintServiceLookup.lookupPrintServices(DocFlavor.SERVICE_FORMATTED.PRINTABLE, null);	
		}	catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		if (pservices != null) {
			logger.info("Ищем в ОС принтер " + printerName);
			for (PrintService s : pservices) {
				logger.info("Принтер в ОС " + s.getName());
				try {
					if (printerName.equals(s.getName())) {
						logger.info("Принтер найден, " + printerName + " = " + s.getName());
						return s;
					}
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			}
		}
		// return PrintServiceLookup.lookupDefaultPrintService();
		logger.info("Принтер не найден.");
		return null;
	}

	public void print(String filePath, String printerName) {
		logger.info("Метод печати файла " + filePath + " на принтер " + printerName);
		PrintService ps = tryToGetPrintService(printerName);
		if (ps != null) {
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
				logger.info("Файл " + filePath + " отправлен на принтер.");
			} catch (PrintException e) {
				logger.error(e.getMessage(), e);
			}
			try {
				fis.close();
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
			logger.info("Печать окончена.");
		}	else	{
			logger.info("Печать не произведена, на найден сервис принтера в ОС.");
		}
	}

	//попытка получить сервис принтера в ОС
	private PrintService tryToGetPrintService(String printerName)	{
		if (printerName == null) return null;
		logger.info("Попытка получить сервис принтера из ОС (" + printerName + ")");
		//количество попыток опроса печатного оборудования
		//счётчик
		int i = 2;
		PrintService ps = null;
		while (i <= TRIES)	{
			//пытаемся получить сервис сетевого принтера ОС по его имени
			ps = getPrintService(printerName);
			//Если сетевой принтер доступен в ОС
			if (ps != null) {
				logger.info("Сервис получен.");
				break;
			}	else	{
				logger.info("Сервис не получен, попытка " + i);
				//ждём две секунды
				try {
					i++;
					refreshSystemPrinterList();
					Thread.sleep(SLEEPTIME);
				} catch (InterruptedException e) {
					logger.error(e.getMessage(), e);
				}
			}
		}
		return ps;
	}
	
	public void print(List <String> imgList, String printerName) {
		logger.info("Метод печати на принтер по списку JPG файлов " + printerName);
		PrintService ps = tryToGetPrintService(printerName);
		if (ps != null) {
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
						job.print(doc, attrib);
						logger.info("Файл " + fileItem + " отправлен на принтер.");
						try {
							fis.close();
						} catch (IOException e) {
							logger.error(e.getMessage(), e);
						}
					} catch (PrintException e) {
						logger.error(e.getMessage(), e);
					}
				} catch (FileNotFoundException e) {
					logger.error(e.getMessage(), e);
				}
			}
			logger.info("Печать окончена.");
		}	else	{
			logger.info("Печать не произведена, на найден сервис принтера в ОС.");
		}
	}
	
	public static void refreshSystemPrinterList() {
	    Class[] classes = PrintServiceLookup.class.getDeclaredClasses();
	    for (int i = 0; i < classes.length; i++) {
	        if ("javax.print.PrintServiceLookup$Services".equals(classes[i].getName())) {
	        	logger.info("Обновление сервиса поиска принтеров " + classes[i].getName());
	            sun.awt.AppContext.getAppContext().remove(classes[i]);
	            break;
	        }
	    }
	}

}
