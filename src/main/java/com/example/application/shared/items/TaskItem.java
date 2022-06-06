package com.example.application.shared.items;

import com.example.application.shared.enums.ETaskState;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author : Adam Barča
 * @created : 6. 6. 2022
 **/
@Data
@AllArgsConstructor
public class TaskItem {

    private long taskNumber;
    private ETaskState taskState;
}
