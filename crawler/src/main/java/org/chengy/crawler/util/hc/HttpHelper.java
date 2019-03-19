package org.chengy.crawler.util.hc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * Created by nali on 2017/9/12.
 */

public class HttpHelper {


	public static final Logger LOGGER = LoggerFactory.getLogger(HttpHelper.class);

	public static CloseableHttpClient client() {
		return HttpClients.custom().
				setConnectionManager(HTTPConnectionManager.getConnectionManager()).build();

	}

	/**
	 * 获取网页
	 * @param url
	 * @return
	 * @throws Exception
	 */
	public static String get(String url) throws Exception {

		HttpGet get = new HttpGet(url);
		get.addHeader(HttpConstants.USER_AGENT, HttpConstants.CHROME_V55);
		get.addHeader(HttpConstants.REFERER, "https://music.163.com/");
		get.addHeader(HttpConstants.HOST, "music.163.com");
		CloseableHttpResponse response = null;
		try {
			response = client().execute(get);
			if (response.getStatusLine().getStatusCode() == 200) {
				return EntityUtils.toString(response.getEntity());
			}else if (response.getStatusLine().getStatusCode() == 503) {
				throw  new Exception("service is temporarily blocked!");
			} else {
				throw new Exception("response code is " + response.getStatusLine().getStatusCode());
			}
		} finally {
			if (response != null) {
				response.close();
			}
		}
	}

	/**
	 * 获取媒体文件
	 * @param file
	 * @param picUrl
	 * @return
	 */
	public static File getPicFile(File file, String picUrl) {
		BufferedOutputStream bufferedOutputStream = null;
		CloseableHttpResponse closeableHttpResponse = null;
		RequestConfig.Builder requestBuilder = RequestConfig.custom();
		requestBuilder = requestBuilder.setConnectTimeout(100 * 1000);
		requestBuilder = requestBuilder.setConnectionRequestTimeout(100 * 1000);
		requestBuilder = requestBuilder.setSocketTimeout(15 * 1000);
		RequestConfig requestConfig =
				requestBuilder.build();

		try {
			CloseableHttpClient httpClient = HttpClients.createDefault();
			HttpGet httpGet = new HttpGet(picUrl);
			httpGet.setConfig(requestConfig);
			closeableHttpResponse = httpClient.execute(httpGet);
			FileOutputStream fileOutputStream = new FileOutputStream(file);
			bufferedOutputStream = new BufferedOutputStream(fileOutputStream);

			HttpEntity entity = closeableHttpResponse.getEntity();
			InputStream inputStream = entity.getContent();
			byte[] b = new byte[4096];
			int length = 0;
			while ((length = inputStream.read(b)) != -1) {
				bufferedOutputStream.write(b, 0, length);
			}
			bufferedOutputStream.flush();

		} catch (Exception e) {
			LOGGER.error("download pic error:{}", file.getName(), e);
			throw new RuntimeException("download pic error");
		} finally {
			try {
				if (bufferedOutputStream != null) {
					bufferedOutputStream.close();
				}
				if (closeableHttpResponse != null) {
					closeableHttpResponse.close();
				}
			} catch (Exception e) {
				LOGGER.error("close io error for file {}", file.getName(), e);
				throw new RuntimeException("close io error");
			}
		}
		return file;
	}


	/**
	 * 上传文件
	 * @param file
	 * @param url
	 * @return
	 * @throws IOException
	 */
	public static String httpUpload(File file, String url) throws IOException {
		try (CloseableHttpClient httpclient = HttpClients.createDefault()) {

			String message = "This is a multipart post";

			// build multipart upload request
			HttpEntity data = MultipartEntityBuilder.create()
					.setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
					.addBinaryBody("upfile", file, ContentType.DEFAULT_BINARY, file.getName())
					.addTextBody("text", message, ContentType.DEFAULT_BINARY)
					.build();


			// build http request and assign multipart upload data
			HttpUriRequest request = RequestBuilder
					.post(url)
					.setEntity(data)
					.build();

			System.out.println("Executing request " + request.getRequestLine());

			// Create a custom response handler
			ResponseHandler<String> responseHandler = response -> {
				int status = response.getStatusLine().getStatusCode();
				if (status >= 200 && status < 300) {
					HttpEntity entity = response.getEntity();
					return entity != null ? EntityUtils.toString(entity) : null;
				} else {
					throw new ClientProtocolException("Unexpected response status: " + status);
				}
			};
			String responseBody = httpclient.execute(request, responseHandler);
			return responseBody;
		}
	}

	/**
	 * 获取响应头的content—length
	 * @param url
	 * @return
	 * @throws Exception
	 */
	public static String getContentLengthHeader(String url) throws Exception {
		HttpGet get = new HttpGet(url);

		CloseableHttpResponse response = null;
		try {
			response = client().execute(get);
			if (response.getStatusLine().getStatusCode() == 200) {
				if (response.containsHeader("Content-Length")) {
					Header[] headers = response.getHeaders("Content-Length");
					return headers[0].getValue();
				}
				return null;
			} else {
				LOGGER.info("response code is" + response.getStatusLine().getStatusCode());
				return null;
			}
		} catch (Exception e) {
			get.abort();
			return null;
		} finally {
			if (response != null) {
				response.close();
			}
		}


	}

	public static String postMessageByJson(String jsonData, String url) throws IOException {

		if (!isJSONValid(jsonData)) {
			LOGGER.error("the postMessageByJson method get an invalid string");
			throw new RuntimeException("postMessageByJson method get an invalid string");
		}

		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
			HttpPost post = new HttpPost(url);
			post.setEntity(new StringEntity(jsonData));

			post.setHeader(new BasicHeader("Content-Type", "application/json"));

			ResponseHandler<String> responseHandler = response -> {
				int status = response.getStatusLine().getStatusCode();
				if (status >= 200 && status < 300) {
					HttpEntity entity = response.getEntity();
					return entity != null ? EntityUtils.toString(entity) : null;
				} else {
					throw new ClientProtocolException("Unexpected response status: " + status);
				}
			};
			String responseBody = httpClient.execute(post, responseHandler);
			return responseBody;
		}

	}

	private static boolean isJSONValid(String str) {
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			JsonNode jsonNode = objectMapper.readTree(str);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
