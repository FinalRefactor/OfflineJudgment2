package kr.dpmc.offlinejudgment.main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Scanner;

import org.apache.poi.hssf.record.CFRuleBase.ComparisonOperator;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFConditionalFormattingRule;
import org.apache.poi.xssf.usermodel.XSSFPatternFormatting;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFSheetConditionalFormatting;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import kr.dpmc.offlinejudgment.OJApi;
import kr.dpmc.offlinejudgment.YamlConfiguration;

public class OfflineJudgment {

	// C ä�� ��� �߰��ؾ� ��
	// C ä�� input�� file io ���� �߰�
	// C ä�� output 
	

	public static void main3(String[] args) throws Exception {
		File file = new File("test.xlsx");
		XSSFWorkbook workBook = new XSSFWorkbook(file);
		XSSFSheet sheet = workBook.getSheetAt(0);
		XSSFSheetConditionalFormatting sheetcf = sheet.getSheetConditionalFormatting();
		XSSFConditionalFormattingRule rule = sheetcf.createConditionalFormattingRule(ComparisonOperator.BETWEEN, "1", "G3");
		XSSFPatternFormatting format = rule.createPatternFormatting();
		format.setFillBackgroundColor(IndexedColors.SKY_BLUE.index);

		CellRangeAddress[] arr = { CellRangeAddress.valueOf("A1:F5") };

		sheetcf.addConditionalFormatting(arr, rule);
		workBook.write(new FileOutputStream(new File("test2.xlsx")));
		workBook.close();
		System.out.println("�Ϸ�");
	}

	public static void main2(String[] args) throws Exception {
		File file = new File("test.txt");
		FileOutputStream fos = new FileOutputStream(file);
		OutputStreamWriter osw = new OutputStreamWriter(fos, "Cp949");
		BufferedWriter bw = new BufferedWriter(osw);
		bw.write("aaa\n");
		bw.write("������\n");
		bw.close();
		System.out.println("��");
	}
	

	public static void main(String[] args) throws Exception {
		YamlConfiguration config = new YamlConfiguration("config.yaml");
		Scanner sc = new Scanner(System.in);

		while (true) {
			try {
				if (mainLoop(sc, config)) {
					break;
				}
			} catch (Exception e) {
				System.out.println(" === ���� �߻� ===");
				e.printStackTrace();
			}
		}
		sc.close();
	}

	public static boolean mainLoop(Scanner sc, YamlConfiguration config) throws Exception {
		if (config.getFile().exists()){
			config.reloadYaml();
		}
		ConfigInit(config);
		System.out.println("Build: " + config.getInt("OJ.build"));
		// config ����

		System.out.println("���� ����� �Է����ּ���");
		System.out.println("1. �����̸�����ȭ-> ���� : " + config.getString("����ȭ.�ٿ�ε�����"));
		System.out.println("2. OJ�ý��� ä���ϱ�-> ���� : " + config.getString("ä��.��������"));
		System.out.println("3. ���絵�˻�-> ���� : " + config.getString("���絵.��������"));
		System.out.println("4. ���� �����-> ���� : " + config.getString("����ȭ.�ٿ�ε�����") + ", " + config.getString("ä��.��������") + ", " + config.getString("ä��.�ڵ����ϸ�������") + ", " + config.getString("ä��.�������ϸ�������") + ", " + config.getString("���絵.��������"));
		System.out.println("5. ������ ����� ���� �����-> ���� : " + config.getString("���ε�.���ε�����"));
		System.out.println("6. ����");
		System.out.print("���: ");

		int cmd = sc.nextInt();
		if (cmd == 1) {
			System.out.println("1-2. �й��� �Է����ּ��� (������ 0 �Ǵ� -1)");
			System.out.print("�Է�: ");
			int classNumber = sc.nextInt();
			HomeworkNormalization.Normalization(config, classNumber);
		} else if (cmd == 2) {
			System.out.println("2-2. �й��� �Է����ּ��� (������ 0 �Ǵ� -1)");
			System.out.print("�Է�: ");
			int classNumber = sc.nextInt();
			ScoreCheck.OfflineJudgmentMain(config, classNumber);
		} else if (cmd == 3) {
			SimilirarityCheck.SimiliraritySummary(config);
		} else if (cmd == 4) {
			System.out.println("��� ���� ���ϵ� ���� �����ұ��? (0 �Ǵ� 1)");
			System.out.print("�Է�: ");
			int result = sc.nextInt();
			System.out.println("��¥�� ���ϵ��� �����ұ��? (0 �Ǵ� 1)");
			System.out.print("�Է�: ");
			int isDelete = sc.nextInt();
			if (isDelete == 1) {
				DeleteFile.DeleteProjectFile(config, result);
			} else {
				OJApi.printSwagWithBraket("��� ������ �������� �ʰ� �����մϴ�.");
			}
		} else if (cmd == 5) {
			UploadToBlackboard.writeUploadFile(config);
			// ���ε� ���� ��ġ��
		} else if (cmd == 6) {
			OJApi.printSwagWithBraket("OJ�� �����մϴ�.");
			return true;
		}

		if (1 <= cmd && cmd <= 6) {
			OJApi.printSwagWithBraket("��ɼ��� �Ϸ�");
		} else {
			OJApi.printSwagWithBraket("�߸��� ����Դϴ�");
		}

		System.out.println("***********************************************************");
		return false;
	}

