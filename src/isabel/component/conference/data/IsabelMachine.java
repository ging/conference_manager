package isabel.component.conference.data;

import isabel.component.conference.ist.SiteType;

import java.io.Serializable;

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

import org.hibernate.annotations.Proxy;

/**
 * M�quina de Isabel que podr� ser utilizada por distintas conferencias.
 * En principio una m�quina de Isabel s�lo podr� ser utilizada por una conferencia
 * a la vez.
 * 
 * @author jcervino@dit.upm.es
 */
@Entity
@Proxy(lazy=false)
public class IsabelMachine implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7558952158289427622L;

	/**
	 * Tipos de m�quinas de Isabel
	 * 
	 * @author jcervino@dit.upm.es
	 *
	 */
	public static enum Type {Spy, VNC, Isabel};
	
	public static enum Cloud {Hardware, VMWare, Amazon};
	
	public static enum SubType {
		Spy(Type.Spy, Cloud.Hardware), 
		vmVNC(Type.VNC, Cloud.VMWare), 
		amiVNC(Type.VNC, Cloud.Amazon), 
		vmIsabel(Type.Isabel, Cloud.VMWare), 
		amiIsabel(Type.Isabel, Cloud.Amazon);
		
		private Type _type;
		
		private Cloud _cloud;
		
		SubType(Type type, Cloud cloud) {
			_type = type;
			_cloud = cloud;
		}
		
		public Type getType() {
			return _type;
		}
		
		public Cloud getCloud() {
			return _cloud;
		}
	}
		
	public static enum IsabelNodeType {
		MASTER(SiteType.MASTER,false, false),
		MASTER_RECORDING(SiteType.MASTER_FLASH, true, false),
		MASTER_RECORDING_HTTPLIVESTREAMING(SiteType.MASTER_FLASH, true, true),
		GW_WEB(SiteType.FLASH, false, false),
		GW_SIP(SiteType.SIP, false, false),
		SPY(SiteType.SPY, false, false),
		MCU(SiteType.MCU, false, false);
		
		private SiteType type;
		
		private boolean recording;
		
		private boolean httpLiveStreaming;
		
		IsabelNodeType(SiteType type, boolean recording, boolean httpLiveStreaming) {
			this.type = type;
			this.recording = recording;
			this.httpLiveStreaming = httpLiveStreaming;
		}
		
		public SiteType getType() {
			return type;
		}
		
		public boolean isRecording() {
			return recording;
		}
		
		public boolean isHTTPLiveStreaming() {
			return httpLiveStreaming;
		}
	}
		
	/**
	 * Identificador de la m�quina de Isabel
	 */
	private long id;
	
	/**
	 * Lista de conferencias que est�n utilizando la m�quina de Isabel.
	 */
	private Conference conference;
	
	/**
	 * Nombre del host de la m�quina.
	 */
	private String hostname;
	
	/**
	 * Tipo de Isabel que est� instalado en la m�quina.
	 */
	private SubType subType;

	/**
	 * Identificador externo en caso de utilizar sistemas Cloud.
	 */
	private String externalID;
	
	/**
	 * Tipo de Isabel que se esta ejecutando (Master, GWFLASH,...)
	 */
	private IsabelNodeType isabelNodeType;
	
	/**
	 * Instancia un objeto de m�quina de Isabel.
	 */
	public IsabelMachine() {
		
	}

	/**
	 * Obtiene el identificador �nico de la m�quina
	 * @return
	 */
	@Id @GeneratedValue
	@Column (name = "ISABELMACHINE_ID")
	public Long getId() {
		return id;
	}
	
	/**
	 * Establece el identificador �nico de la conferencia.
	 * @param id
	 */
	public void setId(Long id) {
		this.id = id;
	}
	
	/**
	 * Devuelve la lista de sesiones que forman parte de la conferencia.
	 * @return
	 */
	@ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE }, fetch = FetchType.LAZY)
	@JoinColumn(name="CONFERENCE_ID")
	public Conference getConference() {
		return this.conference;
	}
	
	/**
	 * Establece una nueva lista de conferencias que usar�n en alg�n momento
	 * esta m�quina.
	 * @param conferences
	 */
	public void setConference(Conference conference) {
		this.conference = conference;
	}
	
	/**
	 * Devuelve el nombre de la sala
	 * 
	 * @return 
	 * Nombre de la sala que la identifica frente a las demas
	 */
	@Column (name = "HOSTNAME", nullable = true)
	public String getHostname() {
		return this.hostname;
	}
	
	/**
	 * Establece el nombre del Host de la m�quina
	 * @param hostname
	 */
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}
	
	/**
	 * Devuelve el tipo de Isabel instalado en la m�quina.
	 * @return
	 */
	@Column (name = "SUBTYPE", nullable = false)
	public SubType getSubType() {
		return this.subType;
	}
	
	/**
	 * Establece el tipo de m�quina de Isabel
	 * @param isabel_type
	 */
	public void setSubType(SubType isabel_type) {
		this.subType = isabel_type;
	}
	
	/**
	 * Devuelve el tipo de Isabel instalado en la m�quina.
	 * @return
	 */
	@Column (name = "EXTERNALID", nullable = true)
	public String getExternalID() {
		return this.externalID;
	}
	
	/**
	 * Establece el tipo de m�quina de Isabel
	 * @param isabel_type
	 */
	public void setExternalID(String externalID) {
		this.externalID = externalID;
	}

	/**
	 * Devuelve el tipo de nodo de Isabel que se esta 
	 * ejecutando en cada momento.
	 * @return
	 * El tipo de nodo ejecutandose. Puede ser null, Master,
	 * GWFLASH, ....
	 */
	@Column (name = "NODETYPE", nullable = true)
	@Enumerated(value = EnumType.STRING)
	public IsabelNodeType getIsabelNodeType() {
		return isabelNodeType;
	}

	/**
	 * Establece un nuevo tipo de nodo de Isabel.
	 * @param isabelNodeType
	 */
	public void setIsabelNodeType(IsabelNodeType isabelNodeType) {
		this.isabelNodeType = isabelNodeType;
	}

	@Transient
	public Type getType() {
		return this.subType.getType();
	}

	@Transient
	public Cloud getCloud() {
		return this.subType.getCloud();	
	}
	
	public boolean equals(IsabelMachine machine) {
		if (	this.getCloud().equals(machine.getCloud()) &&
				this.getHostname().equals(machine.getHostname()) &&
				this.getSubType().equals(machine.getSubType())) {
			return true;
		} else {
			return false;
		}
	}
}
