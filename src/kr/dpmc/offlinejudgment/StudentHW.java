package kr.dpmc.offlinejudgment;

import java.io.File;
import java.util.List;

/**
 * �л� ������ �����ϴ� ��ü
 */
public class StudentHW {

	/**
	 * �̸�
	 */
	public String name = null;
	
	/**
	 * �й�
	 */
	public String id = null;
	
	/**
	 * ���� �̸�
	 */
	public String fileName = null;
	
	/**
	 * ���� ��ü
	 */
	public File hwfile = null;
	
	/**
	 * ��ũ���� ���ϵ�
	 */
	public List<File> screenshotFiles = null;
	
	/**
	 * ��ũ���� �����ڵ�
	 */
	public int screenshotScore = 0;
	
	/**
	 * ������������ �����ڵ�
	 */
	public int homeworkFileScore = 0;
	
	/**
	 * �ҽ����� �����ڵ�
	 */
	public int sourcecodeScore = 0;
	
	/**
	 * �׽�Ʈ���̽� ��������
	 */
	public double totalScore = 0;
	
	/**
	 * �׽�Ʈ ���̽��� �����ڵ�
	 */
	public int[] testcaseScore;

	public StudentHW(String name, String id) {
		this.name = name;
		this.id = id;
	}
	
	//�Ϲ��л�:
	//	hwfile = null�� �� ����
	// 	returnCode = null�� �� ����
	//  fileName = "null"�� �� ����
	
	//�й�-�̸�-�����̸�-��������-��������-�ҽ��ڵ�����-�׽�Ʈ���̽���������-�׽�Ʈ���̽���... -������-����-�ڸ�Ʈ
	//��������: ���� ���⿩��
	//��������: �������� ���翩��
	//�ҽ��ڵ�����: �ҽ��ڵ忡 Ư�� ���ڿ� ����
}
