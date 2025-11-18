package com.example.petservice.task;

import com.example.petcommon.properties.HealthReminderCleanupProperties;
import com.example.petpojo.dto.HealthDto;
import com.example.petpojo.vo.AdoptionsVo;
import com.example.petpojo.vo.HealthAlertsVo;
import com.example.petpojo.entity.Users;
import com.example.petservice.service.AdoptionsService;
import com.example.petservice.service.HealthAlertsService;
import com.example.petservice.service.UsersService;
import com.example.petservice.service.VoiceAlertService;
import com.example.petservice.websocket.WebSocketServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * PetHealthReminderTask 删除功能专项测试
 * 专门测试定时任务的健康提醒删除功能
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("宠物健康提醒删除功能测试")
class PetHealthReminderDeletionTest {

    @Mock
    private HealthAlertsService healthAlertsService;

    @Mock
    private AdoptionsService adoptionsService;

    @Mock
    private UsersService usersService;

    @Mock
    private VoiceAlertService voiceAlertService;

    @Mock
    private HealthReminderCleanupProperties cleanupConfig;

    @Mock
    private WebSocketServer webSocketServer;

    @InjectMocks
    private PetHealthReminderTask petHealthReminderTask;

    private Users testUser;
    private List<AdoptionsVo> testAdoptions;
    private List<HealthAlertsVo> testHealthAlerts;

    @BeforeEach
    void setUp() {
        // 创建测试用户
        testUser = Users.builder()
                .id(1)
                .userName("testuser")
                .email("test@example.com")
                .phone("13800138000")
                .build();

        // 创建测试宠物领养记录
        testAdoptions = new ArrayList<>();
        AdoptionsVo adoption = new AdoptionsVo();
        adoption.setPid(1);
        adoption.setName("小白");
        testAdoptions.add(adoption);

        // 创建测试健康提醒
        testHealthAlerts = new ArrayList<>();
        
        // 配置清理属性
        Map<String, Integer> expiryDaysMap = new HashMap<>();
        expiryDaysMap.put("疫苗接种", 7);
        expiryDaysMap.put("驱虫", 5);
        expiryDaysMap.put("体检", 3);
        expiryDaysMap.put("洗澡", 2);
        expiryDaysMap.put("默认", 1);

        when(cleanupConfig.getExpiryDays()).thenReturn(expiryDaysMap);
        when(cleanupConfig.getArchiveThresholdDays()).thenReturn(30);
        when(cleanupConfig.getDeletionThresholdDays()).thenReturn(365);
        when(cleanupConfig.isEnableDeletion()).thenReturn(true);
        when(cleanupConfig.getExpiryDaysByHealthType(anyString())).thenAnswer(invocation -> {
            String healthType = invocation.getArgument(0);
            return expiryDaysMap.getOrDefault(healthType, expiryDaysMap.get("默认"));
        });

        // 配置服务返回值
        when(usersService.list()).thenReturn(List.of(testUser));
        when(adoptionsService.getUserAdoptions(anyLong(), anyInt(), anyInt())).thenReturn(testAdoptions);
        when(healthAlertsService.getUserHealthAlerts(anyInt())).thenReturn(testHealthAlerts);
    }

    @Test
    @DisplayName("测试删除功能 - 刚好达到删除阈值")
    void testDeletionAtThreshold() {
        // 创建一个刚好达到删除阈值的归档健康提醒
        HealthAlertsVo alert = new HealthAlertsVo();
        alert.setHealthId(1);
        alert.setPid(1);
        alert.setCheckDate(LocalDateTime.of(2023, 11, 1, 10, 0, 0));
        alert.setHealthType("疫苗接种");
        alert.setDescription("狂犬疫苗");
        // 402天前，刚好达到7+30+365=402天的删除阈值
        alert.setReminderTime(LocalDateTime.now().minusDays(402));
        alert.setStatus("archived");
        testHealthAlerts.add(alert);

        // 执行测试
        petHealthReminderTask.cleanupExpiredHealthReminders();

        // 验证健康提醒被删除
        verify(healthAlertsService).removeById(1);
    }

    @Test
    @DisplayName("测试删除功能 - 超过删除阈值")
    void testDeletionBeyondThreshold() {
        // 创建一个超过删除阈值的归档健康提醒
        HealthAlertsVo alert = new HealthAlertsVo();
        alert.setHealthId(1);
        alert.setPid(1);
        alert.setCheckDate(LocalDateTime.of(2023, 11, 1, 10, 0, 0));
        alert.setHealthType("疫苗接种");
        alert.setDescription("狂犬疫苗");
        // 450天前，超过7+30+365=402天的删除阈值
        alert.setReminderTime(LocalDateTime.now().minusDays(450));
        alert.setStatus("archived");
        testHealthAlerts.add(alert);

        // 执行测试
        petHealthReminderTask.cleanupExpiredHealthReminders();

        // 验证健康提醒被删除
        verify(healthAlertsService).removeById(1);
    }

