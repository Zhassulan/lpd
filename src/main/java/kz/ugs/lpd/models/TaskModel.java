package kz.ugs.lpd.models;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * @author ZTokbayev класс модель для задачи пришедгей на виртуальные принтер
 */
@Entity
@Table(name="TASKS")
public class TaskModel {
	
	@Id
	@Column(name="ID")
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private int id;
	
	@Column(name="FROMHOST")
	private String fromHost;
	
	@Column(name="CREATED")
	@GeneratedValue
	private Date created;
	
	@Column(name="STATUS")
	private boolean status;
	
	public TaskModel() {}

	public boolean isStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getFromHost() {
		return fromHost;
	}

	public void setFromHost(String fromHost) {
		this.fromHost = fromHost;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

}
