package kr.dpmc.offlinejudgment.main;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.poi.POIXMLProperties;
import org.apache.poi.hssf.record.CFRuleBase.ComparisonOperator;
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

import kr.dpmc.offlinejudgment.OJApi;
import kr.dpmc.offlinejudgment.StudentHW;
import kr.dpmc.offlinejudgment.TestData;
import kr.dpmc.offlinejudgment.YamlConfiguration;

public class ScoreCheck {

	private static Random random = new Random();

	/**
	 * ���� ä�� �޼���
	 * 
	 * @param config ����
	 * @param classNumber �й� �ѹ�
	 * @throws Exception
	 */
	public static void OfflineJudgmentMain(YamlConfiguration config, int classNumber) throws Exception {
		List<TestData> testDatas = getTestDatas(config);
		OJApi.printSwagWithStars("�׽�Ʈ ������ �ҷ����� �Ϸ� size=" + testDatas.size(), 50);
		// �׽�Ʈ ������ ����

		Map<String, StudentHW> studentMap = checkStudentHwScore(testDatas, config);
		OJApi.printSwagWithStars("ä�� �Ϸ�", 50);
		// �й�, �л����� ������

		writeToSimilirarityFiles(studentMap, config);
		OJApi.printSwagWithStars("���絵 �˻�� ���� ���� �Ϸ�", 50);
		// ���絵 �˻�� ���Ϸ� �ڵ常 ��������
		
		addNotAssignmentStudent(studentMap, testDatas, classNumber, config);
		OJApi.printSwagWithStars("�������� ������ Ȯ��", 50);
		// �������� Ȯ��

		OJApi.printSwagWithStars("������ �������� ����", 50);
		writeToExcel(studentMap, testDatas, config);
		// writeToCSV(studentMap, testDatas, config);
		OJApi.printSwagWithStars("ä�� ��� ���� ���Ϸ� ���� �Ϸ�", 50);
		// ä����� ����
		
		copyToHWSumFolder(studentMap, config);
		OJApi.printSwagWithStars("�ڵ����ϸ��������� �������ϸ��������� �������� �Ϸ�", 50);
	}

	/**
	 * �ڵ�, ���� ���� ���Ϸ� ����
	 * 
	 * @param studentMap
	 * @param config ä��.�ڵ����ϸ�������, ä��.�������ϸ�������
	 * @throws Exception
	 */
	public static void copyToHWSumFolder(Map<String, StudentHW> studentMap, YamlConfiguration config) throws Exception {
		String folder = config.getString("ä��.�ڵ����ϸ�������");
		File fFolder = new File(folder);
		if (!fFolder.exists()) {
			fFolder.mkdirs();
		}
		for (StudentHW student : studentMap.values()) {
			if (student.hwfile == null || student.fileName.equals("null")) {
				continue;
			}
			File toCopy = new File(fFolder.getPath() + "/" + student.id + " " + student.name + ".py");
			OJApi.fileCopy(student.hwfile, toCopy);
		} // �ڵ����� ����

		folder = config.getString("ä��.�������ϸ�������");
		fFolder = new File(folder);
		if (!fFolder.exists()) {
			fFolder.mkdirs();
		}
		for (StudentHW student : studentMap.values()) {
			if (student.screenshotFiles == null || student.screenshotFiles.size() == 0) {
				continue;
			}
			int index = 1;
			for (File file : student.screenshotFiles) {
				String extension = OJApi.getFileExtension(file.getName());
				File toCopy = new File(fFolder.getPath() + "/" + student.id + " " + student.name + " " + index + "." + extension);
				OJApi.fileCopy(file, toCopy);
			}
		} // �������� ����

	}