    @Test
    @DisplayName("测试删除功能 - 未达到删除阈值")
    void testDeletionBelowThreshold() {
        // 创建一个未达到删除阈值的归档健康提醒
        HealthAlertsVo alert = new HealthAlertsVo();
        alert.setHealthId(1);
        alert.setPid(1);
        alert.setCheckDate(LocalDateTime.of(2023, 11, 1, 10, 0, 0));
        alert.setHealthType("疫苗接种");
        alert.setDescription("狂犬疫苗");
        // 350天前，未达到7+30+365=402天的删除阈值
        alert.setReminderTime(LocalDateTime.now().minusDays(350));
        alert.setStatus("archived");
        testHealthAlerts.add(alert);

        // 执行测试
        petHealthReminderTask.cleanupExpiredHealthReminders();

        // 验证健康提醒没有被删除
        verify(healthAlertsService, never()).removeById(anyInt());
    }

    @Test
    @DisplayName("测试删除功能 - 不同健康类型的删除阈值")
    void testDeletionDifferentHealthTypes() {
        // 创建疫苗接种提醒 - 7+30+365=402天阈值
        HealthAlertsVo vaccineAlert = new HealthAlertsVo();
        vaccineAlert.setHealthId(1);
        vaccineAlert.setPid(1);
        vaccineAlert.setHealthType("疫苗接种");
        vaccineAlert.setReminderTime(LocalDateTime.now().minusDays(410)); // 超过阈值
        vaccineAlert.setStatus("archived");
        testHealthAlerts.add(vaccineAlert);

        // 创建驱虫提醒 - 5+30+365=400天阈值
        HealthAlertsVo dewormAlert = new HealthAlertsVo();
        dewormAlert.setHealthId(2);
        dewormAlert.setPid(1);
        dewormAlert.setHealthType("驱虫");
        dewormAlert.setReminderTime(LocalDateTime.now().minusDays(410)); // 超过阈值
        dewormAlert.setStatus("archived");
        testHealthAlerts.add(dewormAlert);

        // 创建体检提醒 - 3+30+365=398天阈值
        HealthAlertsVo checkupAlert = new HealthAlertsVo();
        checkupAlert.setHealthId(3);
        checkupAlert.setPid(1);
        checkupAlert.setHealthType("体检");
        checkupAlert.setReminderTime(LocalDateTime.now().minusDays(390)); // 未超过阈值
        checkupAlert.setStatus("archived");
        testHealthAlerts.add(checkupAlert);

        // 执行测试
        petHealthReminderTask.cleanupExpiredHealthReminders();

        // 验证疫苗接种和驱虫提醒被删除，体检提醒未被删除
        verify(healthAlertsService).removeById(1);
        verify(healthAlertsService).removeById(2);
        verify(healthAlertsService, never()).removeById(3);
    }

    @Test
    @DisplayName("测试删除功能 - 删除功能关闭")
    void testDeletionDisabled() {
        // 配置删除功能关闭
        when(cleanupConfig.isEnableDeletion()).thenReturn(false);

        // 创建一个需要删除的归档健康提醒
        HealthAlertsVo alert = new HealthAlertsVo();
        alert.setHealthId(1);
        alert.setPid(1);
        alert.setHealthType("疫苗接种");
        alert.setReminderTime(LocalDateTime.now().minusDays(450)); // 超过阈值
        alert.setStatus("archived");
        testHealthAlerts.add(alert);

        // 执行测试
        petHealthReminderTask.cleanupExpiredHealthReminders();

        // 验证健康提醒没有被删除
        verify(healthAlertsService, never()).removeById(anyInt());
    }

