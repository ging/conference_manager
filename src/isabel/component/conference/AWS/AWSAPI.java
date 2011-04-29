package isabel.component.conference.AWS;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Placement;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.ec2.model.TerminateInstancesResult;


public class AWSAPI {
	
	public static String amazonURL = "https://eu-west-1.ec2.amazonaws.com";
	// private static String IsabelImageID = "ami-a9507add";
	public static String serviceKey = "AKIAJBSUMETBJKCJYG7A";
	public static String serviceSecret = "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";

	protected static Logger log = LoggerFactory.getLogger(AWSAPI.class);
	
	public static List<AWSInstance> startHost(String amiID, String size, int num) {
		// Crea un nuevo host para el amiID asignado.
		//AmazonEC2Config config = new AmazonEC2Config();
		//config.setServiceURL(amazonURL);
		Placement zone = new Placement();
		zone.setAvailabilityZone("eu-west-1a");
		
		AmazonEC2Client service = new AmazonEC2Client(new BasicAWSCredentials(serviceKey, serviceSecret));
		service.setEndpoint(amazonURL);
		RunInstancesRequest request = new RunInstancesRequest();

		request.setImageId(amiID);
		request.setMaxCount(num);
		request.setMinCount(num);
		request.setPlacement(zone);
		request.setKeyName("isabel");
		request.setInstanceType(size);
		request.setDisableApiTermination(false);

		RunInstancesResult response = invokeRunInstances(service, request);
		List<Instance> instancesID = response.getReservation().getInstances();
			//response.getReservation().getReservationId();
		List<String> IDs = new ArrayList<String>();		
		
		for (Instance inst:instancesID){
			IDs.add(inst.getInstanceId());
		}
		List<AWSInstance> instancesToReturn = null;
		boolean finished = false;
		while (!finished) {
			finished = true;
			List<Instance> temp = updateHostIP(IDs);
			List<AWSInstance> instances = new ArrayList<AWSInstance>();
			for (Instance inst : temp) {
				String dns = inst.getPublicDnsName();
				if (dns == null
						|| dns.equals("")) {
					finished = false;
					try {
						Thread.sleep(3000); // Hace una peticion cada 3 segundos.
					} catch (InterruptedException e) {
						log.error(e.getLocalizedMessage());
					}
				}

				else {
					AWSInstance instance = new AWSInstance();
					instance.amiID = amiID;
					instance.instanceID = inst.getInstanceId();
					instance.ipAddress = dns;
					instances.add(instance);
				}

			}
			instancesToReturn = instances;
		}
		return instancesToReturn;
	}
	
	public static List<Instance> updateHostIP(List<String> instancesID) {
		
		
		AmazonEC2Client service = new AmazonEC2Client(new BasicAWSCredentials(serviceKey, serviceSecret));
		service.setEndpoint(amazonURL);
		
		
		DescribeInstancesRequest req = new DescribeInstancesRequest();
		req.setInstanceIds(instancesID);
		
		DescribeInstancesResult response = invokeGetInfo(service, req);
		// DescribeInstancesResult result = response.getDescribeInstancesResult();
		java.util.List<Reservation> reservationList = response.getReservations();
		
		java.util.List<Instance> runningInstanceList = null;
		
		for (Reservation reservation : reservationList) {
					runningInstanceList = reservation
							.getInstances();
//					for (Instance runningInstance : runningInstanceList) {
//						ipAddress = runningInstance.getPublicIpAddress();
//					}
		}	
		return runningInstanceList;
	} 
	
	
	public static void stopHost(List<AWSInstance> instances) {
		
		AmazonEC2Client service = new AmazonEC2Client(new BasicAWSCredentials(serviceKey, serviceSecret));
		// AmazonEC2 service = new AmazonEC2Mock();
		service.setEndpoint(amazonURL);
		
		TerminateInstancesRequest request = new TerminateInstancesRequest();
		//request.setForce(true);
		List<String> instanceIDs = new ArrayList<String>();
		for (AWSInstance inst : instances) {
			instanceIDs.add(inst.instanceID);
		}
		request.setInstanceIds(instanceIDs);

		invokeStopInstances(service, request);
	}
	
	private static RunInstancesResult invokeRunInstances(AmazonEC2Client service,
			RunInstancesRequest request) {
		RunInstancesResult response = service.runInstances(request);

		return response;
	
	}

	private static TerminateInstancesResult invokeStopInstances(AmazonEC2 service,
			TerminateInstancesRequest terminateInstancesRequest) {

		TerminateInstancesResult response = null;
		
		try {

			response = service.terminateInstances(terminateInstancesRequest);

		} catch (AmazonServiceException ex) {

			log.error("Caught Exception: " + ex.getMessage() + "; "
					+ "Response Status Code: " + ex.getStatusCode() + "; "
					+ "Error Code: " + ex.getErrorCode() + "; "
					+ "Error Type: " + ex.getErrorType() + "; "
					+ "Request ID: " + ex.getRequestId() + "; ");
		}
		return response;
	}

	
	private static DescribeInstancesResult invokeGetInfo(AmazonEC2Client service,
			DescribeInstancesRequest req) {
		DescribeInstancesResult response = null;
		response = service.describeInstances(req);
		return response;
	}

}
