package ru.otus.hw.dao;

import java.io.Reader;

public interface ResourceReader {
    Reader open(String location);
}