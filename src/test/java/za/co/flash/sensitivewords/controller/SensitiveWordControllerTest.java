package za.co.flash.sensitivewords.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import za.co.flash.sensitivewords.dto.SensitiveWordInputResponse;
import za.co.flash.sensitivewords.entity.SensitiveWord;
import za.co.flash.sensitivewords.service.SensitiveWordCacheService;
import za.co.flash.sensitivewords.service.SensitiveWordInputService;
import za.co.flash.sensitivewords.service.SensitiveWordService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SensitiveWordController.class)
class SensitiveWordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SensitiveWordService sensitiveWordService;

    @MockitoBean
    private SensitiveWordInputService sensitiveWordInputService;

    @MockitoBean
    private SensitiveWordCacheService sensitiveWordCacheService;


    @Test
    void shouldAddSensitiveWordsFromJson() throws Exception {

        SensitiveWordInputResponse response = SensitiveWordInputResponse.builder()
                .totalReceived(2)
                .inserted(2)
                .duplicates(0)
                .invalid(0)
                .duplicateWords(List.of())
                .invalidWords(List.of())
                .build();

        when(sensitiveWordInputService.process(any(), eq("admin")))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/sensitive-words/add-from-json")
                        .with(user("admin").roles("ADMIN"))
                        .principal(() -> "admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                   "words": [
                                     "SELECT",
                                     "DROP"
                                  ]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalReceived").value(2))
                .andExpect(jsonPath("$.inserted").value(2))
                .andExpect(jsonPath("$.duplicates").value(0))
                .andExpect(jsonPath("$.invalid").value(0));

        verify(sensitiveWordInputService)
                .process(any(), eq("admin"));
    }


    @Test
    void shouldAddSensitiveWordsFromFile() throws Exception {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "sensitive-words.txt",
                MediaType.TEXT_PLAIN_VALUE,
                """
                        [
                            "SELECT",
                            "DROP"
                        ]
                        """.getBytes()
        );

        SensitiveWordInputResponse response = SensitiveWordInputResponse.builder()
                .totalReceived(2)
                .inserted(2)
                .duplicates(0)
                .invalid(0)
                .duplicateWords(List.of())
                .invalidWords(List.of())
                .build();

        when(sensitiveWordInputService.process(any(), eq("admin")))
                .thenReturn(response);

        mockMvc.perform(multipart("/api/v1/sensitive-words/add-from-file")
                        .file(file)
                        .param("fileType", "TXT")
                        .with(user("admin").roles("ADMIN"))
                        .principal(() -> "admin"))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalReceived").value(2))
                .andExpect(jsonPath("$.inserted").value(2))
                .andExpect(jsonPath("$.duplicates").value(0))
                .andExpect(jsonPath("$.invalid").value(0));

        verify(sensitiveWordInputService)
                .process(any(), eq("admin"));
    }


    @Test
    void shouldGetAllSensitiveWords() throws Exception {

        SensitiveWord sensitiveWord = SensitiveWord.builder()
                .id(1L)
                .word("SELECT")
                .active(true)
                .build();

        PageRequest pageable = PageRequest.of(0, 10);

        Page<SensitiveWord> page =
                new PageImpl<>(List.of(sensitiveWord), pageable, 1);

        when(sensitiveWordService.findAll(eq(true), any()))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/sensitive-words")
                        .with(user("admin").roles("ADMIN"))
                        .principal(() -> "admin")
                        .param("active", "true")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].word").value("SELECT"))
                .andExpect(jsonPath("$.content[0].active").value(true));

        verify(sensitiveWordService)
                .findAll(eq(true), any());
    }


    @Test
    void shouldGetSensitiveWordByWord() throws Exception {

        SensitiveWord sensitiveWord = SensitiveWord.builder()
                .id(1L)
                .word("SELECT")
                .active(true)
                .build();

        when(sensitiveWordService.findByWord("SELECT"))
                .thenReturn(sensitiveWord);

        mockMvc.perform(get("/api/v1/sensitive-words/get-word")
                        .with(user("admin").roles("ADMIN"))
                        .principal(() -> "admin")
                        .param("word", "SELECT"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.word").value("SELECT"))
                .andExpect(jsonPath("$.active").value(true));

        verify(sensitiveWordService)
                .findByWord("SELECT");
    }


    @Test
    void shouldUpdateSensitiveWord() throws Exception {

        SensitiveWord sensitiveWord = SensitiveWord.builder()
                .id(1L)
                .word("UPDATE")
                .active(true)
                .build();

        when(sensitiveWordService.update("SELECT", "UPDATE", "admin"))
                .thenReturn(sensitiveWord);

        mockMvc.perform(put("/api/v1/sensitive-words/update-word")
                        .with(user("admin").roles("ADMIN"))
                        .principal(() -> "admin")
                        .param("word", "SELECT")
                        .param("newWord", "UPDATE"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.word").value("UPDATE"))
                .andExpect(jsonPath("$.active").value(true));

        verify(sensitiveWordService)
                .update("SELECT", "UPDATE", "admin");
    }


    @Test
    void shouldDisableSensitiveWord() throws Exception {

        SensitiveWord sensitiveWord = SensitiveWord.builder()
                .id(1L)
                .word("SELECT")
                .active(false)
                .build();

        when(sensitiveWordService.disable("SELECT", "admin"))
                .thenReturn(sensitiveWord);

        mockMvc.perform(delete("/api/v1/sensitive-words/disable-word")
                        .with(user("admin").roles("ADMIN"))
                        .principal(() -> "admin")
                        .param("word", "SELECT"))
                .andExpect(status().isNoContent());

        verify(sensitiveWordService)
                .disable("SELECT", "admin");
    }


    @Test
    void shouldEnableSensitiveWord() throws Exception {

        SensitiveWord sensitiveWord = SensitiveWord.builder()
                .id(1L)
                .word("SELECT")
                .active(true)
                .build();

        when(sensitiveWordService.enable("SELECT", "admin"))
                .thenReturn(sensitiveWord);

        mockMvc.perform(patch("/api/v1/sensitive-words/enable-word")
                        .with(user("admin").roles("ADMIN"))
                        .principal(() -> "admin")
                        .param("word", "SELECT"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.word").value("SELECT"))
                .andExpect(jsonPath("$.active").value(true));

        verify(sensitiveWordService)
                .enable("SELECT", "admin");
    }


    @Test
    void shouldRefreshSensitiveWordsCache() throws Exception {

        doNothing()
                .when(sensitiveWordCacheService)
                .refresh();

        mockMvc.perform(post("/api/v1/sensitive-words/refresh-cache")
                        .with(user("admin").roles("ADMIN"))
                        .principal(() -> "admin"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message")
                        .value("Sensitive words cache refreshed successfully"));

        verify(sensitiveWordCacheService)
                .refresh();
    }

}