	public static void addNotAssignmentStudent(Map<String, StudentHW> studentMap, List<TestData> testDatas, int classNumber, YamlConfiguration config) {
		if (classNumber > 0) {
			List<String[]> studentList = OJApi.getStudentList(config, classNumber);
			List<String> idList = new ArrayList<>(studentMap.keySet());

			for (int i = 0; i < studentList.size(); i++) {
				String[] args = studentList.get(i);
				String id = args[0];
				String name = args[1];

				if (!idList.contains(id)) {
					StudentHW student = new StudentHW(name, id);
					student.homeworkFileScore = 1;
					student.screenshotScore = 1;
					student.fileName = "null";
					student.testcaseScore = new int[testDatas.size()];
					for (int j = 0; j < student.testcaseScore.length; j++) {
						student.testcaseScore[j] = 9;
					}
					studentMap.put(id, student);
					idList.add(id);
				} // �л� ��Ͽ� ���ٸ�
			}

			Collections.sort(idList, OJApi.comparatorString);

			Map<String, StudentHW> map = new LinkedHashMap<>();
			for (String id : idList) {
				map.put(id, studentMap.get(id));
			}

			studentMap.clear();
			for (String id : map.keySet()) {
				studentMap.put(id, map.get(id));
			}
			// studentMap ���� �Ϸ�
		}
	}

	public static void writeToSimilirarityFiles(Map<String, StudentHW> studentMap, YamlConfiguration config) throws Exception {
		File similirarity = new File(config.getString("���絵.��������"));
		if (!similirarity.exists()) {
			similirarity.mkdirs();
		} // ���絵 �˻� ����

		for (StudentHW student : studentMap.values()) {
			if (student.fileName == null || student.fileName.equals("null")) {
				OJApi.printSwagWithAccent(student.id + " " + student.name + "�л��� ���絵 �˻����� ���� �Ұ�");
				continue;
			}

			if (student.fileName.equals("")) {
				continue;
			} // �������� �ѱ�

			File file = new File(similirarity.getPath() + "/" + student.id + ".py");
			if (!file.exists()) {
				file.createNewFile();
			}

			BufferedReader br = new BufferedReader(new FileReader(student.hwfile));
			BufferedWriter bw = new BufferedWriter(new FileWriter(file));
			String s;
			while ((s = br.readLine()) != null) {
				s = OJApi.stringLineConvertToException(s, config.getStringList("���絵.���ܹ���"), config.getBoolean("���絵.�ּ�����"));

				if (config.getBoolean("���絵.������")) {
					s = s.replace("\t", "");
				} // �� �����ϰ� trim

				s = s.trim();

				bw.write(s);

				if (!config.getBoolean("���絵.�ٳѱ�����")) {
					bw.write('\n');
				} // �ٳѱ� ����

			}
			bw.close();
			br.close();

		}
	}

