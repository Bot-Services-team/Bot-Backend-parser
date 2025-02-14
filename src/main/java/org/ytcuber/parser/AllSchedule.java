package org.ytcuber.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.ytcuber.database.model.Group;
import org.ytcuber.database.repository.GroupRepository;
import org.ytcuber.handler.GroupSchedule;
import org.ytcuber.initialization.Initialization;
import org.ytcuber.initialization.InitializationLocations;
import org.ytcuber.initialization.InitializationReplacement;

import java.io.IOException;
import java.io.File;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class AllSchedule {
    private static final Logger logger = LoggerFactory.getLogger(AllSchedule.class);
    private Initialization initialization;
    private GroupProcessor groupProcessor;
    private GroupRepository groupRepository;
    private InitializationReplacement initializationReplacement;
    private InitializationLocations initializationLocations;
    private GroupSchedule groupSchedule;

    @Autowired
    public void ApplicationInitializer(GroupSchedule groupSchedule, InitializationLocations initializationLocations, GroupProcessor groupProcessor, Initialization initialization, GroupRepository groupRepository, InitializationReplacement initializationReplacement) {
        this.initialization = initialization;
        this.groupProcessor = groupProcessor;
        this.groupRepository = groupRepository;
        this.initializationReplacement = initializationReplacement;
        this.initializationLocations = initializationLocations;
        this.groupSchedule = groupSchedule;
    }

@PostConstruct
public void init() {
    ExecutorService executorService = Executors.newFixedThreadPool(2);
    try {
        // Create directory if it doesn't exist
        File mainExcelDir = new File("./mainexcel");
        if (!mainExcelDir.exists()) {
            mainExcelDir.mkdirs();
        }

        // Wrap potentially failing operations in try-catch blocks
        try {
            // Заполнение групп
            for (int i = 1; i <= 4; i++) {
                groupProcessor.processGroups(String.valueOf(i));
            }
        } catch (IOException e) {
            logger.warn("Failed to process groups: " + e.getMessage());
        }

        try {
            // Заполнение кабинетов
            initializationLocations.processLocationParse("Cab2");
        } catch (IOException e) {
            logger.warn("Failed to parse locations: " + e.getMessage());
        }

        // Group initialization can continue as before
        List<Group> groupsToSave = new ArrayList<>();
        Group group = new Group();
        group.setTitle("АХАХА");
        group.setSquad(10);
        groupsToSave.add(group);
        groupRepository.saveAll(groupsToSave);

        // Получаем результат
        String[] dateRanges = generateWeeklyDateRange();

        // Запуск парсинга замен в отдельном потоке
        Callable<Void> replacementTask = () -> {
            try {
                initializationReplacement.processExcelReplacementParse(dateRanges[0]);
            } catch (Exception ignored) { }
            try {
                initializationReplacement.processExcelReplacementParse(dateRanges[1]);
            } catch (Exception ignored) { }

            return null;
        };

        // Запуск парсинга расисания в отдельном потоке
        Callable<Void> scheduleTask = () -> {
            int lastId = groupRepository.findLastId() - 1;
            for (int i = 1; i <= lastId; i++) {
                String groupName = String.valueOf(groupRepository.findNameById(i));
                initialization.processExcelParse(groupName);
            }
            return null;
        };

        // Запускаем оба парсинга параллельно
        List<Callable<Void>> tasks = new ArrayList<>();
        tasks.add(replacementTask);
        tasks.add(scheduleTask);

        try {
            executorService.invokeAll(tasks);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    } catch (Exception e) {
        logger.error("Failed to initialize AllSchedule: " + e.getMessage());
    } finally {
        executorService.shutdown();
    }
}

    public static String[] generateWeeklyDateRange() {
        // Получаем текущую дату
        LocalDate today = LocalDate.now();

        LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);

        // Определяем диапазон для текущей недели
        LocalDate startDate = startOfWeek;
        LocalDate endDate = startDate.plusDays(2);

        // Форматируем даты в нужный формат
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yy");
        String formattedStartDate = startDate.format(formatter);
        String formattedEndDate = endDate.format(formatter);

        // Генерируем строку диапазона для текущей недели
        String dateRange = formattedStartDate + "-" + formattedEndDate;

        LocalDate nextStartDate = startOfWeek.plusDays(3);
        LocalDate nextEndDate = nextStartDate.plusDays(2);

        // Форматируем следующую дату
        String formattedNextStartDate = nextStartDate.format(formatter);
        String formattedNextEndDate = nextEndDate.format(formatter);

        // Генерируем строку диапазона для следующего диапазона
        String nextDateRange = formattedNextStartDate + "-" + formattedNextEndDate;

        System.out.println("Replace Date:");
        System.out.println(dateRange);
        System.out.println(nextDateRange);

        return new String[] {dateRange, nextDateRange};
    }
}
