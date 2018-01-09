package kz.ugs.lpd.models;

import java.util.Date;

/**
 * @author ZTokbayev класс модель для задачи пришедгей на виртуальные принтер
 */
public class TaskModel {

	private int id;
	private String fromHost;
	private Date created;
	private boolean status;

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
