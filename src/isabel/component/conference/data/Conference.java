package isabel.component.conference.data;

import isabel.component.conference.data.IsabelMachine.Cloud;
import isabel.component.conference.ist.SessionType;
import isabel.component.conference.util.ConfigurationParser;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.annotations.Proxy;

/**
 * Clase que representa una conferencia (evento) de Isabel. En la realidad una de estas conferencias tendr� un
 * nombre de evento, y se dividir� en una serie de sesiones m�s o menos granularizadas. Cada una de estas sesiones
 * tiene una hora de inicio y una hora de fin.
 * Ademas este objeto es el que nos indica si queremos grabar o emitir por streaming una sesi�n.
 * 
 * @author jcervino@dit.upm.es
 *
 */
/**
 * @author jcervino@dit.upm.es
 * 
 */
/**
 * @author javi
 *
 */
@XmlRootElement(name = "event")
@XmlAccessorType(XmlAccessType.FIELD)
@Entity
@Proxy(lazy = false)
public class Conference implements Serializable {

	/**
	 * Indican las diferentes posiciones que puede tomar una nueva sesion al ser a�adida
	 * 
	 * @author jcervino@dit.upm.es
	 */
	@XmlTransient
	public enum SESSION_POSITION {
		/**
		 * La sesion va antes que todas las demas
		 */
		BEFORE,
		/**
		 * La sesion va entre otras dos
		 */
		IN, 
		/**
		 * La sesion solapa con las sesiones existentes
		 */
		OVER,
		/**
		 * La sesion va detras de las demas
		 */
		AFTER, 
		/**
		 * La sesion es unica. No hay mas.
		 */
		UNIQUE
	};
	
	@XmlTransient
	public enum FLASH_CODEC {
		H263,
		H264
	}

	/**
	 * Version
	 */
	private static final long serialVersionUID = -7278959374995431778L;

	/**
	 * Id de la sala en la tabla, necesario para hibernate
	 */
	@XmlElement(name = "id")
	private Long id;

	/**
	 * Nombre de la conferencia que la identifica
	 */
	@XmlElement(name = "name")
	private String name;
	
	/**
	 * Sesiones que forman parte de esta conferencia
	 */
	@XmlTransient
	private List<Session> session;

	/**
	 * 
	 */
	@XmlElement(name = "web-url")
	private String webURL;

	/**
	 * 
	 */
	@XmlElement(name = "sip-url")
	private String sipURL;

	/**
	 * 
	 */
	@XmlElement(name = "isabel-url")
	private String isabelURL;
	
	/**
	 * 
	 */
	@XmlElement(name = "httplivestreaming-url")
	private String httplivestreamingURL;
	
	/**
	 * 
	 */
	@XmlElement(name = "isabel-bw")
	private String isabelBW;
	
	/**
	 * 
	 */
	@XmlElement(name = "web-bw")
	private String webBW;
	
	/**
	 * 
	 */
	@XmlElement(name = "recording-bw")
	private String recordingBW;
	
	/**
	 * 
	 */
	@XmlElement(name = "web-codec")
	private FLASH_CODEC webCodec = FLASH_CODEC.H263;
	
	/**
	 * 
	 */
	@XmlElement(name = "recording-codec")
	private FLASH_CODEC recordingCodec = FLASH_CODEC.H264;

	/**
	 * 
	 */
	@XmlElement(name = "enable-web")
	private boolean enableWeb;

	/**
	 * 
	 */
	@XmlElement(name = "enable-sip")
	private boolean enableSIP;

	/**
	 * 
	 */
	@XmlElement(name = "enable-isabel")
	private boolean enableIsabel;
	
	/**
	 * 
	 */
	@XmlElement(name = "enable-httplivestreaming")
	private boolean enableHttplivestreaming;

	/**
	 * 
	 */
	@XmlElement(name = "httplivestreaming-bw")
	private String httplivestreamingBW;
	
	/**
	 * 
	 */
	@XmlElement(name = "path")
	private String path;
	
	@XmlElement(name = "chat-url")
	private String chatURL;

	/**
	 * Maquina de Isabel reservada.
	 */
	@XmlTransient
	private List<IsabelMachine> isabelMachines = new ArrayList<IsabelMachine>();

	/**
	 * Tipo de conferencia.
	 */
	@XmlElement(name = "mode")
	private String type;

	@XmlElement(name = "initDate", required = true)
	private Date startTime;
	
	@XmlElement(name = "endDate", required = true)
	private Date stopTime;
	
	/**
	 * Crea la nueva sala
	 * 
	 * @param name
	 *            Nombre de la sala
	 */
	public Conference(String name) {
		this.name = name;
	}

	/**
	 * Crea una nueva sala
	 * 
	 */
	public Conference() {

	}

	/**
	 * Obtiene el identificador �nico de la conferencia
	 * 
	 * @return
	 */
	@Id
	@GeneratedValue
	@Column(name = "CONFERENCE_ID")
	public Long getId() {
		return id;
	}

