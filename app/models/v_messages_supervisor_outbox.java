package models;

import java.util.Date;
import javax.persistence.Entity;
import play.db.jpa.Model;

@Entity
public class v_messages_supervisor_outbox extends Model {

	private static final long serialVersionUID = -2408472182977463767L;
	public String title;
    public Date time;
    public String receiver_name;
    public long read_count;
    public String read_status;
	
    @Override
	public String toString() {
		return "v_messages_supervisor_outbox [title=" + title + ", time=" + time
				+ ", receiver_name=" + receiver_name + ", read_count=" + read_count
				+ ", read_status=" + read_status + ", id=" + id + "]\n";
	}

}