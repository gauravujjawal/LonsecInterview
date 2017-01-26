package com.lonsec.interview;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import com.lonsec.interview.bean.FundBean;
import com.lonsec.interview.bean.MonthlyOurperformaceResultBean;

public class FundPerformanceMain {

	private static double OUTPERFORM_VAL = DefaultValues.OUTPERFORM_DEFAULT_VAL;
	private static double UNDERPERFORM_VAL = DefaultValues.UNDERPERFORM_DEFAULT_VAL;

	private static String OUTPERFORM_TXT = DefaultValues.OUTPERFORM_DEFAULT_TXT;
	private static String UNDERPERFORM_TXT = DefaultValues.UNDERPERFORM_DEFAULT_TXT;

	private boolean SKIP_FIRST_LINE_OF_FILES = true;

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		FundPerformanceMain obj = new FundPerformanceMain();
		obj.readPropertyFile();
		obj.gatherData();

	}

	private void gatherData() {
		HashMap<String, FundBean> fundDataMap = new HashMap<>();
		HashMap<String, String> benchmarkDataMap = new HashMap<>();
		HashMap<Date, HashMap<String, Double>> dateBenchmarkSeriesMap = new HashMap<>();
		readFundCSVfile(fundDataMap, DefaultValues.FILES_FOLDER_PATH + DefaultValues.FUND_CSV);
		readBenchmarkCSVfile(benchmarkDataMap, DefaultValues.FILES_FOLDER_PATH + DefaultValues.BENCHMARK_CSV);
		readBenchmarkReturnCSVfile(dateBenchmarkSeriesMap,
				DefaultValues.FILES_FOLDER_PATH + DefaultValues.BENCHMARK_RETURN_SERIES_CSV);

		if (fundDataMap.size() > 0 && benchmarkDataMap.size() > 0 && dateBenchmarkSeriesMap.size() > 0) {
			TreeMap<Date, ArrayList<MonthlyOurperformaceResultBean>> finalResultData = new TreeMap<>();

			try (InputStream in = Files
					.newInputStream(Paths.get(DefaultValues.FILES_FOLDER_PATH + DefaultValues.FUND_RETURN_SERIES_CSV));
					BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
				String line = null;
				int rowCount = 0;
				while ((line = reader.readLine()) != null) {
					if (!(SKIP_FIRST_LINE_OF_FILES && rowCount == 0)) {
						if (line != null && line.split(",").length == 3) {
							String[] rowArr = line.split(",");
							Date fundDate = Utility.convertStringToDate(rowArr[1]);
							if (fundDataMap.containsKey(rowArr[0]) && dateBenchmarkSeriesMap.containsKey(fundDate)) {
								FundBean fBean = fundDataMap.get(rowArr[0]);
								String benchMarkCode = fBean.getBenchmarkCode();
								if (benchmarkDataMap.containsKey(benchMarkCode)) {
									if (dateBenchmarkSeriesMap.get(fundDate).containsKey(benchMarkCode)) {
										MonthlyOurperformaceResultBean outputBean = new MonthlyOurperformaceResultBean();
										outputBean.setFundName(fBean.getFundName());
										outputBean.setReturnPercentage(
												Utility.roundToDown(Double.parseDouble(rowArr[2])));
										outputBean.setDate(fundDate);
										try {
											outputBean.setExcess(calculateExcess(Double.parseDouble(rowArr[2]),
													dateBenchmarkSeriesMap.get(fundDate).get(benchMarkCode)));
										} catch (NumberFormatException nfe) {
											outputBean.setExcess(0.00);
											System.err.println("Incorrect fund return percentage" + line);
										}
										outputBean.setOutPerform(calOutPerformace(outputBean.getExcess()));

										ArrayList<MonthlyOurperformaceResultBean> resultList = null;
										if (finalResultData.containsKey(fundDate)) {
											resultList = finalResultData.get(fundDate);
										} else {
											resultList = new ArrayList<>();
											finalResultData.put(fundDate, resultList);
										}
										resultList.add(outputBean);

									} else {
										System.err.println(
												"Return % not found for fundcode, date and bencmarkcode @ " + line);
									}
								} else {
									System.err.println(
											"Benchmark not found in Master Data for passed fundcode @ " + line);
								}
							} else {
								System.err.println(
										"Fund Name or Benchmark date not found for passed fundcode and date @ " + line);
							}

						} else {
							System.err.println("Incorrect number of columns fund return Series csv @" + line);
						}
					}
					rowCount++;
				}

				if (finalResultData.size() > 0) {
					writeResultToFile(finalResultData, DefaultValues.FILES_FOLDER_PATH + DefaultValues.OUTPUT_CSV);
				}
			} catch (IOException x) {
				System.err.println(x);
			}
		} else {
			System.err.println("Valid data not found in uploaded files");
		}
	}

	private void readFundCSVfile(HashMap<String, FundBean> fundDataMap, String fileLocation) {
		try (InputStream in = Files.newInputStream(Paths.get(fileLocation));
				BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
			String line = null;
			int rowCount = 0;
			while ((line = reader.readLine()) != null) {
				if (!(SKIP_FIRST_LINE_OF_FILES && rowCount == 0)) {
					if (line != null && line.split(",").length == 3) {
						String[] rowArr = line.split(",");
						FundBean fbean = new FundBean();
						fbean.setFundCode(rowArr[0]);
						fbean.setFundName(rowArr[1]);
						fbean.setBenchmarkCode(rowArr[2]);
						fundDataMap.put(rowArr[0], fbean);
					} else {
						System.err.println("Incorrect number of columns fund csv @" + line);
					}
				}
				rowCount++;
			}
		} catch (IOException x) {
			System.err.println(x);
		}
	}

	private void readBenchmarkCSVfile(HashMap<String, String> benchmarkDataMap, String fileLocation) {
		try (InputStream in = Files.newInputStream(Paths.get(fileLocation));
				BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
			String line = null;
			int rowCount = 0;
			while ((line = reader.readLine()) != null) {
				if (!(SKIP_FIRST_LINE_OF_FILES && rowCount == 0)) {
					if (line != null && line.split(",").length == 2) {
						String[] rowArr = line.split(",");
						benchmarkDataMap.put(rowArr[0], rowArr[1]);
					} else {
						System.err.println("Incorrect number of columns benchMark csv @" + line);
					}
				}
				rowCount++;
			}
		} catch (IOException x) {
			System.err.println(x);
		}
	}

	private void readBenchmarkReturnCSVfile(HashMap<Date, HashMap<String, Double>> dateBenchmarkSeriesMap,
			String fileLocation) {
		try (InputStream in = Files.newInputStream(Paths.get(fileLocation));
				BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
			String line = null;
			int rowCount = 0;
			while ((line = reader.readLine()) != null) {
				if (!(SKIP_FIRST_LINE_OF_FILES && rowCount == 0)) {
					if (line != null && line.split(",").length == 3) {
						String[] rowArr = line.split(",");
						Date benchmarkDate = Utility.convertStringToDate(rowArr[1]);
						if (benchmarkDate != null) {
							try {
								HashMap<String, Double> benchMarkReturnData = null;
								if (dateBenchmarkSeriesMap.containsKey(benchmarkDate)) {
									benchMarkReturnData = dateBenchmarkSeriesMap.get(benchmarkDate);
								} else {
									benchMarkReturnData = new HashMap<>();
									dateBenchmarkSeriesMap.put(benchmarkDate, benchMarkReturnData);
								}
								benchMarkReturnData.put(rowArr[0], Double.parseDouble(rowArr[2]));
							} catch (NumberFormatException nfe) {
								System.err.println("Incorrect benchmark percentage" + line);
							}
						}
					} else {
						System.err.println("Incorrect number of columns benchMarkReturn Series csv @" + line);
					}
				}
				rowCount++;
			}
		} catch (IOException x) {
			System.err.println(x);
		}
	}

	private void writeResultToFile(TreeMap<Date, ArrayList<MonthlyOurperformaceResultBean>> finalResultData,
			String outputFilePath) {
		Path p = Paths.get(outputFilePath);
		for (Map.Entry<Date, ArrayList<MonthlyOurperformaceResultBean>> entry : finalResultData.entrySet()) {
			ArrayList<MonthlyOurperformaceResultBean> listOfRows = entry.getValue();
			Collections.sort(listOfRows);
			int rank = 1;
			try (OutputStream os = new BufferedOutputStream(
					Files.newOutputStream(p, StandardOpenOption.CREATE, StandardOpenOption.APPEND))) {
				for (MonthlyOurperformaceResultBean row : listOfRows) {
					StringBuffer data = new StringBuffer().append(row.getFundName().trim()).append(",")
							.append(Utility.convertDateToString(row.getDate())).append(",").append(row.getExcess())
							.append(",").append(row.getOutPerform()).append(",").append(row.getReturnPercentage())
							.append(",").append(rank).append("\n");
					os.write(data.toString().getBytes(), 0, data.length());
					rank++;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println("Report Successfully Generated");
	}

	/*
	 * This method accepts fund return percentage and benchmark return
	 * percentage. First the excess is calculated and then it it rounded down to
	 * specific decimal places mentioned in property files
	 */
	private double calculateExcess(double fundReturn, double benchReturn) {
		double excessPer = Math.round((fundReturn - benchReturn) * 100.0) / 100.0;
		DecimalFormat df = new DecimalFormat(DefaultValues.DEFAULT_DECIMAL_FORMAT);
		return Double.parseDouble(df.format(excessPer));
	}

	/*
	 * This method accepts excess percentage and based on the excess value
	 * returns the specific string found in property file. If no data is found
	 * in property file it uses the default string
	 */

	private String calOutPerformace(double excess) {
		if (excess > OUTPERFORM_VAL) {
			return OUTPERFORM_TXT;
		} else if (excess < UNDERPERFORM_VAL) {
			return UNDERPERFORM_TXT;
		}
		return "";
	}

	/*
	 * This method reads the property file in resource folder and overrides the
	 * default values
	 */
	private void readPropertyFile() {
		Properties prop = new Properties();
		try (InputStream in = getClass().getResourceAsStream("Lonsec.properties")) {
			prop.load(in);
			if (prop.getProperty("DECIMAL_FORMAT") != null) {
				DefaultValues.DEFAULT_DECIMAL_FORMAT = prop.getProperty("DECIMAL_FORMAT").trim();
			}
			if (prop.getProperty("DATE_FROMAT") != null) {
				DefaultValues.DEFAULT_DATE_FROMAT = prop.getProperty("DATE_FROMAT").trim();
			}
			if (prop.getProperty("EXCESS_OUTPERFORM_VAL") != null) {
				OUTPERFORM_VAL = Double.parseDouble(prop.getProperty("EXCESS_OUTPERFORM_VAL").trim());
			}
			if (prop.getProperty("EXCESS_UNDERPERFORM_VAL") != null) {
				UNDERPERFORM_VAL = Double.parseDouble(prop.getProperty("EXCESS_UNDERPERFORM_VAL").trim());
			}
			if (prop.getProperty("EXCESS_OUTPERFORM_TXT") != null) {
				OUTPERFORM_TXT = prop.getProperty("EXCESS_OUTPERFORM_TXT").trim();
			}
			if (prop.getProperty("EXCESS_UNDERPERFORM_TXT") != null) {
				UNDERPERFORM_TXT = prop.getProperty("EXCESS_UNDERPERFORM_TXT").trim();
			}
			if (prop.getProperty("FILES_FOLDER_PATH") != null) {
				DefaultValues.FILES_FOLDER_PATH = prop.getProperty("FILES_FOLDER_PATH").trim();
			}
			if (prop.getProperty("SKIP_FIRST_LINE_OF_FILES") != null) {
				SKIP_FIRST_LINE_OF_FILES = Boolean.parseBoolean(prop.getProperty("SKIP_FIRST_LINE_OF_FILES").trim());
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

}
