package isabel.component.conference.data;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.annotations.Proxy;

/**
 * Clase que representa a una sesi�n de Isabel que se podr� ofrecer por Streaming o grabar.
 * Una sesi�n siempre es parte de una conferencia.
 * 
 * @author jcervino@dit.upm.es
 *
 */
@Entity
@Proxy(lazy=false)
@XmlRootElement ( name = "session" )
@XmlAccessorType(XmlAccessType.FIELD)
public class Session implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 782546652739318304L;
	
	public enum Status {
		INIT("Init"),
		RECORDING("Recording"),
		RECORDED("Recorded"),
		PUBLISHED("Published");
		
		private String name;
		
		Status(String name) {
			this.name = name;
		}
		
		public String toString() {
			return name;
		}
		
		public static Status getStatus(String name) {
			if (name.equals(INIT.toString()))
				return INIT;
			else if (name.equals(RECORDING.toString()))
				return RECORDING;
			else if (name.equals(RECORDED.toString()))
				return RECORDED;
			else if (name.equals(PUBLISHED.toString()))
				return PUBLISHED;
			else return null;
		}
		
	}

	/**
	 * Identificador �nico de la sesi�n en la aplicaci�n.
	 */
	@XmlElement(name="id")
	private long id;
	
	/**
	 * Hora y dia de inicio de la sesi�n
	 */
	@XmlElement(name = "initDate", required = true)
	private long startTime;
	
	/**
	 * Hora y d�a de fin de la sesi�n 
	 */
	@XmlElement(name = "endDate", required = true)
	private long stopTime;
	
	/**
	 * Nombre de la sesi�n 
	 */
	@XmlElement(name = "name", required = true)
	private String name;
	
	/**
	 * Conferencia a la que pertenece esta sesi�n. 
	 */
	@XmlTransient
	private Conference conference;

	/**
	 * Variable que indica si se va a grabar o no la conferencia. 
	 */
	@XmlElement(name = "recording", required = true)
	private boolean enableRecord;
	
	/**
	 * Variable que indica si se va a poder participar en la conferencia.
	 */
	@XmlElement(name = "streaming", required = true)
	private boolean enableStreaming;
	
	@XmlTransient
	private Status status = Status.INIT;

	/**
	 * Inicia el objeto sesi�n.
	 */
	public Session() {
	}
	
	/**
	 * Devuelve el identificador de la sesi�n dentro de la aplicaci�n.
	 * @return
	 * El identificador �nico de la sesi�n.
	 */
	@Id @GeneratedValue
	@Column (name = "SESSION_ID")
	public Long getId() {
		return id;
	}
	
	/**
	 * Establece el identificador de la sesi�n.
	 * @param id
	 */
	public void setId(Long id) {
		this.id = id;
	}
	
	/**
	 * Devuelve el nombre de la sala
	 * 
	 * @return 
	 * Nombre de la sala que la identifica frente a las demas
	 */
	@Column (name = "NAME", nullable = false)
	public String getName()	{
		return name;
	}
	
	/**
	 * Asigna el nombre de la sala
	 * 
	 * @param name
	 * nombre de la sala
	 */
	public void setName(String name){
		this.name = name;		
	}
	
	/**
	 * Devuelve la hora de inicio de la sesion.
	 * 
	 * @return
	 * 	La hora y el dia de inicio de la sesion.
	 */
	@Column (name = "START_TIME", nullable = false)
	public long getStartTime() {
		return startTime;
	}
	
	/**
	 * Establece la hora y el dia de inicio de la sesion.
	 * 
	 * @param startTime
	 * 	La hora y el dia de inicio de la sesion.
	 */
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}
	
	/**
	 * Devuelve la hora de finalizaci�n de la sesi�n.
	 * 
	 * @return
	 * 	La hora y el dia de fin de la sesion.
	 */
	@Column (name = "STOP_TIME", nullable = false)
	public long getStopTime() {
		return stopTime;
	}
	
	public void setStopTime(long stopTime) {
		this.stopTime = stopTime;
	}
	
	@Transient
	public Date getStartDate() {
		Date newDate = new Date(conference.getStartTime().getTime() + getStartTime());
		return newDate;
	}
	
	public void setStartDate(Date startDate) {
		long newTime = startDate.getTime() - conference.getStartTime().getTime();
		setStartTime(newTime);
	}
	
	@Transient
	public Date getStopDate() {
		Date newDate = new Date(conference.getStartTime().getTime() + getStopTime());
		return newDate;
	}
	
	public void setStopDate(Date stopDate) {
		long newTime = stopDate.getTime() - conference.getStartTime().getTime();
		setStopTime(newTime);
	}
	
	/**
	 * Devuelve la conferencia padre de la sesi�n.
	 * 
	 * @return
	 * La conferencia a la que pertenece esta sesion
	 */
	@ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE }, fetch = FetchType.LAZY)
	@JoinColumn(name="CONFERENCE_ID")
    public Conference getConference() {
		return conference;
	}
	
	/**
	 * Establece la conferencia a la que pertenece esta sesi�n.
	 * 
	 * @param conference
	 * Conferencia a la que pertenecer� esta sesi�n.
	 */
	public void setConference(Conference conference) {
		this.conference = conference;
	}
	
	/**
	 * Devuelve si se grabar� la conferencia de Isabel
	 * 
	 * @return
	 * Una variable indicando si se grabar� o no la conferencia
	 */
	@Column (name = "ENABLE_RECORD", nullable = false)
	public boolean isAutomaticRecord() {
		return enableRecord;
	}
	
	/**
	 * Establece si se grabar� o no la conferencia de Isabel
	 * 
	 * @param enableRecord
	 * Una variable indicando si se grabar� o no la conferencia
	 */
	public void setAutomaticRecord(boolean enableRecord) {
		this.enableRecord = enableRecord;
	}
	
	/**
	 * Devuelve si se emitir� la conferencia de Isabel
	 * 
	 * @return
	 * Una variable indicando si se emitir� o no la conferencia
	 */
	@Column (name = "ENABLE_STREAMING", nullable = false)
	public boolean getEnableStreaming() {
		return enableStreaming;
	}
	
	/**
	 * Establece si se emitir� o no la conferencia de Isabel
	 * 
	 * @param enableRecord
	 * Una variable indicando si se emitir� o no la conferencia
	 */
	public void setEnableStreaming(boolean enableStreaming) {
		this.enableStreaming = enableStreaming;
	}
	
	/**
	 * Compara la sesi�n con otra.
	 * @param session
	 * @return
	 * Devuelve true si las sesiones son iguales.
	 */
	@Transient
	public boolean equals(Session session) {
		if (	this.getName().equals(session.getName()) 					&&
				this.getStartDate().equals(session.getStartDate()) 			&&
				this.getStopDate().equals(session.getStopDate())			&&
				this.isAutomaticRecord() == session.isAutomaticRecord()			&&
				this.getEnableStreaming() == session.getEnableStreaming() 	&&
				this.getConference().equals(session.getConference())
				) {
			return true;
		}
		else return false;
	}

	public void setStatus(Status status) {
		this.status = status;
	}
	
	@Column(name="STATUS", nullable=false)
	@Enumerated(value = EnumType.STRING)
	public Status getStatus() {
		return status;
	}


}
