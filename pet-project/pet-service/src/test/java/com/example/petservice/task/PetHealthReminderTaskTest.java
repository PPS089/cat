package com.example.petservice.task;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.petcommon.properties.HealthReminderCleanupProperties;
import com.example.petpojo.dto.HealthDto;
import com.example.petpojo.entity.Users;
import com.example.petpojo.vo.AdoptionsVo;
import com.example.petpojo.vo.HealthAlertsVo;
import com.example.petservice.service.AdoptionsService;
import com.example.petservice.service.HealthAlertsService;
import com.example.petservice.service.UsersService;
import com.example.petservice.service.VoiceAlertService;
import com.example.petservice.websocket.WebSocketServer;

/**
 * PetHealthReminderTask 单元测试
 * 测试定时任务的健康提醒检查和过期清理功能
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("宠物健康提醒任务测试")
class PetHealthReminderTaskTest {

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
    private LocalDateTime testNow;

    @BeforeEach
    void setUp() {
        // 初始化测试时间
        testNow = LocalDateTime.of(2023, 12, 1, 10, 0, 0);

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
        HealthAlertsVo alert = new HealthAlertsVo();
        alert.setHealthId(1);
        alert.setPid(1);
        alert.setCheckDate(LocalDateTime.of(2023, 11, 1, 10, 0, 0));
        alert.setHealthType("疫苗接种");
        alert.setDescription("狂犬疫苗");
        alert.setReminderTime(testNow.plusSeconds(10)); // 10秒后提醒
        alert.setStatus("pending");
        testHealthAlerts.add(alert);

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
    @DisplayName("测试定时健康提醒检查 - 正常情况")
    void testCheckScheduledHealthReminders_NormalCase() {
        // 设置提醒时间在当前时间前后30秒内
        HealthAlertsVo alert = testHealthAlerts.get(0);
        alert.setReminderTime(LocalDateTime.now().plusSeconds(10));
        alert.setStatus("pending");

        // 执行测试
        petHealthReminderTask.checkScheduledHealthReminders();

        // 验证WebSocket消息发送
        verify(webSocketServer).sendToUsersClient(eq("1"), contains("宠物健康提醒"));

        // 验证语音提醒发送
        verify(voiceAlertService).speakPetHealthReminder(eq("小白"), eq("狂犬疫苗"));

        // 验证健康提醒状态更新
        ArgumentCaptor<HealthDto> healthDtoCaptor = ArgumentCaptor.forClass(HealthDto.class);
        verify(healthAlertsService).updateHealthAlert(eq(1), healthDtoCaptor.capture());
        
        HealthDto updatedHealth = healthDtoCaptor.getValue();
        assertEquals("reminded", updatedHealth.getStatus());
    }

    @Test
    @DisplayName("测试定时健康提醒检查 - 提醒时间不在范围内")
    void testCheckScheduledHealthReminders_OutOfTimeRange() {
        // 设置提醒时间超出当前时间前后30秒范围
        HealthAlertsVo alert = testHealthAlerts.get(0);
        alert.setReminderTime(LocalDateTime.now().plusMinutes(5)); // 5分钟后
        alert.setStatus("pending");

        // 执行测试
        petHealthReminderTask.checkScheduledHealthReminders();

        // 验证没有发送WebSocket消息
        verify(webSocketServer, never()).sendToUsersClient(anyString(), anyString());

        // 验证没有发送语音提醒
        verify(voiceAlertService, never()).speakPetHealthReminder(anyString(), anyString());

        // 验证没有更新健康提醒状态
        verify(healthAlertsService, never()).updateHealthAlert(anyInt(), any(HealthDto.class));
    }

    @Test
    @DisplayName("测试定时健康提醒检查 - 状态不匹配")
    void testCheckScheduledHealthReminders_StatusNotMatch() {
        // 设置提醒状态为已提醒
        HealthAlertsVo alert = testHealthAlerts.get(0);
        alert.setReminderTime(LocalDateTime.now().plusSeconds(10));
        alert.setStatus("reminded");

        // 执行测试
        petHealthReminderTask.checkScheduledHealthReminders();

        // 验证没有发送WebSocket消息
        verify(webSocketServer, never()).sendToUsersClient(anyString(), anyString());

        // 验证没有发送语音提醒
        verify(voiceAlertService, never()).speakPetHealthReminder(anyString(), anyString());

        // 验证没有更新健康提醒状态
        verify(healthAlertsService, never()).updateHealthAlert(anyInt(), any(HealthDto.class));
    }

    @Test
    @DisplayName("测试清理过期健康提醒 - 待处理状态转为过期")
    void testCleanupExpiredHealthReminders_NormalToExpired() {
        // 创建一个过期的健康提醒
        HealthAlertsVo alert = testHealthAlerts.get(0);
        alert.setReminderTime(LocalDateTime.now().minusDays(8)); // 8天前，超过疫苗接种的7天过期阈值
        alert.setStatus("pending");

        // 执行测试
        petHealthReminderTask.cleanupExpiredHealthReminders();

        // 验证健康提醒状态更新为过期
        ArgumentCaptor<HealthDto> healthDtoCaptor = ArgumentCaptor.forClass(HealthDto.class);
        verify(healthAlertsService).updateHealthAlert(eq(1), healthDtoCaptor.capture());
        
        HealthDto updatedHealth = healthDtoCaptor.getValue();
        assertEquals("expired", updatedHealth.getStatus());
    }

    @Test
    @DisplayName("测试清理过期健康提醒 - 过期状态转为归档")
    void testCleanupExpiredHealthReminders_ExpiredToArchived() {
        // 创建一个需要归档的健康提醒
        HealthAlertsVo alert = testHealthAlerts.get(0);
        alert.setReminderTime(LocalDateTime.now().minusDays(40)); // 40天前，超过7天过期+30天归档阈值
        alert.setStatus("expired");

        // 执行测试
        petHealthReminderTask.cleanupExpiredHealthReminders();

        // 验证健康提醒状态更新为归档
        ArgumentCaptor<HealthDto> healthDtoCaptor = ArgumentCaptor.forClass(HealthDto.class);
        verify(healthAlertsService).updateHealthAlert(eq(1), healthDtoCaptor.capture());
        
        HealthDto updatedHealth = healthDtoCaptor.getValue();
        assertEquals("archived", updatedHealth.getStatus());
    }

    @Test
    @DisplayName("测试清理过期健康提醒 - 已提醒状态转为过期")
    void testCleanupExpiredHealthReminders_RemindedToExpired() {
        // 创建一个已提醒但过期的健康提醒
        HealthAlertsVo alert = testHealthAlerts.get(0);
        alert.setReminderTime(LocalDateTime.now().minusDays(8)); // 8天前，超过疫苗接种的7天过期阈值
        alert.setStatus("reminded");

        // 执行测试
        petHealthReminderTask.cleanupExpiredHealthReminders();

        // 验证健康提醒状态更新为过期
        ArgumentCaptor<HealthDto> healthDtoCaptor = ArgumentCaptor.forClass(HealthDto.class);
        verify(healthAlertsService).updateHealthAlert(eq(1), healthDtoCaptor.capture());
        
        HealthDto updatedHealth = healthDtoCaptor.getValue();
        assertEquals("expired", updatedHealth.getStatus());
    }

    @Test
    @DisplayName("测试清理过期健康提醒 - 归档状态删除")
    void testCleanupExpiredHealthReminders_ArchivedToDeleted() {
        // 创建一个需要删除的归档健康提醒
        HealthAlertsVo alert = testHealthAlerts.get(0);
        alert.setReminderTime(LocalDateTime.now().minusDays(410)); // 410天前，超过7天过期+30天归档+365天删除阈值
        alert.setStatus("archived");

        // 配置删除功能启用
        when(cleanupConfig.isEnableDeletion()).thenReturn(true);

        // 执行测试
        petHealthReminderTask.cleanupExpiredHealthReminders();

        // 验证健康提醒被删除
        verify(healthAlertsService).removeById(1);
    }

    @Test
    @DisplayName("测试清理过期健康提醒 - 删除功能关闭")
    void testCleanupExpiredHealthReminders_DeletionDisabled() {
        // 配置删除功能关闭
        when(cleanupConfig.isEnableDeletion()).thenReturn(false);

        // 创建一个需要删除的归档健康提醒
        HealthAlertsVo alert = testHealthAlerts.get(0);
        alert.setReminderTime(LocalDateTime.now().minusDays(400)); // 400天前
        alert.setStatus("archived");

        // 执行测试
        petHealthReminderTask.cleanupExpiredHealthReminders();

        // 验证健康提醒没有被删除
        verify(healthAlertsService, never()).removeById(anyInt());
    }

    @Test
    @DisplayName("测试createHealthDto方法")
    void testCreateHealthDto() {
        // 使用反射调用私有方法
        HealthAlertsVo alert = testHealthAlerts.get(0);
        HealthDto healthDto = (HealthDto) ReflectionTestUtils.invokeMethod(
            petHealthReminderTask, "createHealthDto", alert);

        // 验证DTO字段
        assertEquals(alert.getPid(), healthDto.getPid());
        assertEquals(alert.getCheckDate(), healthDto.getCheckDate());
        assertEquals(alert.getHealthType(), healthDto.getHealthType());
        assertEquals(alert.getDescription(), healthDto.getDescription());
        assertEquals(alert.getReminderTime(), healthDto.getReminderTime());
    }

    @Test
    @DisplayName("测试健康提醒处理 - 空提醒时间")
    void testHealthAlertWithNullReminderTime() {
        // 设置提醒时间为空
        HealthAlertsVo alert = testHealthAlerts.get(0);
        alert.setReminderTime(null);
        alert.setStatus("normal");

        // 执行测试
        petHealthReminderTask.checkScheduledHealthReminders();

        // 验证没有发送任何提醒
        verify(webSocketServer, never()).sendToUsersClient(anyString(), anyString());
        verify(voiceAlertService, never()).speakPetHealthReminder(anyString(), anyString());
    }

    @Test
    @DisplayName("测试健康提醒处理 - 多个用户多个提醒")
    void testMultipleUsersMultipleAlerts() {
        // 设置第一个用户的健康提醒在时间窗口内
        HealthAlertsVo alert1 = testHealthAlerts.get(0);
        alert1.setReminderTime(LocalDateTime.now().plusSeconds(10)); // 10秒后提醒
        alert1.setStatus("pending");
        
        // 设置第二个测试用户
        Users user2 = Users.builder()
                .id(2)
                .userName("testuser2")
                .email("test2@example.com")
                .phone("13800138001")
                .build();

        // 为第二个用户创建宠物
        AdoptionsVo adoption2 = new AdoptionsVo();
        adoption2.setPid(2);
        adoption2.setName("小黑");
        
        HealthAlertsVo alert2 = new HealthAlertsVo();
        alert2.setHealthId(2);
        alert2.setPid(2);
        alert2.setCheckDate(LocalDateTime.of(2023, 11, 1, 10, 0, 0));
        alert2.setHealthType("驱虫");
        alert2.setDescription("体内驱虫");
        alert2.setReminderTime(LocalDateTime.now().plusSeconds(5)); // 5秒后提醒
        alert2.setStatus("pending");

        // 配置服务返回多个用户和提醒
        when(usersService.list()).thenReturn(List.of(testUser, user2));
        when(adoptionsService.getUserAdoptions(eq(2L), anyInt(), anyInt())).thenReturn(List.of(adoption2));
        when(healthAlertsService.getUserHealthAlerts(eq(2))).thenReturn(List.of(alert2));

        // 执行测试
        petHealthReminderTask.checkScheduledHealthReminders();

        // 验证两个用户都收到了提醒
        verify(webSocketServer).sendToUsersClient(eq("1"), contains("宠物健康提醒"));
        verify(webSocketServer).sendToUsersClient(eq("2"), contains("宠物健康提醒"));
        
        // 验证发送了两个语音提醒
        verify(voiceAlertService, times(2)).speakPetHealthReminder(anyString(), anyString());

        // 验证更新了两个健康提醒状态
        verify(healthAlertsService, times(2)).updateHealthAlert(anyInt(), any(HealthDto.class));
    }
}