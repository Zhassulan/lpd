package kz.ugs.lpd.services;

import java.util.Date;

import org.hibernate.Session;

import kz.ugs.lpd.models.TaskModel;
import kz.ugs.lpd.services.HibernateUtil;

public class TaskService {

	private TaskModel task;

	public void createTask(String fromHost) {
		task = new TaskModel();
		task.setFromHost(fromHost);
		task.setCreated(new Date());
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		session.save(task);
		session.getTransaction().commit();
		HibernateUtil.getSessionFactory().close();
	}

	public TaskModel getTask() {
		return task;
	}

	public void setTask(TaskModel task) {
		this.task = task;
	}
}
