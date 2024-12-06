package org.ytcuber.handler;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.ytcuber.database.dto.LessonDTO;
import org.ytcuber.database.dto.ReplacementDTO;
import org.ytcuber.database.model.Lesson;
import org.ytcuber.database.model.Replacement;
import org.ytcuber.database.repository.GroupRepository;
import org.ytcuber.database.repository.LessonRepository;
import org.ytcuber.database.repository.ReplacementRepository;

import java.util.ArrayList;
import java.util.List;

@Component
public class GroupSchedule {
    private final GroupRepository groupRepository;
    private final LessonRepository lessonRepository;
    private final ReplacementRepository replacementRepository;

    @Autowired
    public GroupSchedule(GroupRepository groupRepository, LessonRepository lessonRepository, ReplacementRepository replacementRepository) {
        this.groupRepository = groupRepository;
        this.lessonRepository = lessonRepository;
        this.replacementRepository = replacementRepository;
    }

    @PostConstruct
    private void init() {
        System.out.println("Можно написать инициализацию, но я не буду");
    }

    public List<Object> giveSchedule(String groupName, Integer subgroup, Integer odd) throws Exception {

        Integer groupId = groupRepository.findByName(groupName);
        List<Lesson> lessons = lessonRepository.findLessonsByGroupIdAndSubgroupAndOdd(groupId, subgroup, odd);
        List<Replacement> replacements = replacementRepository.findReplacementsByGroupTitleAndSubgroup(groupName, subgroup);

        List<LessonDTO> lessonDTOS = lessons.stream()
                .map(lesson -> new LessonDTO(
                        lesson.getDayOfWeek(),
                        lesson.getOrdinal(),
                        lesson.getLocation(),
                        lesson.getSubject(),
                        lesson.getTeacher()
                ))
                .toList();

        List<ReplacementDTO> replacementDTOS = replacements.stream()
                .map(replacement -> new ReplacementDTO(
                        replacement.getDate(),
                        replacement.getDatOfWeek(),
                        replacement.getOrdinal(),
                        replacement.getLocation(),
                        replacement.getSubject(),
                        replacement.getTeacher()
                ))
                .toList();

        // Создаём результирующий список
        List<Object> mergedList = new ArrayList<>(lessonDTOS);

        // Заменяем элементы в lessonDTOS элементами из replacementDTOS при совпадении
        for (ReplacementDTO replacement : replacementDTOS) {
            boolean replaced = false; // Флаг для отслеживания замены

            // Ищем элемент в lessonDTOS, совпадающий по критериям
            for (int i = 0; i < lessonDTOS.size(); i++) {
                LessonDTO lesson = lessonDTOS.get(i);

                if (lesson.getOrdinal() == replacement.getOrdinal() &&
                        lesson.getDayOfWeek().equals(replacement.getDatOfWeek())) {
                    // Заменяем элемент в mergedList
                    mergedList.set(i, replacement);
                    replaced = true;
                    break; // Прекращаем поиск, так как нашли совпадение
                }
            }

            // Если replacement не нашёл пары, добавляем его в конец списка
            if (!replaced) {
                mergedList.add(replacement);
            }
        }

        return mergedList;
    }
}