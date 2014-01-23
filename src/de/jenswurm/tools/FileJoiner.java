package de.jenswurm.tools;
import java.util.List;

import de.jenswurm.tools.JaQL.Tuple;



/**
 * @author jwurm
 *Reads two files, splits the content by a given separator and joins them at a given column
 */
public class FileJoiner {
	
	
	public List<Tuple<String, String, String>> join(String file1, final String separator1, final int joinColumn1, String file2, final String separator2, final int joinColumn2){
		List<String> file1Content = new FileLineReader().read(file1);
		List<String> file2Content = new FileLineReader().read(file2);
		
		List<Tuple<String,String,String>> join = JaQL.innerJoin(file1Content, file2Content, new JaQL.JoinIndexer<String, String, String>() {

			@Override
			public String joinOnLeft(String string) {
				return string.split(separator1)[joinColumn1];
			}

			@Override
			public String joinOnRight(String string) {
				return string.split(separator2)[joinColumn2];
			}
		});
		
		return join;
		
		
		
	}

	public List<Tuple<String, String, String>> leftJoin(String file1, final String separator1, final int joinColumn1, String file2, final String separator2, final int joinColumn2) {
		List<String> file1Content = new FileLineReader().read(file1);
		List<String> file2Content = new FileLineReader().read(file2);
		
		List<Tuple<String,String,String>> join = JaQL.leftJoin(file1Content, file2Content, new JaQL.JoinIndexer<String, String, String>() {

			@Override
			public String joinOnLeft(String string) {
				return string.split(separator1)[joinColumn1];
			}

			@Override
			public String joinOnRight(String string) {
				return string.split(separator2)[joinColumn2];
			}
		});
		
		return join;
	}

}