	public static Map<String, StudentHW> checkStudentHwScore(List<TestData> testDatas, YamlConfiguration config) throws Exception {
		Map<String, StudentHW> studentMap = new LinkedHashMap<>();
		List<String> checkExceptionChar = config.getStringList("ä��.ä�����ܹ���");
		List<String> hwRecognizeList = config.getStringList("ä��.�����νĹ���");
		List<String> screenshotExtension = config.getStringList("ä��.�����ν�Ȯ����");
		List<String> sourcecodeCheck = config.getStringList("ä��.�ҽ����ϰ˻�");

		File parentFolder = new File(config.getString("ä��.��������"));
		if (!parentFolder.exists()) {
			parentFolder.mkdir();
		} // ���� ������ �����
		File instFolder = new File("���");
		instFolder.mkdirs();
		int num = 0;
		for (File folder : parentFolder.listFiles()) {
			// �л��� ���� ���� = folder

			if (!folder.isDirectory()) {
				OJApi.printSwagWithBraket(folder.getName() + "�� ������ �ƴ�");
				continue;
			}
			// ������ �ƴ� ���

			String fname = folder.getName();
			if (!fname.matches("[0-9]+ [0-9��-�R]+")) {
				OJApi.printSwagWithBraket(fname + " �������� �л� �̸��� �й� �ν� �Ұ���");
				continue;
			}
			// ���Խ� ��ġ���� ���� ���

			String[] args = fname.split(" ");
			if (args.length != 2) {
				OJApi.printSwagWithBraket(Arrays.toString(args) + " ���� �̸����� �л��� �й��ܿ� �ٸ� ������ ����");
				continue;
			}
			// ������ �ʹ� ���� ���

			String id = args[0];
			String name = args[1];
			StudentHW student = new StudentHW(name, id);
			studentMap.put(id, student);
			// �л� �̸��� �й� ���� �и�

			// ���� ���� ����
			student.screenshotFiles = new LinkedList<>();
			for (File file : folder.listFiles()) {
				boolean isEndwith = false;
				String fileName = file.getName().toLowerCase();
				for (String extens : screenshotExtension) {
					if (fileName.endsWith(extens)) {
						isEndwith = true;
						break;
					}
				}
				if (isEndwith) {
					student.screenshotFiles.add(file);
				}
			}

			if (student.screenshotFiles.size() >= 1) {
				student.screenshotScore = 2;
				// ���� ���� ����
			} else {
				student.screenshotScore = 1;
				// ���� ���� ����
			}

			// ���� ������ ã�Ҵ°�
			boolean isFindHW = false;
			for (File hwFile : folder.listFiles()) {
				isFindHW = false;
				if (hwFile.getName().endsWith(".py")) {
					isFindHW = true;
					StringBuilder sb = OJApi.getSourceCodeToStringBuilder(hwFile, config.getStringList("ä��.�����ν����ܹ���"), true);
					for (String s : hwRecognizeList) {
						if (sb.indexOf(s) == -1) {
							isFindHW = false;
							break;
						}
					} // ���� ���� ã�� for

					if (isFindHW) {
						student.fileName = hwFile.getName();
						student.hwfile = hwFile;
						break;
					} // ���ϴ� ������ ã���� ���
				}
			}
			// ���� ���� ã��

			num++;
			if (num % 10 == 0) {
				//OJApi.printSwagWithBraket(num + "�� ä�� �Ϸ�");
			} // ��¿� �޼���

			if (isFindHW) {
				student.testcaseScore = new int[testDatas.size()];
				for (int i = 0; i < testDatas.size(); i++) {
					student.testcaseScore[i] = 9;
				}
				student.homeworkFileScore = 3;
				//System.out.print(student.id + " " + student.name + "->");
				//System.out.println(student.hwfile);
				StringBuilder sb = OJApi.getSourceCodeToStringBuilder(student.hwfile, null, true);
				boolean hasSourceCodeString = true; // �ҽ��ڵ忡 Ư�� ���ڿ� �ִ��� �˷��ִ� ����
				for (String str : sourcecodeCheck) {
					if (sb.indexOf(str) == -1) {
						hasSourceCodeString = false;
					}
				}
				if (hasSourceCodeString) {
					student.sourcecodeScore = 2;
				} else {
					student.sourcecodeScore = 1;
				}
				// ���� ���� ã����
			} else {
				OJApi.printSwagWithAccent(id + " " + name + "�������� ���� ���� ã�� ����");
				student.testcaseScore = new int[testDatas.size()];
				for (int i = 0; i < testDatas.size(); i++) {
					student.testcaseScore[i] = 9;
				}
				student.homeworkFileScore = 2;
				student.sourcecodeScore = 0;
				student.hwfile = null;
				student.fileName = "null";
				continue;
				// ���������� ã�� ���ؼ� �Ѿ
			}

			double score = 0;
			File originalFile = null;
			if (config.getBoolean("ä��.�Է�������Ʈ�޼�������")) {
				originalFile = student.hwfile;

				String pathf = student.hwfile.getPath();
				String namef = student.hwfile.getName();
				pathf = pathf.replace(namef, "");
				pathf = pathf + File.separator + random.nextInt(1000000) + ".py";
				student.hwfile = new File(pathf);

				BufferedReader br = new BufferedReader(new FileReader(originalFile));
				BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(student.hwfile), "Cp949"));
				String s;
				while ((s = br.readLine()) != null) {
					s = OJApi.stringLineConvertToException(s, null, false);
					if (s.contains("input")) {
						int i1 = s.indexOf("input") + "input".length();
						boolean isOpenQuotes = false;
						int countOpenBracket = 0;
						int endIndex = -1;
						for (int i = i1; i < s.length(); i++) {
							char c = s.charAt(i);
							if (isOpenQuotes) {
								if (s.charAt(i - 1) != '\\' && (c == '\"' || c == '\'')) {
									// \"�� ��� �����ϰ� " �϶��� �۵�
									isOpenQuotes = false;
								}
							} else {
								if (c == '(') {
									countOpenBracket++;
									// ���� ��ȣ�� 1����
								} else if (c == '\"' || c == '\'') {
									isOpenQuotes = true;
								} else if (c == ')') {
									// ���� ��ȣ�� 1����
									countOpenBracket--;
									if (countOpenBracket == 0) {
										endIndex = i;
										break;
									}
								}
							}
						}

						if (endIndex == -1) {
							//System.out.println("�ҽ� �ҷ� " + student.id + " " + student.name);
							//System.out.println("s=[" + s + "]");
							bw.append(s).append('\n');
						} else {
							String s2 = s.substring(0, i1 + 1);
							String s3 = s.substring(endIndex);
							//System.out.println("�ҽ� ���� " + student.id + " " + student.name);
							//System.out.println("s=[" + s2 + s3 + "]");
							bw.append(s2).append(s3).append('\n');
						}
					} else {
						bw.append(s).append('\n');
					}
				}
				br.close();
				bw.close();
			}
			
