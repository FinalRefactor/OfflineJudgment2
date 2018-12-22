package kr.dpmc.offlinejudgment.main;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.poi.POIXMLProperties;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import kr.dpmc.offlinejudgment.MetaDataHW;
import kr.dpmc.offlinejudgment.MetaDataHW.File2;
import kr.dpmc.offlinejudgment.OJApi;
import kr.dpmc.offlinejudgment.YamlConfiguration;

public class HomeworkNormalization {

	public static void Normalization(YamlConfiguration config, int classNumber) throws Exception {
		File downloads = new File(config.getString("����ȭ.�ٿ�ε�����"));
		// File normalizationDownloads = new File(config.getString("����ȭ.�ٿ�ε���������"));
		File normalizationScore = new File(config.getString("ä��.��������"));
		List<MetaDataHW> metaDatas = new LinkedList<>();
		List<File> metaFiles = new LinkedList<>();

		loadHomeworkMetaData(metaDatas, metaFiles, downloads, normalizationScore, config);
		OJApi.printSwagWithStars("��Ÿ ���� �ҷ����� ����", 50);
		// ��Ÿ ���ϸ� metaDatas�� �ҷ���

		// homeworkFileNormalization(metaDatas, normalizationDownloads);
		homeworkFileNormalizationWithUnzip(metaDatas, normalizationScore);
		OJApi.printSwagWithStars("���� ���� ����ȭ �Ϸ�", 50);
		// �ٿ�ε����������� �������� �����ϰ� �̸� ����ȭ ��

		addNotAssignmentStudents(metaDatas, classNumber, config);
		OJApi.printSwagWithStars("�������� ��� Ȯ��", 50);
		// �������� ��� metaDatas�� ����

		OJApi.printSwagWithStars("���� ���Ϸ� �������� ����", 50);
		SummarizeMetaDatas(metaDatas, config);
		OJApi.printSwagWithStars("���� ������ ��� ��� ���� ����", 50);
		// ���� ��Ÿ ������ ������ ����ؼ� ����
	}

	public static void loadHomeworkMetaData(List<MetaDataHW> metaDatas, List<File> metaFiles, File downloads, File normalizationDownloads, YamlConfiguration config) {
		if (!downloads.exists())
			downloads.mkdirs();
		if (!normalizationDownloads.exists())
			normalizationDownloads.mkdirs();
		// ���� ������ ����

		for (File file : downloads.listFiles()) {
			if (file.getName().matches("^[\\s\\w��-�R]+_\\d+_Ȯ��_\\d{4}-\\d{2}-\\d{2}-\\d{2}-\\d{2}-\\d{2}.txt")) {
				metaFiles.add(file);
				metaDatas.add(new MetaDataHW(file));
			}
		}
	}

	public static void homeworkFileNormalization(List<MetaDataHW> metaDatas, File normalizationDownloads) throws Exception {
		for (MetaDataHW meta : metaDatas) {
			if (meta.files != null) {
				if (meta.files.size() == 1) {
					File2 f2 = meta.files.get(0);
					String extension = OJApi.getFileExtension(f2.original.getName());
					File to = new File(normalizationDownloads, meta.id + " " + meta.name + "." + extension);
					File source = f2.newer;
					OJApi.fileCopy(source, to);
				} else if (meta.files.size() >= 2) {
					File toParent = new File(normalizationDownloads, meta.id + " " + meta.name);
					if (!toParent.exists()) {
						toParent.mkdirs();
					}
					for (File2 f2 : meta.files) {
						File to = new File(toParent, f2.original.getName());
						File source = f2.newer;
						OJApi.fileCopy(source, to);
					}
				} else {
					OJApi.printSwagWithAccent(meta.id + " " + meta.name + " ���� ���� ���� 0����");
				}
			} // files null�̸� ��Ÿ ���� ������ ó�� �Ұ�
		}
	}// ������ ��Ǯ��

	public static int moveFileWithUnzip(File older, File folder) throws Exception {
		// older�� zip�����̶�� newer������ �ƴ�, newer������ ����Ǯ��
		int count = 0;// �ű� ���� ����

		count += OJApi.unZipIt(older.getPath(), folder.getPath());
		for (File file : folder.listFiles()) {
			if (file.getName().endsWith(".zip")) {
				String newFolderName = file.getName().substring(0, file.getName().lastIndexOf('.'));
				File newFolder = new File(newFolderName);
				newFolder.mkdirs();
				count += moveFileWithUnzip(file, newFolder);
			}
		}

		return count;
	}

