package kr.dpmc.offlinejudgment.main;

import java.io.File;

import kr.dpmc.offlinejudgment.OJApi;
import kr.dpmc.offlinejudgment.YamlConfiguration;

public class DeleteFile {

	public static void DeleteProjectFile(YamlConfiguration config, int isDeleteResultFile) throws Exception {
		File normalOriginal = new File(config.getString("����ȭ.�ٿ�ε�����"));
		File check = new File(config.getString("ä��.��������"));
		File codeSummary = new File(config.getString("ä��.�ڵ����ϸ�������"));
		File screenshotSummary = new File(config.getString("ä��.�������ϸ�������"));
		File similarity = new File(config.getString("���絵.��������"));

		deleteSubFiles(normalOriginal);
		normalOriginal.mkdirs();
		OJApi.printSwagWithStars("�ٿ�ε����� ���� �Ϸ�", 50);

		deleteSubFiles(check);
		check.mkdirs();
		OJApi.printSwagWithStars("�������� ���� �Ϸ�", 50);
		
		deleteSubFiles(codeSummary);
		codeSummary.mkdirs();
		OJApi.printSwagWithStars("�ڵ����ϸ������� ���� �Ϸ�", 50);
		
		deleteSubFiles(screenshotSummary);
		screenshotSummary.mkdirs();
		OJApi.printSwagWithStars("�������ϸ������� ���� �Ϸ�", 50);

		deleteSubFiles(similarity);
		similarity.mkdirs();
		OJApi.printSwagWithStars("���絵���� ���� �Ϸ�", 50);

		if (isDeleteResultFile == 1) {
			File parent = new File(".");
			for (File file : parent.listFiles()) {
				if (file.isFile() && (file.getName().endsWith(".xlsx") || file.getName().endsWith(".xls"))) {
					file.delete();
				}
			}
			OJApi.printSwagWithStars("����������� ���� �Ϸ�", 50);
		}
	}

	public static void deleteSubFiles(File file) {
		if (file.exists()) {
			if (file.isDirectory()) {
				for (File sub : file.listFiles()) {
					deleteSubFiles(sub);
				}
				file.delete();
			} else {
				file.delete();
			}
		}
	}
}
