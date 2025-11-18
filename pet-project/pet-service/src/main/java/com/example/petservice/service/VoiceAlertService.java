package com.example.petservice.service;

import java.util.Locale;

import javax.speech.Central;
import javax.speech.synthesis.Synthesizer;
import javax.speech.synthesis.SynthesizerModeDesc;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * 语音提醒服务类
 * 负责将文字转换为语音并播放
 * 支持中文和英文语音
 */
@Service
@Slf4j
public class VoiceAlertService {
    
    private Synthesizer synthesizer;
    private boolean isInitialized = false;
    private boolean useWindowsSAPI = false;
    
    /**
     * 初始化语音合成器
     */
    private void initializeSynthesizer() {
        // 首先尝试使用Windows SAPI（支持中文）
        if (initializeWindowsSAPI()) {
            log.info("使用Windows SAPI语音引擎初始化成功");
            return;
        }
        
        // 如果Windows SAPI不可用，回退到FreeTTS
        log.warn("Windows SAPI不可用，尝试使用FreeTTS");
        initializeFreeTTS();
    }
    
    /**
     * 初始化Windows SAPI语音引擎
     * @return 初始化是否成功
     */
    private boolean initializeWindowsSAPI() {
        try {
            // 检查是否是Windows系统
            String osName = System.getProperty("os.name").toLowerCase();
            if (!osName.contains("windows")) {
                log.debug("非Windows系统，跳过SAPI初始化");
                return false;
            }
            
            // 测试PowerShell语音命令是否可用
            Process testProcess = Runtime.getRuntime().exec(
                "powershell -Command \"Add-Type -AssemblyName System.Speech; (New-Object System.Speech.Synthesis.SpeechSynthesizer).GetInstalledVoices()\"");
            int exitCode = testProcess.waitFor();
            
            if (exitCode == 0) {
                useWindowsSAPI = true;
                isInitialized = true;
                return true;
            }
            
            log.debug("Windows SAPI语音引擎不可用");
            return false;
        } catch (Exception e) {
            log.debug("Windows SAPI初始化失败: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 初始化FreeTTS语音引擎
     */
    private void initializeFreeTTS() {
        try {
            // 设置语音合成引擎属性，添加中文语音支持
            System.setProperty("freetts.voices", 
                "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory," +
                "com.sun.speech.freetts.en.us.cmu_time_awb.AlanVoiceDirectory," +
                "de.dfki.lt.freetts.en.us.MbrolaVoiceDirectory");
            
            // 注册语音引擎
            Central.registerEngineCentral("com.sun.speech.freetts.jsapi.FreeTTSEngineCentral");
            
            // 尝试创建中文语音合成器，如果失败则使用默认
            try {
                // 尝试使用中文语音
                SynthesizerModeDesc desc = new SynthesizerModeDesc(Locale.CHINA);
                synthesizer = Central.createSynthesizer(desc);
                log.info("尝试使用中文语音合成器");
            } catch (Exception e) {
                log.warn("中文语音合成器不可用，使用默认英文语音: {}", e.getMessage());
                // 如果中文语音不可用，使用默认英文语音
                SynthesizerModeDesc desc = new SynthesizerModeDesc(Locale.US);
                synthesizer = Central.createSynthesizer(desc);
            }
            
            // 分配和恢复合成器
            synthesizer.allocate();
            synthesizer.resume();
            
            isInitialized = true;
            log.info("FreeTTS语音合成器初始化成功");
        } catch (Exception e) {
            log.error("FreeTTS语音合成器初始化失败", e);
            isInitialized = false;
        }
    }
    
    /**
     * 播放语音提醒
     * @param message 要转换为语音的消息
     */
    public void speak(String message) {
        try {
            // 如果未初始化，先初始化
            if (!isInitialized) {
                initializeSynthesizer();
            }
            
            if (isInitialized) {
                log.info("播放语音提醒: {}", message);
                
                // 根据初始化的语音引擎类型选择播放方式
                if (useWindowsSAPI) {
                    speakWithWindowsSAPI(message);
                } else if (synthesizer != null) {
                    speakWithFreeTTS(message);
                }
                
                log.info("语音提醒播放完成");
            } else {
                log.warn("语音合成器未初始化，无法播放语音提醒: {}", message);
            }
        } catch (Exception e) {
            log.error("播放语音提醒时发生错误: {}", message, e);
        }
    }
    
    /**
     * 使用Windows SAPI播放语音
     * @param message 要播放的消息
     */
    private void speakWithWindowsSAPI(String message) {
        try {
            // 使用PowerShell调用Windows SAPI播放中文语音
            String command = String.format(
                "powershell -Command \"Add-Type -AssemblyName System.Speech; $synth = New-Object System.Speech.Synthesis.SpeechSynthesizer; $synth.SelectVoice('Microsoft Huihui Desktop'); $synth.Speak('%s')\"",
                message.replace("'", "''").replace("\"", "\"\"") // 转义单引号和双引号
            );
            
            Process process = Runtime.getRuntime().exec(command);
            process.waitFor();
            
            log.debug("Windows SAPI语音播放完成");
        } catch (Exception e) {
            log.error("使用Windows SAPI播放语音时发生错误", e);
            // 如果SAPI失败，尝试使用FreeTTS
            if (synthesizer != null) {
                speakWithFreeTTS(message);
            }
        }
    }
    
    /**
     * 使用FreeTTS播放语音
     * @param message 要播放的消息
     */
    private void speakWithFreeTTS(String message) {
        try {
            // 播放语音
            synthesizer.speakPlainText(message, null);
            
            // 等待语音播放完成
            synthesizer.waitEngineState(Synthesizer.QUEUE_EMPTY);
            
            log.debug("FreeTTS语音播放完成");
        } catch (Exception e) {
            log.error("使用FreeTTS播放语音时发生错误", e);
        }
    }
    
    /**
     * 播放宠物健康提醒
     * @param petName 宠物名称
     * @param healthType 健康检查类型
     * @param description 描述信息
     */
    public void speakPetHealthReminder(String petName, String description) {
        String message = String.format("宠物健康提醒：%s：%s", petName, description);
        speak(message);
    }
    
    /**
     * 关闭语音合成器
     */
    public void shutdown() {
        try {
            if (useWindowsSAPI) {
                // Windows SAPI不需要特殊关闭处理
                log.info("Windows SAPI语音引擎已关闭");
            } else if (synthesizer != null) {
                // 关闭FreeTTS合成器
                synthesizer.deallocate();
                log.info("FreeTTS语音合成器已关闭");
            }
            
            isInitialized = false;
        } catch (Exception e) {
            log.error("关闭语音合成器时发生错误", e);
        }
    }
}