	/**
	 * Establece el identificador �nico de la conferencia.
	 * 
	 * @param id
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * Devuelve el nombre de la sala
	 * 
	 * @return Nombre de la sala que la identifica frente a las demas
	 */
	@Column(name = "NAME", nullable = false)
	public String getName() {
		return name;
	}

	/**
	 * Asigna el nombre de la sala
	 * 
	 * @param name
	 *            nombre de la sala
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Devuelve la lista de sesiones que forman parte de la conferencia.
	 * 
	 * @return
	 */
	@OneToMany(targetEntity = Session.class, mappedBy = "conference", cascade = {
			CascadeType.PERSIST, CascadeType.MERGE }, fetch = FetchType.EAGER)
	@JoinColumn(name = "CONFERENCE_ID")
	public List<Session> getSession() {
		return session;
	}

	/**
	 * Establece la lista de sesiones que forman parte de la conferencia.
	 * 
	 * @param session
	 */
	public void setSession(List<Session> session) {
		this.session = session;
	}

	/**
	 * Devuelve una lista de maquinas de Isabel que ser�n utilizadas en esta
	 * conferencia.
	 * 
	 * @return
	 */
	@OneToMany(targetEntity = IsabelMachine.class, mappedBy = "conference", cascade = {
		CascadeType.PERSIST, CascadeType.MERGE }, fetch = FetchType.LAZY)
	@JoinColumn(name = "CONFERENCE_ID")
	public List<IsabelMachine> getIsabelMachines() {
		return this.isabelMachines;
	}

	/**
	 * Establece una nueva lista de m�quinas de Isabel que ser�n utilizadas en
	 * la conferencia.
	 * 
	 * @param isabelMachines
	 */
	public void setIsabelMachines(List<IsabelMachine> isabelMachines) {
		this.isabelMachines = isabelMachines;
	}

	/**
	 * Devuelve el tipo de conferencia.
	 * 
	 * @return
	 */
	@Column(name = "TYPE", nullable = false)
	public String getType() {
		return type;
	}

	/**
	 * Estable el tipo de conferencia de Isabel.
	 * 
	 * @param type
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * Devuelve la hora de inicio teniendo en cuenta el margen dado.
	 * 
	 * @return
	 */
	@Column(name = "START_TIME", nullable = false)
	public Date getStartTime() {
		return startTime;
	}
	
	/**
	 * Establece la fecha de inicio del evento.
	 * @param startTime
	 */
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
	
	@Transient
	public Date getResourcesStartTime() {
		Date newStart = new Date(startTime.getTime() - ConfigurationParser.marginConferenceSession);
		
		return newStart;
	}
	
	/**
	 * Devuelve la hora de parada teniendo en cuenta el margen dado.
	 * 
	 * @return
	 */
	@Column(name = "STOP_TIME", nullable = false)
	public Date getStopTime() {
		return stopTime;
	}
	
	/**
	 * Establecen el nuevo tiempo de fin de sesion.
	 * @param stopTime
	 */
	public void setStopTime(Date stopTime) {
		this.stopTime = stopTime;
	}
	
	@Transient
	public Date getResourcesStopTime() {
		return stopTime;
	}

	/**
	 * Indica si se grabar� la conferencia.
	 * 
	 * @return
	 */
	@Transient
	public boolean isRecordingEnabled() {
		for (Session session : getSession()) {
			if (session.isAutomaticRecord())
				return true;
		}
		return false;
	}

	/**
	 * Indicar� si se dar� por streaming la conferencia.
	 * 
	 * @return
	 */
	@Transient
	public boolean isStreamingEnabled() {
		for (Session session : getSession()) {
			if (session.getEnableStreaming())
				return true;
		}
		return false;
	}

	/**
	 * A�ade una nueva m�quina de Isabel de la conferencia.
	 * 
	 * @param machine
	 */
	public void addMachine(IsabelMachine machine) {
		isabelMachines.add(machine);
	}

	/**
	 * Quita la utilizaci�n de una de las m�quinas de Isabel.
	 * 
	 * @param machine
	 */
	public void removeMachine(IsabelMachine machine) {
		isabelMachines.remove(machine);
	}

	/**
	 * Compara la conferencia con otra.
	 * 
	 * @param conference
	 * @return true si son iguales.
	 */
	@Transient
	public boolean equals(Conference conference) {
		if (this.getName().equals(conference.getName())
				&& this.getType().equals(conference.getType())
				&& this.getStartTime().equals(conference.getStartTime())
				&& this.getStopTime().equals(conference.getStopTime())) {
			return true;
		} else
			return false;
	}

	public void addSession(Session confSession) {
		this.session.add(confSession);
	}

	@Column(name = "WEB_URL")
	public String getWebURL() {
		return webURL;
	}

	public void setWebURL(String webURL) {
		this.webURL = webURL;
	}

	@Column(name = "SIP_URL")
	public String getSipURL() {
		return sipURL;
	}

