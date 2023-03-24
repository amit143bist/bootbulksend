package com.ds.proserv.feign.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.EnumUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;

import com.ds.proserv.common.constant.APICategoryType;
import com.ds.proserv.common.exception.CreateTableException;
import com.ds.proserv.common.exception.ResourceNotSavedException;
import com.ds.proserv.feign.authentication.domain.AuthenticationResponse;
import com.ds.proserv.feign.cachedata.domain.CacheLogDefinition;
import com.ds.proserv.feign.report.domain.ManageDataAPI;
import com.ds.proserv.feign.report.domain.PathParam;
import com.ds.proserv.feign.report.domain.PrepareDataAPI;
import com.ds.proserv.feign.report.domain.TableDefinition;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReportAppUtil {

	public static String createTableQuery(TableDefinition tableDefinition) {

		List<String> requiredColumnsList = Stream
				.of("recordid,createddatetime,createdby,accountid,batchid,processid".split(",")).map(String::trim)
				.collect(Collectors.toList());

		StringBuilder stringBuilder = new StringBuilder();

		stringBuilder.append("CREATE TABLE ");
		stringBuilder.append(tableDefinition.getTableName());
		stringBuilder.append(" ( ");
		tableDefinition.getColumns().forEach(col -> {

			stringBuilder.append("\"");
			stringBuilder.append(col.getColumnName());
			stringBuilder.append("\"");
			stringBuilder.append(" ");
			stringBuilder.append(col.getColumnType());
			stringBuilder.append(", ");

			requiredColumnsList.remove(col.getColumnName().toLowerCase());
		});

		if (!requiredColumnsList.isEmpty()) {

			throw new CreateTableException(
					requiredColumnsList + " is not empty, some required columns are not set in the createtable json");
		}

		if (null != tableDefinition.getPrimaryKey() && !tableDefinition.getPrimaryKey().isEmpty()) {

			stringBuilder.append("PRIMARY KEY");
			stringBuilder.append("(");

			tableDefinition.getPrimaryKey().forEach(key -> {

				stringBuilder.append("\"");
				stringBuilder.append(key);
				stringBuilder.append("\"");
				stringBuilder.append(",");
			});

			stringBuilder.deleteCharAt(stringBuilder.length() - 1);
			stringBuilder.append(")");
		}

		stringBuilder.append(" );");
		return stringBuilder.toString();
	}

	public static List<CacheLogDefinition> createCacheLogInfoList(TableDefinition tableDefinition) {

		List<CacheLogDefinition> cacheLogDefinitionList = new ArrayList<CacheLogDefinition>();
		tableDefinition.getColumns().forEach(col -> {

			if (!StringUtils.isEmpty(col.getColumnName()) && !StringUtils.isEmpty(col.getCsvHeaderName())) {

				CacheLogDefinition cacheLogDefinition = new CacheLogDefinition();
				cacheLogDefinition.setCacheKey(col.getColumnName());
				cacheLogDefinition.setCacheValue(col.getCsvHeaderName());
				cacheLogDefinition.setCacheReference("ColumnName");

				cacheLogDefinitionList.add(cacheLogDefinition);
			}
		});

		return cacheLogDefinitionList;
	}

	public static HttpEntity<String> prepareHTTPEntity(AuthenticationResponse authenticationResponse, String acceptType,
			String contentType) {

		return prepareHTTPEntity(authenticationResponse, acceptType, contentType, null);
	}

	public static HttpEntity<String> prepareHTTPEntity(AuthenticationResponse authenticationResponse, String acceptType,
			String contentType, String msgBody) {

		HttpHeaders headers = new HttpHeaders();
		if (StringUtils.isEmpty(acceptType)) {
			acceptType = "application/json";
		}

		if (StringUtils.isEmpty(contentType)) {
			contentType = "application/json";
		}

		headers.set("Accept", acceptType);
		headers.set("Content-Type", contentType);

		if (null != authenticationResponse) {

			headers.set("Authorization",
					authenticationResponse.getTokenType() + " " + authenticationResponse.getAccessToken());
		}

		if (!StringUtils.isEmpty(msgBody)) {

			return new HttpEntity<>(msgBody, headers);
		} else {
			return new HttpEntity<>(headers);
		}

	}

	public static String escapeSpecialCharacters(String data) {

		String escapedData = data.replaceAll("\\R", " ");
		if (data.contains("'")) {
			escapedData = data.replace("'", "\'");
		}

		log.debug("Escaping Data -> {} with {}", data, escapedData);
		return escapedData;
	}

	public static void createDirectory(String newDirectoryPath) {

		File file = new File(newDirectoryPath);
		if (!file.exists()) {

			if (file.mkdir()) {

				log.info("Directory -> {} is created!", newDirectoryPath);
			} else {

				log.error("Failed to create directory -> {}!", newDirectoryPath);
				throw new ResourceNotSavedException("Failed to create directory -> " + newDirectoryPath);
			}
		}
	}

	public static void createDirectoryNIO(String dirPath) {

		Path dirPathObj = Paths.get(dirPath);
		boolean dirExists = Files.exists(dirPathObj);

		if (dirExists) {
			log.warn("! {} directory Already Exists !", dirPath);
		} else {
			try {
				// Creating The New Directory Structure
				Files.createDirectories(dirPathObj);
				log.info("! New Directory {} Successfully Created !", dirPathObj);
			} catch (IOException ioExceptionObj) {
				log.error("Problem Occured While Creating The Directory Structure= {}", ioExceptionObj.getMessage());
			}
		}
	}

	public static void createDirectory(URI newDirectoryPath) {

		File file = new File(newDirectoryPath);
		if (!file.exists()) {

			if (file.mkdir()) {

				log.info("Directory -> {} is created!", newDirectoryPath);
			} else {

				log.error("Failed to create directory -> {}!", newDirectoryPath);
				throw new ResourceNotSavedException("Failed to create directory -> " + newDirectoryPath);
			}
		}
	}

	public static void writeBytesToFileNio(byte[] bFile, String fileDest) {

		try {

			Path path = Paths.get(fileDest);
			Files.write(path, bFile);
		} catch (IOException e) {

			log.error("IOException {} occurred with the message {} and cause {}", e, e.getMessage(), e);
			e.printStackTrace();
			throw new ResourceNotSavedException("File not saved in " + fileDest);
		}

	}

	public static PathParam findPathParam(List<PathParam> pathParams, String paramName) {

		return pathParams.stream().filter(pathParam -> paramName.equalsIgnoreCase(pathParam.getParamName())).findAny()
				.orElse(null);
	}

	public static PrepareDataAPI findDataPrepareAPI(List<PrepareDataAPI> dataPrepareAPIs, String batchType) {

		return dataPrepareAPIs.stream()
				.filter(dataPrepareAPI -> batchType.equalsIgnoreCase(dataPrepareAPI.getApiRunArgs().getBatchType()))
				.findAny().orElse(null);
	}

	public static ManageDataAPI findCsvReportDataExport(List<ManageDataAPI> csvReportDataExports, String batchType) {

		return csvReportDataExports.stream()
				.filter(csvReportDataExport -> batchType
						.equalsIgnoreCase(csvReportDataExport.getExportRunArgs().getBatchType()))
				.findAny().orElse(null);
	}

	public static APICategoryType getAPICategoryType(String apiCategory) {

		return EnumUtils.getEnum(APICategoryType.class, apiCategory.toUpperCase());
	}

	public static boolean isValidURL(String name) {

		boolean validUrl = true;
		try {

			if (StringUtils.isEmpty(name)) {

				return false;
			}

			URL u = new URL(name); // this would check for the protocol
			u.toURI();
		} catch (URISyntaxException e) {
			validUrl = false;
		} catch (MalformedURLException e) {
			validUrl = false;
		}

		return validUrl;
	}

	public static void splitFiles(int lines, int files, String inputfilePath, String splitFilePrefix)
			throws FileNotFoundException, IOException {

		BufferedReader br = new BufferedReader(new FileReader(inputfilePath)); // reader for input file intitialized
																				// only

		Path parentDirectory = Paths.get(inputfilePath).getParent();

		String headerLine = br.readLine();
		String strLine = null;
		for (int i = 1; i <= files; i++) {

			log.info("Creating file# " + i + " with name " + parentDirectory + File.separator + splitFilePrefix + "_"
					+ i + ".csv");
			FileWriter fstream1 = new FileWriter(parentDirectory + File.separator + splitFilePrefix + "_" + i + ".csv"); // creating
																															// a
																															// new
																															// file
																															// writer.
			BufferedWriter out = new BufferedWriter(fstream1);
			out.write(headerLine);
			out.newLine();
			for (int j = 0; j < lines; j++) { // iterating the reader to read only the first few lines of the csv as
												// defined earlier
				strLine = br.readLine();
				if (strLine != null) {

					out.write(strLine);
					out.newLine();
				}
			}
			out.close();
		}

		br.close();
	}

}