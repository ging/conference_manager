package isabel.component.conference.vst;

import java.util.ArrayList;

public class ShareProp {
	
	private ArrayList<Field> fields;
	private String templateFile;
	private String[] lines;
	
	public ShareProp(String... defaultValues) {
		
		SmbWizard wizard = SmbWizard.getInstance();
		Template selectedTpl = new Template("vst/templates/Public_Read_Directory_-_only_read_access_to_all_users.tpl");
		
		this.templateFile=selectedTpl.getFileName();
		parse();
		
		Field field=null;
		String label="";
		
		if ((defaultValues!=null)&&(defaultValues.length < fields.size())){
			System.out.println("Falta informacion : " + defaultValues[0] + " " + defaultValues[1]);
			defaultValues=null;
		}
		
		for (int i=0; i<fields.size(); i++){
			field=(Field)fields.get(i);
			label=field.label;
			if (!field.optional) label=label+"*";
			if (field.type.equals("STRING")){ // Comentario
				StringField sf=new StringField();
				field.input=sf;
				if (defaultValues!=null) field.input.setContent(defaultValues[i]);
			}
			else if (field.type.equals("PATH")){ // Directorio
				FileSelect fs=new FileSelect("",true);
				field.input=fs;
				if (defaultValues!=null) field.input.setContent(defaultValues[i]);
			}
			else if (field.type.equals("SHARENAME")){ // Sharename
				SharenameInput si=new SharenameInput(wizard);
				field.input=si;
				if (defaultValues!=null) field.input.setContent(defaultValues[i]);
			}
			
			else if (field.type.equals("OPTION")){
				OptionField of=new OptionField();
				field.input=of;
				if (defaultValues!=null) field.input.setContent(defaultValues[i]);
			}

		}
		

	}
	
	public String[] output(){
		String[] ret=new String[lines.length];
		int fieldNum=0;
		for (int i=0; i<lines.length; i++){
			if (lines[i].matches(".*\\*.*\\*.*")){
				String[] sl=lines[i].split("\\*");
				ret[i]=sl[0]+((Field)fields.get(fieldNum)).input.getContent();
				if (sl.length>2) ret[i]=ret[i]+sl[2];
				fieldNum++;
			} else {
				ret[i]=lines[i];
			}
		}
		return ret;	
	}
	
	public void parse(){
		fields=new ArrayList<Field>();
		lines=readTemplate();
		for (int i=0; i<lines.length; i++){
			if (lines[i].matches(".*\\*.*\\*.*")){
				String[] sl=lines[i].split("\\*");
				String[] f=sl[1].split("/");

				String type="";
				String label="";
				boolean optional=false;
				if (f[0].matches("optional:.*")){
					optional=true;
					String[] f2=f[0].split(":");
					type=f2[1];
				} else {
					type=f[0];
				}
				label=f[1];
				fields.add(new Field(type,label,optional));
				//System.out.println("Field: "+type+";"+label+";"+optional);
			}
		}		
	}
	
	public String[] readTemplate(){
		IO io=new IO(templateFile);
		String[] templateContent=null;
		try{
			templateContent=io.load();
		}catch(Exception e){
			System.out.println("File Error!");
			e.printStackTrace();
		}
		String[] ret=new String[templateContent.length];
		for (int i=0; i<ret.length;i++){
			ret[i]=templateContent[i];
		}
		return ret;
	}


}
