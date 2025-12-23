package com.example.petservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 语音提醒服务类
 * 负责将文字转换为语音并播放
 * 支持中文和英文语音
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class VoiceAlertService {

    // 直接用操作系统自带的语音引擎，零依赖、声音自然、支持中文
    private static final String OS = System.getProperty("os.name").toLowerCase();

    @Async
    public void speakPetHealthReminderAsync(String petName, String description) {
        String text = String.format("宠物健康提醒：%s，%s", petName, description);
        speak(text);
    }

    public void speak(String text) {
        try {
            if (OS.contains("win")) {
                windowsSpeak(text);
            } else if (OS.contains("mac")) {
                macSpeak(text);
            } else {
                linuxSpeak(text);
            }
        } catch (Exception e) {
            log.warn("语音播放失败（这不会影响主业务）：{}", text);
            // 静默降级，不抛异常，不阻塞
        }
    }

    private void windowsSpeak(String text) throws Exception {
        // 终极稳版：自动优先选最自然的中文语音，永不说英文！
        String ps = """
        Add-Type -AssemblyName System.Speech;
        $synth = New-Object System.Speech.Synthesis.SpeechSynthesizer;
        
        # 优先级最高 → 最低（越靠前越自然）
        $priorityVoices = @(
            'Microsoft Xiaoxiao',           # Win11 最强女声（神经网络）
            'Microsoft Xiaoni',             # Win11 第二自然
            'Microsoft Yunxi',              # 男声最自然
            'Microsoft Yunjian',            # 男声第二
            'Microsoft Huihui Desktop',     # 你现在唯一能用的（老版）
            'Microsoft Yaoyao Desktop'      # 备胎
        )
        
        $selected = $false
        foreach ($v in $priorityVoices) {
            try {
                $synth.SelectVoice($v)
                $selected = $true
                break
            } catch { }
        }
        
        # 如果上面全挂了，至少保证能说话（不会静默失败）
        if (-not $selected) {
            # 强制选一个中文的（根据语言代码找）
            foreach ($voiceInfo in $synth.GetInstalledVoices()) {
                if ($voiceInfo.VoiceInfo.Culture.Name -eq 'zh-CN') {
                    $synth.SelectVoice($voiceInfo.VoiceInfo.Name)
                    $selected = $true
                    break
                }
            }
        }
        
        $synth.Rate = 1          # 语速正常
        $synth.Volume = 100      # 音量最大
        $synth.Speak('%s')
        """.formatted(text.replace("'", "''").replace("\"", "\\\""));

        // 执行 PowerShell（加 -NoProfile 提速）
        Runtime.getRuntime().exec(new String[]{
                "powershell", "-NoProfile", "-ExecutionPolicy", "Bypass", "-Command", ps
        });
    }

    private void macSpeak(String text) throws Exception {
        Runtime.getRuntime().exec(new String[]{"say", "-v", "XiaoYan", text});
        // 备选音色：XiaoYan（小燕）、Meijia（美佳）等，可自行替换
    }

    private void linuxSpeak(String text) throws Exception {
        // 优先用 espeak-ng（几乎所有发行版都有，支持中文）
        ProcessBuilder pb = new ProcessBuilder("espeak-ng", "-v", "zh", text);
        Process p = pb.start();
        if (p.waitFor() != 0) {
            // 备选：用 pico2wave + aplay（树莓派/Debian 常用）
            Runtime.getRuntime().exec(new String[]{
                "sh", "-c", 
                "pico2wave -l zh-CN -w /tmp/tts.wav \"" + text + "\" && aplay /tmp/tts.wav && rm /tmp/tts.wav"
            });
        }
    }
}