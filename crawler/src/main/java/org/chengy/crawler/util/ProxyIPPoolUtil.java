package org.chengy.crawler.util;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.impl.ConcurrentHashSet;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.chengy.crawler.util.hc.HttpHelper;
import org.chengy.crawler.util.vertx.VertxClientFactory;
import org.chengy.crawler.website.neteastmusic.NetEastApiCons;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


@Component
public class ProxyIPPoolUtil {

    private static String xiciUrl = "http://www.xicidaili.com/nn";
    @Autowired
    VertxClientFactory vertxClientFactory;

    @Value("${profile}")
    String env;


    private static String targetUrl = NetEastApiCons.userHost + "330313";


    /**
     * 研究一下copyonwrite
     */
    private static Set<Pair<String, Integer>> ipSet = new HashSet<>();

    private AtomicInteger index = new AtomicInteger(0);

    private static List<Pair<String, Integer>> ipList = new ArrayList<>(ipSet);

    @PostConstruct
    public void init() throws ExecutionException, InterruptedException {
        if (env.equals("test")) {
            return;
        }
        getAvailableIps();
        System.out.println("get useful ip size:" + ipList.size());
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
        executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    getAvailableIps();
                } catch (Exception e) {
                    System.out.println("get proxy ipsets error");
                }

            }
        }, 10, 10, TimeUnit.MINUTES);
    }


    /**
     * round robin get available ip
     *
     * @return
     */
    public Pair<String, Integer> peekIp() {
        if (index.get() >= ipList.size()) {
            index.compareAndSet(ipList.size(), 0);
        }
        return ipList.get(index.getAndIncrement());

    }


    public boolean getAvailableIps() throws ExecutionException, InterruptedException {
        Set<Pair<String, Integer>> pairSet =   getKuaiDailiProxyIps();
        ipSet.addAll(pairSet);
        ipSet = validateProxyIp(ipSet, targetUrl);
        ipList.clear();
        ipList.addAll(ipSet);
        index.set(0);
        return true;
    }


    /**
     * 通过目标网址校验 ip的可用性
     *
     * @param needValidateIps
     * @param targetUrl
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public Set<Pair<String, Integer>> validateProxyIp(Set<Pair<String, Integer>> needValidateIps, String targetUrl) throws ExecutionException, InterruptedException {
        CompletableFuture<Set<Pair<String, Integer>>> future = new CompletableFuture<>();
        Set<Pair<String, Integer>> res = new ConcurrentHashSet<>();
        AtomicInteger count = new AtomicInteger();
        WebClient webClient = vertxClientFactory.newWebClient(50, 1000);

        needValidateIps.stream().forEach(pair -> {
            HttpRequest<Buffer> httpRequest = webClient.getAbs(targetUrl);
            httpRequest.send(ar -> {
                if (ar.succeeded()) {
                    HttpResponse<Buffer> response = ar.result();
                    if (response.statusCode() == 200) {
                        res.add(new ImmutablePair<>(pair.getLeft(), pair.getRight()));
                    }
                    if (count.getAndIncrement() == needValidateIps.size() - 1) {
                        future.complete(res);
                    }
                } else if (ar.failed()) {
                    if (count.getAndIncrement() == needValidateIps.size() - 1) {
                        future.complete(res);
                    }
                }
            });
        });
        return future.get();
    }


    /**
     * kauidaili的代理ip
     * @return
     */
    public Set<Pair<String,Integer>> getKuaiDailiProxyIps(){

        Set<Pair<String, Integer>> pairSet = new ConcurrentHashSet<>(16);

        String html = null;
        try {
            html = HttpHelper.get("https://www.kuaidaili.com/free/inha/1/");
        } catch (Exception e) {
            e.printStackTrace();
        }

        Document document = Jsoup.parse(html);
        Elements elements=
                document.select("#list > table > tbody > tr");
        for (Element element:elements){
            String  host=
                    element.select("td:nth-child(1)").html();
            String port=element.select("td:nth-child(2)").html();
            pairSet.add(new ImmutablePair<>(host,Integer.parseInt(port)));
        }
        return pairSet;
    }


    /**
     * 获取xici的高匿ip
     *
     * @return
     */
    public Set<Pair<String, Integer>> getXiciProxyIps() {

        Set<Pair<String, Integer>> pairSet = new ConcurrentHashSet<>(16);
        // 爬去xici的前三页
        List<String> urls = Arrays.asList(xiciUrl, xiciUrl + "/2", xiciUrl + "/3");
        WebClient webClient = vertxClientFactory.newWebClient(2, 1000);
        urls.forEach(url -> {
                    CompletableFuture<String> futureHtml = new CompletableFuture<>();

                    HttpRequest<Buffer> httpRequest = webClient.getAbs(url);
                    httpRequest.send(ar -> {
                        if (ar.succeeded()) {
                            HttpResponse<Buffer> response = ar.result();
                            if (response.statusCode() == 200) {
                                String html = response.body().toString(StandardCharsets.UTF_8);
                                futureHtml.complete(html);
                            }
                            futureHtml.complete(response.statusCode() + "");
                        } else if (ar.failed()) {
                            futureHtml.completeExceptionally(ar.cause());
                        }
                    });

                    try {
                        String html = futureHtml.get();
                        Document document = Jsoup.parse(html);
                        Element element = document.select("#ip_list > tbody").get(0);
                        Elements elements = element.getElementsByTag("tr");
                        List<Pair<String, Integer>> pageList = elements.subList(1, elements.size()).stream().map(ob -> {
                            String ip = ob.getElementsByTag("td").get(1).text().trim();
                            Integer port = Integer.valueOf(ob.getElementsByTag("td").get(2).text().trim());
                            return new ImmutablePair<String, Integer>(ip, port);
                        }).collect(Collectors.toList());
                        pairSet.addAll(pageList);
                    } catch (Exception e) {
                        System.out.println("page url extract info error" + e.getMessage());
                    }
                }
        );
        return pairSet;
    }
}
