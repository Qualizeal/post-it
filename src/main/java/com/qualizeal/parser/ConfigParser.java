package com.qualizeal.parser;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import lombok.NoArgsConstructor;

import java.io.FileNotFoundException;
import java.io.FileReader;

@NoArgsConstructor
public class ConfigParser {
	public <T> T parse(String filePath, Class<T> tClass) throws FileNotFoundException {
		Gson gson = new Gson();
		JsonReader reader = new JsonReader(new FileReader(filePath));
		return gson.fromJson(reader, tClass);
	}
}