	public void setSipURL(String sipURL) {
		this.sipURL = sipURL;
	}

	@Column(name = "ISABEL_URL")
	public String getIsabelURL() {
		return isabelURL;
	}

	public void setIsabelURL(String isabelURL) {
		this.isabelURL = isabelURL;
	}
	
	@Column(name = "HTTPLIVESTREAMING_URL")
	public String getHTTPLiveStreamingURL() {
		return httplivestreamingURL;
	}
	
	public void setHTTPLiveStreamingURL(String url) {
		this.httplivestreamingURL = url;
	}

	@Column(name = "ENABLE_WEB", nullable = false)
	public boolean getEnableWeb() {
		return enableWeb;
	}

	public void setEnableWeb(boolean enable) {
		this.enableWeb = enable;
	}

	@Column(name = "ENABLE_SIP", nullable = false)
	public boolean getEnableSIP() {
		return enableSIP;
	}

	public void setEnableSIP(boolean enable) {
		this.enableSIP = enable;
	}

	@Column(name = "ENABLE_ISABEL", nullable = false)
	public boolean getEnableIsabel() {
		return enableIsabel;
	}

	public void setEnableIsabel(boolean enable) {
		this.enableIsabel = enable;
	}
	
	@Column(name = "ENABLE_HTTPLIVESTREAMING", nullable = false)
	public boolean getEnableHTTPLiveStreaming() {
		return enableHttplivestreaming;
	}

	public void setEnableHTTPLiveStreaming(boolean enable) {
		this.enableHttplivestreaming = enable;
	}
	
	@Column(name = "ISABEL_BW", nullable = true)
	public void setIsabelBW(String bw) {
		this.isabelBW = bw;
	}
	
	public String getIsabelBW() {
		return isabelBW;
	}
	
	@Column(name = "WEB_BW", nullable = true)
	public void setWebBW(String bw) {
		this.webBW = bw;
	}
	
	public String getWebBW() {
		return webBW;
	}
	
	@Column(name = "STREAMING_BW", nullable = true)
	public void setRecordingBW(String bw) {
		this.recordingBW = bw;
	}
	
	public String getRecordingBW() {
		return recordingBW;
	}
	
	@Column(name = "HTTPLIVESTREAMING_BW", nullable = true)
	public void setHTTPLiveStreamingBW(String bw) {
		this.httplivestreamingBW = bw;
	}
	
	public String getHTTPLiveStreamingBW() {
		return httplivestreamingBW;
	}
	
	@Column(name = "WEB_CODEC", nullable = true)
	public void setWebCodec(FLASH_CODEC codec) {
		this.webCodec = codec;
	}
	
	public FLASH_CODEC getWebCodec() {
		return webCodec;
	}
	
	@Column(name = "STREAMING_CODEC", nullable = true)
	public void setRecordingCodec(FLASH_CODEC codec) {
		this.recordingCodec = codec;
	}
	
	public FLASH_CODEC getRecordingCodec() {
		return recordingCodec;
	}
	
	@Column(name = "PATH")
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
	
	@Transient
	public SessionType getSessionType() {
		SessionType type = SessionType.TELEMEETING;
		if (getType().equals("meeting")) {
			type = SessionType.TELEMEETING;
		} else if (getType().equals("conference")) {
			type = SessionType.TELECONFERENCE;
		} else if (getType().equals("class")) {
			type = SessionType.TELECLASS;
		}
		return type;
	}
	
	@Transient 
	public Cloud getCloud() {
		List<IsabelMachine> machines = getIsabelMachines();
		Cloud cloud = null;
		if (machines != null && machines.size() > 0) {
			for (IsabelMachine machine : machines) {
				switch(machine.getType()) {
				case Isabel:
				case VNC:
					cloud = machine.getCloud();
					return cloud;
				}
			}
		}
		
		return cloud;
	}
	
	@Override
	public String toString(){
		String result = "Conference ";
		result += name + ": ";
		result += "Type " + this.type + "; ";
		result += "StartDate " + this.getResourcesStartTime() + "; ";
		result += "StopDate " + this.getResourcesStopTime() + "; ";
		result += "Path " + this.path + "; ";
		if (enableIsabel) {
			result += "IsabelBW " + this.isabelBW + "; ";
		}
		if (enableWeb) {
			result += "WebBW " + this.webBW + "; ";
			result += "WebCodec " + this.webCodec + "; ";
		}
		result += "RecordingBW " + this.recordingBW + "; ";
		result += "RecordingCodec " + this.recordingCodec + "; ";
		result += "EnableSIP " + this.enableSIP + "; ";
		if (enableHttplivestreaming) {
			result += "EnableHTTPLiveStreamingBW " + this.httplivestreamingBW + "; ";
		}
		
		return result;
	}

	public void setChatURL(String chatURL) {
		this.chatURL = chatURL;
	}

	@Column(name = "CHAT")
	public String getChatURL() {
		return chatURL;
	}
	
}