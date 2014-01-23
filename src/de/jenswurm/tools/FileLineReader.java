package de.jenswurm.tools;
import java.util.ArrayList;
import java.util.List;


public class FileLineReader {
	
	public List<String> read(String file){
		final List<String> fileContent=new ArrayList<String>();
		new FileProcessor(file) {
			@Override
			public void handleLine(String line) {
				fileContent.add(line);
			}
		};
		return fileContent;
	}

}
