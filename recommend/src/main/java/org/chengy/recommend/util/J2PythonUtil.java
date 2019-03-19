package org.chengy.recommend.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.chengy.crawler.util.hc.HttpHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

@Component
public class J2PythonUtil {
	static ObjectMapper objectMapper = new ObjectMapper();


	private static String recommend_url;




	public static PythonRes callPythonRPC(String[] args)  {
		PythonRes pythonRes = new PythonRes();

		try {
			objectMapper.configure(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS, true);
			String argsStr = objectMapper.writeValueAsString(args);


			String res = HttpHelper.get(recommend_url + URLEncoder.encode(argsStr, "utf-8"));
			pythonRes.scoreMap = objectMapper.readValue(res, new TypeReference<Map<String, Object>>() {
			});
			pythonRes.setCode(0);
			return pythonRes;
		} catch (Exception e) {
			pythonRes.setCode(1);
		}
		return pythonRes;

	}

//	public static PythonRes callPythonProcess(String[] args) {
//		objectMapper.configure(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS, true);
//		Process process = null;
//		PythonRes pythonRes = new PythonRes();
//		try {
//			process = Runtime.getRuntime().exec(args);
//			BufferedReader stdOut = new BufferedReader(new InputStreamReader(process.getInputStream()));
//			String s;
//			String ss = null;
//			while ((s = stdOut.readLine()) != null) {
//				ss = s;
//			}
//			try {
//				Map<String, Object> map = objectMapper.readValue(ss, new TypeReference<Map<String, Object>>() {
//				});
//				pythonRes.scoreMap = map;
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//			// 0是调用进程正常
//			int result = 1;
//			result = process.waitFor();
//			pythonRes.code = result;
//			process.destroy();
//		} catch (InterruptedException | IOException e) {
//			// e.printStackTrace();
//		} finally {
//			if (process != null) {
//				process.destroy();
//			}
//		}
//		return pythonRes;
//	}


	public static String getRecommend_url() {
		return recommend_url;
	}

	@Value("${rec.url}")
	public  void setRecommend_url(String recommend_url) {
		J2PythonUtil.recommend_url = recommend_url;
	}

	public static class PythonRes {
		private int code;
		private Map<String, Object> scoreMap;

		public int getCode() {
			return code;
		}

		public void setCode(int code) {
			this.code = code;
		}


		public Map<String, Object> getScoreMap() {
			return scoreMap;
		}

		public void setScoreMap(Map<String, Object> scoreMap) {
			this.scoreMap = scoreMap;
		}
	}

}
