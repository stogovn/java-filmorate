package ru.yandex.practicum.filmorate.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.MpaService;

import java.util.List;

@RestController
@AllArgsConstructor
public class MpaController {
    private MpaService mpaService;

    @GetMapping("/mpa")
    public List<Mpa> getMpa() {
        return mpaService.getMpa();
    }

    @GetMapping("/mpa/{id}")
    public Mpa getMpaById(@PathVariable("id") Long id) {
        return mpaService.getMpaById(id);
    }
}