			FileWriter fw = new FileWriter(instFolder + "/" + student.id + " " + student.name + ".txt");
			for (int i = 0; i < testDatas.size(); i++) {
				TestData testData = testDatas.get(i);
				//�˸���
				List<String> list = new ArrayList<>();
				int code = ScoreCheck.check(student.hwfile.getPath(), testData, checkExceptionChar, list);
				fw.append("TestCase: " + (i+1)).append('\n');
				for(String s : list){
					fw.append(s).append('\n');
				}
				fw.append('\n');
				student.testcaseScore[i] = code;
				if (code == 7)
					score++;
			} // �׽�Ʈ ���̽����� ä���ϴ°�
			fw.close();
			score /= testDatas.size();
			student.totalScore = score;

			if (config.getBoolean("ä��.�Է�������Ʈ�޼�������")) {
				student.hwfile.delete();
				student.hwfile = originalFile;
			}

			// �л� �Ѹ�(���� �ϳ�) ��
		}
		return studentMap;
	}

	@SuppressWarnings({ "unused", "deprecation" })
	public static List<TestData> getTestDatas(YamlConfiguration config) throws Exception {
		List<TestData> testDatas = new ArrayList<>();
		File testDataFolder = new File(config.getString("ä��.���������"));
		if (!testDataFolder.exists()) {
			testDataFolder.mkdirs();
		} // �׽�Ʈ ������ ��������

		if (testDataFolder.listFiles().length == 0) {
			OJApi.printSwagWithAccent("�׽�Ʈ �����Ͱ� �����ϴ�. �� ������ �о��ּ���.");
			return null;
		} // �׽�Ʈ ������ �����Ƿ� ����

		if (false) {
			Set<String> testDataNames = new LinkedHashSet<>();
			for (File file : testDataFolder.listFiles()) {
				String name = file.getName();
				if (name.endsWith(".in") || name.endsWith(".outlow") || name.endsWith(".outhigh")) {
					int i1 = name.lastIndexOf('.');
					testDataNames.add(name.substring(0, i1));
				} else {
					
					System.out.println(name + "�� ������ ���� Ȯ���ڰ� �ƴմϴ�. (.in .outlow .outhigh)");
				}
			} // �׽�Ʈ ������ ��ϵ鸸 ��������

			String parentPath = testDataFolder.getPath();
			for (String name : testDataNames) {
				File in = new File(parentPath + "/" + name + ".in");
				File outraw = new File(parentPath + "/" + name + ".outlow");
				File outhigh = new File(parentPath + "/" + name + ".outhigh");
				if (in.exists() && outraw.exists() && outhigh.exists()) {
					testDatas.add(new TestData(in, outraw, outhigh));
				} else {
					System.out.println(name + " �׽�Ʈ �����ʹ� .in .outlow .outhigh Ȯ���ڰ� ���� �������� �ʽ��ϴ�.");
				}
			}
		} // ���Ž� �ڵ� (����6 ����)

		for (File file : testDataFolder.listFiles()) {
			if (file.getName().endsWith(".testcase")) {
				testDatas.add(new TestData(file));
			} else {
				OJApi.printSwagWithAccent(file.getName() +"�� ������ ���� Ȯ���ڰ� �ƴմϴ�. (.case)");
			}

		} // test data�ε� ����7�̻�

		return testDatas;
	}

	public static void writeToExcel(Map<String, StudentHW> map, List<TestData> testDatas, YamlConfiguration config) throws Exception {
		XSSFWorkbook workBook = new XSSFWorkbook();
		POIXMLProperties xmlProps = workBook.getProperties();
		POIXMLProperties.CoreProperties coreProps = xmlProps.getCoreProperties();
		coreProps.setCreator("DPmc");
		// ���� ����

		XSSFSheet sheet = workBook.createSheet("���");
		
		XSSFRow row = sheet.createRow(0);
		String[] args = new String[] { "�й�", "�̸�", "�����̸�", "��������", "��������", "��������", "�ҽ��ڵ�����" };
		String[] args2 = new String[] { "��� ���", "����", "�ڸ�Ʈ", "���" };

		int rowIndex = 1;
		int columnIndex = 0;
		columnIndex = setCellValueForObject(row, columnIndex, args);
		for (int i = 0; i < testDatas.size(); i++) {
			row.createCell(columnIndex).setCellValue("���̽�" + (i + 1));
			columnIndex++;
		}
		columnIndex = setCellValueForObject(row, columnIndex, args2);

		XSSFCellStyle style = workBook.createCellStyle();
		XSSFDataFormat format = workBook.createDataFormat();
		style.setDataFormat(format.getFormat("0.0\"%\""));
		// �ۼ�Ʈ��ȣ ���̴� ����
		// �������� �����ε� ����

		int num = 0;
		for (StudentHW std : map.values()) {
			row = sheet.createRow(rowIndex);
			rowIndex++;

			columnIndex = 0;
			columnIndex = setCellValueForObject(row, columnIndex, std.id);
			columnIndex = setCellValueForObject(row, columnIndex, std.name);
			columnIndex = setCellValueForObject(row, columnIndex, std.fileName);
			columnIndex = setCellValueForObject(row, columnIndex, getTotalScore(std));// ��������
			columnIndex = setCellValueForObject(row, columnIndex, std.homeworkFileScore);
			columnIndex = setCellValueForObject(row, columnIndex, std.screenshotScore);
			columnIndex = setCellValueForObject(row, columnIndex, std.sourcecodeScore);
			// row.getCell(columnIndex - 1).setCellStyle(style);
			// args1 �����͵� �Է�

			columnIndex = setCellValueForObject(row, columnIndex, std.testcaseScore);
			// �׽�Ʈ ���̽� �Է�

			boolean isPerfectCode = true;
			boolean isNotAssignment = false;
			boolean isCantFindPYFile = false;
			isNotAssignment = std.homeworkFileScore == 1;
			isCantFindPYFile = std.homeworkFileScore == 2;
			for (int i = 0; i < std.testcaseScore.length; i++) {
				int code = std.testcaseScore[i];
				if (code != 7)
					isPerfectCode = false;
			} // ��� �Ǵ� ���

			String result;
			if (isNotAssignment) {
				result = "���� ������";
			} else if (isCantFindPYFile) {
				result = "�ڵ����� ��ã��";
			} else if (isPerfectCode) {
				result = "���� ä��";
			} else {
				result = "��˻� �ʿ�";
			}
			columnIndex = setCellValueForObject(row, columnIndex, result);
			// ��� ��� �Է�

			num++;
			if (num % 10 == 0) {
				// System.out.println(" *** " + num + "�� ������ �ۼ� �Ϸ� ***");
			}
		}
		OJApi.printSwagWithStars("ä�� ������ �ۼ� �Ϸ�", 50);

		{
			StringBuilder totalDes2 = new StringBuilder("322");
			for (int i = 0; i < testDatas.size(); i++) {
				totalDes2.append("7");
			}
			String[] totalscoreDescription = { "���� ���� �ڵ带 ��ģ ��", totalDes2.toString() + "�� �Ϻ��� �����ڵ�" };// index 3
			String[] fileDescription = { "0: null", "1: ���� ������", "2: �ڵ����� ��ã��", "3: �ڵ����� ã��" };// index 4
			String[] screenshotDescription = { "0: null", "1: �������� ��ã��", "2: �������� ã��" };// index 5
			String[] codeDescription = { "0: null", "1: �ڵ忡 Ư�� ���ڿ� ����", "2: �ڵ忡 Ư�� ���ڿ� ����" };// index 6
			String[] testcaseDescription = { "low/high/equal�� ������� �� �ڸ��� �Ҵ���", "0: 000 (���� ����ġ)", "1: 001", "2: 010", "3: 011", "4: 100", "5: 101", "6: 110", "7: 111 (���� ��ġ)", "8: ���̽� ���� �߻�", "9: null" }; // 7
			String[] colorDescription = {"��Ȳ: 7(111)�� �ƴ� �͵�", "����: 9(���Ͼ���)", "�ʷ�: 100���� �ƴ� �͵�"};
			
			rowIndex += 5;
			//5ĭ ������
			
			for (int i = rowIndex; i < rowIndex + testcaseDescription.length; i++) {
				sheet.createRow(i);
			}
			// testcase������ ���� ��ϱ� �� ���̸� �����

			sheet.setColumnWidth(2, (int)(sheet.getColumnWidth(2) * 2.5));
			sheet.setColumnWidth(3, sheet.getColumnWidth(3) * 2);
			sheet.setColumnWidth(2, (int)(sheet.getColumnWidth(4) * 1.5));
			sheet.setColumnWidth(2, (int)(sheet.getColumnWidth(5) * 1.5));
			sheet.setColumnWidth(2, (int)(sheet.getColumnWidth(6) * 1.5));
			
			int resultIndex = args.length + testDatas.size();
			sheet.setColumnWidth(resultIndex, (int)(sheet.getColumnWidth(resultIndex) * 1.75));
			//���� ����
			
			
			setColumnValueForStringArray(sheet, rowIndex, 3, totalscoreDescription);
			setColumnValueForStringArray(sheet, rowIndex, 4, fileDescription);
			setColumnValueForStringArray(sheet, rowIndex, 5, screenshotDescription);
			setColumnValueForStringArray(sheet, rowIndex, 6, codeDescription);
			setColumnValueForStringArray(sheet, rowIndex, 7, testcaseDescription);
			setColumnValueForStringArray(sheet, rowIndex, 8, colorDescription);
		}

		XSSFSheetConditionalFormatting sheetcf = sheet.getSheetConditionalFormatting();
		// ���Ǻμ��� ������
		
		{
			XSSFConditionalFormattingRule rule = sheetcf.createConditionalFormattingRule(ComparisonOperator.EQUAL, "9");
			XSSFPatternFormatting patternFormat = rule.createPatternFormatting();
			patternFormat.setFillBackgroundColor(IndexedColors.RED.index);
			// ���Ǻ� ���� ����

			CellRangeAddress[] arr = { new CellRangeAddress(1, map.size(), args.length, args.length + testDatas.size() - 1) };
			sheetcf.addConditionalFormatting(arr, rule);
			// ���Ǻ� ���� �Է�
		} // �׽�Ʈ ���̽��� ���� ���� �Է�
		
		{
			XSSFConditionalFormattingRule rule = sheetcf.createConditionalFormattingRule(ComparisonOperator.NOT_EQUAL, "7");
			XSSFPatternFormatting patternFormat = rule.createPatternFormatting();
			patternFormat.setFillBackgroundColor(IndexedColors.LIGHT_ORANGE.index);
			// ���Ǻ� ���� ����

			CellRangeAddress[] arr = { new CellRangeAddress(1, map.size(), args.length, args.length + testDatas.size() - 1) };
			sheetcf.addConditionalFormatting(arr, rule);
			// System.out.println("��Ȳ=" + arr[0].formatAsString());
			// ���Ǻ� ���� �Է�
		} // �׽�Ʈ ���̽��� ��Ȳ ���� �Է�

		{
			XSSFConditionalFormattingRule rule = sheetcf.createConditionalFormattingRule(ComparisonOperator.NOT_EQUAL, "\"���� ä��\"");
			XSSFPatternFormatting patternFormat = rule.createPatternFormatting();
			patternFormat.setFillBackgroundColor(new XSSFColor(new Color(146, 208, 80)));
			// ���Ǻ� ���� ����

			CellRangeAddress[] arr = { new CellRangeAddress(1, map.size(), args.length + testDatas.size(), args.length + testDatas.size()) };
			sheetcf.addConditionalFormatting(arr, rule);
			// System.out.println("�ʷ�=" + arr[0].formatAsString());
			// ���Ǻ� ���� �Է�
		}
		OJApi.printSwagWithStars("���Ǻ� ���� ���� �Ϸ�", 50);

		File excelFile = new File(config.getString("ä��.�������"));
		if (excelFile.exists())
			excelFile.delete();
		workBook.write(new FileOutputStream(excelFile));
		workBook.close();
	}

	public static long getTotalScore(StudentHW std) {
		long l = 0;
		l *= 10;
		l += std.homeworkFileScore;

		l *= 10;
		l += std.screenshotScore;

		l *= 10;
		l += std.sourcecodeScore;

		for (int i = 0; i < std.testcaseScore.length; i++) {
			l *= 10;
			l += std.testcaseScore[i];
		}
		return l;
	}

	public static void setColumnValueForStringArray(XSSFSheet sheet, int rowIndex, int columnIndex, String[] arr) {
		for (int i = 0; i < arr.length; i++) {
			XSSFRow row = sheet.getRow(rowIndex + i);
			XSSFCell cell = row.createCell(columnIndex);
			cell.setCellValue(arr[i]);
		}
	}

	public static int setCellValueForObject(XSSFRow row, int columnIndex, Object obj) {
		if (obj instanceof int[]) {
			int[] arr = (int[]) obj;
			for (int i = 0; i < arr.length; i++) {
				row.createCell(columnIndex).setCellValue(arr[i]);
				columnIndex++;
			}
		} else if (obj instanceof String[]) {
			String[] arr = (String[]) obj;
			for (int i = 0; i < arr.length; i++) {
				row.createCell(columnIndex).setCellValue(arr[i]);
				columnIndex++;
			}
		} else if (obj instanceof Double) {
			row.createCell(columnIndex).setCellValue((double) obj);
			columnIndex++;
		} else if (obj instanceof String) {
			row.createCell(columnIndex).setCellValue((String) obj);
			columnIndex++;
		} else if (obj instanceof Integer) {
			row.createCell(columnIndex).setCellValue((int) obj);
			columnIndex++;
		} else if (obj instanceof Long) {
			row.createCell(columnIndex).setCellValue((long) obj);
			columnIndex++;
		}
		return columnIndex;
	}

	public static boolean IS_DEBUG = false;

	/**
	 * �л� 1�� ��ü üũ
	 * 
	 * @param filePath ���� ��ġ
	 * @param testData �׽�Ʈ ������ ����
	 * @param outputException ä���� ������ ���ڵ�
	 * @return ä���ڵ�
	 * @throws IOException
	 */
	public static int check(String filePath, TestData testData, List<String> outputException, List<String> output) throws IOException {
		Process pc = Runtime.getRuntime().exec("python \"" + filePath + "\"");

		OutputStream os = pc.getOutputStream();
		InputStream is = pc.getInputStream();
		InputStream es = pc.getErrorStream();

		for (int i = 0; i < testData.input.size(); i++) {
			os.write((testData.input.get(i) + "\n").getBytes());
			if (IS_DEBUG)
				System.out.println("[Std in] " + testData.input.get(i));
		}
		os.flush();
		os.close();
		// input ������ �Է�

		List<String> outLines = new ArrayList<>();
		List<String> errLines = new ArrayList<>();
		{
			String line;
			BufferedReader brCleanUp = new BufferedReader(new InputStreamReader(is));
			while ((line = brCleanUp.readLine()) != null) {
				output.add(line);
				outLines.add(OJApi.stringLineConvertToException(line, outputException, false));// Ű����
																								// ����
				if (IS_DEBUG)
					System.out.println("[Stdout] " + line);
			}
			brCleanUp.close();
			// output �Է¹ޱ�

			brCleanUp = new BufferedReader(new InputStreamReader(es));
			while ((line = brCleanUp.readLine()) != null) {
				errLines.add(line);
				if (IS_DEBUG)
					System.out.println("[Stderr] " + line);
			}
			brCleanUp.close();
			// ���� �޼��� �Է¹ޱ�
		}

		boolean isLowChecked = false;
		boolean isHighChecked = false;
		boolean isEqualChecked = false;

		isLowChecked = listContainsString(outLines, testData.outputLow);
		isHighChecked = listContainsString(outLines, testData.outputHigh);
		isEqualChecked = listEqualsString(outLines, testData.outputEqual);

		int code = 0;
		if (isLowChecked)
			code += 100;
		if (isHighChecked)
			code += 10;
		if (isEqualChecked)
			code += 1;
		// code - low/high/equal ���� 1�ڸ��� ����

		if (errLines.size() >= 1) {
			// python ����
			return 8;
		} else if (code == 000) {
			return 0;
		} else if (code == 001) {
			return 1;
		} else if (code == 010) {
			return 2;
		} else if (code == 011) {
			return 3;
		} else if (code == 100) {
			return 4;
		} else if (code == 101) {
			return 5;
		} else if (code == 110) {
			return 6;
		} else if (code == 111) {
			return 7;
		} else {
			return 9;
		}
	}

	/**
	 * outLines�ȿ� testData�� ������� �ִ��� �˻�
	 * 
	 * @param outLines �˻�� ���� ����Ʈ
	 * @param testData �˻��� ���� ����Ʈ
	 * @return contains ����
	 */
	public static boolean listContainsString(List<String> outLines, List<String> testData) {
		if (testData.size() == 0) {
			return true;
		}

		boolean isContains = false;
		int index = 0;
		for (String line : outLines) {
			String compareStr = testData.get(index);
			if (line.contains(compareStr)) {
				index++;
			}
			if (index == testData.size()) {
				isContains = true;
				break;
			}
		}
		return isContains;
	}

	/**
	 * outLines�ȿ� testData�� ������� �ִ��� �˻�
	 * 
	 * @param outLines �˻�� ���� ����Ʈ
	 * @param testData �˻��� ���� ����Ʈ
	 * @return equals ����
	 */
	public static boolean listEqualsString(List<String> outLines, List<String> testData) {
		if (testData.size() == 0) {
			return true;
		}

		boolean isEqual = false;
		int index = 0;
		for (String line : outLines) {
			String compareStr = testData.get(index);
			if (line.equals(compareStr)) {
				index++;
			}
			if (index == testData.size()) {
				isEqual = true;
				break;
			}
		}
		return isEqual;
	}
}
