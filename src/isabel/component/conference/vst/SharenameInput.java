package isabel.component.conference.vst;

import java.util.Set;

public class SharenameInput implements PropertyInput {

	private SmbWizard wizard = null;
	private int notInt = -1;
	private String content;

	public SharenameInput(SmbWizard wizard) {
		this.wizard = wizard;
	}

	public String getContent() {
		return content;
	}

	public boolean setContent(String cont) {
		content = cont;
		return true;
	}

	public ErrorMessage control() {
		ErrorMessage em = new ErrorMessage();

		if (getContent().matches(".*[^a-zA-Z_0-9\\$\\-].*")) {
			em
					.addError("You have typed an invalid character in this sharename.");
		}
		if (getContent().length() > 8) {
			em
					.addWarning("Your sharename is longer than 8 charcters. Might not work with older clients!");
		}

		if (getContent().length() == 0) {
			em.addError("Your sharename is exactly 0 bytes long. Gratulation!");
		}

		Set<String> shareNames = wizard.getShareNames();
		int i = 0;
		for (String shareName : shareNames) {
			if ((i != notInt) && (getContent().equals(shareName))) {
				em.addError("This sharename already exists!");
				i = shareNames.size(); // leave the loop
			}
		}

		return em;
	}

}