	public static void homeworkFileNormalizationWithUnzip(List<MetaDataHW> metaDatas, File normalizationDownloads) throws Exception {
		for (MetaDataHW meta : metaDatas) {
			if (meta.files != null) {
				if (meta.files.size() >= 1) {
					try {
						File toParent = new File(normalizationDownloads, meta.id + " " + meta.name);
						if (!toParent.exists()) {
							toParent.mkdirs();
						} // �й� �̸� ���� ����

						if (meta.files.size() == 1) {
							meta.isSubmitFileZiped = meta.files.get(0).newer.getName().endsWith(".zip");
						} // �������Ϸ� �����ߴ��� �˻�

						for (File2 f2 : meta.files) {
							if (f2.newer.getName().endsWith(".zip")) {
								int count = moveFileWithUnzip(f2.newer, toParent);
								if (count == 0) {
									count = 1;
								}
								meta.outputFilesCount += count;
								// zip�����̸� ����Ǯ��
							} else {
								OJApi.fileCopy(f2.newer, new File(toParent, f2.original.getName()));
								meta.outputFilesCount += 1;
								// �ƴϸ� �׳� ���� ī��
							}

							// ������ ���Ͽ��� ä�������� �̵�
						}
					} catch (Exception e) {
						OJApi.printSwagWithAccent(meta.id + " " + meta.name + " ���� ���� ���� ���� ���� ���� �߻�: \"" + meta.id + " " + meta.name + "\"������ �״�� �Ű���ϴ�.");
						File folder = new File(normalizationDownloads.getPath(), meta.id + " " + meta.name);
						folder.mkdirs();
						for (File2 file : meta.files) {
							OJApi.fileCopy(file.newer, new File(folder, file.original.getName()));
						}
					}

				} else {
					OJApi.printSwagWithAccent(meta.id + " " + meta.name + " ���� ���� ���� 0����");
				}
			} // files null�̸� ��Ÿ ���� ������ ó�� �Ұ�
		}
	}// ���൵ Ǯ��

	public static void addNotAssignmentStudents(List<MetaDataHW> metaDatas, int classNumber, YamlConfiguration config) {
		if (classNumber > 0) {
			// classnum�� 0,-1�̸� �� ���� �Ѿ

			List<String[]> studentList = OJApi.getStudentList(config, classNumber);

			for (int i = 0; i < studentList.size(); i++) {
				String[] args = studentList.get(i);
				String id = args[0];
				String name = args[1];
				// System.out.println(id + " " + name + " �⼮�ο��� ����");

				boolean isContains = false;
				for (MetaDataHW meta : metaDatas) {
					if (meta.id.equals(id))
						isContains = true;
				} // ���� ��Ÿ �����Ϳ� ��ġ���� �˻�

				if (!isContains) {
					metaDatas.add(new MetaDataHW(id, name));
				} // ���� ��Ÿ �����Ϳ� �����Ƿ� ������
			}
			metaDatas.sort(OJApi.comparatorMeta);// �й������� �ø����� �����ϴ°�
		}
	}

	public static void SummarizeMetaDatas(List<MetaDataHW> metaDatas, YamlConfiguration config) throws Exception {
		File excelFile = new File(config.getString("����ȭ.�������"));
		if (excelFile.exists()) {
			if (!excelFile.delete()) {
				OJApi.printSwagWithAccent("������� ������ �Ұ����մϴ�.");
			}
		}

		XSSFWorkbook workBook = new XSSFWorkbook();
		POIXMLProperties xmlProps = workBook.getProperties();
		POIXMLProperties.CoreProperties coreProps = xmlProps.getCoreProperties();
		coreProps.setCreator("DPmc");
		// ���� ����

		XSSFSheet sheet = workBook.createSheet("meta");
		int rowIndex = 0;
		XSSFRow row = sheet.createRow(rowIndex);
		XSSFCell cell;
		String[] args = new String[] { "�̸�", "����", "���� ����", "zip���⿩��", "���� ��¥", "���" };
		// ���⳯¥�� �����ʵ�� ������
		for (int i = 0; i < args.length; i++) {
			cell = row.createCell(i);
			cell.setCellValue(args[i]);
		} // �� ���� ������ �Է�

		int num = 0;
		for (int i = 0; i < metaDatas.size(); i++) {
			MetaDataHW meta = metaDatas.get(i);
			row = sheet.createRow(i + 1);
			row.createCell(0).setCellValue(Integer.valueOf(meta.id));
			row.createCell(1).setCellValue(meta.name);
			row.createCell(2).setCellValue(meta.outputFilesCount);
			row.createCell(3).setCellValue(meta.isSubmitFileZiped);
			row.createCell(4).setCellValue(meta.submitDate);
			for (int j = 0; j < meta.comment.size(); j++) {
				row.createCell(5 + j).setCellValue(meta.comment.get(j));
			}
			num++;
			if (num % 10 == 0) {
				OJApi.printSwagWithStars(num + "�� �ۼ� �Ϸ�", 50);
			}
		} // �л��� ������ �Է�

		try {
			workBook.write(new FileOutputStream(excelFile));
			workBook.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
