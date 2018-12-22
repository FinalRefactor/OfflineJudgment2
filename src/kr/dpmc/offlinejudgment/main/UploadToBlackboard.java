package kr.dpmc.offlinejudgment.main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import kr.dpmc.offlinejudgment.OJApi;
import kr.dpmc.offlinejudgment.YamlConfiguration;

public class UploadToBlackboard {

	public static class Student {
		public String name;
		public String id;
		public double score;
		public String comment;

		public Student() {

		}

		public Student(String name, String id, double score, String comment) {
			this.name = name;
			this.id = id;
			this.score = score;
			this.comment = comment;
		}
	}

	public static boolean isDoublePositive(String string) {
		return string.matches("([0-9]+|[0-9]+[.][0-9]+)");
	}

	public static void writeUploadFile(YamlConfiguration config) throws Exception {
		File scoreFile = new File(config.getString("���ε�.��������"));
		File uploadFile = new File(config.getString("���ε�.���ε�����"));
		File uploadSumFile = new File(config.getString("���ε�.���ε���������"));

		if (!scoreFile.exists()) {
			OJApi.printSwagWithAccent("���� ������ ã�� �� �����ϴ�.");
			return;
		}
		if (!uploadFile.exists()) {
			OJApi.printSwagWithAccent("���ε� ������ ã�� �� �����ϴ�.");
			return;
		}

		Map<String, Student> stdMap = new HashMap<>();
		boolean isGetScore = getScoreData(scoreFile, stdMap);
		if (!isGetScore) {
			OJApi.printSwagWithAccent("ä�� �����͸� �ҷ��� �� �����ϴ�.");
			return;
		}
		OJApi.printSwagWithAccent("ä�� �����͸� �ҷ��Խ��ϴ�.");
		
		Map<String, String[]> csvMap = new HashMap<>();
		String title = getCSVData(uploadFile, csvMap);
		// ù��° �� ��ȯ

		OJApi.printSwagWithAccent("���ε� �����͸� �ҷ��Խ��ϴ�.");
		
		summarizeToUploadData(stdMap, csvMap);
		OJApi.printSwagWithAccent("���ε� �����Ϳ� ä�� ������ ���� �Ϸ�");
		
		writeToCSV(csvMap, title, uploadSumFile);
		OJApi.printSwagWithAccent("���ε� ���� ���Ϸ� �ۼ� �Ϸ�");
	}

	public static void writeToCSV(Map<String, String[]> csvMap, String title, File uploadFile) throws Exception {
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(uploadFile), Charset.forName("UTF-16").newEncoder()));
		bw.append(title).append('\n');
		for (String[] args : csvMap.values()) {
			for (int i = 0; i < args.length; i++) {
				bw.append('\"').append(args[i]).append('\"');
				if (i + 1 != args.length) {
					bw.append('\t');
				}
			}
			bw.append('\n');
		}
		bw.close();
	}

	public static void summarizeToUploadData(Map<String, Student> stdMap, Map<String, String[]> csvMap) {
		for (String id : csvMap.keySet()) {
			Student std = stdMap.get(id);
			String[] args = csvMap.get(id);
			if (args[4].equals("ä�� �ʿ�")) {
				args[4] = String.valueOf(std.score);
				args[7] = std.comment;
			}
		}
	}

	public static String getCSVData(File uploadFile, Map<String, String[]> csvMap) throws Exception {
		List<String> lines = OJApi.getSourceCodeToStringBuilder_UTF16(uploadFile);
		// �̸�, id, �а�, ��������, ������(����), �����޸�, �޸�����, �ǵ��, �ǵ������
		// 9�� �Ǵ� 4��?
		for (int i = 1; i < lines.size(); i++) {
			String[] args = lines.get(i).split("\t");
			if (args.length < 2) {
				OJApi.printSwag("�ùٸ��� ���� ����(���� 2�� �̸�)", 50, " ����", "����");
				System.out.println(lines.get(i));
			} else {
				if (args.length == 9) {
					// ��ü �� �ִ� ���
					for (int j = 0; j < args.length; j++) {
						if (args[j].startsWith("\""))
							args[j] = args[j].substring(1, args[j].length() - 1);
					}
					// ū����ǥ ����
					csvMap.put(args[1], args);
				} else if (args.length >= 4) {
					// �й� �̸��� �ִ� ���
					for (int j = 0; j < args.length; j++) {
						if (args[j].startsWith("\""))
							args[j] = args[j].substring(1, args[j].length() - 1);
					}
					// ū����ǥ ����
					String[] args2 = new String[9];
					for (int j = 0; j < 4; j++) {
						args2[j] = args[j];

					}
					for (int j = 4; j < args2.length; j++) {
						args2[j] = "";
					}
					csvMap.put(args[1], args2);
				} else {
					OJApi.printSwag("�ùٸ��� ���� ����(���� 4�� �̸�)", 50, " ����", "����");
					System.out.println(lines.get(i));
				}
			}
		}
		return lines.get(0);
	}

	public static boolean getScoreData(File scoreFile, Map<String, Student> stdmap) throws Exception {
		XSSFWorkbook workBook = new XSSFWorkbook(scoreFile);
		XSSFSheet sheet = workBook.getSheetAt(0);

		XSSFRow row = sheet.getRow(0);
		if (row == null) {
			workBook.close();
			return false;
		}

		int scoreIndex = -1;
		int commentIndex = -1;
		int nameIndex = -1;
		int idIndex = -1;

		int rowEndNum = row.getLastCellNum();
		DataFormatter formatter = new DataFormatter();

		for (int i = row.getFirstCellNum(); i < rowEndNum; i++) {
			XSSFCell cell = row.getCell(i);
			if (cell != null) {
				String value = formatter.formatCellValue(cell);
				if (value.equals("�й�")) {
					idIndex = i;
				} else if (value.equals("�̸�")) {
					nameIndex = i;
				} else if (value.equals("����")) {
					scoreIndex = i;
				} else if (value.equals("�ڸ�Ʈ")) {
					commentIndex = i;
				}
			}
		}

		if (idIndex == -1 || commentIndex == -1 || nameIndex == -1 || scoreIndex == -1) {
			workBook.close();
			return false;
		}

		try {
			int columnEndNum = sheet.getPhysicalNumberOfRows();
			for (int i = 1; i < columnEndNum; i++) {
				row = sheet.getRow(i);
				String name = formatter.formatCellValue(row.getCell(nameIndex));
				String id = formatter.formatCellValue(row.getCell(idIndex));
				String score = formatter.formatCellValue(row.getCell(scoreIndex));
				String comment = formatter.formatCellValue(row.getCell(commentIndex));

				if (isDoublePositive(score)) {
					Student std = new Student(name, id, Double.valueOf(score), comment);
					// System.out.println("name=" + name + ", id=" + id + ", score="
					// + score + ", comment=#" + comment + "#");
					stdmap.put(id, std);
				} else {
					OJApi.printSwagWithAccent(name + " " + id + "�л� ���� �Ǽ��� �ƴ�");
				}
			}
		} catch (Exception e) {
			//������ ���°����� ���� �о���� ��
		}
		
		workBook.close();

		return true;
	}

}
