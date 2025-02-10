package org.ytcuber.parser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.ytcuber.database.model.Group;
import org.ytcuber.database.repository.GroupRepository;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Component
public class GroupProcessor {
    private final GroupRepository groupRepository;

    @Autowired
    public GroupProcessor(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    public void processGroups(String squadNum) throws IOException {
        String folderPath = "./mainexcel/squad" + squadNum + "/";
//        String folderPath = "/home/ytcuber/site/mainexcel/squad" + squadNum + "/";
        Path folder = Paths.get(folderPath);

        if (!Files.exists(folder) || !Files.isDirectory(folder)) {
            System.out.println("Directory does not exist: " + folderPath);
            return;
        }

        List<Group> groupsToSave = new ArrayList<>();
        DirectoryStream<Path> stream = Files.newDirectoryStream(folder, "*.xlsx");

        for (Path filePath : stream) {
            String fileName = filePath.getFileName().toString();
            if (fileName.endsWith(".xlsx")) {
                String groupName = fileName.substring(0, fileName.length() - 5); // Убираем .xlsx
//                groupName = groupName.replace('-', '.'); // Заменяем дефисы на подчеркивания
                Group group = new Group();
                group.setTitle(groupName);
                group.setSquad(Integer.valueOf(squadNum));
                groupsToSave.add(group);
            }
        }

        // Удаление всех PDF-файлов из папки
        try (DirectoryStream<Path> pdfStream = Files.newDirectoryStream(folder, "*.pdf")) {
            for (Path pdfPath : pdfStream) {
                Files.delete(pdfPath);
                System.out.println("Deleted PDF file: " + pdfPath.getFileName());
            }
        } catch (IOException e) {
            System.err.println("Error while deleting PDF files: " + e.getMessage());
        }

        // Сохраняем группы в базу
        groupRepository.saveAll(groupsToSave);
        System.out.println("Группы успешно сохранены.");
    }
}