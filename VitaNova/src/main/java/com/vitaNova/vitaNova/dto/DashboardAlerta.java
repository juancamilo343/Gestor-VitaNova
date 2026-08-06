package com.vitaNova.vitaNova.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DashboardAlerta {

    private String severity;
    private String iconClass;
    private String title;
    private String description;
    private String actionText;
    private String actionHref;
}
