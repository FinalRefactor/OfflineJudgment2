package kr.dpmc.offlinejudgment.main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.poi.POIXMLProperties;
import org.apache.poi.hssf.record.CFRuleBase.ComparisonOperator;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFConditionalFormattingRule;
import org.apache.poi.xssf.usermodel.XSSFDataFormat;
import org.apache.poi.xssf.usermodel.XSSFPatternFormatting;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFSheetConditionalFormatting;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import info.debatty.java.stringsimilarity.JaroWinkler;
import kr.dpmc.offlinejudgment.OJApi;
import kr.dpmc.offlinejudgment.YamlConfiguration;

public class SimilirarityCheck {

	public static JaroWinkler jaro = new JaroWinkler();

	@SuppressWarnings("deprecation")
	public static void SimiliraritySummary(YamlConfiguration config) throws Exception {
		File parentFolder = new File(config.getString("���絵.��������"));
		Map<String, StringBuilder> map = new LinkedHashMap<>();
		// id, �ҽ�����

		List<String> exceptionlist = new ArrayList<String>();
		exceptionlist.add(" ");
		
		for (File file : parentFolder.listFiles()) {
			map.put(file.getName().replace(".txt", ""), OJApi.getSourceCodeToStringBuilder(file, exceptionlist, true));
		}
		//1.c�� .py�� ��ü�ؾ���

		Map<String, List<Double>> distanceMap = new LinkedHashMap<>();

		int num = 0;
		List<String> studentList = new LinkedList<>(map.keySet());
		studentList.sort(String.CASE_INSENSITIVE_ORDER);
		
		for (String id : studentList) {
			StringBuilder sb1 = map.get(id);
			List<Double> list = new LinkedList<>();
			for (String idp : studentList) {
				StringBuilder sb2 = map.get(idp);
				if (id.equals(idp)) {
					list.add(1.0);
				} else {
					double d = getDistance(sb1, sb2);
					list.add(d);
					// System.out.println(id + "??" + idp + "= " + d);
				}
			}
			distanceMap.put(id, list);
			num++;
			if (num % 10 == 0) {
				OJApi.printSwagWithStars(num + "�� �� �Ϸ�", 30);
			}
		}
		OJApi.printSwagWithStars("��ü �л� ���� ���ϱ� �Ϸ� O(n^2)", 50);
		// �ڵ� ���絵 �˻�


		OJApi.printSwagWithStars("������ �������� ����", 50);
		
		File excelFile = new File(config.getString("���絵.�������"));
		if (excelFile.exists())
			excelFile.delete();

		XSSFWorkbook workBook = new XSSFWorkbook();
		POIXMLProperties xmlProps = workBook.getProperties();
		POIXMLProperties.CoreProperties coreProps = xmlProps.getCoreProperties();
		coreProps.setCreator("DPmc");
		//���� ����
		
		XSSFSheet sheet = workBook.createSheet("���絵");

		XSSFCellStyle style = workBook.createCellStyle();
		XSSFDataFormat format = workBook.createDataFormat();
		style.setDataFormat(format.getFormat("0.00\"%\""));

		XSSFCellStyle style2 = workBook.createCellStyle();
		XSSFColor color = new XSSFColor(new java.awt.Color(255, 255, 204));
		style2.setFillForegroundColor(color);// ��������
		// style2.setFillBackgroundColor(color);
		style2.setFillPattern(CellStyle.SOLID_FOREGROUND);

		XSSFRow row = sheet.createRow(0);
		XSSFCell cell;
		cell = row.createCell(0);
		cell.setCellValue(studentList.size() + "��");
		cell.setCellStyle(style2);

		for (int i = 0; i < studentList.size(); i++) {
			cell = row.createCell(i + 1);
			cell.setCellValue(studentList.get(i));
			cell.setCellStyle(style2);
		}
		for (int i = 0; i < studentList.size(); i++) {
			cell = sheet.createRow(i + 1).createCell(0);
			cell.setCellValue(studentList.get(i));
			cell.setCellStyle(style2);
		} // �� ����, �� ������ �й� �Է�

		num = 0;
		for (int i = 0; i < studentList.size(); i++) {
			row = sheet.getRow(i + 1);
			List<Double> distances = distanceMap.get(studentList.get(i));
			for (int j = 0; j < distances.size(); j++) {
				cell = row.createCell(j + 1);
				cell.setCellStyle(style);
				cell.setCellValue(distances.get(j) * 100);
			} // row 1�� �Է�

			num++;
			if (num % 10 == 0) {
				OJApi.printSwagWithStars(num + "�� �ۼ� �Ϸ�", 50);
			}
		}
		// �Է� �Ϸ�

		int studentSize = studentList.size();
		CellRangeAddress range = new CellRangeAddress(1, 1, studentSize + 3, studentSize + 3);
		// ���Ǻ� ���� ��
		// System.out.println(range.formatAsString("���絵", true));
		sheet.getRow(0).createCell(studentSize + 3).setCellValue("���Ǻ� ���� ���");
		if (sheet.getRow(1) == null) {
			sheet.createRow(1);
		}
		sheet.getRow(1).createCell(studentSize + 3).setCellValue(80);
		
		XSSFSheetConditionalFormatting sheetcf = sheet.getSheetConditionalFormatting();
		XSSFConditionalFormattingRule rule = sheetcf.createConditionalFormattingRule(ComparisonOperator.BETWEEN, range.formatAsString("���絵", true), "100");
		XSSFPatternFormatting patternFormat = rule.createPatternFormatting();
		patternFormat.setFillBackgroundColor(IndexedColors.SKY_BLUE.index);
		// ���Ǻ� ���� ����

		CellRangeAddress[] arr = { new CellRangeAddress(1, studentSize, 1, studentSize) };
		sheetcf.addConditionalFormatting(arr, rule);
		// ���Ǻ� ���� �Է�

		workBook.write(new FileOutputStream(excelFile));
		workBook.close();
		OJApi.printSwagWithStars("���絵 ��� ��� ���� ����", 50); 
	}

	public static void writeToSCV(Map<String, StringBuilder> map, Map<String, List<Double>> distanceMap, YamlConfiguration config) throws Exception {
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(config.getString("���絵.�������"))));
		bw.write(" ");
		for (String id : map.keySet()) {
			bw.write("," + id);
		}
		bw.write("\n");
		// �� ù��

		for (String id : map.keySet()) {
			bw.write(id);
			for (Double d : distanceMap.get(id)) {
				bw.write(", " + Double.toString(d));
			}
			bw.write("\n");
		}
		bw.close();
		System.out.println("CSV���Ϸ� �������� �Ϸ�");
	}

	public static double getDistance(StringBuilder sb1, StringBuilder sb2) {
		return 1 - jaro.distance(sb1.toString(), sb2.toString());
	}

}
