package org.simoes.util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;
import org.fit.pdfdom.PDFDomTree;

import javax.imageio.ImageIO;
import com.itextpdf.text.pdf.PRStream;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfObject;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfImageObject;

import org.simoes.lpd.LPD;

/**
 * Класс для обработки PDF файлов
 * @author Zhass
 *
 */
public class MyConverter {
	
	public static Logger log = LogManager.getLogger(LPD.class);

	public static void generateHTMLFromPDF(String pdfFilePath, String htmlFilePath) {
		log.info("Started to convert file " + pdfFilePath + " to HTML file " + htmlFilePath);
	    PDDocument pdf = null;
		try {
	    	pdf = PDDocument.load(new File(pdfFilePath));
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
		Writer output = null;
		try {
			output = new PrintWriter(htmlFilePath, "utf-8");
		} catch (FileNotFoundException e) {
			log.error(e.getMessage(), e);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		try {
			new PDFDomTree().writeText(pdf, output);
			log.info("Конвертирован файл " + pdfFilePath + " в HTML файл " + htmlFilePath);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		} catch (ParserConfigurationException e) {
			log.error(e.getMessage(), e);
		}
		try {
			output.close();
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
	}
	
	/**
	 * Метод извлечения картинок в JPG формате из PDF файла 
	 * @param pdfFilePath
	 * @param outDir
	 */
	public static List <String> generateImageFromPDF(String pdfFilePath, String outDir) {
		
		List <String> imgList = new ArrayList <String> ();
	    PDDocument document = null;
	    
		try {
			document = PDDocument.load(new File(pdfFilePath));
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
	    PDFRenderer pdfRenderer = new PDFRenderer(document);
	    for (int page = 0; page < document.getNumberOfPages(); ++page) {
	        BufferedImage bim = null;
			try {
				bim = pdfRenderer.renderImageWithDPI(page, 300, ImageType.RGB);
			} catch (IOException e) {
				log.error(e.getMessage(), e);
			}
	        try {
	        	String fileName = String.format(outDir + "pdf-%d.%s", page + 1, "jpg");
				ImageIOUtil.writeImage(bim, fileName, 300);
				imgList.add(fileName);
				log.info("PDF файл " + pdfFilePath + " конвертирован в JPG " + fileName);
			} catch (IOException e) {
				log.error(e.getMessage(), e);
			}
	    }
	    try {
			document.close();
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
	    return imgList;
	}
	
	public static void extractImage(String pdfFilePath, String outDir)	{
		try	{
			//create pdf reader object 
			PdfReader pr = new PdfReader(pdfFilePath);
			PRStream pst;
			PdfImageObject pio;
			PdfObject po;
			int n = pr.getXrefSize(); //number of objects in pdf document
			for (int i = 0; i < n; i++)	{
				po=pr.getPdfObject(i); //get the object at the index i in the objects collection
				if (po == null || !po.isStream()) //object not found so continue
					continue;
				pst = (PRStream)po; //cast object to stream
				PdfObject type = pst.get(PdfName.SUBTYPE); //get the object type
				//check if the object is the image type object
				if (type!=null && type.toString().equals(PdfName.IMAGE.toString()))	{
					pio = new PdfImageObject(pst); //get the image  
					BufferedImage bi=pio.getBufferedImage(); //convert the image to buffered image
					ImageIO.write(bi, "jpg", new File(outDir + "image" + i + ".jpg")); //write the buffered image
					//to local disk
					}
				}
			log.info("Распарсены картинки из PDF " + pdfFilePath);
		  } catch(Exception e)	{
			  log.error(e.getMessage(), e);
			  }
	}
}
