/**
 * 
 */

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Logger;


/**
 * @author Jens Wurm
 * 
 *         Beschreibung:
 */
public abstract class FileProcessor {
	private static final Logger LOGGER = Logger.getLogger(FileProcessor.class.getCanonicalName());

	public abstract void handleLine(String line);

	public FileProcessor(String filename) {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(filename));
			String line = null;
			int lines = 0;
			LOGGER.info("Lese File: " + filename);
			while ((line = br.readLine()) != null) {
				if (++lines % 100000 == 0) {
					LOGGER.info(lines + " Zeilen gelesen aus " + filename);
				}
				handleLine(line);
			}
		} catch (Exception e) {
			LOGGER.severe(e.getMessage());
			throw new RuntimeException(e.getMessage(), e);
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					LOGGER.severe(e.getMessage());
					throw new RuntimeException(e.getMessage(), e);
				}
			}
		}
	}

}
