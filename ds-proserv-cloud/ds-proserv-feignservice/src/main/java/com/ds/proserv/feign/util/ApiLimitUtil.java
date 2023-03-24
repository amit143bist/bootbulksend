package com.ds.proserv.feign.util;

import com.ds.proserv.common.util.DateTimeUtil;
import com.ds.proserv.feign.domain.ApiHourlyLimitData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ApiLimitUtil {

    public static ApiHourlyLimitData readApiHourlyLimitData(HttpHeaders responseHeaders,
                                                            Integer apiThresholdlLimitPercent) {

        ApiHourlyLimitData apiHourlyLimitData = null;
        if (null != responseHeaders && null != responseHeaders.entrySet()) {

            apiHourlyLimitData = new ApiHourlyLimitData();

            Set<Entry<String, List<String>>> headerEntrySet = responseHeaders.entrySet();
            for (Entry<String, List<String>> headerEntry : headerEntrySet) {

                log.debug("In readApiHourlyLimitData header key is {} and value is {} ", headerEntry.getKey(),
                        headerEntry.getValue());

                switch (headerEntry.getKey()) {

                    case "X-RateLimit-Reset":
                        apiHourlyLimitData.setRateLimitReset(headerEntry.getValue().get(0));
                        break;
                    case "X-RateLimit-Limit":
                        apiHourlyLimitData.setRateLimitLimit(headerEntry.getValue().get(0));
                        break;
                    case "X-RateLimit-Remaining":
                        apiHourlyLimitData.setRateLimitRemaining(headerEntry.getValue().get(0));
                        break;
                    case "X-BurstLimit-Remaining":
                        apiHourlyLimitData.setBurstLimitRemaining(headerEntry.getValue().get(0));
                        break;
                    case "X-BurstLimit-Limit":
                        apiHourlyLimitData.setBurstLimitLimit(headerEntry.getValue().get(0));
                        break;
                    case "X-DocuSign-TraceToken":
                        apiHourlyLimitData.setDocuSignTraceToken(headerEntry.getValue().get(0));
                        break;

                    default:
                        log.debug(
                                "-------------------- Wrong case -> {}, this is not handled in this method --------------------",
                                headerEntry.getKey());
                }
            }

            validateDSApiLimit(apiHourlyLimitData, apiThresholdlLimitPercent);
        }

        return apiHourlyLimitData;
    }

    private static void validateDSApiLimit(ApiHourlyLimitData apiHourlyLimitData, Integer apiThresholdlLimitPercent) {

        // apiThresholdLimitPercent
        log.debug(
                "Checking hourly limit apiHourlyLimitData.getRateLimitRemaining() is {} and apiHourlyLimitData.getRateLimitReset() is {}",
                apiHourlyLimitData.getRateLimitRemaining(), apiHourlyLimitData.getRateLimitReset());

        if (null != apiHourlyLimitData && !StringUtils.isEmpty(apiHourlyLimitData.getRateLimitRemaining())
                && !StringUtils.isEmpty(apiHourlyLimitData.getRateLimitReset())) {

            long longEpochTime = Long.parseLong(apiHourlyLimitData.getRateLimitReset());

            Date resetDate = Date.from(Instant.ofEpochSecond(longEpochTime));

            log.debug("Logging resetTime in validateDSApiLimit is {}", resetDate);

            long sleepMillis = DateTimeUtil.getDateDiff(Calendar.getInstance().getTime(), resetDate,
                    TimeUnit.MILLISECONDS);

            Float thresholdLimit = Float.parseFloat(apiHourlyLimitData.getRateLimitLimit()) * apiThresholdlLimitPercent
                    / 100f;

            log.info(
                    "Checking if need to send thread to sleep in PrepareReportDataService or not with sleepMillis -> {}, RateLimitRemaining-> {}, RateLimitResetValue -> {}, Float(RateLimitRemaining) -> {}, Float(RateLimitLimit) -> {} and ThresholdLimit is {}",
                    sleepMillis, apiHourlyLimitData.getRateLimitRemaining(), apiHourlyLimitData.getRateLimitReset(),
                    Float.parseFloat(apiHourlyLimitData.getRateLimitRemaining()),
                    Float.parseFloat(apiHourlyLimitData.getRateLimitLimit()), thresholdLimit);

            if (((Float.parseFloat(apiHourlyLimitData.getRateLimitRemaining()) < thresholdLimit) && sleepMillis > 0)) {

                apiHourlyLimitData.setSleepThread(true);
                sleepThread(LocalDateTime.ofInstant(Instant.ofEpochSecond(longEpochTime), ZoneId.systemDefault()),
                        sleepMillis);
            }

        }

        log.debug(
                "Checking burst limit apiHourlyLimitData.getBurstLimitRemaining() is {} and apiHourlyLimitData.getBurstLimitLimit() is {}",
                apiHourlyLimitData.getBurstLimitRemaining(), apiHourlyLimitData.getBurstLimitLimit());

        if (null != apiHourlyLimitData && !StringUtils.isEmpty(apiHourlyLimitData.getBurstLimitRemaining())) {

            Float thresholdBurstLimit = Float.parseFloat(apiHourlyLimitData.getBurstLimitLimit())
                    * apiThresholdlLimitPercent / 100f;
            log.info(
                    "Checking if need to send thread to sleep in PrepareReportDataService or not with apiHourlyLimitData.getBurstLimitRemaining() is {}, Float.parseFloat(apiHourlyLimitData.getBurstLimitRemaining()) -> {} and ThresholdBurstLimit is {}",
                    apiHourlyLimitData.getBurstLimitRemaining(),
                    Float.parseFloat(apiHourlyLimitData.getBurstLimitRemaining()), thresholdBurstLimit);

            if (Float.parseFloat(apiHourlyLimitData.getBurstLimitRemaining()) < thresholdBurstLimit) {

                apiHourlyLimitData.setSleepThread(true);
                sleepThread(LocalDateTime.now().plusSeconds(30), 30000);
            }

        }
    }

    public static void sleepThread(LocalDateTime resetDate, long sleepMillis) {

        log.debug(
                "Sending thread name-> {} and threadId-> {} to sleep in validateDSApiLimit() for {} milliseconds, and expected to wake up at {}",
                Thread.currentThread().getName(), Thread.currentThread().getId(), sleepMillis + 10000, resetDate);
        try {

            log.info("Sending thread to sleep, resetTime in validateDSApiLimit is {}", resetDate);
            Thread.sleep(sleepMillis + 10000);
        } catch (InterruptedException e) {

            log.error("InterruptedException thrown for thread name- {} and threadId- {}",
                    Thread.currentThread().getName(), Thread.currentThread().getId());
            e.printStackTrace();
        }
    }

}