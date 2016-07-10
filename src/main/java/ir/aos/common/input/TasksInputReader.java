package ir.aos.common.input;

import ir.aos.common.task.Task;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class TasksInputReader {

    public static final String IDS_SPLITTER = ":";
    public static final String RESOURCES_SPLITTER = "#";

    public List<Task> parseInput(String input) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(input)));
        List<Task> tasks = new ArrayList<>();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            if (line.isEmpty()) {
                continue;
            }
            String[] splitTasksStrings = line.split(IDS_SPLITTER);
            List<Integer> resources = new ArrayList<>();
            if (splitTasksStrings.length > 3) {
                String[] splitResources = splitTasksStrings[3].split(RESOURCES_SPLITTER);
                for (String splitResource : splitResources) {
                    resources.add(Integer.valueOf(splitResource));
                }
            }
            tasks.add(new Task(Integer.valueOf(splitTasksStrings[0]), Integer.valueOf(splitTasksStrings[1]), Integer.valueOf(splitTasksStrings[2]), resources));
        }
        bufferedReader.close();
        return tasks;
    }

}
