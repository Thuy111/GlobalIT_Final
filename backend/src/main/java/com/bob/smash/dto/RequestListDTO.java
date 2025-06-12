package com.bob.smash.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;

public record RequestListDTO(
    Integer idx,
    String title,
    String content,
    Byte isDone,
    @JsonFormat(pattern = "yyyy.MM.dd") LocalDate createdAt,
    @JsonFormat(pattern = "yyyy.MM.dd") LocalDate useDate,
    String dDay
) {}
