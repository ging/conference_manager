package isabel.component.conference.util;

import java.util.Date;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Clase que envia correo con formato HTML.
 * 
 * @author jcervino@dit.upm.es
 *
 */
public class Mailer {
	/**
	 * Logs
	 */
	protected static Logger log = LoggerFactory.getLogger(Mailer.class);
	
	public static void sendMail(String recipients, String from, String subject, String message, String smtpServer) {
		Properties props = new Properties();
		props.put("mail.smtp.host", smtpServer);
		props.put("mail.from", from);
		Session session = Session.getInstance(props, null);
		
		try {
	        MimeMessage msg = new MimeMessage(session);
	        msg.setFrom();
	        msg.setRecipients(Message.RecipientType.TO,
	                          recipients);
	        msg.setSubject(subject);
	        msg.setSentDate(new Date());
	        msg.setText(message,"UTF-8","html");
	        Transport.send(msg);
	    } catch (MessagingException mex) {
	        log.error("Error sending mail, exception: " + mex);
	    }
	}
	
}
