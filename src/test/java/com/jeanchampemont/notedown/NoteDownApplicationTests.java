package com.jeanchampemont.notedown;

import com.jeanchampemont.notedown.NoteDownApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = NoteDownApplication.class)
@WebAppConfiguration
public class NoteDownApplicationTests {

	@Test
	public void contextLoads() {
	}

}