	public static void ConfigInit(YamlConfiguration config) throws Exception {
		int value = config.getInt("OJ.build");
		if (value <= 7) {
			for (int i = 0; i < 11; i++) {
				System.out.println();
			}
			OJApi.printSwag("config ������ ������ �����Դϴ�.", 10, " ===", "===");
			OJApi.printSwag("config�� �� �������� �����ϴ�.", 10, " ===", "===");
			OJApi.printSwag("config ���� ������ �����ϼ���.", 10, " ===", "===");
			for (int i = 0; i < 11; i++) {
				System.out.println();
			}

			config.set("OJ.build", 8);

			ArrayList<String> list;

			config.set("ä��.��������", "hw");
			config.set("ä��.���������", "�׽�Ʈ���̽�");
			config.set("ä��.�ڵ����ϸ�������", "�ڵ����ϸ���");
			config.set("ä��.�������ϸ�������", "�������ϸ���");
			config.set("ä��.�������", "ä�����.xlsx");
			config.set("ä��.�Է�������Ʈ�޼�������", true);

			list = new ArrayList<>();
			list.add(".jpg");
			list.add(".jpeg");
			list.add(".png");
			list.add(".bmp");
			list.add(".hwp");
			list.add(".docx");
			config.set("ä��.�����ν�Ȯ����", list);

			list = new ArrayList<>();
			list.add(" ");
			config.set("ä��.ä�����ܹ���", list);

			list = new ArrayList<>();
			list.add("input");
			list.add("\"");
			config.set("ä��.�ҽ����ϰ˻�", list);

			list = new ArrayList<>();
			list.add("��");
			list.add("ȣ");
			config.set("ä��.�����νĹ���", list);

			list = new ArrayList<>();
			list.add(" ");
			list.add("*");
			config.set("ä��.�����ν����ܹ���", list);
			// ä�� ������

			config.set("���ε�.��������", "ä�����.xlsx");
			config.set("���ε�.���ε�����", "���ε�.xls");
			config.set("���ε�.���ε���������", "���ε�����.xls");

			config.set("���絵.��������", "CodeDistance");
			config.set("���絵.�������", "���絵���.xlsx");
			config.set("���絵.�ּ�����", false);
			config.set("���絵.������", true);
			config.set("���絵.�ٳѱ�����", false);

			list = new ArrayList<>();
			list.add(" ");
			list.add("**");
			config.set("���絵.���ܹ���", list);
			// ���絵 ������

			config.set("����ȭ.�ٿ�ε�����", "�ٿ�ε�");
			// config.set("����ȭ.�ٿ�ε���������", "�ٿ�ε�����");
			config.set("����ȭ.�������", "������.xlsx");
			config.set("����ȭ.�⼮������", "�йݺ��⼮��");

			// ����ȭ ������
			config.saveYaml();
		}
	}

}
