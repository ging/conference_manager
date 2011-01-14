package isabel.component.conference.persistence;

import org.hibernate.*;
import org.hibernate.cfg.*;

/**
 * Clase que se encarga de generar sesiones contra la base de datos.
 * 
 * @author jcervino@dit.upm.es
 *
 */
public class HibernateUtil {

	/**
	 * Generador de sesiones contra la base de datos
	 */
	private static SessionFactory sessionFactory;
	
	public static boolean debug = false;

	/**
	 * Devuelve una instancia capaz de generar sesiones contra la base de datos.
	 * 
	 * @return
	 * Generador de sesiones contra la base de datos.
	 */
	public static SessionFactory getSessionFactory() {
		if (sessionFactory == null) {
			if (!debug) {
				sessionFactory = new AnnotationConfiguration().configure().buildSessionFactory();
			} else {
				sessionFactory = new AnnotationConfiguration().configure("hibernate-test.cfg.xml").buildSessionFactory();
			}
		}
		return sessionFactory;
	}

}