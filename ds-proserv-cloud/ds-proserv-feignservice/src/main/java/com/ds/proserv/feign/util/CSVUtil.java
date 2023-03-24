package com.ds.proserv.feign.util;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.supercsv.io.CsvMapWriter;
import org.supercsv.io.ICsvMapWriter;
import org.supercsv.prefs.CsvPreference;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CSVUtil {

	public static void writeCSV(List<Map<String, Object>> rowList, Collection<String> headerList, String csvPath,
			boolean writeToStream, HttpServletResponse response, boolean addHeader) throws IOException {

		String[] headerArray = headerList.stream().toArray(n -> new String[n]);

		ICsvMapWriter mapWriter = null;
		try {

			if (writeToStream && null != response) {

				mapWriter = new CsvMapWriter(response.getWriter(), CsvPreference.STANDARD_PREFERENCE);
			} else {

				mapWriter = new CsvMapWriter(new FileWriter(csvPath, true), CsvPreference.STANDARD_PREFERENCE);
			}

			// write the header
			if (addHeader) {

				mapWriter.writeHeader(headerArray);
			}

			// write the customer maps
			for (Map<String, Object> row : rowList) {

				log.debug("Row data for csvPath is -> {}", row);
				mapWriter.write(row, headerArray);
			}

		} catch (IOException exp) {

			exp.printStackTrace();
			throw exp;
		} finally {

			if (writeToStream) {

				log.info("All data written to stream");
			} else {
				log.info("All data written to a csv -> {}", csvPath);
			}
			if (null != mapWriter) {

				try {

					mapWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}