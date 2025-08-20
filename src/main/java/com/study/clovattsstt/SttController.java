package com.study.clovattsstt;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Type;
import java.util.Map;

@Controller
public class SttController {

    @Autowired
    private ClovaSpeechClient clovaSpeechClient;

    private Gson gson = new Gson();

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @PostMapping("/process")
    public String processObjectStorage(@RequestParam("dataKey") String dataKey,
                                       @RequestParam(value = "language", defaultValue = "ko-KR") String language,
                                       @RequestParam(value = "url", required = false) String url,
                                       @RequestParam(value = "wordAlignment", defaultValue = "true") boolean wordAlignment,
                                       @RequestParam(value = "fullText", defaultValue = "true") boolean fullText,
                                       @RequestParam(value = "diarization", defaultValue = "false") boolean enableDiarization,
                                       @RequestParam(value = "speakerMin", required = false) Integer speakerMin,
                                       @RequestParam(value = "speakerMax", required = false) Integer speakerMax,
                                       @RequestParam(value = "forbiddens", required = false) String forbiddens,
                                       Model model) {

        if (url != null && !url.trim().isEmpty()) {
            // URL로 처리
            return processUrl(url, language, wordAlignment, fullText, enableDiarization, speakerMin, speakerMax, forbiddens, model);
        }

        if (dataKey == null || dataKey.trim().isEmpty()) {
            model.addAttribute("error", "Object Storage 데이터 키를 입력해주세요.");
            return "index";
        }

        try {
            // STT 요청 설정
            ClovaSpeechClient.NestRequestEntity requestEntity = new ClovaSpeechClient.NestRequestEntity();
            requestEntity.setLanguage(language);
            requestEntity.setWordAlignment(wordAlignment);
            requestEntity.setFullText(fullText);
            requestEntity.setForbiddens(forbiddens);

            // Speaker diarization 설정
            if (enableDiarization) {
                ClovaSpeechClient.Diarization diarization = new ClovaSpeechClient.Diarization();
                diarization.setEnable(true);
                if (speakerMin != null) diarization.setSpeakerCountMin(speakerMin);
                if (speakerMax != null) diarization.setSpeakerCountMax(speakerMax);
                requestEntity.setDiarization(diarization);
            }

            // Object Storage STT 실행
            String result = clovaSpeechClient.objectStorage(dataKey, requestEntity);

            // JsonObject를 Map으로 변환하여 Thymeleaf에서 사용하기 쉽게 만듦
            Type mapType = new TypeToken<Map<String, Object>>(){}.getType();
            Map<String, Object> resultMap = gson.fromJson(result, mapType);

            model.addAttribute("fileName", dataKey);
            model.addAttribute("language", language);
            model.addAttribute("rawResult", result);
            model.addAttribute("result", resultMap);  // JsonObject 대신 Map 사용
            model.addAttribute("processType", "Object Storage");

        } catch (Exception e) {
            model.addAttribute("error", "STT 처리 중 오류가 발생했습니다: " + e.getMessage());
            return "index";
        }

        return "result";
    }

    private String processUrl(String url, String language, boolean wordAlignment, boolean fullText,
                              boolean enableDiarization, Integer speakerMin, Integer speakerMax,
                              String forbiddens, Model model) {
        try {
            ClovaSpeechClient.NestRequestEntity requestEntity = new ClovaSpeechClient.NestRequestEntity();
            requestEntity.setLanguage(language);
            requestEntity.setWordAlignment(wordAlignment);
            requestEntity.setFullText(fullText);
            requestEntity.setForbiddens(forbiddens);

            // Speaker diarization 설정
            if (enableDiarization) {
                ClovaSpeechClient.Diarization diarization = new ClovaSpeechClient.Diarization();
                diarization.setEnable(true);
                if (speakerMin != null) diarization.setSpeakerCountMin(speakerMin);
                if (speakerMax != null) diarization.setSpeakerCountMax(speakerMax);
                requestEntity.setDiarization(diarization);
            }

            String result = clovaSpeechClient.url(url, requestEntity);

            // JsonObject를 Map으로 변환
            Type mapType = new TypeToken<Map<String, Object>>(){}.getType();
            Map<String, Object> resultMap = gson.fromJson(result, mapType);

            model.addAttribute("fileName", url);
            model.addAttribute("language", language);
            model.addAttribute("rawResult", result);
            model.addAttribute("result", resultMap);  // JsonObject 대신 Map 사용
            model.addAttribute("processType", "URL");

        } catch (Exception e) {
            model.addAttribute("error", "URL STT 처리 중 오류가 발생했습니다: " + e.getMessage());
            return "index";
        }

        return "result";
    }

    @GetMapping("/test")
    public String testObjectStorage(Model model) {
        try {
            // 테스트용 데이터 키 (실제 업로드된 파일로 변경하세요)
            String testDataKey = "mission.wav"; // Object Storage에 업로드한 파일명

            ClovaSpeechClient.NestRequestEntity requestEntity = new ClovaSpeechClient.NestRequestEntity();
            requestEntity.setLanguage("ko-KR");

            String result = clovaSpeechClient.objectStorage(testDataKey, requestEntity);

            // JsonObject를 Map으로 변환
            Type mapType = new TypeToken<Map<String, Object>>(){}.getType();
            Map<String, Object> resultMap = gson.fromJson(result, mapType);

            model.addAttribute("fileName", testDataKey);
            model.addAttribute("language", "ko-KR");
            model.addAttribute("rawResult", result);
            model.addAttribute("result", resultMap);  // JsonObject 대신 Map 사용
            model.addAttribute("processType", "Test Object Storage");

            return "result";
        } catch (Exception e) {
            model.addAttribute("error", "테스트 실행 중 오류: " + e.getMessage());
            return "index";
        }
    }
}