package edu.kndev.numbercite;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import kndev.model.mongo.paper.Sentence;

/**
 * Hello world!
 *
 */
public class App {
	public static void main(String[] args) throws IOException {
		
		NumberCite nbc = new NumberCite("D:\\pdf\\pdfs\\t24.pdf", "temp/123456768", "D:\\Tools\\grobid\\grobid-home\\pdf2xml\\win-64");
		for(Sentence s : nbc.getSentences()) {
			System.out.println(s.getText());
			System.out.println(s.getReferencesOrders());
			System.out.println("---------------------------");
		}
		System.out.println(nbc.getArticle().getOutlines().get(0).getContents().get(0));

		
	}
}