    @Test
    @DisplayName("测试删除功能 - 混合状态的健康提醒")
    void testDeletionMixedStatus() {
        // 创建待处理状态的健康提醒
        HealthAlertsVo normalAlert = new HealthAlertsVo();
        normalAlert.setHealthId(1);
        normalAlert.setPid(1);
        normalAlert.setHealthType("疫苗接种");
        normalAlert.setReminderTime(LocalDateTime.now().minusDays(450)); // 超过阈值
        normalAlert.setStatus("pending");
        testHealthAlerts.add(normalAlert);

        // 创建过期状态的健康提醒
        HealthAlertsVo expiredAlert = new HealthAlertsVo();
        expiredAlert.setHealthId(2);
        expiredAlert.setPid(1);
        expiredAlert.setHealthType("疫苗接种");
        expiredAlert.setReminderTime(LocalDateTime.now().minusDays(450)); // 超过阈值
        expiredAlert.setStatus("expired");
        testHealthAlerts.add(expiredAlert);

        // 创建归档状态的健康提醒
        HealthAlertsVo archivedAlert = new HealthAlertsVo();
        archivedAlert.setHealthId(3);
        archivedAlert.setPid(1);
        archivedAlert.setHealthType("疫苗接种");
        archivedAlert.setReminderTime(LocalDateTime.now().minusDays(450)); // 超过阈值
        archivedAlert.setStatus("archived");
        testHealthAlerts.add(archivedAlert);

        // 执行测试
        petHealthReminderTask.cleanupExpiredHealthReminders();

        // 验证只有归档状态的提醒被删除
        verify(healthAlertsService, never()).removeById(1);
        verify(healthAlertsService, never()).removeById(2);
        verify(healthAlertsService).removeById(3);

        // 验证待处理状态的提醒被更新为过期
        ArgumentCaptor<HealthDto> normalDtoCaptor = ArgumentCaptor.forClass(HealthDto.class);
        verify(healthAlertsService).updateHealthAlert(eq(1), normalDtoCaptor.capture());
        assertEquals("expired", normalDtoCaptor.getValue().getStatus());
        
        // 验证过期状态的提醒被更新为归档（因为它超过了归档阈值）
        ArgumentCaptor<HealthDto> expiredDtoCaptor = ArgumentCaptor.forClass(HealthDto.class);
        verify(healthAlertsService).updateHealthAlert(eq(2), expiredDtoCaptor.capture());
        assertEquals("archived", expiredDtoCaptor.getValue().getStatus());
    }

    @Test
    @DisplayName("测试删除功能 - 多个用户多个提醒")
    void testDeletionMultipleUsers() {
        // 创建第二个用户
        Users user2 = Users.builder()
                .id(2)
                .userName("testuser2")
                .email("test2@example.com")
                .build();

        // 为第二个用户创建宠物
        AdoptionsVo adoption2 = new AdoptionsVo();
        adoption2.setPid(2);
        adoption2.setName("小黑");

        // 创建第一个用户的归档提醒
        HealthAlertsVo alert1 = new HealthAlertsVo();
        alert1.setHealthId(1);
        alert1.setPid(1);
        alert1.setHealthType("疫苗接种");
        alert1.setReminderTime(LocalDateTime.now().minusDays(450)); // 超过阈值
        alert1.setStatus("archived");

        // 创建第二个用户的归档提醒
        HealthAlertsVo alert2 = new HealthAlertsVo();
        alert2.setHealthId(2);
        alert2.setPid(2);
        alert2.setHealthType("驱虫");
        alert2.setReminderTime(LocalDateTime.now().minusDays(450)); // 超过阈值
        alert2.setStatus("archived");

        // 配置服务返回多个用户和提醒
        when(usersService.list()).thenReturn(List.of(testUser, user2));
        when(adoptionsService.getUserAdoptions(eq(2L), anyInt(), anyInt())).thenReturn(List.of(adoption2));
        when(healthAlertsService.getUserHealthAlerts(eq(1))).thenReturn(List.of(alert1));
        when(healthAlertsService.getUserHealthAlerts(eq(2))).thenReturn(List.of(alert2));

        // 执行测试
        petHealthReminderTask.cleanupExpiredHealthReminders();

        // 验证两个用户的提醒都被删除
        verify(healthAlertsService).removeById(1);
        verify(healthAlertsService).removeById(2);
    }

    @Test
    @DisplayName("测试删除功能 - 空提醒时间")
    void testDeletionWithNullReminderTime() {
        // 创建一个提醒时间为空的归档健康提醒
        HealthAlertsVo alert = new HealthAlertsVo();
        alert.setHealthId(1);
        alert.setPid(1);
        alert.setHealthType("疫苗接种");
        alert.setReminderTime(null); // 空提醒时间
        alert.setStatus("archived");
        testHealthAlerts.add(alert);

        // 执行测试
        petHealthReminderTask.cleanupExpiredHealthReminders();

        // 验证健康提醒没有被删除
        verify(healthAlertsService, never()).removeById(anyInt());
    }
}