package or.util;
import java.util.*;
import java.io.*;

public class AMPLUtils {
	public static boolean isCommentLine(String str){		
		return str.length()>0 && str.trim().charAt(0)=='#';
	}	
	
	public static Map<String,String> generateMap(File file) throws IOException{
		Map<String,String> ris=new HashMap<String,String>();
		BufferedReader br=new BufferedReader(new FileReader(file));
		String buf;
		while( (buf=br.readLine()) != null){
			if(isCommentLine(buf)) continue;
			int index=buf.indexOf(":=");
			if(index==-1) continue;
			StringTokenizer tkz=new StringTokenizer(buf," :=");
			String type=tkz.nextToken();
			String name=tkz.nextToken();
			StringBuffer data=new StringBuffer(buf.substring(index+2));
			while((index=data.indexOf(";"))==-1){
				buf=br.readLine();
				data.append(" "+buf);
			}
			data.deleteCharAt(index);
			
			ris.put(name,data.toString().trim());
		}
		br.close();
		return ris;
	}

}
